# Headless-Browser

A new, headless browser for Java.

```java
HBrowser hBrowser = new HBrowser();
try(PlaywrightWindow window = hBrowser.openWindow()){
    window.load("https://example.com");
   // ...   
}
```
Note that the first run may take a bit because Node.js and its modules get installed into your current working dir.

### Installation

- Java 8 or higher required.
- [Click here for maven/gradle/sbt/leinigen instructions.](https://jitpack.io/#Osiris-Team/Headless-Browser/LATEST)
- Make sure to watch this repository to get notified of future updates.

### Features
 - Multiple browsers to choose from (Playwright, Puppeteer etc.)
 - Implemented GraalJS and Node.js engines
 - Jsoup for easy HTML handling
 - Supported platforms are all from Java and all from Node.js

### Browsers

| Name | Latest JS | JS-Engine | Downloads | Full Java
| :-----: | :-----: | :-----: | :-----: | :-----: |
| [Playwright](https://github.com/microsoft/playwright) | Yes | Node.js/V8 | Yes | No |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Yes | Node.js/V8 | No | No |
| [Graal](https://github.com/oracle/graaljs) | Yes | GraalJS | No | Yes |

(JS = JavaScript; Full Java = If the browser is completely written in Java or not; Downloads = If the browser is able to download files other than html/xml/pdf)

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

### Links

- https://spec.whatwg.org/ | Details about JS Web-APIs
- https://www.w3.org/TR/?tag=webapi | Details about JS Web-APIs

### Examples
<details>
<summary>Running Node.js independently</summary>
<pre lang="java">
// Installs Node.js into current working directory if needed
NodeContext ctx = new NodeContext(); // Use another constructor for customization
try{
  // Easily install/update needed modules
  ctx.npmInstall("name of node module");

  // To be able to see the JavaScript code results.
  // Otherwise you can also init NodeContext with debugOutput=System.out to achieve this.
  ctx.onPrintLine(line -> System.out.println(line);
  ctx.executeJavaScript("console.log('hello world!');");

  // You can return JavaScript results too.
  // Note that you must have a result variable in the provided JS Code for this to work!
  String result = ctx.executeJavaScriptAndGetResult("var result = 'my JavaScript result!';");
} catch(Exception e){
  e.printStacktrace();
}
</pre>
</details>

### FAQ
<details>
<summary>What about GraalJS and a full Java browser?</summary>
Creating a new browser completely in Java is ongoing work (with GraalJs).
Since this is something that looks like it could take year, I
implemented the Node.js engine to be completely usable in Java and some 
'Browser Drivers' like Puppeteer and Playwright, which run on Node.js,
to have something usable right now.
Read more about this in "Why contribute?".
</details>
<details>
<summary>Why contribute?</summary>
I worked with multiple different browsers like JCEF, Selenium, JWebdriver, HtmlUnit and maybe some more I don't remember
now, but all have one thing in common. They have some kind of caveat.

That's why I started this project. To create a new browser, not dependent on Chromium or Waterfox etc., written in Java,
compatible with all operating systems that can run Java. We use Jsoup to handle HTML and the GraalJS engine to handle
JavaScript.

Now you may ask: Why do you need my help? Our problem is that most of the JavaScript code out there uses so
called Web-APIs: https://developer.mozilla.org/en-US/docs/Web/API
, which get shipped with each browser. We will need to implement those APIs by ourselfs.

There are a lot of Web-APIs available, so we need some motivated people to implement them. If you want to help, thank
you <b>very</b> much, already in
advance! Here is a list of already implemented APIs and how to implement one: /how-to-implement-a-js-web-api.md
If you are working on an implementation open an issue to keep track of who is working on what and avoid duplicate work.
</details>
<details>
<summary>What is the motivation behind this project?</summary>
There are only Java browsers available that you have to buy, or they are free but come with some caveat.
Thats why this project exists.
We want to provide the latest and best technologies regarding headless browsers and make them available to Java applications.
</details>

![](src/test/java/com/osiris/headlessbrowser/Playground.java)

### Libraries

| Name/Link | Usage | License |
| :-----: | :-----: | :-----: |
| [Playwright](https://github.com/microsoft/playwright) | Emulates different types of browsers | [License](https://github.com/microsoft/playwright/blob/master/LICENSE) |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Emulates different types of browsers  | [License](https://github.com/puppeteer/puppeteer/blob/main/LICENSE) |
| [Node.js](https://github.com/nodejs/node) | Enables executing JavaScript code | [License](https://github.com/nodejs/node/blob/master/LICENSE) |
| [GraalJS](https://github.com/oracle/graaljs) | Enables executing JavaScript code from Java | [License](https://github.com/oracle/graaljs/blob/master/LICENSE) |
| [Jsoup](https://github.com/jhy/jsoup)      | Used to load pages and modify their HTML code      |   [License](https://github.com/jhy/jsoup/blob/master/LICENSE) |
