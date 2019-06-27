package com.mpstats.mpstats.Data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mpstats.mpstats.DataSender;
import com.mpstats.mpstats.Interfaces.IQueue;
import com.mpstats.mpstats.Interfaces.IWebRequest;
import com.mpstats.mpstats.MPStats;
import com.mpstats.mpstats.MPStatsConfig;
import com.mpstats.mpstats.QueueSerializable;
import com.mpstats.mpstats.Utility;

import java.util.ArrayList;
import java.util.UUID;

public class MPStatsData {
    public static Context getContext() {
        if (DataSender.getContext() == null) {
            return MPStats.getInstance().getContext();
        }
        return DataSender.getContext();
    }
    static SharedPreferences cachedPreferences;
    static SharedPreferences getPreferences () {
        if (cachedPreferences == null) {
            cachedPreferences = getContext().getSharedPreferences(AppPreferances.PreferanceName, getContext().MODE_PRIVATE);
        }
        return cachedPreferences;
    }
    public static void saveQueue (String saveMe) {
        getPreferences().edit().putString(AppPreferances.Queue, saveMe).apply();
    }
    public static String loadQueue () {
        return getPreferences().getString(AppPreferances.Queue, "");
    }
    static Boolean installSent = null;
    public static void setInstallSent (boolean value) {
        if (installSent != value) {
            getPreferences().edit().putBoolean(AppPreferances.Install, value).apply();
        }
        installSent = value;
    }
    public static boolean getInstallSent () {
        if (installSent == null) {
            installSent = getPreferences().getBoolean(AppPreferances.Install,false);
        }
        return installSent;
    }


    static Boolean statisticDisabled = null;
    public static void setStatisticDisabled (boolean value) {
        if (statisticDisabled != value) {
            getPreferences().edit().putBoolean(AppPreferances.StatisticDisabled, value).apply();
        }
        statisticDisabled = value;
    }
    public static boolean getStatisticDisabled () {
        if (statisticDisabled == null) {
            statisticDisabled = getPreferences().getBoolean(AppPreferances.StatisticDisabled,false);
        }
        return statisticDisabled;
    }


    static String installReferrer;
    public static void setInstallReferrer (String value) {
        if (installReferrer != value) {
            getPreferences().edit().putString(AppPreferances.InstallReferrer, value).apply();
        }
        installReferrer = value;
    }
    public static String getInstallReferrer () {
        if (installReferrer == null) {
            installReferrer = getPreferences().getString(AppPreferances.InstallReferrer,null);
        }
        return installReferrer;
    }
    static String deepLink;
    public static void setDeepLink (String value) {
        deepLink = value;
        if (deepLink != null) {
            getPreferences().edit().putString(AppPreferances.DeepLink, value).apply();
        }
    }
    public static String getDeepLink () {
        if (deepLink == null) {
            deepLink = getPreferences().getString(AppPreferances.DeepLink,null);
        }
        return deepLink;
    }


    static Integer installSentAttemptsLeft = null;
    public static void setInstallSentAttemptsLeft (int value) {
        if (installSentAttemptsLeft != value) {
            getPreferences().edit().putInt(AppPreferances.InstallSentAttemptsLeft, value).apply();
        }
        installSentAttemptsLeft = value;
    }
    public static int getInstallSentAttemptsLeft () {
        if (installSentAttemptsLeft == null) {
            installSentAttemptsLeft = getPreferences().getInt(AppPreferances.InstallSentAttemptsLeft,10);
        }
        return installSentAttemptsLeft;
    }








    public static String getEndPoint () {
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            Utility.LogError("Set project name");
        }
        if (apiKey.contains(" ")) {
            Utility.LogError("Project name can't containt spaces");
        }
        return config.getUrlStart() + apiKey;
    }

    static String _UUID = null;
    public static String getUUID () {
        if (_UUID == null) {
            _UUID = getPreferences().getString(AppPreferances.UUID, null);
            if (_UUID == null) {
                _UUID = UUID.randomUUID().toString();
                getPreferences().edit().putString(AppPreferances.UUID, _UUID);
            }
        }
        return _UUID;
    }
    public static MPStatsConfig config;
    public static boolean startAdded = false;
}
