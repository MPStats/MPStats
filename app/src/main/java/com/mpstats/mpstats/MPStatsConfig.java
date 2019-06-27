package com.mpstats.mpstats;

import com.mpstats.mpstats.Data.MPStatsData;

public class MPStatsConfig {
    //private static final String urlStartDefult = "https://mpstats.net/api/stats?projectName=";
    private String api_key;

    private String urlBefore = "https://";
    private String urlMiddleDefault = "mpstats.net";
    private String urlMiddle = "mpstats.net";
    private String urlAfter = "/api/stats?projectName=";

    public String getApiKey () {
        return api_key;
    }
    public String getUrlStart () { return mergeUrl(); }
    private String mergeUrl () { return urlBefore + urlMiddle + urlAfter; }
    public MPStatsConfig (String _appName) {
        api_key = _appName;
        urlMiddle = urlMiddleDefault;
    }
    public MPStatsConfig (String _appName, String _url) {
        api_key = _appName;
        urlMiddle = _url;
    }
}
