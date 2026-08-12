import "./types-DEGIl8BG.mjs";
import { t as unplugin } from "./index-BplHWBeB.mjs";

//#region src/esbuild.d.ts

/**
* Esbuild plugin
*
* @example
* ```ts
* import { build } from 'esbuild'
* import ElementPlus from 'unplugin-element-plus/esbuild'
*
* build({ plugins: [ElementPlus()] })
```
*/
declare const esbuild: typeof unplugin.esbuild;
//#endregion
export { esbuild as default, esbuild as "module.exports" };