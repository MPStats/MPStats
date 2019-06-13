package com.mpstats.mpstats;

public class MPStatsConfig {
    private String api_key;
    public String getApiKey () {
        return api_key;
    }
    public MPStatsConfig (String _appName) {
        api_key = _appName;
    }
}
