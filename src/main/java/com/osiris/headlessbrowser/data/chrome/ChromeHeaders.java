package com.osiris.headlessbrowser.data.chrome;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Headers from a real Chrome browser. <br>
 * Last checked 09.10.2021. <br>
 * Should be updated regularly. <br>
 */
public class ChromeHeaders {
    public String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
    public String accept_encoding = "gzip, deflate, br";
    public String accept_language = "en-US,en;";
    public String cache_crontrol = "max-age=0";
    public String sec_ch_ua = ("`Chromium`;v=`94`, `Google Chrome`;v=`94`, `;Not A Brand`;v=`99`").replace("`", "\"");
    public String sec_ch_ua_mobile = "?0";
    public String sec_ch_ua_platform = "Windows";
    public String sec_fetch_dest = "document";
    public String sec_fetch_mode = "navigate";
    public String sec_fetch_site = "none";
    public String sec_fetch_user = "?1";
    public String upgrade_insecure_requests = "1";
    public String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36";

    public Map<String, String> getMap() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", accept);
        headers.put("accept-encoding", accept_encoding);
        headers.put("accept-language", accept_language);
        headers.put("cache-control", cache_crontrol);
        headers.put("sec-ch-ua", sec_ch_ua);
        headers.put("sec-ch-ua-mobile", sec_ch_ua_mobile);
        headers.put("sec-ch-ua-platform", sec_ch_ua_platform);
        headers.put("sec-fetch-dest", sec_fetch_dest);
        headers.put("sec-fetch-mode", sec_fetch_mode);
        headers.put("sec-fetch-site", sec_fetch_site);
        headers.put("sec-fetch-user", sec_fetch_user);
        headers.put("upgrade-insecure-requests", upgrade_insecure_requests);
        headers.put("user-agent", user_agent);
        return headers;
    }

    public JsonObject getJson() {
        JsonObject headers = new JsonObject();
        headers.addProperty("accept", accept);
        headers.addProperty("accept-encoding", accept_encoding);
        headers.addProperty("accept-language", accept_language);
        headers.addProperty("cache-control", cache_crontrol);
        headers.addProperty("sec-ch-ua", sec_ch_ua);
        headers.addProperty("sec-ch-ua-mobile", sec_ch_ua_mobile);
        headers.addProperty("sec-ch-ua-platform", sec_ch_ua_platform);
        headers.addProperty("sec-fetch-dest", sec_fetch_dest);
        headers.addProperty("sec-fetch-mode", sec_fetch_mode);
        headers.addProperty("sec-fetch-site", sec_fetch_site);
        headers.addProperty("sec-fetch-user", sec_fetch_user);
        headers.addProperty("upgrade-insecure-requests", upgrade_insecure_requests);
        headers.addProperty("user-agent", user_agent);
        return headers;
    }

}
