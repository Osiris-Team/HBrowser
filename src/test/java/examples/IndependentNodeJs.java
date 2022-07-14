package examples;

import com.osiris.headlessbrowser.js.contexts.NodeContext;
import org.junit.jupiter.api.Test;

public class IndependentNodeJs {

    @Test
    void test() throws Exception{


        // Installs Node.js into current working directory if needed
        try (NodeContext ctx = new NodeContext()){ // Use another constructor for customization

            // Easily install/update needed modules
            ctx.npmInstall("async"); // name of Node module

            // To be able to see the JavaScript code results.
            // Otherwise you can also init NodeContext with debugOutput=System.out to achieve this.
            ctx.onPrintLine(line -> System.out.println(line));
            ctx.executeJavaScript("console.log('hello world!');");

            // You can return JavaScript results too.
            // Note that you must have a result variable in the provided JS Code for this to work!
            String result = ctx.executeJSAndGetResult("var result = 'my JavaScript result!';");
        }

    }
}
