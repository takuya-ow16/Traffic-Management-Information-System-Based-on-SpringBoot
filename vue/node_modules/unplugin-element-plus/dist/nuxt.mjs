import { t as src_default } from "./src-C1__PwhY.mjs";
import { addBuildPlugin, defineNuxtModule } from "@nuxt/kit";

//#region src/nuxt.ts
const module = defineNuxtModule({
	meta: {
		name: "unplugin-element-plus",
		configKey: "elementPlus"
	},
	setup(options) {
		addBuildPlugin({
			vite: () => src_default.vite(options),
			webpack: () => src_default.webpack(options),
			rspack: () => src_default.rspack(options)
		});
	}
});
var nuxt_default = module;

//#endregion
export { nuxt_default as default };