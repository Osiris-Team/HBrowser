# Research
Organized list containing easy to forget things.

## GraalJS - JavaScript Engine
Problem: `eval()` fails to detect correct binding when nested classes are used (at least in tests).

```java
import com.osiris.headlessbrowser.javascript.JavaScriptAPI;

public class JS_API_Test extends JavaScriptAPI {
    // stuff...
    class NestedObject {
    }

    @Override
    public Object getObject() {
        return new NestedObject();
    }
}
```
Fix: Simply don't use nested classes/objects.