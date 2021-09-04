# How to implement a JavaScript Web-API

1. Simply copy the `JS_API_Console` class from the `com.osiris.headlessbrowser.javascript` package 
   and paste it with another name into the same package.
2. Register your new class in the `JSContext` class, so it gets loaded into it.
3. The `JS_API_Console` is the Java-Implementation of the JavaScript console api and
   thus provides all the information you need to start implementing a new web-api.