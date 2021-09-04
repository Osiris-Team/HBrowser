package com.osiris.headlessbrowser.data.chrome;

import java.util.HashMap;
import java.util.Map;

public class ChromeHeaders {


    public Map<String, String> get() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("accept-encoding", "gzip, deflate, br");
        headers.put("accept-language", "en-US,en;");
        headers.put("cache-control", "max-age=0");
        headers.put("referer", "https://google.com/");
        headers.put("sec-ch-ua", "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
        headers.put("sec-ch-ua-mobile", "?1");
        headers.put("sec-fetch-dest", "document");
        headers.put("sec-fetch-mode", "navigate");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("sec-fetch-user", "?1");
        headers.put("upgrade-insecure-requests", "1");
        headers.put("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Mobile Safari/537.36");
        return headers;
    }

}
