package com.example.controller;


import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//用于处理文件相关的接口
@RestController
@RequestMapping("/files")
public class Filecontroller {
    //System.getProperty("user.dir")获取到当前项目的根路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";


    //文件上传
    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file) {  //接收前端发送的文件
        String OriginalFilename = file.getOriginalFilename();  //xxx.png 获取文件名

        if (!FileUtil.isDirectory(filePath)) {  //如果没有目录则创建目录
            FileUtil.mkdir(filePath);
        }
        //提供文件存储的完整路径
        //给文件名加一个唯一的标识
        String fileName = System.currentTimeMillis() + "_" +OriginalFilename;   //时间戳_文件名
        String realPath = filePath + fileName;  //完整的文件路径
        try {
            //file.getInputStream().transferTo(realPath);
            FileUtil.writeBytes(file.getBytes(), realPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500", "文件上传失败");
        }
        //返回一个网络链接
        String url = "/files/download/" + fileName;
        //http://local:9090/files/download/xxxx.jpg
        return Result.success(url);
    }

    //文件下载
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            //将文件名设置成同样的编码
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType("application/octet-stream");
            OutputStream os = response.getOutputStream();
            String realPath = filePath + fileName;
            //获取文件的字节数组
            byte[] bytes = FileUtil.readBytes(realPath);
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new CustomException("500", "文件下载失败");
        }
    }

    @PostMapping("wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);
        }

        String fileName = System.currentTimeMillis() + "_" + originalFilename;   //时间戳_文件名
        String realPath = filePath + fileName;  //完整的文件路径
        try {
            //file.getInputStream().transferTo(realPath);
            FileUtil.writeBytes(file.getBytes(), realPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500", "文件上传失败");
        }
        String url = "/files/download/" + fileName;

        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("url", url);
        list.add(urlMap);
        resMap.put("error", 0);
        resMap.put("data", list);
        return resMap;
    }
}
