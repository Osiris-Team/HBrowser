# Headless-Browser

A new, headless browser written in Java, running the GraalJS-Engine with latest JavaScript support.

```java
HBrowser hBrowser = new HBrowser();
HWindow hWindow = hBrowser.openNewWindow().load("https://wikipedia.org");
```
## Features
- [x] Can load pages and partially execute their JavaScript code. Contributions are needed for implementing all [JS Web-APIs](https://developer.mozilla.org/en-US/docs/Web/API), to achieve full JavaScript support. [Click here to see a list of already implemented APIs and how to implement one.](how-to-implement-a-js-web-api.md)
- [x] Uses Jsoup for editing HTML directly in Java.
- [x] Uses GraalJS-Engine to execute JavaScript code.
- [ ] Access to all JS-Web APIs from within Java.

## Installation

- Java 8 or higher required.
- [Click here for maven/gradle/sbt/leinigen instructions.](https://jitpack.io/#Osiris-Team/Headless-Browser)
- Make sure to watch this repository to get notified of future updates.

## Contribute/Build

#### Why contribute?

I worked with multiple different browsers like JCEF, Selenium, JWebdriver,
HtmlUnit and maybe some more I don't remember now, but all have one thing in common. 
They have some kind of caveat.

That's why I started this project. To create a new browser, not dependent on Chromium or Waterfox etc., written in Java,
compatible with all operating systems that can run Java.
We use Jsoup to handle HTML and the GraalJS engine to handle JavaScript.

Now you may ask: Why do you need my help? 
Our problem is that most of the JavaScript code out there uses so called [Web-APIs](https://developer.mozilla.org/en-US/docs/Web/API)
, which get shipped with each browser.
We will need to implement those APIs by ourselfs.

There are a lot of Web-APIs available, so we need some motivated people to implement them.
If you want to help, thank you **very** much, already in advance! [Click here to see a list of already implemented APIs and how to implement one.](how-to-implement-a-js-web-api.md)
If you are working on an implementation open an issue to keep track of who is working on what and avoid duplicate work.

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

## Links

- https://spec.whatwg.org/ | Details about JS Web-APIs
- https://www.w3.org/TR/?tag=webapi | Details about JS Web-APIs

## Examples

Insert_usage_examples_here

## FAQ

Frequently_asked_questions_here

