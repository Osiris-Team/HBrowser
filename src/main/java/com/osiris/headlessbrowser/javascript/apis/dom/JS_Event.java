package com.osiris.headlessbrowser.javascript.apis.dom;

import org.graalvm.polyglot.HostAccess;

/**
 * Standard from: https://dom.spec.whatwg.org/ <br>
 * Checked: 07.09.2021 <br>
 */
public class JS_Event {

    @HostAccess.Export
    public JS_Event(String type, JS_EventInit eventInitDict) {

    }


/*

    [Exposed=(Window,Worker,AudioWorklet)]
    interface Event {
        constructor(DOMString type, optional EventInit eventInitDict = {});

        readonly attribute DOMString type;
        readonly attribute EventTarget? target;
        readonly attribute EventTarget? srcElement; // legacy
        readonly attribute EventTarget? currentTarget;
        sequence<EventTarget> composedPath();

  const unsigned short NONE = 0;
  const unsigned short CAPTURING_PHASE = 1;
  const unsigned short AT_TARGET = 2;
  const unsigned short BUBBLING_PHASE = 3;
        readonly attribute unsigned short eventPhase;

        undefined stopPropagation();
        attribute boolean cancelBubble; // legacy alias of .stopPropagation()
        undefined stopImmediatePropagation();

        readonly attribute boolean bubbles;
        readonly attribute boolean cancelable;
        attribute boolean returnValue;  // legacy
        undefined preventDefault();
        readonly attribute boolean defaultPrevented;
        readonly attribute boolean composed;

  [LegacyUnforgeable] readonly attribute boolean isTrusted;
        readonly attribute DOMHighResTimeStamp timeStamp;

        undefined initEvent(DOMString type, optional boolean bubbles = false, optional boolean cancelable = false); // legacy
    };

    dictionary EventInit {
        boolean bubbles = false;
        boolean cancelable = false;
        boolean composed = false;
    };


 */
}
