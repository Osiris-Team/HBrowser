# HBrowser [![](https://jitpack.io/v/Osiris-Team/HBrowser.svg)](https://jitpack.io/#Osiris-Team/HBrowser)

Headless/full Java browser with support for downloading files, working with cookies, retrieving HTML and simulating real user input. Possible via Node.js with Puppeteer and/or Playwright. Main focus on ease of use and high-level methods.
Add this to your project with [Maven/Gradle/Sbt/Leinigen](https://jitpack.io/#Osiris-Team/HBrowser/LATEST)
(Java 8 or higher required).

```java
try(PlaywrightWindow window = HB.newWin()){
    window.load("https://example.com");
   // ...   
}
```
All examples [here](src/test/java/examples).
Note that the first run may take a bit because Node.js and its modules get installed into your current working dir under `./headless-browser`.

### Features
 - **High-Level methods for...**
   - downloading files.
   - working with cookies.
   - retrieving HTML.
   - simulating real user input. 
 - **Integrated evasions for headless detection:**
 ```java
try (PlaywrightWindow w = HB.newWinBuilder()
     .headless(true).makeUndetectable(true).buildPlaywrightWindow())
{
    w.load("https://infosimples.github.io/detect-headless/");
    w.makeScreenshot(new File("screenshot.png"), true);
} 
catch (Exception e) {e.printStackTrace();}
 ```
 - **Easy access to Node.js from within Java:**
 ```java
 new NodeContext().executeJavaScript("console.log('Hello!');");
 ```
 - **HTML handling via Jsoup and JSON with Gson.**

### Drivers
Playwright is the default and recommended browser driver to use, since it supports downloads
and more of its features were ported to Java.
Checkout [JG-Browser](https://github.com/Osiris-Team/JG-Browser) for a browser completely written in Java.

| Name |    JS-Engine    | Downloads |
| :-----: |:---------------:| :-----:
| [Playwright](https://github.com/microsoft/playwright)|   Node.js/V8    | Yes | No |
| [Puppeteer](https://github.com/puppeteer/puppeteer) |   Node.js/V8    | No | No |

You can find their versions in [this class](https://github.com/Osiris-Team/HBrowser/blob/main/src/main/java/com/osiris/headlessbrowser/Versions.java),
which also allows you to set custom versions.
(JS = JavaScript; Downloads = If the browser is able to download files other than html/xml/pdf;)

### Contribute/Build

#### Beginners

If you have never contributed before, we recommend
this [Beginners Article](https://www.jetbrains.com/help/idea/contribute-to-projects.html). If you are planning to make
big changes, create an issue first, where you explain what you want to do. Thank you in advance for every contribution!
If you don't know how to import a GitHub project, check out this
guide: [IntelliJ IDEA Cloning Guide](https://blog.jetbrains.com/idea/2020/10/clone-a-project-from-github/)

#### Build-Details

- Written in [Java](https://java.com/),
  with [JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html), inside
  of [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- Built with [Maven](https://maven.apache.org/), profiles: clean package

### Libraries

| Name/Link | Usage | License |
| :-----: | :-----: | :-----: |
| [Playwright](https://github.com/microsoft/playwright) | Emulates different types of browsers | [License](https://github.com/microsoft/playwright/blob/master/LICENSE) |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Emulates different types of browsers  | [License](https://github.com/puppeteer/puppeteer/blob/main/LICENSE) |
| [Node.js](https://github.com/nodejs/node) | Enables executing JavaScript code | [License](https://github.com/nodejs/node/blob/master/LICENSE) |
| [Jsoup](https://github.com/jhy/jsoup)      | Used to load pages and modify their HTML code      |   [License](https://github.com/jhy/jsoup/blob/master/LICENSE) |
