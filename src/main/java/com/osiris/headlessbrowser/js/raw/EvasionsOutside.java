package com.osiris.headlessbrowser.js.raw;

/**
 * Contains all evasions from puppeteer extra stealth, compatible
 * with playwright. <br>
 * All the code below must be run outside the pages' context, usually at initialisation of puppeteer. <br>
 */
public class EvasionsOutside {

    public String strip_sourceurl = "" +
            "var cdpSession = null;\n" +
            "// Add support for playwright:\n" +
            "var isPlaywright = false;\n" +
            "// Catch not defined exception and ignore it\n" +
            "try{ if(browserCtx!=null) isPlaywright = true; } catch(e){}\n" +
            "if (isPlaywright) cdpSession = browserCtx.newCDPSession(page);\n" +
            "else if (!page || !page._client || !page._client.send) { // Support for puppeteer:\n" +
            "      throw new Error('Warning, missing page._client to intercept CDP.')\n" +
            "    }\n" +
            "else {" +
            "    cdpSession = page._client;\n" +
            "}" +
            "\n" +
            "    // Intercept CDP commands and strip identifying and unnecessary sourceURL\n" +
            "    // https://github.com/puppeteer/puppeteer/blob/9b3005c105995cd267fdc7fb95b78aceab82cf0e/new-docs/puppeteer.cdpsession.md\n" +
            "    cdpSession.send = (function(originalMethod, context) {\n" +
            "      return async function() {\n" +
            "        const [method, paramArgs] = arguments || []\n" +
            "        const next = async () => {\n" +
            "          try {\n" +
            "            return await originalMethod.apply(context, [method, paramArgs])\n" +
            "          } catch(error) {\n" +
            "            // This seems to happen sometimes when redirects cause other outstanding requests to be cut short\n" +
            "            if (error instanceof Error && error.message.includes(`Protocol error (Network.getResponseBody): No resource with given identifier found`)) {\n" +
            "              console.error(`Caught and ignored an error about a missing network resource.`, { error })\n" +
            "            } else {\n" +
            "              throw error\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "\n" +
            "        if (!method || !paramArgs) {\n" +
            "          return next()\n" +
            "        }\n" +
            "\n" +
            "        // To find the methods/props in question check `_evaluateInternal` at:\n" +
            "        // https://github.com/puppeteer/puppeteer/blob/main/src/common/ExecutionContext.ts#L186\n" +
            "        const methodsToPatch = {\n" +
            "          'Runtime.evaluate': 'expression',\n" +
            "          'Runtime.callFunctionOn': 'functionDeclaration'\n" +
            "        }\n" +
            "        const SOURCE_URL_SUFFIX =\n" +
            "          '//# sourceURL=__puppeteer_evaluation_script__'\n" +
            "\n" +
            "        if (!methodsToPatch[method] || !paramArgs[methodsToPatch[method]]) {\n" +
            "          return next()\n" +
            "        }\n" +
            "\n" +
            "        console.log('Stripping sourceURL', { method })\n" +
            "        paramArgs[methodsToPatch[method]] = paramArgs[\n" +
            "          methodsToPatch[method]\n" +
            "        ].replace(SOURCE_URL_SUFFIX, '')\n" +
            "\n" +
            "        return next()\n" +
            "      }\n" +
            "    })(cdpSession.send, cdpSession)\n";

    public String getAll() {
        return strip_sourceurl;
    }


}
