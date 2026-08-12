import "./types-DEGIl8BG.mjs";
import { t as unplugin } from "./index-BplHWBeB.mjs";

//#region src/vite.d.ts

/**
* Vite plugin
*
* @example
* ```ts
* // vite.config.ts
* import ElementPlus from 'unplugin-element-plus/vite'
*
* export default defineConfig({
*   plugins: [ElementPlus()],
* })
* ```
*/
declare const vite: typeof unplugin.vite;
//#endregion
export { vite as default, vite as "module.exports" };