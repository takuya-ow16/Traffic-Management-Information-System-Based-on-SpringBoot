import "./types-DEGIl8BG.mjs";
import { t as unplugin } from "./index-BplHWBeB.mjs";

//#region src/rollup.d.ts

/**
* Rollup plugin
*
* @example
* ```ts
* // rollup.config.js
* import ElementPlus from 'unplugin-element-plus/rollup'
*
* export default {
*   plugins: [ElementPlus()],
* }
* ```
*/
declare const rollup: typeof unplugin.rollup;
//#endregion
export { rollup as default, rollup as "module.exports" };