import "./types-DEGIl8BG.mjs";
import { t as unplugin } from "./index-BplHWBeB.mjs";

//#region src/rspack.d.ts

/**
* Rspack plugin
*
* @example
* ```js
* // rspack.config.js
* import ElementPlus from 'unplugin-element-plus/rspack'
*
* default export {
*  plugins: [ElementPlus()],
* }
* ```
*/
declare const rspack: typeof unplugin.rspack;
//#endregion
export { rspack as default, rspack as "module.exports" };