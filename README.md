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
 - Implemented Node.js
 - Jsoup for easy HTML handling
 - Supported platforms are all from Java and all from Node.js

### Browsers

| Name | Latest JS | JS-Engine | Downloads | Full Java
| :-----: | :-----: | :-----: | :-----: | :-----: |
| [Playwright](https://github.com/microsoft/playwright) | Yes | Node.js/V8 | Yes | No |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Yes | Node.js/V8 | No | No |

(JS = JavaScript; Full Java = If the browser is completely written in Java or not; Downloads = If the browser is able to download files other than html/xml/pdf)

### [Contribute/Build](CONTRIBUTE.md)

### Examples
 - Checkout [JG-Browser](https://github.com/Osiris-Team/JG-Browser) for a browser completely written in Java
 - [Running Node.js independently](src/test/java/examples/IndependentNodeJs.java)
 - [Customizing browser windows](src/test/java/examples/CustomWindows.java)

### FAQ
<details>
<summary>What about the full Java browser?</summary>
<p>
Creating a new browser completely in Java is ongoing work (with GraalJs).
Implementing all the web apis for me alone would take years,
thats why I implemented Playwright into Java to have something usable right now.

If you want to contribute to the full Java browser take a look at the [GraalWindow](src/main/java/com/osiris/headlessbrowser/windows/GraalWindow.java) class,
the [GraalContext](src/main/java/com/osiris/headlessbrowser/js/contexts/GraalContext.java) class and [how to implement a js web apis](how-to-implement-a-js-web-api.md).
    
If you are usure why to contribute to the full Java browser, read the next section below.
</p>
</details>
<details>
<summary>Why contribute to the full Java browser?</summary>
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

### Libraries

| Name/Link | Usage | License |
| :-----: | :-----: | :-----: |
| [Playwright](https://github.com/microsoft/playwright) | Emulates different types of browsers | [License](https://github.com/microsoft/playwright/blob/master/LICENSE) |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Emulates different types of browsers  | [License](https://github.com/puppeteer/puppeteer/blob/main/LICENSE) |
| [Node.js](https://github.com/nodejs/node) | Enables executing JavaScript code | [License](https://github.com/nodejs/node/blob/master/LICENSE) |
| [Jsoup](https://github.com/jhy/jsoup)      | Used to load pages and modify their HTML code      |   [License](https://github.com/jhy/jsoup/blob/master/LICENSE) |

### Links

- https://spec.whatwg.org/ | Details about JS Web-APIs
- https://www.w3.org/TR/?tag=webapi | Details about JS Web-APIs
