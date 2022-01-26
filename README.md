# HBrowser

A new, headless browser for Java.

```java
HBrowser hBrowser = new HBrowser();
try(PlaywrightWindow window = hBrowser.openWindow()){
    window.load("https://example.com");
   // ...   
}
```
All examples [here](src/test/java/examples).
Note that the first run may take a bit because Node.js (if not installed) and its modules get installed into your current working dir.

### Installation

- Java 8 or higher required.
- [Click here for maven/gradle/sbt/leinigen instructions.](https://jitpack.io/#Osiris-Team/HBrowser/LATEST)
- Make sure to watch this repository to get notified of future updates.

### Features
 - Multiple browsers to choose from (Playwright, Puppeteer etc.)
 - Implemented Node.js
 - Jsoup for easy HTML handling
 - Supported platforms are all from Java and all from Node.js

### Browsers
Playwright is the default and recommended browser to use, since it supports downloads
and more of its features were ported to Java.

| Name | Version| JS-Engine | Downloads |
| :-----: | :-----: | :-----: | :-----:
| [Playwright](https://github.com/microsoft/playwright)| Latest | Node.js/V8 | Yes | No |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Latest  | Node.js/V8 | No | No |

(JS = JavaScript; Downloads = If the browser is able to download files other than html/xml/pdf;)

### [Contribute/Build](CONTRIBUTE.md)

### Libraries

| Name/Link | Usage | License |
| :-----: | :-----: | :-----: |
| [Playwright](https://github.com/microsoft/playwright) | Emulates different types of browsers | [License](https://github.com/microsoft/playwright/blob/master/LICENSE) |
| [Puppeteer](https://github.com/puppeteer/puppeteer) | Emulates different types of browsers  | [License](https://github.com/puppeteer/puppeteer/blob/main/LICENSE) |
| [Node.js](https://github.com/nodejs/node) | Enables executing JavaScript code | [License](https://github.com/nodejs/node/blob/master/LICENSE) |
| [Jsoup](https://github.com/jhy/jsoup)      | Used to load pages and modify their HTML code      |   [License](https://github.com/jhy/jsoup/blob/master/LICENSE) |

### Links
- Checkout [JG-Browser](https://github.com/Osiris-Team/JG-Browser) for a browser completely written in Java
- https://spec.whatwg.org/ | Details about JS Web-APIs
- https://www.w3.org/TR/?tag=webapi | Details about JS Web-APIs
