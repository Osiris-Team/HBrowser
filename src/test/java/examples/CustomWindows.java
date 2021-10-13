package examples;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.windows.GraalWindow;
import com.osiris.headlessbrowser.windows.LightWindow;
import com.osiris.headlessbrowser.windows.PlaywrightWindow;
import com.osiris.headlessbrowser.windows.PuppeteerWindow;
import org.junit.jupiter.api.Test;

public class CustomWindows {

    @Test
    void test() {


        HBrowser hBrowser = new HBrowser();
        // You can further customize the window like this:
        try (PlaywrightWindow playwrightWindow = hBrowser.openCustomWindow()
                .debugOutputStream(System.out).headless(true).jsTimeout(5) // etc.
                .buildPlaywrightWindow()) {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }

        // There are multiple types of windows you can build.
        // Remember that its important to close the windows, that's why we use the try/catch block,
        // which will auto-close the window when we leave the block.
        try (PlaywrightWindow playwrightWindow = hBrowser.openCustomWindow().buildPlaywrightWindow()) {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PuppeteerWindow puppeteerWindow = hBrowser.openCustomWindow().buildPuppeteerWindow()) {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (LightWindow lightWindow = hBrowser.openCustomWindow().buildLightWindow()) {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (GraalWindow playwrightWindow = hBrowser.openCustomWindow().buildGraalWindow()) {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
