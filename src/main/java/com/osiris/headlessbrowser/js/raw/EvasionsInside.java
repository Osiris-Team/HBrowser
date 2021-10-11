package com.osiris.headlessbrowser.js.raw;

/**
 * Contains all evasions from puppeteer extra stealth, compatible
 * with playwright. <br>
 * All the code below must be run within the pages' context, before it gets loaded. <br>
 */
public class EvasionsInside {

    public String utils = "/**\n" +
            " * A set of shared utility functions specifically for the purpose of modifying native browser APIs without leaving traces.\n" +
            " *\n" +
            " * Meant to be passed down in puppeteer and used in the context of the page (everything in here runs in NodeJS as well as a browser).\n" +
            " *\n" +
            " * Note: If for whatever reason you need to use this outside of `puppeteer-extra`:\n" +
            " * Just remove the `module.exports` statement at the very bottom, the rest can be copy pasted into any browser context.\n" +
            " *\n" +
            " * Alternatively take a look at the `extract-stealth-evasions` package to create a finished bundle which includes these utilities.\n" +
            " *\n" +
            " */\n" +
            "const utils = {}\n" +
            "\n" +
            "utils.init = () => {\n" +
            "  utils.preloadCache()\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Wraps a JS Proxy Handler and strips it's presence from error stacks, in case the traps throw.\n" +
            " *\n" +
            " * The presence of a JS Proxy can be revealed as it shows up in error stack traces.\n" +
            " *\n" +
            " * @param {object} handler - The JS Proxy handler to wrap\n" +
            " */\n" +
            "utils.stripProxyFromErrors = (handler = {}) => {\n" +
            "  const newHandler = {\n" +
            "    setPrototypeOf: function (target, proto) {\n" +
            "      if (proto === null)\n" +
            "        throw new TypeError('Cannot convert object to primitive value')\n" +
            "      if (Object.getPrototypeOf(target) === Object.getPrototypeOf(proto)) {\n" +
            "        throw new TypeError('Cyclic __proto__ value')\n" +
            "      }\n" +
            "      return Reflect.setPrototypeOf(target, proto)\n" +
            "    }\n" +
            "  }\n" +
            "  // We wrap each trap in the handler in a try/catch and modify the error stack if they throw\n" +
            "  const traps = Object.getOwnPropertyNames(handler)\n" +
            "  traps.forEach(trap => {\n" +
            "    newHandler[trap] = function () {\n" +
            "      try {\n" +
            "        // Forward the call to the defined proxy handler\n" +
            "        return handler[trap].apply(this, arguments || [])\n" +
            "      } catch (err) {\n" +
            "        // Stack traces differ per browser, we only support chromium based ones currently\n" +
            "        if (!err || !err.stack || !err.stack.includes(`at `)) {\n" +
            "          throw err\n" +
            "        }\n" +
            "\n" +
            "        // When something throws within one of our traps the Proxy will show up in error stacks\n" +
            "        // An earlier implementation of this code would simply strip lines with a blacklist,\n" +
            "        // but it makes sense to be more surgical here and only remove lines related to our Proxy.\n" +
            "        // We try to use a known \"anchor\" line for that and strip it with everything above it.\n" +
            "        // If the anchor line cannot be found for some reason we fall back to our blacklist approach.\n" +
            "\n" +
            "        const stripWithBlacklist = (stack, stripFirstLine = true) => {\n" +
            "          const blacklist = [\n" +
            "            `at Reflect.${trap} `, // e.g. Reflect.get or Reflect.apply\n" +
            "            `at Object.${trap} `, // e.g. Object.get or Object.apply\n" +
            "            `at Object.newHandler.<computed> [as ${trap}] ` // caused by this very wrapper :-)\n" +
            "          ]\n" +
            "          return (\n" +
            "            err.stack\n" +
            "              .split('\\n')\n" +
            "              // Always remove the first (file) line in the stack (guaranteed to be our proxy)\n" +
            "              .filter((line, index) => !(index === 1 && stripFirstLine))\n" +
            "              // Check if the line starts with one of our blacklisted strings\n" +
            "              .filter(line => !blacklist.some(bl => line.trim().startsWith(bl)))\n" +
            "              .join('\\n')\n" +
            "          )\n" +
            "        }\n" +
            "\n" +
            "        const stripWithAnchor = (stack, anchor) => {\n" +
            "          const stackArr = stack.split('\\n')\n" +
            "          anchor = anchor || `at Object.newHandler.<computed> [as ${trap}] ` // Known first Proxy line in chromium\n" +
            "          const anchorIndex = stackArr.findIndex(line =>\n" +
            "            line.trim().startsWith(anchor)\n" +
            "          )\n" +
            "          if (anchorIndex === -1) {\n" +
            "            return false // 404, anchor not found\n" +
            "          }\n" +
            "          // Strip everything from the top until we reach the anchor line\n" +
            "          // Note: We're keeping the 1st line (zero index) as it's unrelated (e.g. `TypeError`)\n" +
            "          stackArr.splice(1, anchorIndex)\n" +
            "          return stackArr.join('\\n')\n" +
            "        }\n" +
            "\n" +
            "        // Special cases due to our nested toString proxies\n" +
            "        err.stack = err.stack.replace(\n" +
            "          'at Object.toString (',\n" +
            "          'at Function.toString ('\n" +
            "        )\n" +
            "        if ((err.stack || '').includes('at Function.toString (')) {\n" +
            "          err.stack = stripWithBlacklist(err.stack, false)\n" +
            "          throw err\n" +
            "        }\n" +
            "\n" +
            "        // Try using the anchor method, fallback to blacklist if necessary\n" +
            "        err.stack = stripWithAnchor(err.stack) || stripWithBlacklist(err.stack)\n" +
            "\n" +
            "        throw err // Re-throw our now sanitized error\n" +
            "      }\n" +
            "    }\n" +
            "  })\n" +
            "  return newHandler\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Strip error lines from stack traces until (and including) a known line the stack.\n" +
            " *\n" +
            " * @param {object} err - The error to sanitize\n" +
            " * @param {string} anchor - The string the anchor line starts with\n" +
            " */\n" +
            "utils.stripErrorWithAnchor = (err, anchor) => {\n" +
            "  const stackArr = err.stack.split('\\n')\n" +
            "  const anchorIndex = stackArr.findIndex(line => line.trim().startsWith(anchor))\n" +
            "  if (anchorIndex === -1) {\n" +
            "    return err // 404, anchor not found\n" +
            "  }\n" +
            "  // Strip everything from the top until we reach the anchor line (remove anchor line as well)\n" +
            "  // Note: We're keeping the 1st line (zero index) as it's unrelated (e.g. `TypeError`)\n" +
            "  stackArr.splice(1, anchorIndex)\n" +
            "  err.stack = stackArr.join('\\n')\n" +
            "  return err\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Replace the property of an object in a stealthy way.\n" +
            " *\n" +
            " * Note: You also want to work on the prototype of an object most often,\n" +
            " * as you'd otherwise leave traces (e.g. showing up in Object.getOwnPropertyNames(obj)).\n" +
            " *\n" +
            " * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/defineProperty\n" +
            " *\n" +
            " * @example\n" +
            " * replaceProperty(WebGLRenderingContext.prototype, 'getParameter', { value: \"alice\" })\n" +
            " * // or\n" +
            " * replaceProperty(Object.getPrototypeOf(navigator), 'languages', { get: () => ['en-US', 'en'] })\n" +
            " *\n" +
            " * @param {object} obj - The object which has the property to replace\n" +
            " * @param {string} propName - The property name to replace\n" +
            " * @param {object} descriptorOverrides - e.g. { value: \"alice\" }\n" +
            " */\n" +
            "utils.replaceProperty = (obj, propName, descriptorOverrides = {}) => {\n" +
            "  return Object.defineProperty(obj, propName, {\n" +
            "    // Copy over the existing descriptors (writable, enumerable, configurable, etc)\n" +
            "    ...(Object.getOwnPropertyDescriptor(obj, propName) || {}),\n" +
            "    // Add our overrides (e.g. value, get())\n" +
            "    ...descriptorOverrides\n" +
            "  })\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Preload a cache of function copies and data.\n" +
            " *\n" +
            " * For a determined enough observer it would be possible to overwrite and sniff usage of functions\n" +
            " * we use in our internal Proxies, to combat that we use a cached copy of those functions.\n" +
            " *\n" +
            " * Note: Whenever we add a `Function.prototype.toString` proxy we should preload the cache before,\n" +
            " * by executing `utils.preloadCache()` before the proxy is applied (so we don't cause recursive lookups).\n" +
            " *\n" +
            " * This is evaluated once per execution context (e.g. window)\n" +
            " */\n" +
            "utils.preloadCache = () => {\n" +
            "  if (utils.cache) {\n" +
            "    return\n" +
            "  }\n" +
            "  utils.cache = {\n" +
            "    // Used in our proxies\n" +
            "    Reflect: {\n" +
            "      get: Reflect.get.bind(Reflect),\n" +
            "      apply: Reflect.apply.bind(Reflect)\n" +
            "    },\n" +
            "    // Used in `makeNativeString`\n" +
            "    nativeToStringStr: Function.toString + '' // => `function toString() { [native code] }`\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Utility function to generate a cross-browser `toString` result representing native code.\n" +
            " *\n" +
            " * There's small differences: Chromium uses a single line, whereas FF & Webkit uses multiline strings.\n" +
            " * To future-proof this we use an existing native toString result as the basis.\n" +
            " *\n" +
            " * The only advantage we have over the other team is that our JS runs first, hence we cache the result\n" +
            " * of the native toString result once, so they cannot spoof it afterwards and reveal that we're using it.\n" +
            " *\n" +
            " * @example\n" +
            " * makeNativeString('foobar') // => `function foobar() { [native code] }`\n" +
            " *\n" +
            " * @param {string} [name] - Optional function name\n" +
            " */\n" +
            "utils.makeNativeString = (name = '') => {\n" +
            "  return utils.cache.nativeToStringStr.replace('toString', name || '')\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Helper function to modify the `toString()` result of the provided object.\n" +
            " *\n" +
            " * Note: Use `utils.redirectToString` instead when possible.\n" +
            " *\n" +
            " * There's a quirk in JS Proxies that will cause the `toString()` result to differ from the vanilla Object.\n" +
            " * If no string is provided we will generate a `[native code]` thing based on the name of the property object.\n" +
            " *\n" +
            " * @example\n" +
            " * patchToString(WebGLRenderingContext.prototype.getParameter, 'function getParameter() { [native code] }')\n" +
            " *\n" +
            " * @param {object} obj - The object for which to modify the `toString()` representation\n" +
            " * @param {string} str - Optional string used as a return value\n" +
            " */\n" +
            "utils.patchToString = (obj, str = '') => {\n" +
            "  const handler = {\n" +
            "    apply: function (target, ctx) {\n" +
            "      // This fixes e.g. `HTMLMediaElement.prototype.canPlayType.toString + \"\"`\n" +
            "      if (ctx === Function.prototype.toString) {\n" +
            "        return utils.makeNativeString('toString')\n" +
            "      }\n" +
            "      // `toString` targeted at our proxied Object detected\n" +
            "      if (ctx === obj) {\n" +
            "        // We either return the optional string verbatim or derive the most desired result automatically\n" +
            "        return str || utils.makeNativeString(obj.name)\n" +
            "      }\n" +
            "      // Check if the toString protype of the context is the same as the global prototype,\n" +
            "      // if not indicates that we are doing a check across different windows., e.g. the iframeWithdirect` test case\n" +
            "      const hasSameProto = Object.getPrototypeOf(\n" +
            "        Function.prototype.toString\n" +
            "      ).isPrototypeOf(ctx.toString) // eslint-disable-line no-prototype-builtins\n" +
            "      if (!hasSameProto) {\n" +
            "        // Pass the call on to the local Function.prototype.toString instead\n" +
            "        return ctx.toString()\n" +
            "      }\n" +
            "      return target.call(ctx)\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  const toStringProxy = new Proxy(\n" +
            "    Function.prototype.toString,\n" +
            "    utils.stripProxyFromErrors(handler)\n" +
            "  )\n" +
            "  utils.replaceProperty(Function.prototype, 'toString', {\n" +
            "    value: toStringProxy\n" +
            "  })\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Make all nested functions of an object native.\n" +
            " *\n" +
            " * @param {object} obj\n" +
            " */\n" +
            "utils.patchToStringNested = (obj = {}) => {\n" +
            "  return utils.execRecursively(obj, ['function'], utils.patchToString)\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Redirect toString requests from one object to another.\n" +
            " *\n" +
            " * @param {object} proxyObj - The object that toString will be called on\n" +
            " * @param {object} originalObj - The object which toString result we wan to return\n" +
            " */\n" +
            "utils.redirectToString = (proxyObj, originalObj) => {\n" +
            "  const handler = {\n" +
            "    apply: function (target, ctx) {\n" +
            "      // This fixes e.g. `HTMLMediaElement.prototype.canPlayType.toString + \"\"`\n" +
            "      if (ctx === Function.prototype.toString) {\n" +
            "        return utils.makeNativeString('toString')\n" +
            "      }\n" +
            "\n" +
            "      // `toString` targeted at our proxied Object detected\n" +
            "      if (ctx === proxyObj) {\n" +
            "        const fallback = () =>\n" +
            "          originalObj && originalObj.name\n" +
            "            ? utils.makeNativeString(originalObj.name)\n" +
            "            : utils.makeNativeString(proxyObj.name)\n" +
            "\n" +
            "        // Return the toString representation of our original object if possible\n" +
            "        return originalObj + '' || fallback()\n" +
            "      }\n" +
            "\n" +
            "      if (typeof ctx === 'undefined' || ctx === null) {\n" +
            "        return target.call(ctx)\n" +
            "      }\n" +
            "\n" +
            "      // Check if the toString protype of the context is the same as the global prototype,\n" +
            "      // if not indicates that we are doing a check across different windows., e.g. the iframeWithdirect` test case\n" +
            "      const hasSameProto = Object.getPrototypeOf(\n" +
            "        Function.prototype.toString\n" +
            "      ).isPrototypeOf(ctx.toString) // eslint-disable-line no-prototype-builtins\n" +
            "      if (!hasSameProto) {\n" +
            "        // Pass the call on to the local Function.prototype.toString instead\n" +
            "        return ctx.toString()\n" +
            "      }\n" +
            "\n" +
            "      return target.call(ctx)\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  const toStringProxy = new Proxy(\n" +
            "    Function.prototype.toString,\n" +
            "    utils.stripProxyFromErrors(handler)\n" +
            "  )\n" +
            "  utils.replaceProperty(Function.prototype, 'toString', {\n" +
            "    value: toStringProxy\n" +
            "  })\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * All-in-one method to replace a property with a JS Proxy using the provided Proxy handler with traps.\n" +
            " *\n" +
            " * Will stealthify these aspects (strip error stack traces, redirect toString, etc).\n" +
            " * Note: This is meant to modify native Browser APIs and works best with prototype objects.\n" +
            " *\n" +
            " * @example\n" +
            " * replaceWithProxy(WebGLRenderingContext.prototype, 'getParameter', proxyHandler)\n" +
            " *\n" +
            " * @param {object} obj - The object which has the property to replace\n" +
            " * @param {string} propName - The name of the property to replace\n" +
            " * @param {object} handler - The JS Proxy handler to use\n" +
            " */\n" +
            "utils.replaceWithProxy = (obj, propName, handler) => {\n" +
            "  const originalObj = obj[propName]\n" +
            "  const proxyObj = new Proxy(obj[propName], utils.stripProxyFromErrors(handler))\n" +
            "\n" +
            "  utils.replaceProperty(obj, propName, { value: proxyObj })\n" +
            "  utils.redirectToString(proxyObj, originalObj)\n" +
            "\n" +
            "  return true\n" +
            "}\n" +
            "/**\n" +
            " * All-in-one method to replace a getter with a JS Proxy using the provided Proxy handler with traps.\n" +
            " *\n" +
            " * @example\n" +
            " * replaceGetterWithProxy(Object.getPrototypeOf(navigator), 'vendor', proxyHandler)\n" +
            " *\n" +
            " * @param {object} obj - The object which has the property to replace\n" +
            " * @param {string} propName - The name of the property to replace\n" +
            " * @param {object} handler - The JS Proxy handler to use\n" +
            " */\n" +
            "utils.replaceGetterWithProxy = (obj, propName, handler) => {\n" +
            "  const fn = Object.getOwnPropertyDescriptor(obj, propName).get\n" +
            "  const fnStr = fn.toString() // special getter function string\n" +
            "  const proxyObj = new Proxy(fn, utils.stripProxyFromErrors(handler))\n" +
            "\n" +
            "  utils.replaceProperty(obj, propName, { get: proxyObj })\n" +
            "  utils.patchToString(proxyObj, fnStr)\n" +
            "\n" +
            "  return true\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * All-in-one method to mock a non-existing property with a JS Proxy using the provided Proxy handler with traps.\n" +
            " *\n" +
            " * Will stealthify these aspects (strip error stack traces, redirect toString, etc).\n" +
            " *\n" +
            " * @example\n" +
            " * mockWithProxy(chrome.runtime, 'sendMessage', function sendMessage() {}, proxyHandler)\n" +
            " *\n" +
            " * @param {object} obj - The object which has the property to replace\n" +
            " * @param {string} propName - The name of the property to replace or create\n" +
            " * @param {object} pseudoTarget - The JS Proxy target to use as a basis\n" +
            " * @param {object} handler - The JS Proxy handler to use\n" +
            " */\n" +
            "utils.mockWithProxy = (obj, propName, pseudoTarget, handler) => {\n" +
            "  const proxyObj = new Proxy(pseudoTarget, utils.stripProxyFromErrors(handler))\n" +
            "\n" +
            "  utils.replaceProperty(obj, propName, { value: proxyObj })\n" +
            "  utils.patchToString(proxyObj)\n" +
            "\n" +
            "  return true\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * All-in-one method to create a new JS Proxy with stealth tweaks.\n" +
            " *\n" +
            " * This is meant to be used whenever we need a JS Proxy but don't want to replace or mock an existing known property.\n" +
            " *\n" +
            " * Will stealthify certain aspects of the Proxy (strip error stack traces, redirect toString, etc).\n" +
            " *\n" +
            " * @example\n" +
            " * createProxy(navigator.mimeTypes.__proto__.namedItem, proxyHandler) // => Proxy\n" +
            " *\n" +
            " * @param {object} pseudoTarget - The JS Proxy target to use as a basis\n" +
            " * @param {object} handler - The JS Proxy handler to use\n" +
            " */\n" +
            "utils.createProxy = (pseudoTarget, handler) => {\n" +
            "  const proxyObj = new Proxy(pseudoTarget, utils.stripProxyFromErrors(handler))\n" +
            "  utils.patchToString(proxyObj)\n" +
            "\n" +
            "  return proxyObj\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Helper function to split a full path to an Object into the first part and property.\n" +
            " *\n" +
            " * @example\n" +
            " * splitObjPath(`HTMLMediaElement.prototype.canPlayType`)\n" +
            " * // => {objName: \"HTMLMediaElement.prototype\", propName: \"canPlayType\"}\n" +
            " *\n" +
            " * @param {string} objPath - The full path to an object as dot notation string\n" +
            " */\n" +
            "utils.splitObjPath = objPath => ({\n" +
            "  // Remove last dot entry (property) ==> `HTMLMediaElement.prototype`\n" +
            "  objName: objPath.split('.').slice(0, -1).join('.'),\n" +
            "  // Extract last dot entry ==> `canPlayType`\n" +
            "  propName: objPath.split('.').slice(-1)[0]\n" +
            "})\n" +
            "\n" +
            "/**\n" +
            " * Convenience method to replace a property with a JS Proxy using the provided objPath.\n" +
            " *\n" +
            " * Supports a full path (dot notation) to the object as string here, in case that makes it easier.\n" +
            " *\n" +
            " * @example\n" +
            " * replaceObjPathWithProxy('WebGLRenderingContext.prototype.getParameter', proxyHandler)\n" +
            " *\n" +
            " * @param {string} objPath - The full path to an object (dot notation string) to replace\n" +
            " * @param {object} handler - The JS Proxy handler to use\n" +
            " */\n" +
            "utils.replaceObjPathWithProxy = (objPath, handler) => {\n" +
            "  const { objName, propName } = utils.splitObjPath(objPath)\n" +
            "  const obj = eval(objName) // eslint-disable-line no-eval\n" +
            "  return utils.replaceWithProxy(obj, propName, handler)\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Traverse nested properties of an object recursively and apply the given function on a whitelist of value types.\n" +
            " *\n" +
            " * @param {object} obj\n" +
            " * @param {array} typeFilter - e.g. `['function']`\n" +
            " * @param {Function} fn - e.g. `utils.patchToString`\n" +
            " */\n" +
            "utils.execRecursively = (obj = {}, typeFilter = [], fn) => {\n" +
            "  function recurse(obj) {\n" +
            "    for (const key in obj) {\n" +
            "      if (obj[key] === undefined) {\n" +
            "        continue\n" +
            "      }\n" +
            "      if (obj[key] && typeof obj[key] === 'object') {\n" +
            "        recurse(obj[key])\n" +
            "      } else {\n" +
            "        if (obj[key] && typeFilter.includes(typeof obj[key])) {\n" +
            "          fn.call(this, obj[key])\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  recurse(obj)\n" +
            "  return obj\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Everything we run through e.g. `page.evaluate` runs in the browser context, not the NodeJS one.\n" +
            " * That means we cannot just use reference variables and functions from outside code, we need to pass everything as a parameter.\n" +
            " *\n" +
            " * Unfortunately the data we can pass is only allowed to be of primitive types, regular functions don't survive the built-in serialization process.\n" +
            " * This utility function will take an object with functions and stringify them, so we can pass them down unharmed as strings.\n" +
            " *\n" +
            " * We use this to pass down our utility functions as well as any other functions (to be able to split up code better).\n" +
            " *\n" +
            " * @see utils.materializeFns\n" +
            " *\n" +
            " * @param {object} fnObj - An object containing functions as properties\n" +
            " */\n" +
            "utils.stringifyFns = (fnObj = { hello: () => 'world' }) => {\n" +
            "  // Object.fromEntries() ponyfill (in 6 lines) - supported only in Node v12+, modern browsers are fine\n" +
            "  // https://github.com/feross/fromentries\n" +
            "  function fromEntries(iterable) {\n" +
            "    return [...iterable].reduce((obj, [key, val]) => {\n" +
            "      obj[key] = val\n" +
            "      return obj\n" +
            "    }, {})\n" +
            "  }\n" +
            "  return (Object.fromEntries || fromEntries)(\n" +
            "    Object.entries(fnObj)\n" +
            "      .filter(([key, value]) => typeof value === 'function')\n" +
            "      .map(([key, value]) => [key, value.toString()]) // eslint-disable-line no-eval\n" +
            "  )\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * Utility function to reverse the process of `utils.stringifyFns`.\n" +
            " * Will materialize an object with stringified functions (supports classic and fat arrow functions).\n" +
            " *\n" +
            " * @param {object} fnStrObj - An object containing stringified functions as properties\n" +
            " */\n" +
            "utils.materializeFns = (fnStrObj = { hello: \"() => 'world'\" }) => {\n" +
            "  return Object.fromEntries(\n" +
            "    Object.entries(fnStrObj).map(([key, value]) => {\n" +
            "      if (value.startsWith('function')) {\n" +
            "        // some trickery is needed to make oldschool functions work :-)\n" +
            "        return [key, eval(`() => ${value}`)()] // eslint-disable-line no-eval\n" +
            "      } else {\n" +
            "        // arrow functions just work\n" +
            "        return [key, eval(value)] // eslint-disable-line no-eval\n" +
            "      }\n" +
            "    })\n" +
            "  )\n" +
            "}\n" +
            "\n" +
            "// Proxy handler templates for re-usability\n" +
            "utils.makeHandler = () => ({\n" +
            "  // Used by simple `navigator` getter evasions\n" +
            "  getterValue: value => ({\n" +
            "    apply(target, ctx, args) {\n" +
            "      // Let's fetch the value first, to trigger and escalate potential errors\n" +
            "      // Illegal invocations like `navigator.__proto__.vendor` will throw here\n" +
            "      utils.cache.Reflect.apply(...arguments)\n" +
            "      return value\n" +
            "    }\n" +
            "  })\n" +
            "})\n";
    /**
     * Mock the `chrome.app` object if not available (e.g. when running headless).
     */
    public String chrome_app = "      if (!window.chrome) {\n" +
            "        // Use the exact property descriptor found in headful Chrome\n" +
            "        // fetch it via `Object.getOwnPropertyDescriptor(window, 'chrome')`\n" +
            "        Object.defineProperty(window, 'chrome', {\n" +
            "          writable: true,\n" +
            "          enumerable: true,\n" +
            "          configurable: false, // note!\n" +
            "          value: {} // We'll extend that later\n" +
            "        })\n" +
            "      }\n" +
            "\n" +
            "      // That means we're running headful and don't need to mock anything\n" +
            "      if ('app' in window.chrome) {\n" +
            "        return // Nothing to do here\n" +
            "      }\n" +
            "\n" +
            "      const makeError = {\n" +
            "        ErrorInInvocation: fn => {\n" +
            "          const err = new TypeError(`Error in invocation of app.${fn}()`)\n" +
            "          return utils.stripErrorWithAnchor(\n" +
            "            err,\n" +
            "            `at ${fn} (eval at <anonymous>`\n" +
            "          )\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      // There's a some static data in that property which doesn't seem to change,\n" +
            "      // we should periodically check for updates: `JSON.stringify(window.app, null, 2)`\n" +
            "      var STATIC_DATA = JSON.parse(\n" +
            "        `\n" +
            "{\n" +
            "  \"isInstalled\": false,\n" +
            "  \"InstallState\": {\n" +
            "    \"DISABLED\": \"disabled\",\n" +
            "    \"INSTALLED\": \"installed\",\n" +
            "    \"NOT_INSTALLED\": \"not_installed\"\n" +
            "  },\n" +
            "  \"RunningState\": {\n" +
            "    \"CANNOT_RUN\": \"cannot_run\",\n" +
            "    \"READY_TO_RUN\": \"ready_to_run\",\n" +
            "    \"RUNNING\": \"running\"\n" +
            "  }\n" +
            "}\n" +
            "        `.trim()\n" +
            "      )\n" +
            "\n" +
            "      window.chrome.app = {\n" +
            "        ...STATIC_DATA,\n" +
            "\n" +
            "        get isInstalled() {\n" +
            "          return false\n" +
            "        },\n" +
            "\n" +
            "        getDetails: function getDetails() {\n" +
            "          if (arguments.length) {\n" +
            "            throw makeError.ErrorInInvocation(`getDetails`)\n" +
            "          }\n" +
            "          return null\n" +
            "        },\n" +
            "        getIsInstalled: function getDetails() {\n" +
            "          if (arguments.length) {\n" +
            "            throw makeError.ErrorInInvocation(`getIsInstalled`)\n" +
            "          }\n" +
            "          return false\n" +
            "        },\n" +
            "        runningState: function getDetails() {\n" +
            "          if (arguments.length) {\n" +
            "            throw makeError.ErrorInInvocation(`runningState`)\n" +
            "          }\n" +
            "          return 'cannot_run'\n" +
            "        }\n" +
            "      }\n" +
            "      utils.patchToStringNested(window.chrome.app)\n";
    public String chrome_csi = "      if (!window.chrome) {\n" +
            "        // Use the exact property descriptor found in headful Chrome\n" +
            "        // fetch it via `Object.getOwnPropertyDescriptor(window, 'chrome')`\n" +
            "        Object.defineProperty(window, 'chrome', {\n" +
            "          writable: true,\n" +
            "          enumerable: true,\n" +
            "          configurable: false, // note!\n" +
            "          value: {} // We'll extend that later\n" +
            "        })\n" +
            "      }\n" +
            "\n" +
            "      // That means we're running headful and don't need to mock anything\n" +
            "      if ('csi' in window.chrome) {\n" +
            "        return // Nothing to do here\n" +
            "      }\n" +
            "\n" +
            "      // Check that the Navigation Timing API v1 is available, we need that\n" +
            "      if (!window.performance || !window.performance.timing) {\n" +
            "        return\n" +
            "      }\n" +
            "\n" +
            "      var { timing } = window.performance\n" +
            "\n" +
            "      window.chrome.csi = function() {\n" +
            "        return {\n" +
            "          onloadT: timing.domContentLoadedEventEnd,\n" +
            "          startE: timing.navigationStart,\n" +
            "          pageT: Date.now() - timing.navigationStart,\n" +
            "          tran: 15 // Transition type or something\n" +
            "        }\n" +
            "      }\n" +
            "      utils.patchToString(window.chrome.csi)\n";
    public String chrome_loadtimes = "        if (!window.chrome) {\n" +
            "          // Use the exact property descriptor found in headful Chrome\n" +
            "          // fetch it via `Object.getOwnPropertyDescriptor(window, 'chrome')`\n" +
            "          Object.defineProperty(window, 'chrome', {\n" +
            "            writable: true,\n" +
            "            enumerable: true,\n" +
            "            configurable: false, // note!\n" +
            "            value: {} // We'll extend that later\n" +
            "          })\n" +
            "        }\n" +
            "\n" +
            "        // That means we're running headful and don't need to mock anything\n" +
            "        if ('loadTimes' in window.chrome) {\n" +
            "          return // Nothing to do here\n" +
            "        }\n" +
            "\n" +
            "        // Check that the Navigation Timing API v1 + v2 is available, we need that\n" +
            "        if (\n" +
            "          !window.performance ||\n" +
            "          !window.performance.timing ||\n" +
            "          !window.PerformancePaintTiming\n" +
            "        ) {\n" +
            "          return\n" +
            "        }\n" +
            "\n" +
            "        const { performance } = window\n" +
            "\n" +
            "        // Some stuff is not available on about:blank as it requires a navigation to occur,\n" +
            "        // let's harden the code to not fail then:\n" +
            "        const ntEntryFallback = {\n" +
            "          nextHopProtocol: 'h2',\n" +
            "          type: 'other'\n" +
            "        }\n" +
            "\n" +
            "        // The API exposes some funky info regarding the connection\n" +
            "        const protocolInfo = {\n" +
            "          get connectionInfo() {\n" +
            "            const ntEntry =\n" +
            "              performance.getEntriesByType('navigation')[0] || ntEntryFallback\n" +
            "            return ntEntry.nextHopProtocol\n" +
            "          },\n" +
            "          get npnNegotiatedProtocol() {\n" +
            "            // NPN is deprecated in favor of ALPN, but this implementation returns the\n" +
            "            // HTTP/2 or HTTP2+QUIC/39 requests negotiated via ALPN.\n" +
            "            const ntEntry =\n" +
            "              performance.getEntriesByType('navigation')[0] || ntEntryFallback\n" +
            "            return ['h2', 'hq'].includes(ntEntry.nextHopProtocol)\n" +
            "              ? ntEntry.nextHopProtocol\n" +
            "              : 'unknown'\n" +
            "          },\n" +
            "          get navigationType() {\n" +
            "            const ntEntry =\n" +
            "              performance.getEntriesByType('navigation')[0] || ntEntryFallback\n" +
            "            return ntEntry.type\n" +
            "          },\n" +
            "          get wasAlternateProtocolAvailable() {\n" +
            "            // The Alternate-Protocol header is deprecated in favor of Alt-Svc\n" +
            "            // (https://www.mnot.net/blog/2016/03/09/alt-svc), so technically this\n" +
            "            // should always return false.\n" +
            "            return false\n" +
            "          },\n" +
            "          get wasFetchedViaSpdy() {\n" +
            "            // SPDY is deprecated in favor of HTTP/2, but this implementation returns\n" +
            "            // true for HTTP/2 or HTTP2+QUIC/39 as well.\n" +
            "            const ntEntry =\n" +
            "              performance.getEntriesByType('navigation')[0] || ntEntryFallback\n" +
            "            return ['h2', 'hq'].includes(ntEntry.nextHopProtocol)\n" +
            "          },\n" +
            "          get wasNpnNegotiated() {\n" +
            "            // NPN is deprecated in favor of ALPN, but this implementation returns true\n" +
            "            // for HTTP/2 or HTTP2+QUIC/39 requests negotiated via ALPN.\n" +
            "            const ntEntry =\n" +
            "              performance.getEntriesByType('navigation')[0] || ntEntryFallback\n" +
            "            return ['h2', 'hq'].includes(ntEntry.nextHopProtocol)\n" +
            "          }\n" +
            "        }\n" +
            "\n" +
            "        var { timing } = window.performance\n" +
            "\n" +
            "        // Truncate number to specific number of decimals, most of the `loadTimes` stuff has 3\n" +
            "        function toFixed(num, fixed) {\n" +
            "          var re = new RegExp('^-?\\\\d+(?:.\\\\d{0,' + (fixed || -1) + '})?')\n" +
            "          return num.toString().match(re)[0]\n" +
            "        }\n" +
            "\n" +
            "        const timingInfo = {\n" +
            "          get firstPaintAfterLoadTime() {\n" +
            "            // This was never actually implemented and always returns 0.\n" +
            "            return 0\n" +
            "          },\n" +
            "          get requestTime() {\n" +
            "            return timing.navigationStart / 1000\n" +
            "          },\n" +
            "          get startLoadTime() {\n" +
            "            return timing.navigationStart / 1000\n" +
            "          },\n" +
            "          get commitLoadTime() {\n" +
            "            return timing.responseStart / 1000\n" +
            "          },\n" +
            "          get finishDocumentLoadTime() {\n" +
            "            return timing.domContentLoadedEventEnd / 1000\n" +
            "          },\n" +
            "          get finishLoadTime() {\n" +
            "            return timing.loadEventEnd / 1000\n" +
            "          },\n" +
            "          get firstPaintTime() {\n" +
            "            const fpEntry = performance.getEntriesByType('paint')[0] || {\n" +
            "              startTime: timing.loadEventEnd / 1000 // Fallback if no navigation occured (`about:blank`)\n" +
            "            }\n" +
            "            return toFixed(\n" +
            "              (fpEntry.startTime + performance.timeOrigin) / 1000,\n" +
            "              3\n" +
            "            )\n" +
            "          }\n" +
            "        }\n" +
            "\n" +
            "        window.chrome.loadTimes = function() {\n" +
            "          return {\n" +
            "            ...protocolInfo,\n" +
            "            ...timingInfo\n" +
            "          }\n" +
            "        }\n" +
            "        utils.patchToString(window.chrome.loadTimes)\n";
    public String chrome_runtime = "" +
            "var STATIC_DATA = {\n" +
            "  \"OnInstalledReason\": {\n" +
            "    \"CHROME_UPDATE\": \"chrome_update\",\n" +
            "    \"INSTALL\": \"install\",\n" +
            "    \"SHARED_MODULE_UPDATE\": \"shared_module_update\",\n" +
            "    \"UPDATE\": \"update\"\n" +
            "  },\n" +
            "  \"OnRestartRequiredReason\": {\n" +
            "    \"APP_UPDATE\": \"app_update\",\n" +
            "    \"OS_UPDATE\": \"os_update\",\n" +
            "    \"PERIODIC\": \"periodic\"\n" +
            "  },\n" +
            "  \"PlatformArch\": {\n" +
            "    \"ARM\": \"arm\",\n" +
            "    \"ARM64\": \"arm64\",\n" +
            "    \"MIPS\": \"mips\",\n" +
            "    \"MIPS64\": \"mips64\",\n" +
            "    \"X86_32\": \"x86-32\",\n" +
            "    \"X86_64\": \"x86-64\"\n" +
            "  },\n" +
            "  \"PlatformNaclArch\": {\n" +
            "    \"ARM\": \"arm\",\n" +
            "    \"MIPS\": \"mips\",\n" +
            "    \"MIPS64\": \"mips64\",\n" +
            "    \"X86_32\": \"x86-32\",\n" +
            "    \"X86_64\": \"x86-64\"\n" +
            "  },\n" +
            "  \"PlatformOs\": {\n" +
            "    \"ANDROID\": \"android\",\n" +
            "    \"CROS\": \"cros\",\n" +
            "    \"LINUX\": \"linux\",\n" +
            "    \"MAC\": \"mac\",\n" +
            "    \"OPENBSD\": \"openbsd\",\n" +
            "    \"WIN\": \"win\"\n" +
            "  },\n" +
            "  \"RequestUpdateCheckStatus\": {\n" +
            "    \"NO_UPDATE\": \"no_update\",\n" +
            "    \"THROTTLED\": \"throttled\",\n" +
            "    \"UPDATE_AVAILABLE\": \"update_available\"\n" +
            "  }\n" +
            "}\n" +
            "        if (!window.chrome) {\n" +
            "          // Use the exact property descriptor found in headful Chrome\n" +
            "          // fetch it via `Object.getOwnPropertyDescriptor(window, 'chrome')`\n" +
            "          Object.defineProperty(window, 'chrome', {\n" +
            "            writable: true,\n" +
            "            enumerable: true,\n" +
            "            configurable: false, // note!\n" +
            "            value: {} // We'll extend that later\n" +
            "          })\n" +
            "        }\n" +
            "\n" +
            "        // That means we're running headful and don't need to mock anything\n" +
            "        const existsAlready = 'runtime' in window.chrome\n" +
            "        // `chrome.runtime` is only exposed on secure origins\n" +
            "        const isNotSecure = !window.location.protocol.startsWith('https')\n" +
            "        if (existsAlready || (isNotSecure && !opts.runOnInsecureOrigins)) {\n" +
            "          return // Nothing to do here\n" +
            "        }\n" +
            "\n" +
            "        window.chrome.runtime = {\n" +
            "          // There's a bunch of static data in that property which doesn't seem to change,\n" +
            "          // we should periodically check for updates: `JSON.stringify(window.chrome.runtime, null, 2)`\n" +
            "          ...STATIC_DATA,\n" +
            "          // `chrome.runtime.id` is extension related and returns undefined in Chrome\n" +
            "          get id() {\n" +
            "            return undefined\n" +
            "          },\n" +
            "          // These two require more sophisticated mocks\n" +
            "          connect: null,\n" +
            "          sendMessage: null\n" +
            "        }\n" +
            "\n" +
            "        const makeCustomRuntimeErrors = (preamble, method, extensionId) => ({\n" +
            "          NoMatchingSignature: new TypeError(\n" +
            "            preamble + `No matching signature.`\n" +
            "          ),\n" +
            "          MustSpecifyExtensionID: new TypeError(\n" +
            "            preamble +\n" +
            "              `${method} called from a webpage must specify an Extension ID (string) for its first argument.`\n" +
            "          ),\n" +
            "          InvalidExtensionID: new TypeError(\n" +
            "            preamble + `Invalid extension id: '${extensionId}'`\n" +
            "          )\n" +
            "        })\n" +
            "\n" +
            "        // Valid Extension IDs are 32 characters in length and use the letter `a` to `p`:\n" +
            "        // https://source.chromium.org/chromium/chromium/src/+/master:components/crx_file/id_util.cc;drc=14a055ccb17e8c8d5d437fe080faba4c6f07beac;l=90\n" +
            "        const isValidExtensionID = str =>\n" +
            "          str.length === 32 && str.toLowerCase().match(/^[a-p]+$/)\n" +
            "\n" +
            "        /** Mock `chrome.runtime.sendMessage` */\n" +
            "        const sendMessageHandler = {\n" +
            "          apply: function(target, ctx, args) {\n" +
            "            const [extensionId, options, responseCallback] = args || []\n" +
            "\n" +
            "            // Define custom errors\n" +
            "            const errorPreamble = `Error in invocation of runtime.sendMessage(optional string extensionId, any message, optional object options, optional function responseCallback): `\n" +
            "            const Errors = makeCustomRuntimeErrors(\n" +
            "              errorPreamble,\n" +
            "              `chrome.runtime.sendMessage()`,\n" +
            "              extensionId\n" +
            "            )\n" +
            "\n" +
            "            // Check if the call signature looks ok\n" +
            "            const noArguments = args.length === 0\n" +
            "            const tooManyArguments = args.length > 4\n" +
            "            const incorrectOptions = options && typeof options !== 'object'\n" +
            "            const incorrectResponseCallback =\n" +
            "              responseCallback && typeof responseCallback !== 'function'\n" +
            "            if (\n" +
            "              noArguments ||\n" +
            "              tooManyArguments ||\n" +
            "              incorrectOptions ||\n" +
            "              incorrectResponseCallback\n" +
            "            ) {\n" +
            "              throw Errors.NoMatchingSignature\n" +
            "            }\n" +
            "\n" +
            "            // At least 2 arguments are required before we even validate the extension ID\n" +
            "            if (args.length < 2) {\n" +
            "              throw Errors.MustSpecifyExtensionID\n" +
            "            }\n" +
            "\n" +
            "            // Now let's make sure we got a string as extension ID\n" +
            "            if (typeof extensionId !== 'string') {\n" +
            "              throw Errors.NoMatchingSignature\n" +
            "            }\n" +
            "\n" +
            "            if (!isValidExtensionID(extensionId)) {\n" +
            "              throw Errors.InvalidExtensionID\n" +
            "            }\n" +
            "\n" +
            "            return undefined // Normal behavior\n" +
            "          }\n" +
            "        }\n" +
            "        utils.mockWithProxy(\n" +
            "          window.chrome.runtime,\n" +
            "          'sendMessage',\n" +
            "          function sendMessage() {},\n" +
            "          sendMessageHandler\n" +
            "        )\n" +
            "\n" +
            "        /**\n" +
            "         * Mock `chrome.runtime.connect`\n" +
            "         *\n" +
            "         * @see https://developer.chrome.com/apps/runtime#method-connect\n" +
            "         */\n" +
            "        const connectHandler = {\n" +
            "          apply: function(target, ctx, args) {\n" +
            "            const [extensionId, connectInfo] = args || []\n" +
            "\n" +
            "            // Define custom errors\n" +
            "            const errorPreamble = `Error in invocation of runtime.connect(optional string extensionId, optional object connectInfo): `\n" +
            "            const Errors = makeCustomRuntimeErrors(\n" +
            "              errorPreamble,\n" +
            "              `chrome.runtime.connect()`,\n" +
            "              extensionId\n" +
            "            )\n" +
            "\n" +
            "            // Behavior differs a bit from sendMessage:\n" +
            "            const noArguments = args.length === 0\n" +
            "            const emptyStringArgument = args.length === 1 && extensionId === ''\n" +
            "            if (noArguments || emptyStringArgument) {\n" +
            "              throw Errors.MustSpecifyExtensionID\n" +
            "            }\n" +
            "\n" +
            "            const tooManyArguments = args.length > 2\n" +
            "            const incorrectConnectInfoType =\n" +
            "              connectInfo && typeof connectInfo !== 'object'\n" +
            "\n" +
            "            if (tooManyArguments || incorrectConnectInfoType) {\n" +
            "              throw Errors.NoMatchingSignature\n" +
            "            }\n" +
            "\n" +
            "            const extensionIdIsString = typeof extensionId === 'string'\n" +
            "            if (extensionIdIsString && extensionId === '') {\n" +
            "              throw Errors.MustSpecifyExtensionID\n" +
            "            }\n" +
            "            if (extensionIdIsString && !isValidExtensionID(extensionId)) {\n" +
            "              throw Errors.InvalidExtensionID\n" +
            "            }\n" +
            "\n" +
            "            // There's another edge-case here: extensionId is optional so we might find a connectInfo object as first param, which we need to validate\n" +
            "            const validateConnectInfo = ci => {\n" +
            "              // More than a first param connectInfo as been provided\n" +
            "              if (args.length > 1) {\n" +
            "                throw Errors.NoMatchingSignature\n" +
            "              }\n" +
            "              // An empty connectInfo has been provided\n" +
            "              if (Object.keys(ci).length === 0) {\n" +
            "                throw Errors.MustSpecifyExtensionID\n" +
            "              }\n" +
            "              // Loop over all connectInfo props an check them\n" +
            "              Object.entries(ci).forEach(([k, v]) => {\n" +
            "                const isExpected = ['name', 'includeTlsChannelId'].includes(k)\n" +
            "                if (!isExpected) {\n" +
            "                  throw new TypeError(\n" +
            "                    errorPreamble + `Unexpected property: '${k}'.`\n" +
            "                  )\n" +
            "                }\n" +
            "                const MismatchError = (propName, expected, found) =>\n" +
            "                  TypeError(\n" +
            "                    errorPreamble +\n" +
            "                      `Error at property '${propName}': Invalid type: expected ${expected}, found ${found}.`\n" +
            "                  )\n" +
            "                if (k === 'name' && typeof v !== 'string') {\n" +
            "                  throw MismatchError(k, 'string', typeof v)\n" +
            "                }\n" +
            "                if (k === 'includeTlsChannelId' && typeof v !== 'boolean') {\n" +
            "                  throw MismatchError(k, 'boolean', typeof v)\n" +
            "                }\n" +
            "              })\n" +
            "            }\n" +
            "            if (typeof extensionId === 'object') {\n" +
            "              validateConnectInfo(extensionId)\n" +
            "              throw Errors.MustSpecifyExtensionID\n" +
            "            }\n" +
            "\n" +
            "            // Unfortunately even when the connect fails Chrome will return an object with methods we need to mock as well\n" +
            "            return utils.patchToStringNested(makeConnectResponse())\n" +
            "          }\n" +
            "        }\n" +
            "        utils.mockWithProxy(\n" +
            "          window.chrome.runtime,\n" +
            "          'connect',\n" +
            "          function connect() {},\n" +
            "          connectHandler\n" +
            "        )\n" +
            "\n" +
            "        function makeConnectResponse() {\n" +
            "          const onSomething = () => ({\n" +
            "            addListener: function addListener() {},\n" +
            "            dispatch: function dispatch() {},\n" +
            "            hasListener: function hasListener() {},\n" +
            "            hasListeners: function hasListeners() {\n" +
            "              return false\n" +
            "            },\n" +
            "            removeListener: function removeListener() {}\n" +
            "          })\n" +
            "\n" +
            "          const response = {\n" +
            "            name: '',\n" +
            "            sender: undefined,\n" +
            "            disconnect: function disconnect() {},\n" +
            "            onDisconnect: onSomething(),\n" +
            "            onMessage: onSomething(),\n" +
            "            postMessage: function postMessage() {\n" +
            "              if (!arguments.length) {\n" +
            "                throw new TypeError(`Insufficient number of arguments.`)\n" +
            "              }\n" +
            "              throw new Error(`Attempting to use a disconnected port object`)\n" +
            "            }\n" +
            "          }\n" +
            "          return response\n" +
            "        }\n";
    public String iframe_contentWindow = "     try {\n" +
            "        // Adds a contentWindow proxy to the provided iframe element\n" +
            "        const addContentWindowProxy = iframe => {\n" +
            "          const contentWindowProxy = {\n" +
            "            get(target, key) {\n" +
            "              // Now to the interesting part:\n" +
            "              // We actually make this thing behave like a regular iframe window,\n" +
            "              // by intercepting calls to e.g. `.self` and redirect it to the correct thing. :)\n" +
            "              // That makes it possible for these assertions to be correct:\n" +
            "              // iframe.contentWindow.self === window.top // must be false\n" +
            "              if (key === 'self') {\n" +
            "                return this\n" +
            "              }\n" +
            "              // iframe.contentWindow.frameElement === iframe // must be true\n" +
            "              if (key === 'frameElement') {\n" +
            "                return iframe\n" +
            "              }\n" +
            "              // Intercept iframe.contentWindow[0] to hide the property 0 added by the proxy.\n" +
            "              if (key === '0') {\n" +
            "                  return undefined\n" +
            "              }\n" +
            "              return Reflect.get(target, key)\n" +
            "            }\n" +
            "          }\n" +
            "\n" +
            "          if (!iframe.contentWindow) {\n" +
            "            const proxy = new Proxy(window, contentWindowProxy)\n" +
            "            Object.defineProperty(iframe, 'contentWindow', {\n" +
            "              get() {\n" +
            "                return proxy\n" +
            "              },\n" +
            "              set(newValue) {\n" +
            "                return newValue // contentWindow is immutable\n" +
            "              },\n" +
            "              enumerable: true,\n" +
            "              configurable: false\n" +
            "            })\n" +
            "          }\n" +
            "        }\n" +
            "\n" +
            "        // Handles iframe element creation, augments `srcdoc` property so we can intercept further\n" +
            "        const handleIframeCreation = (target, thisArg, args) => {\n" +
            "          const iframe = target.apply(thisArg, args)\n" +
            "\n" +
            "          // We need to keep the originals around\n" +
            "          const _iframe = iframe\n" +
            "          const _srcdoc = _iframe.srcdoc\n" +
            "\n" +
            "          // Add hook for the srcdoc property\n" +
            "          // We need to be very surgical here to not break other iframes by accident\n" +
            "          Object.defineProperty(iframe, 'srcdoc', {\n" +
            "            configurable: true, // Important, so we can reset this later\n" +
            "            get: function() {\n" +
            "              return _srcdoc\n" +
            "            },\n" +
            "            set: function(newValue) {\n" +
            "              addContentWindowProxy(this)\n" +
            "              // Reset property, the hook is only needed once\n" +
            "              Object.defineProperty(iframe, 'srcdoc', {\n" +
            "                configurable: false,\n" +
            "                writable: false,\n" +
            "                value: _srcdoc\n" +
            "              })\n" +
            "              _iframe.srcdoc = newValue\n" +
            "            }\n" +
            "          })\n" +
            "          return iframe\n" +
            "        }\n" +
            "\n" +
            "        // Adds a hook to intercept iframe creation events\n" +
            "        const addIframeCreationSniffer = () => {\n" +
            "          /* global document */\n" +
            "          const createElementHandler = {\n" +
            "            // Make toString() native\n" +
            "            get(target, key) {\n" +
            "              return Reflect.get(target, key)\n" +
            "            },\n" +
            "            apply: function(target, thisArg, args) {\n" +
            "              const isIframe =\n" +
            "                args && args.length && `${args[0]}`.toLowerCase() === 'iframe'\n" +
            "              if (!isIframe) {\n" +
            "                // Everything as usual\n" +
            "                return target.apply(thisArg, args)\n" +
            "              } else {\n" +
            "                return handleIframeCreation(target, thisArg, args)\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "          // All this just due to iframes with srcdoc bug\n" +
            "          utils.replaceWithProxy(\n" +
            "            document,\n" +
            "            'createElement',\n" +
            "            createElementHandler\n" +
            "          )\n" +
            "        }\n" +
            "\n" +
            "        // Let's go\n" +
            "        addIframeCreationSniffer()\n" +
            "      } catch (err) {\n" +
            "        // console.warn(err)\n" +
            "      }\n";
    public String media_codecs = "      /**\n" +
            "       * Input might look funky, we need to normalize it so e.g. whitespace isn't an issue for our spoofing.\n" +
            "       *\n" +
            "       * @example\n" +
            "       * video/webm; codecs=\"vp8, vorbis\"\n" +
            "       * video/mp4; codecs=\"avc1.42E01E\"\n" +
            "       * audio/x-m4a;\n" +
            "       * audio/ogg; codecs=\"vorbis\"\n" +
            "       * @param {String} arg\n" +
            "       */\n" +
            "      const parseInput = arg => {\n" +
            "        const [mime, codecStr] = arg.trim().split(';')\n" +
            "        let codecs = []\n" +
            "        if (codecStr && codecStr.includes('codecs=\"')) {\n" +
            "          codecs = codecStr\n" +
            "            .trim()\n" +
            "            .replace(`codecs=\"`, '')\n" +
            "            .replace(`\"`, '')\n" +
            "            .trim()\n" +
            "            .split(',')\n" +
            "            .filter(x => !!x)\n" +
            "            .map(x => x.trim())\n" +
            "        }\n" +
            "        return {\n" +
            "          mime,\n" +
            "          codecStr,\n" +
            "          codecs\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      const canPlayType = {\n" +
            "        // Intercept certain requests\n" +
            "        apply: function(target, ctx, args) {\n" +
            "          if (!args || !args.length) {\n" +
            "            return target.apply(ctx, args)\n" +
            "          }\n" +
            "          const { mime, codecs } = parseInput(args[0])\n" +
            "          // This specific mp4 codec is missing in Chromium\n" +
            "          if (mime === 'video/mp4') {\n" +
            "            if (codecs.includes('avc1.42E01E')) {\n" +
            "              return 'probably'\n" +
            "            }\n" +
            "          }\n" +
            "          // This mimetype is only supported if no codecs are specified\n" +
            "          if (mime === 'audio/x-m4a' && !codecs.length) {\n" +
            "            return 'maybe'\n" +
            "          }\n" +
            "\n" +
            "          // This mimetype is only supported if no codecs are specified\n" +
            "          if (mime === 'audio/aac' && !codecs.length) {\n" +
            "            return 'probably'\n" +
            "          }\n" +
            "          // Everything else as usual\n" +
            "          return target.apply(ctx, args)\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      /* global HTMLMediaElement */\n" +
            "      utils.replaceWithProxy(\n" +
            "        HTMLMediaElement.prototype,\n" +
            "        'canPlayType',\n" +
            "        canPlayType\n" +
            "      )\n";
    public String navigator_hardwareConcurrency = "utils.replaceGetterWithProxy(\n" +
            "          Object.getPrototypeOf(navigator),\n" +
            "          'hardwareConcurrency',\n" +
            "          utils.makeHandler().getterValue(4)\n" + // [opts.hardwareConcurrency] - The value to use in `navigator.hardwareConcurrency` (default: `4`)
            "        )\n";
    public String navigator_languages = "" +
            "const opts = {languages:[]}\n" + // Empty default, otherwise this would be merged with user defined array override
            "const languages = opts.languages.length\n" +
            "          ? opts.languages\n" +
            "          : ['en-US', 'en']\n" +
            "        utils.replaceGetterWithProxy(\n" +
            "          Object.getPrototypeOf(navigator),\n" +
            "          'languages',\n" +
            "          utils.makeHandler().getterValue(Object.freeze([...languages]))\n" +
            "        )\n";
    public String navigator_permissions = "      const isSecure = document.location.protocol.startsWith('https')\n" +
            "\n" +
            "      // In headful on secure origins the permission should be \"default\", not \"denied\"\n" +
            "      if (isSecure) {\n" +
            "        utils.replaceGetterWithProxy(Notification, 'permission', {\n" +
            "          apply() {\n" +
            "            return 'default'\n" +
            "          }\n" +
            "        })\n" +
            "      }\n" +
            "\n" +
            "      // Another weird behavior:\n" +
            "      // On insecure origins in headful the state is \"denied\",\n" +
            "      // whereas in headless it's \"prompt\"\n" +
            "      if (!isSecure) {\n" +
            "        const handler = {\n" +
            "          apply(target, ctx, args) {\n" +
            "            const param = (args || [])[0]\n" +
            "\n" +
            "            const isNotifications =\n" +
            "              param && param.name && param.name === 'notifications'\n" +
            "            if (!isNotifications) {\n" +
            "              return utils.cache.Reflect.apply(...arguments)\n" +
            "            }\n" +
            "\n" +
            "            return Promise.resolve(\n" +
            "              Object.setPrototypeOf(\n" +
            "                {\n" +
            "                  state: 'denied',\n" +
            "                  onchange: null\n" +
            "                },\n" +
            "                PermissionStatus.prototype\n" +
            "              )\n" +
            "            )\n" +
            "          }\n" +
            "        }\n" +
            "        // Note: Don't use `Object.getPrototypeOf` here\n" +
            "        utils.replaceWithProxy(Permissions.prototype, 'query', handler)\n" +
            "      }\n";
    public String navigator_plugins = "" +
            "const { generateMimeTypeArray } = module.exports.generateMimeTypeArray = (utils, fns) => mimeTypesData => {\n" +
            "  return fns.generateMagicArray(utils, fns)(\n" +
            "    mimeTypesData,\n" +
            "    MimeTypeArray.prototype,\n" +
            "    MimeType.prototype,\n" +
            "    'type'\n" +
            "  )\n" +
            "}\n" +
            "const { generatePluginArray } = module.exports.generatePluginArray = (utils, fns) => pluginsData => {\n" +
            "  return fns.generateMagicArray(utils, fns)(\n" +
            "    pluginsData,\n" +
            "    PluginArray.prototype,\n" +
            "    Plugin.prototype,\n" +
            "    'name'\n" +
            "  )\n" +
            "}\n" +
            "const { generateMagicArray } = module.exports.generateMagicArray = (utils, fns) =>\n" +
            "  function(\n" +
            "    dataArray = [],\n" +
            "    proto = MimeTypeArray.prototype,\n" +
            "    itemProto = MimeType.prototype,\n" +
            "    itemMainProp = 'type'\n" +
            "  ) {\n" +
            "    // Quick helper to set props with the same descriptors vanilla is using\n" +
            "    const defineProp = (obj, prop, value) =>\n" +
            "      Object.defineProperty(obj, prop, {\n" +
            "        value,\n" +
            "        writable: false,\n" +
            "        enumerable: false, // Important for mimeTypes & plugins: `JSON.stringify(navigator.mimeTypes)`\n" +
            "        configurable: true\n" +
            "      })\n" +
            "\n" +
            "    // Loop over our fake data and construct items\n" +
            "    const makeItem = data => {\n" +
            "      const item = {}\n" +
            "      for (const prop of Object.keys(data)) {\n" +
            "        if (prop.startsWith('__')) {\n" +
            "          continue\n" +
            "        }\n" +
            "        defineProp(item, prop, data[prop])\n" +
            "      }\n" +
            "      return patchItem(item, data)\n" +
            "    }\n" +
            "\n" +
            "    const patchItem = (item, data) => {\n" +
            "      let descriptor = Object.getOwnPropertyDescriptors(item)\n" +
            "\n" +
            "      // Special case: Plugins have a magic length property which is not enumerable\n" +
            "      // e.g. `navigator.plugins[i].length` should always be the length of the assigned mimeTypes\n" +
            "      if (itemProto === Plugin.prototype) {\n" +
            "        descriptor = {\n" +
            "          ...descriptor,\n" +
            "          length: {\n" +
            "            value: data.__mimeTypes.length,\n" +
            "            writable: false,\n" +
            "            enumerable: false,\n" +
            "            configurable: true // Important to be able to use the ownKeys trap in a Proxy to strip `length`\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      // We need to spoof a specific `MimeType` or `Plugin` object\n" +
            "      const obj = Object.create(itemProto, descriptor)\n" +
            "\n" +
            "      // Virtually all property keys are not enumerable in vanilla\n" +
            "      const blacklist = [...Object.keys(data), 'length', 'enabledPlugin']\n" +
            "      return new Proxy(obj, {\n" +
            "        ownKeys(target) {\n" +
            "          return Reflect.ownKeys(target).filter(k => !blacklist.includes(k))\n" +
            "        },\n" +
            "        getOwnPropertyDescriptor(target, prop) {\n" +
            "          if (blacklist.includes(prop)) {\n" +
            "            return undefined\n" +
            "          }\n" +
            "          return Reflect.getOwnPropertyDescriptor(target, prop)\n" +
            "        }\n" +
            "      })\n" +
            "    }\n" +
            "\n" +
            "    const magicArray = []\n" +
            "\n" +
            "    // Loop through our fake data and use that to create convincing entities\n" +
            "    dataArray.forEach(data => {\n" +
            "      magicArray.push(makeItem(data))\n" +
            "    })\n" +
            "\n" +
            "    // Add direct property access  based on types (e.g. `obj['application/pdf']`) afterwards\n" +
            "    magicArray.forEach(entry => {\n" +
            "      defineProp(magicArray, entry[itemMainProp], entry)\n" +
            "    })\n" +
            "\n" +
            "    // This is the best way to fake the type to make sure this is false: `Array.isArray(navigator.mimeTypes)`\n" +
            "    const magicArrayObj = Object.create(proto, {\n" +
            "      ...Object.getOwnPropertyDescriptors(magicArray),\n" +
            "\n" +
            "      // There's one ugly quirk we unfortunately need to take care of:\n" +
            "      // The `MimeTypeArray` prototype has an enumerable `length` property,\n" +
            "      // but headful Chrome will still skip it when running `Object.getOwnPropertyNames(navigator.mimeTypes)`.\n" +
            "      // To strip it we need to make it first `configurable` and can then overlay a Proxy with an `ownKeys` trap.\n" +
            "      length: {\n" +
            "        value: magicArray.length,\n" +
            "        writable: false,\n" +
            "        enumerable: false,\n" +
            "        configurable: true // Important to be able to use the ownKeys trap in a Proxy to strip `length`\n" +
            "      }\n" +
            "    })\n" +
            "\n" +
            "    // Generate our functional function mocks :-)\n" +
            "    const functionMocks = fns.generateFunctionMocks(utils)(\n" +
            "      proto,\n" +
            "      itemMainProp,\n" +
            "      magicArray\n" +
            "    )\n" +
            "\n" +
            "    // We need to overlay our custom object with a JS Proxy\n" +
            "    const magicArrayObjProxy = new Proxy(magicArrayObj, {\n" +
            "      get(target, key = '') {\n" +
            "        // Redirect function calls to our custom proxied versions mocking the vanilla behavior\n" +
            "        if (key === 'item') {\n" +
            "          return functionMocks.item\n" +
            "        }\n" +
            "        if (key === 'namedItem') {\n" +
            "          return functionMocks.namedItem\n" +
            "        }\n" +
            "        if (proto === PluginArray.prototype && key === 'refresh') {\n" +
            "          return functionMocks.refresh\n" +
            "        }\n" +
            "        // Everything else can pass through as normal\n" +
            "        return utils.cache.Reflect.get(...arguments)\n" +
            "      },\n" +
            "      ownKeys(target) {\n" +
            "        // There are a couple of quirks where the original property demonstrates \"magical\" behavior that makes no sense\n" +
            "        // This can be witnessed when calling `Object.getOwnPropertyNames(navigator.mimeTypes)` and the absense of `length`\n" +
            "        // My guess is that it has to do with the recent change of not allowing data enumeration and this being implemented weirdly\n" +
            "        // For that reason we just completely fake the available property names based on our data to match what regular Chrome is doing\n" +
            "        // Specific issues when not patching this: `length` property is available, direct `types` props (e.g. `obj['application/pdf']`) are missing\n" +
            "        const keys = []\n" +
            "        const typeProps = magicArray.map(mt => mt[itemMainProp])\n" +
            "        typeProps.forEach((_, i) => keys.push(`${i}`))\n" +
            "        typeProps.forEach(propName => keys.push(propName))\n" +
            "        return keys\n" +
            "      },\n" +
            "      getOwnPropertyDescriptor(target, prop) {\n" +
            "        if (prop === 'length') {\n" +
            "          return undefined\n" +
            "        }\n" +
            "        return Reflect.getOwnPropertyDescriptor(target, prop)\n" +
            "      }\n" +
            "    })\n" +
            "\n" +
            "    return magicArrayObjProxy\n" +
            "  }\n" +
            "const { generateFunctionMocks } = module.exports.generateFunctionMocks = utils => (\n" +
            "  proto,\n" +
            "  itemMainProp,\n" +
            "  dataArray\n" +
            ") => ({\n" +
            "  /** Returns the MimeType object with the specified index. */\n" +
            "  item: utils.createProxy(proto.item, {\n" +
            "    apply(target, ctx, args) {\n" +
            "      if (!args.length) {\n" +
            "        throw new TypeError(\n" +
            "          `Failed to execute 'item' on '${\n" +
            "            proto[Symbol.toStringTag]\n" +
            "          }': 1 argument required, but only 0 present.`\n" +
            "        )\n" +
            "      }\n" +
            "      // Special behavior alert:\n" +
            "      // - Vanilla tries to cast strings to Numbers (only integers!) and use them as property index lookup\n" +
            "      // - If anything else than an integer (including as string) is provided it will return the first entry\n" +
            "      const isInteger = args[0] && Number.isInteger(Number(args[0])) // Cast potential string to number first, then check for integer\n" +
            "      // Note: Vanilla never returns `undefined`\n" +
            "      return (isInteger ? dataArray[Number(args[0])] : dataArray[0]) || null\n" +
            "    }\n" +
            "  }),\n" +
            "  /** Returns the MimeType object with the specified name. */\n" +
            "  namedItem: utils.createProxy(proto.namedItem, {\n" +
            "    apply(target, ctx, args) {\n" +
            "      if (!args.length) {\n" +
            "        throw new TypeError(\n" +
            "          `Failed to execute 'namedItem' on '${\n" +
            "            proto[Symbol.toStringTag]\n" +
            "          }': 1 argument required, but only 0 present.`\n" +
            "        )\n" +
            "      }\n" +
            "      return dataArray.find(mt => mt[itemMainProp] === args[0]) || null // Not `undefined`!\n" +
            "    }\n" +
            "  }),\n" +
            "  /** Does nothing and shall return nothing */\n" +
            "  refresh: proto.refresh\n" +
            "    ? utils.createProxy(proto.refresh, {\n" +
            "        apply(target, ctx, args) {\n" +
            "          return undefined\n" +
            "        }\n" +
            "      })\n" +
            "    : undefined\n" +
            "})\n" +
            "const data = {\n" +
            "  \"mimeTypes\": [\n" +
            "    {\n" +
            "      \"type\": \"application/pdf\",\n" +
            "      \"suffixes\": \"pdf\",\n" +
            "      \"description\": \"\",\n" +
            "      \"__pluginName\": \"Chrome PDF Viewer\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"application/x-google-chrome-pdf\",\n" +
            "      \"suffixes\": \"pdf\",\n" +
            "      \"description\": \"Portable Document Format\",\n" +
            "      \"__pluginName\": \"Chrome PDF Plugin\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"application/x-nacl\",\n" +
            "      \"suffixes\": \"\",\n" +
            "      \"description\": \"Native Client Executable\",\n" +
            "      \"__pluginName\": \"Native Client\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"application/x-pnacl\",\n" +
            "      \"suffixes\": \"\",\n" +
            "      \"description\": \"Portable Native Client Executable\",\n" +
            "      \"__pluginName\": \"Native Client\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"plugins\": [\n" +
            "    {\n" +
            "      \"name\": \"Chrome PDF Plugin\",\n" +
            "      \"filename\": \"internal-pdf-viewer\",\n" +
            "      \"description\": \"Portable Document Format\",\n" +
            "      \"__mimeTypes\": [\"application/x-google-chrome-pdf\"]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Chrome PDF Viewer\",\n" +
            "      \"filename\": \"mhjfbmdgcfjbbpaeojofohoefgiehjai\",\n" +
            "      \"description\": \"\",\n" +
            "      \"__mimeTypes\": [\"application/pdf\"]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Native Client\",\n" +
            "      \"filename\": \"internal-nacl-plugin\",\n" +
            "      \"description\": \"\",\n" +
            "      \"__mimeTypes\": [\"application/x-nacl\", \"application/x-pnacl\"]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "var fns = utils.stringifyFns({\n" +
            "          generateMimeTypeArray,\n" +
            "          generatePluginArray,\n" +
            "          generateMagicArray,\n" +
            "          generateFunctionMocks\n" +
            "        })\n" +
            "fns = utils.materializeFns(fns)\n" +
            "\n" +
            "        // That means we're running headful\n" +
            "        const hasPlugins = false;\n" +
            "        try{ hasPlugins = navigator.plugins.length > 0; }catch(e){}\n" +
            "        if (!hasPlugins) {\n" +
            "        const mimeTypes = fns.generateMimeTypeArray(utils, fns)(data.mimeTypes)\n" +
            "        const plugins = fns.generatePluginArray(utils, fns)(data.plugins)\n" +
            "\n" +
            "        // Plugin and MimeType cross-reference each other, let's do that now\n" +
            "        // Note: We're looping through `data.plugins` here, not the generated `plugins`\n" +
            "        for (const pluginData of data.plugins) {\n" +
            "          pluginData.__mimeTypes.forEach((type, index) => {\n" +
            "            plugins[pluginData.name][index] = mimeTypes[type]\n" +
            "\n" +
            "            Object.defineProperty(plugins[pluginData.name], type, {\n" +
            "              value: mimeTypes[type],\n" +
            "              writable: false,\n" +
            "              enumerable: false, // Not enumerable\n" +
            "              configurable: true\n" +
            "            })\n" +
            "            Object.defineProperty(mimeTypes[type], 'enabledPlugin', {\n" +
            "              value:\n" +
            "                type === 'application/x-pnacl'\n" +
            "                  ? mimeTypes['application/x-nacl'].enabledPlugin // these reference the same plugin, so we need to re-use the Proxy in order to avoid leaks\n" +
            "                  : new Proxy(plugins[pluginData.name], {}), // Prevent circular references\n" +
            "              writable: false,\n" +
            "              enumerable: false, // Important: `JSON.stringify(navigator.plugins)`\n" +
            "              configurable: true\n" +
            "            })\n" +
            "          })\n" +
            "        }\n" +
            "\n" +
            "        const patchNavigator = (name, value) =>\n" +
            "          utils.replaceProperty(Object.getPrototypeOf(navigator), name, {\n" +
            "            get() {\n" +
            "              return value\n" +
            "            }\n" +
            "          })\n" +
            "\n" +
            "        patchNavigator('mimeTypes', mimeTypes)\n" +
            "        patchNavigator('plugins', plugins)\n" +
            "// All done!\n" +
            "        }\n";
    public String navigator_vendor = "utils.replaceGetterWithProxy(\n" +
            "          Object.getPrototypeOf(navigator),\n" +
            "          'vendor',\n" +
            "          utils.makeHandler().getterValue('Google Inc.')\n" +
            "        )\n";
    public String navigator_webdriver = "if (navigator.webdriver === false) {\n" +
            "        // Post Chrome 89.0.4339.0 and already good\n" +
            "      } else if (navigator.webdriver === undefined) {\n" +
            "        // Pre Chrome 89.0.4339.0 and already good\n" +
            "      } else {\n" +
            "        // Pre Chrome 88.0.4291.0 and needs patching\n" +
            "        delete Object.getPrototypeOf(navigator).webdriver\n" +
            "      }\n";
    public String webgl_vendor = "const getParameterProxyHandler = {\n" +
            "        apply: function(target, ctx, args) {\n" +
            "          const param = (args || [])[0]\n" +
            "          const result = utils.cache.Reflect.apply(target, ctx, args)\n" +
            "          // UNMASKED_VENDOR_WEBGL\n" +
            "          if (param === 37445) {\n" +
            "            return opts.vendor || 'Intel Inc.' // default in headless: Google Inc.\n" +
            "          }\n" +
            "          // UNMASKED_RENDERER_WEBGL\n" +
            "          if (param === 37446) {\n" +
            "            return opts.renderer || 'Intel Iris OpenGL Engine' // default in headless: Google SwiftShader\n" +
            "          }\n" +
            "          return result\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      // There's more than one WebGL rendering context\n" +
            "      // https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext#Browser_compatibility\n" +
            "      // To find out the original values here: Object.getOwnPropertyDescriptors(WebGLRenderingContext.prototype.getParameter)\n" +
            "      const addProxy = (obj, propName) => {\n" +
            "        utils.replaceWithProxy(obj, propName, getParameterProxyHandler)\n" +
            "      }\n" +
            "      // For whatever weird reason loops don't play nice with Object.defineProperty, here's the next best thing:\n" +
            "      addProxy(WebGLRenderingContext.prototype, 'getParameter')\n" +
            "      addProxy(WebGL2RenderingContext.prototype, 'getParameter')\n";
    public String window_outer_dimensions = "try {\n" +
            "        if (window.outerWidth && window.outerHeight) {\n" +
            "          return // nothing to do here\n" +
            "        }\n" +
            "        const windowFrame = 85 // probably OS and WM dependent\n" +
            "        window.outerWidth = window.innerWidth\n" +
            "        window.outerHeight = window.innerHeight + windowFrame\n" +
            "      } catch (err) {}\n";

    public String getAll() {
        return utils + chrome_app + chrome_csi + chrome_loadtimes + chrome_runtime + iframe_contentWindow + media_codecs + navigator_hardwareConcurrency +
                navigator_languages + navigator_permissions + navigator_plugins + navigator_vendor + navigator_webdriver + webgl_vendor + window_outer_dimensions;
    }
}
