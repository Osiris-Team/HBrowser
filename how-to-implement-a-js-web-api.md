# How to implement a JavaScript Web-API

1. Simply copy the [JS_API_Console](https://github.com/Osiris-Team/Headless-Browser/blob/main/src/main/java/com/osiris/headlessbrowser/JS_API_Console.java)
   class and paste it with another name into the same package.
2. Register your new class in the [JSContext](https://github.com/Osiris-Team/Headless-Browser/blob/main/src/main/java/com/osiris/headlessbrowser/JSContext.java)
   class, so it gets loaded into it before the user loads a page.
3. The [JS_API_Console](https://github.com/Osiris-Team/Headless-Browser/blob/main/src/main/java/com/osiris/headlessbrowser/JS_API_Console.java) is the Java-Implementation of the JavaScript console api and
   thus provides all the information you need to start implementing a new web-api.
