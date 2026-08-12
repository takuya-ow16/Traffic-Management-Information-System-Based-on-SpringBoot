import "./types-DEGIl8BG.mjs";
import { t as unplugin } from "./index-BplHWBeB.mjs";

//#region src/webpack.d.ts

/**
* Webpack plugin
*
* @example
* ```js
* // webpack.config.js
* import ElementPlus from 'unplugin-element-plus/webpack'
*
* default export {
*  plugins: [ElementPlus()],
* }
* ```
*/
declare const webpack: typeof unplugin.webpack;
//#endregion
export { webpack as default, webpack as "module.exports" };