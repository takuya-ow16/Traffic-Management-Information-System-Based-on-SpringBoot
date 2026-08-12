
import requests
from flask import Flask, request, jsonify, send_file, Response
from flask_cors import CORS
import torch
import cv2
import numpy as np
import os
import sys
import copy
import io
import tempfile
import time
import re

# 将当前目录加入模块搜索路径，便于直接导入本地识别模块
sys.path.append(os.getcwd())

from ultralytics import YOLO
from plate_recognition.plate_rec import get_plate_result, init_model
from plate_recognition.double_plate_split_merge import get_split_merge
from fonts.cv_puttext import cv2ImgAddText

app = Flask(__name__)
CORS(app) # 允许前端跨域访问 Python 识别服务

# 用于在实时监控页面中缓存最近识别结果
latest_plates = []
unique_plate_set = set()

# 模型与推理配置
DETECT_WEIGHTS = 'weights/yolov8s.pt' # YOLO 车牌检测模型权重
REC_WEIGHTS = 'weights/plate_rec_color.pth' # 车牌字符识别模型权重
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu") # 优先使用 GPU，未检测到时回退到 CPU
IMG_SIZE = 640

# 全局模型实例，服务启动后按需懒加载
detect_model = None
plate_rec_model = None
# 识别过滤阈值：用于降低误检和误识别
MIN_DETECT_CONF = 0.55
MIN_RECOGNITION_CONF = 0.85
MIN_PLATE_ASPECT_RATIO = 2.0
MAX_PLATE_ASPECT_RATIO = 6.5
REQUIRED_CONSECUTIVE_FRAMES = 2
MIN_BOX_IOU_FOR_TRACKING = 0.3

# 车牌格式正则：用于对模型输出做最终规则校验
PLATE_PATTERNS = [
    re.compile(r'^[京沪津渝冀晋蒙辽吉黑苏浙皖闽赣鲁豫鄂湘粤桂琼川贵云藏陕甘青宁新学警港澳使领民航][A-Z][A-HJ-NP-Z0-9]{5}$'),
    re.compile(r'^[京沪津渝冀晋蒙辽吉黑苏浙皖闽赣鲁豫鄂湘粤桂琼川贵云藏陕甘青宁新学警港澳使领民航][A-Z][A-HJ-NP-Z0-9]{6}$')
]

def load_models():
    """加载检测和识别模型"""
    global detect_model, plate_rec_model
    # 已经加载过模型时直接复用，避免重复加载带来额外开销
    if detect_model is not None and plate_rec_model is not None:
        return

    print("正在加载模型...")
    detect_model = YOLO(DETECT_WEIGHTS).model.to(DEVICE)
    plate_rec_model = init_model(DEVICE, REC_WEIGHTS, is_color=True)
    detect_model.eval()
    print("模型加载成功!")

def is_valid_plate_number(plate_number):
    """校验识别出的车牌号是否符合常见车牌格式"""
    if not plate_number:
        return False
    text = plate_number.strip().upper()
    return any(pattern.fullmatch(text) for pattern in PLATE_PATTERNS)

def calc_box_iou(rect_a, rect_b):
    """计算两个检测框的 IoU，用于判断前后帧是否为同一目标"""
    left = max(rect_a[0], rect_b[0])
    top = max(rect_a[1], rect_b[1])
    right = min(rect_a[2], rect_b[2])
    bottom = min(rect_a[3], rect_b[3])

    inter_w = max(0, right - left)
    inter_h = max(0, bottom - top)
    inter_area = inter_w * inter_h
    if inter_area == 0:
        return 0.0

    area_a = max(0, rect_a[2] - rect_a[0]) * max(0, rect_a[3] - rect_a[1])
    area_b = max(0, rect_b[2] - rect_b[0]) * max(0, rect_b[3] - rect_b[1])
    union_area = area_a + area_b - inter_area
    if union_area <= 0:
        return 0.0
    return inter_area / union_area

def check_models():
    """检查模型是否已加载"""
    if detect_model is None or plate_rec_model is None:
        load_models()


# 图像预处理与检测后处理
def letter_box(img, size=(640, 640)):
    """图像缩放并填充边框，保持长宽比"""
    h, w, _ = img.shape
    r = min(size[0] / h, size[1] / w)
    new_h, new_w = int(h * r), int(w * r)
    new_img = cv2.resize(img, (new_w, new_h))
    left = int((size[1] - new_w) / 2)
    top = int((size[0] - new_h) / 2)
    right = size[1] - left - new_w
    bottom = size[0] - top - new_h
    # 填充灰色边框
    img = cv2.copyMakeBorder(new_img, top, bottom, left, right, cv2.BORDER_CONSTANT, value=(114, 114, 114))
    return img, r, left, top

def xywh2xyxy(det):
    """坐标转换: 中心点wh -> 左上右下xyxy"""
    y = det.clone()
    y[:, 0] = det[:, 0] - det[0:, 2] / 2
    y[:, 1] = det[:, 1] - det[0:, 3] / 2
    y[:, 2] = det[:, 0] + det[0:, 2] / 2
    y[:, 3] = det[:, 1] + det[0:, 3] / 2
    return y

def my_nums(dets, iou_thresh):
    """自定义非极大值抑制(NMS)"""
    y = dets.clone()
    y_box_score = y[:, :5]
    index = torch.argsort(y_box_score[:, -1], descending=True)
    keep = []
    while index.size()[0] > 0:
        i = index[0].item()
        keep.append(i)
        x1 = torch.maximum(y_box_score[i, 0], y_box_score[index[1:], 0])
        y1 = torch.maximum(y_box_score[i, 1], y_box_score[index[1:], 1])
        x2 = torch.minimum(y_box_score[i, 2], y_box_score[index[1:], 2])
        y2 = torch.minimum(y_box_score[i, 3], y_box_score[index[1:], 3])
        zero_ = torch.tensor(0).to(dets.device)
        w = torch.maximum(zero_, x2 - x1)
        h = torch.maximum(zero_, y2 - y1)
        inter_area = w * h
        nuion_area1 = (y_box_score[i, 2] - y_box_score[i, 0]) * (y_box_score[i, 3] - y_box_score[i, 1])
        union_area2 = (y_box_score[index[1:], 2] - y_box_score[index[1:], 0]) * (y_box_score[index[1:], 3] - y_box_score[index[1:], 1])
        iou = inter_area / (nuion_area1 + union_area2 - inter_area)
        idx = torch.where(iou <= iou_thresh)[0]
        index = index[idx + 1]
    return keep

def restore_box(dets, r, left, top):
    """将坐标还原回原图尺寸"""
    dets[:, [0, 2]] = dets[:, [0, 2]] - left
    dets[:, [1, 3]] = dets[:, [1, 3]] - top
    dets[:, :4] /= r
    return dets

def post_processing(prediction, conf, iou_thresh, r, left, top):
    """后处理：过滤低置信度框，执行NMS，还原坐标"""
    # YOLO 输出维度转换为逐框格式，便于后续筛选
    prediction = prediction.permute(0, 2, 1).squeeze(0)
    # 取类别置信度较高的候选框
    xc = prediction[:, 4:6].amax(1) > conf
    x = prediction[xc]
    if not len(x):
        return []
    boxes = x[:, :4]
    boxes = xywh2xyxy(boxes)
    score, index = torch.max(x[:, 4:6], dim=-1, keepdim=True)
    x = torch.cat((boxes, score, x[:, 6:14], index), dim=1)
    score = x[:, 4]
    keep = my_nums(x, iou_thresh)
    x = x[keep]
    x = restore_box(x, r, left, top)
    return x

def pre_processing(img, img_size, device):
    """预处理：LetterBox缩放，HWC->CHW，归一化"""
    img, r, left, top = letter_box(img, (img_size, img_size))
    img = img[:, :, ::-1].transpose((2, 0, 1)).copy() # BGR -> RGB -> CHW
    img = torch.from_numpy(img).to(device)
    img = img.float()
    img = img / 255.0
    img = img.unsqueeze(0)
    return img, r, left, top

def crop_and_filter_roi(img_ori, rect, output):
    rect[0] = max(rect[0], 0)
    rect[1] = max(rect[1], 0)
    rect[2] = min(rect[2], img_ori.shape[1])
    rect[3] = min(rect[3], img_ori.shape[0])
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    if width <= 0 or height <= 0:
        return None
    aspect_ratio = width / max(height, 1)
    if aspect_ratio < MIN_PLATE_ASPECT_RATIO or aspect_ratio > MAX_PLATE_ASPECT_RATIO:
        return None
    roi_img = img_ori[rect[1]:rect[3], rect[0]:rect[2]]
    if roi_img.size == 0:
        return None
    if int(output[-1]):
        roi_img = get_split_merge(roi_img)
    return roi_img

# 车牌检测与识别主流程
def det_rec_plate(img, img_ori, detect_model, plate_rec_model, img_size, device):
    """核心函数：执行车牌检测与识别"""
    result_list = []
    img_input, r, left, top = pre_processing(img, img_size, device)
    predict = detect_model(img_input)[0]
    outputs = post_processing(predict, 0.3, 0.5, r, left, top)
    for output in outputs:
        output = output.squeeze().cpu().numpy().tolist()
        rect = [int(x) for x in output[:4]]
        roi_img = crop_and_filter_roi(img_ori, rect, output)
        if roi_img is None:
            continue
        plate_number, rec_prob, plate_color, color_conf = get_plate_result(roi_img, device, plate_rec_model, is_color=True)
        detect_conf = float(output[4])
        rec_conf = float(np.mean(rec_prob)) if len(rec_prob) else 0.0
        if detect_conf >= MIN_DETECT_CONF and rec_conf >= MIN_RECOGNITION_CONF and is_valid_plate_number(plate_number):
            result_list.append({
                'plate_no': plate_number,
                'plate_color': plate_color,
                'rect': rect,
                'detect_conf': detect_conf,
                'rec_conf': rec_conf,
                'color_conf': float(color_conf),
                'plate_type': int(output[-1])
            })
    return result_list

def draw_result(img, result_dict):
    """在图片上绘制识别结果（矩形框+文字）"""
    rect = result_dict['rect']
    label = f"{result_dict['plate_no']} {result_dict['plate_color']}"
    # 红框表示检测到的车牌区域，绿色文字显示车牌号与颜色
    cv2.rectangle(img, (rect[0], rect[1]), (rect[2], rect[3]), (0, 0, 255), 2)
    img = cv2ImgAddText(img, label, rect[0], rect[1]-30, (0, 255, 0), 25) # 绘制中文
    return img

def draw_results_and_collect(img, results):
    """绘制所有识别结果，并仅收集成功画框的目标"""
    drawn_results = []
    for res in results:
        try:
            img = draw_result(img, res)
            drawn_results.append(res)
        except Exception as e:
            print(f"绘制识别框失败: {e}", flush=True)
    return img, drawn_results

# 连续帧确认与结果落库
def persist_drawn_results(drawn_results, road_id, confirm_state, frame_index):
    """仅对连续多帧都稳定出现的车牌执行保存"""
    global latest_plates, unique_plate_set
    # 当前帧没有有效车牌时，清空确认状态，避免旧状态误延续
    if not drawn_results:
        confirm_state.clear()
        return

    current_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    current_keys = set()
    for res in drawn_results:
        plate_no = res['plate_no']
        plate_color = res['plate_color']
        rect = res['rect']
        current_keys.add(plate_no)

        # 只有车牌号一致、且与上一检测帧位置重叠度足够高，才视为同一目标持续出现
        track = confirm_state.get(plate_no)
        same_track = (
            track is not None
            and track['last_frame'] == frame_index - 1
            and calc_box_iou(track['rect'], rect) >= MIN_BOX_IOU_FOR_TRACKING
        )

        # 记录当前车牌在连续检测帧中的出现次数
        confirm_count = track['count'] + 1 if same_track else 1
        confirm_state[plate_no] = {
            "count": confirm_count,
            "rect": rect,
            "last_frame": frame_index
        }

        # 未达到连续确认阈值前，只缓存不落库
        if confirm_count < REQUIRED_CONSECUTIVE_FRAMES:
            continue
        # 同一轮监控中已经保存过的车牌不再重复写入
        if plate_no in unique_plate_set:
            continue

        unique_plate_set.add(plate_no)
        print(f"检测到车牌: {plate_no} road_id: {road_id}", flush=True)

        if road_id:
            save_road_condition(road_id, plate_no, current_time)

        latest_plates.insert(0, {
            "plate": plate_no,
            "color": plate_color,
            "time": current_time,
            "type": "小型车" if "蓝" in plate_color else "新能源" if "绿" in plate_color else "大型车"
        })
        if len(latest_plates) > 20:
            latest_plates.pop()

    # 删除本帧已经消失的目标，避免确认状态无限积累
    stale_keys = [key for key in confirm_state.keys() if key not in current_keys]
    for key in stale_keys:
        del confirm_state[key]

# 图片识别接口
@app.route('/predict', methods=['POST'])
def predict():
    """接口：上传图片进行车牌识别"""
    # Flask 从 multipart/form-data 中取出上传文件
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file uploaded"}), 400
        
    file = request.files['file']
    # 将上传的二进制图片解码成 OpenCV 图像
    img_bytes = file.read()
    nparr = np.frombuffer(img_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    if img is None:
        return jsonify({"status": "error", "message": "Invalid image"}), 400
        
    try:
        check_models()
        img_ori = copy.deepcopy(img)
        # 返回纯识别结果，不附带可视化图像
        results = det_rec_plate(img, img_ori, detect_model, plate_rec_model, IMG_SIZE, DEVICE)
        
        return jsonify({"status": "success", "results": results})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/predict_visualize', methods=['POST'])
def predict_visualize():
    """接口：上传图片识别并返回可视化结果图"""
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file uploaded"}), 400
        
    file = request.files['file']
    img_bytes = file.read()
    nparr = np.frombuffer(img_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    if img is None:
        return jsonify({"status": "error", "message": "Invalid image"}), 400
        
    try:
        check_models()
        img_ori = copy.deepcopy(img)
        results = det_rec_plate(img, img_ori, detect_model, plate_rec_model, IMG_SIZE, DEVICE)
        
        # 在图片副本上绘制识别框和文字，避免修改原始图像对象
        vis_img = img.copy()
        for res in results:
            vis_img = draw_result(vis_img, res)
            
        # 编码为jpg格式返回
        _, buffer = cv2.imencode('.jpg', vis_img)
        io_buf = io.BytesIO(buffer)
        
        return send_file(io_buf, mimetype='image/jpeg')
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

def save_road_condition(road_id, plate, timestamp):
    """
    将识别结果保存到Java后端数据库
    """
    try:
        # 通过 HTTP 请求把 Python 识别结果同步给 Java 业务后端
        url = "http://localhost:9090/roadCondition/add"
        data = {
            "roadId": int(road_id),
            "plate": plate,
            "time": timestamp
        }
        print(f"发送识别结果到后端: {data}", flush=True)
        # 实际场景建议使用异步队列避免阻塞
        response = requests.post(url, json=data, timeout=3)
        if response.status_code != 200:
             print(f"后端返回错误: {response.status_code} {response.text}", flush=True)
        else:
             print("成功保存至后端", flush=True)
    except Exception as e:
        print(f"保存路况失败: {e}", flush=True)

# 视频流处理与实时监控接口
def gen_frames(video_url, road_id=None):
    """生成器：逐帧处理视频流"""
    global latest_plates, unique_plate_set
    check_models()
    unique_plate_set.clear()
    # 每一路视频流都维护自己的连续帧确认状态
    confirm_state = {}
    detect_frame_index = 0
    
    print(f"开始处理视频流 road_id: {road_id}", flush=True)
    
    # 针对本地文件的优化处理
    # 兼容完整的URL和相对路径
    if 'localhost:9090/files/download/' in video_url or '/files/download/' in video_url:
        try:
            filename = video_url.split('/download/')[-1]
            import urllib.parse
            filename = urllib.parse.unquote(filename)
            # api_server.py 位于 spingboot/python-service
            # files 位于 spingboot/files
            # 需要向上跳转一级目录
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            local_path = os.path.join(base_dir, 'files', filename)
            if os.path.exists(local_path):
                # 读取服务器本地文件比再次走 HTTP 下载更高效
                video_url = local_path
                print(f"使用本地文件路径: {video_url}")
        except Exception as e:
            print(f"本地路径解析错误: {e}")

    # 检查输入是图片还是视频
    is_image = False
    if isinstance(video_url, str):
        ext = os.path.splitext(video_url)[1].lower()
        if ext in ['.jpg', '.jpeg', '.png', '.bmp', '.webp']:
            is_image = True
            
    if is_image:
        # 图片模式：读取一张静态图，并以 MJPEG 方式重复推送，便于前端统一展示
        frame = cv2.imread(video_url)
        if frame is None:
            return
            
        img_ori = copy.deepcopy(frame)
        try:
            results = det_rec_plate(frame, img_ori, detect_model, plate_rec_model, IMG_SIZE, DEVICE)
            frame, drawn_results = draw_results_and_collect(frame, results)
            # 单张图片没有前后帧，因此重复计数以满足连续确认逻辑
            for _ in range(REQUIRED_CONSECUTIVE_FRAMES):
                detect_frame_index += 1
                persist_drawn_results(drawn_results, road_id, confirm_state, detect_frame_index)
                
        except Exception as e:
            print(f"图片处理错误: {e}")
            
        ret, buffer = cv2.imencode('.jpg', frame)
        frame_bytes = buffer.tobytes()
        
        # 重复发送同一帧，前端会把它当作视频流持续显示
        while True:
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')
            time.sleep(1) 
            
    else:
        # 视频模式：逐帧读取并抽帧识别，兼顾性能与实时性
        cap = cv2.VideoCapture(video_url)
        
        frame_count = 0
        skip_frames = 2 # 跳帧处理以降低负载
        last_results = [] # 复用最近一次检测结果，避免非检测帧完全无框
        
        while True:
            success, frame = cap.read()
            if not success:
                break
                
            frame_count += 1
            
            # 每隔 N 帧才真正调用一次模型，减少服务器推理压力
            if frame_count % skip_frames == 0:
                img_ori = copy.deepcopy(frame)
                try:
                    results = det_rec_plate(frame, img_ori, detect_model, plate_rec_model, IMG_SIZE, DEVICE)
                    frame, last_results = draw_results_and_collect(frame, results)
                    detect_frame_index += 1
                    persist_drawn_results(last_results, road_id, confirm_state, detect_frame_index)
                except Exception as e:
                    print(f"帧处理错误: {e}")
            
            # 非检测帧直接复用上一轮结果，保证前端画面连续
            if frame_count % skip_frames != 0:
                for res in last_results:
                    try:
                        frame = draw_result(frame, res)
                    except Exception:
                        pass
            
            ret, buffer = cv2.imencode('.jpg', frame)
            frame_bytes = buffer.tobytes()
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')
        
        cap.release()

# 实时监控页通过该接口获取 MJPEG 视频流
@app.route('/video_feed')
def video_feed():
    """接口：视频流服务（MJPEG）"""
    print("收到 /video_feed 请求", flush=True)
    video_url = request.args.get('url')
    road_id = request.args.get('roadId')
    print(f"请求参数 - url: {video_url}, roadId: {road_id}", flush=True)
    if not video_url:
        return "Missing video url", 400
    return Response(gen_frames(video_url, road_id), mimetype='multipart/x-mixed-replace; boundary=frame')

# 返回最近一次识别到的车牌列表，供前端轮询展示
@app.route('/get_latest_results')
def get_latest_results():
    """接口：获取最新的车牌识别记录"""
    global latest_plates
    return jsonify({"status": "success", "data": latest_plates})

@app.route('/predict_video', methods=['POST'])
def predict_video():
    """接口：上传视频文件并返回所有识别结果列表"""
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file uploaded"}), 400
        
    file = request.files['file']
    
    # OpenCV 读取视频需要文件路径，因此先将上传内容落到临时文件
    fd, video_path = tempfile.mkstemp(suffix='.mp4')
    os.close(fd)
    file.save(video_path)

    try:
        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
             os.remove(video_path)
             return jsonify({"status": "error", "message": "Cannot open video file"}), 400

        all_plates = []
        unique_plates = set()
        frame_count = 0
        
        # 离线视频识别不需要实时性，因此采用更大的抽帧间隔加快处理速度
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            
            frame_count += 1
            if frame_count % 10 != 0: # 抽帧处理，每10帧检测一次以提高速度
                continue

            img_ori = copy.deepcopy(frame)
            try:
                results = det_rec_plate(frame, img_ori, detect_model, plate_rec_model, IMG_SIZE, DEVICE)
                
                for res in results:
                    plate_no = res['plate_no']
                    plate_color = res['plate_color']
                    
                    # 同一个视频内只返回一次相同车牌，避免结果列表过长
                    if len(plate_no) >= 7 and plate_no not in unique_plates:
                        unique_plates.add(plate_no)
                        current_time = time.strftime("%H:%M:%S", time.localtime())
                        all_plates.append({
                            "plate": plate_no,
                            "color": plate_color,
                            "time": current_time
                        })
            except Exception as inner_e:
                print(f"帧 {frame_count} 处理错误: {inner_e}")
                continue

        cap.release()
        os.remove(video_path) # 处理完成后删除临时文件

        return jsonify({"status": "success", "results": all_plates})

    except Exception as e:
        if os.path.exists(video_path):
            os.remove(video_path)
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    # 服务启动前先检查权重文件是否存在，避免运行时才报错
    if not os.path.exists(DETECT_WEIGHTS) or not os.path.exists(REC_WEIGHTS):
        print("错误: 权重文件未找到!")
        sys.exit(1)
        
    # 启动时预加载模型，减少首次请求的冷启动延迟
    load_models()
    app.run(host='0.0.0.0', port=5000, debug=False)
