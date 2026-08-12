import { withMagicString } from "rolldown-string";
import { createUnplugin } from "unplugin";
import escapeStringRegexp from "escape-string-regexp";
import { init, parse } from "es-module-lexer";

//#region src/core/default-locale.ts
function getLocaleRE(options) {
	return /* @__PURE__ */ new RegExp(`${escapeStringRegexp(`${options.lib}/`)}(es|lib)${escapeStringRegexp("/hooks/use-locale/index")}`);
}
function transformDefaultLocale(source, id, options) {
	if (!getLocaleRE(options).test(id)) return;
	source.replaceAll("locale/lang/en", `locale/lang/${options.defaultLocale}`);
}
function getViteDepPlugin(options) {
	const localeImporterRE = /* @__PURE__ */ new RegExp(`${escapeStringRegexp(`node_modules/${options.lib}/`)}(es|lib)${escapeStringRegexp("/hooks/use-locale/index")}`);
	const localePath = "/locale/lang/en";
	const localePathFixed = `/locale/lang/${options.defaultLocale}`;
	return {
		name: "unplugin-element-plus:default-locale",
		setup(build) {
			build.onResolve({
				filter: new RegExp(escapeStringRegexp(localePath)),
				namespace: "file"
			}, ({ path, importer, kind, resolveDir }) => {
				if (localeImporterRE.test(importer)) return build.resolve(path.replace(localePath, localePathFixed), {
					importer,
					kind,
					resolveDir
				});
			});
		}
	};
}

//#endregion
//#region src/core/style.ts
const hyphenateRE = /\B([A-Z])/g;
const hyphenate = (str) => str.replaceAll(hyphenateRE, "-$1").toLowerCase();
const formatMap = {
	cjs: "lib",
	esm: "es"
};
const multilineCommentsRE = /\/\*\s(.|[\n\r])*?\*\//g;
const singlelineCommentsRE = /\/\/\s.*/g;
function stripeComments(code) {
	return code.replaceAll(multilineCommentsRE, "").replaceAll(singlelineCommentsRE, "");
}
function transformImportStyle(specifier, source, useSource = false, options) {
	const { prefix, lib, format, ignoreComponents } = options;
	const statement = stripeComments(source.slice(specifier.ss, specifier.se));
	const leftBracket = statement.indexOf("{");
	if (leftBracket !== -1) {
		const components = statement.slice(leftBracket + 1, statement.indexOf("}")).split(",");
		const styleImports = [];
		components.forEach((c) => {
			const trimmed = c.replace(/\sas\s.+/, "").trim();
			if (trimmed.startsWith(prefix)) {
				const component = trimmed.slice(prefix.length);
				if (ignoreComponents.includes(component)) return;
				if (useSource) styleImports.push(`import '${lib}/${formatMap[format]}/components/${hyphenate(component)}/style/index'`);
				else styleImports.push(`import '${lib}/${formatMap[format]}/components/${hyphenate(component)}/style/css'`);
			}
		});
		return styleImports.join("\n");
	}
}
async function transformStyle(s, options) {
	const { useSource, lib, prefix, format, ignoreComponents } = options;
	const source = s.toString();
	if (!source) return;
	await init;
	const specifiers = parse(source)[0].filter(({ n }) => {
		return n === lib || n === `${lib}/es/components` || n === `${lib}/lib/components`;
	});
	if (specifiers.length === 0) return;
	const styleImports = specifiers.map((s$1) => {
		return transformImportStyle(s$1, source, useSource, {
			lib,
			prefix,
			format,
			ignoreComponents
		});
	}).filter(Boolean).join("\n");
	const lastSpecifier = specifiers.at(-1);
	s.appendLeft(lastSpecifier.se + 1, `\n${styleImports}\n`);
}

//#endregion
//#region src/index.ts
const defaultOptions = {
	include: [
		"**/*.vue",
		"**/*.ts",
		"**/*.js",
		"**/*.tsx",
		"**/*.jsx",
		"**/*.vue?vue&type=script*",
		/\.vue\.[tj]sx?\?vue/
	],
	exclude: [
		/[/\\]node_modules[/\\]/,
		/[/\\]\.git[/\\]/,
		/[/\\]\.nuxt[/\\]/
	],
	lib: "element-plus",
	ignoreComponents: [],
	useSource: false,
	defaultLocale: "",
	format: "esm",
	prefix: "El",
	sourceMap: false
};
function toArray(thing) {
	if (thing == null) return [];
	return Array.isArray(thing) ? thing : [thing];
}
const unplugin = createUnplugin((userOptions = {}) => {
	const options = {
		...defaultOptions,
		...userOptions
	};
	return {
		name: "unplugin-element-plus",
		enforce: "post",
		transform: {
			filter: { id: {
				include: [getLocaleRE(options), ...toArray(options.include)],
				exclude: options.exclude
			} },
			handler: withMagicString((s, id) => {
				if (options.defaultLocale) return transformDefaultLocale(s, id, options);
				return transformStyle(s, options);
			})
		},
		vite: { config() {
			if (options.defaultLocale) return { optimizeDeps: { esbuildOptions: { plugins: [getViteDepPlugin(options)] } } };
		} }
	};
});
var src_default = unplugin;

//#endregion
export { src_default as t };