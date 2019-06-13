package com.mpstats.mpstats;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.mpstats.mpstats.Data.DefaultEventNames;
import com.mpstats.mpstats.Data.MPStatsData;
import com.mpstats.mpstats.Interfaces.IStatElement;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

public class StatisticsElement implements IStatElement {

    private String uuid;
    private String projectName;
    private String device;
    private String appVersion;
    private String osName;
    private String osVersion;
    private HashMap<String, String> installReferrer;
    private String actionName;

    //private Hashtable<String, String> actValues;
    private String actionValue;
    private String receiptData;

    String getAppVersion () {
        return BuildConfig.VERSION_NAME;
    }
    //private String getSdkVersion () { return "1.1.1"; }
    HashMap<String,String> getInstallReferrer () {
        HashMap<String, String> installReferrerDict = new HashMap<>();
        if (MPStatsData.getInstallReferrer() != null) {
            installReferrerDict.put("referrer", MPStatsData.getInstallReferrer());
        }
        if (MPStatsData.getDeepLink() != null) {
            installReferrerDict.put("deeplink", MPStatsData.getDeepLink());
        }
        if (installReferrerDict.size() == 0) {
            return null;
        }
        //return gson.toJson(installReferrerDict);
        return installReferrerDict;
    }
    String getOSName() {
        return "android";
    }
    String getOsVersion() {
        return Build.VERSION.RELEASE;
    }
    static String _device;
    String getDevice () {
        if (_device == null) {
            _device = android.os.Build.MODEL;
        }
        return _device;
    }

    void AutoFill () {
        uuid = MPStatsData.getUUID();
        device = getDevice();
        appVersion = getAppVersion();
    }
    public StatisticsElement (String _projectName, String _actionName, String _actionValue) {
        projectName = _projectName;
        actionName = _actionName;
        actionValue = _actionValue;
        AutoFill();
    }
    public StatisticsElement (String _projectName, String _receipt, boolean isSubscribe) {
        projectName = _projectName;
        if (isSubscribe) {
            actionName = DefaultEventNames.SubscribeDone;
        }
        else {
            actionName = DefaultEventNames.PurchaseDone;
        }
        receiptData = _receipt;
        AutoFill();
    }
    public StatisticsElement (String _projectName, String _actionName) {
        projectName = _projectName;
        actionName = _actionName;
        AutoFill();
    }
    public StatisticsElement (String _projectName, boolean install) {
        // Install event
        if (install) {
            projectName = _projectName;
            actionName = DefaultEventNames.Install;
            installReferrer = getInstallReferrer();
            osName = getOSName();
            osVersion = getOsVersion();
        }
        else {
            // AppStart event
            projectName = _projectName;
            actionName = DefaultEventNames.Start;
        }
        AutoFill();
    }

    private static Gson gson = new Gson();

    public StatisticsElement (String json) {
        // From json
        StatisticsElement statisticsElement = gson.fromJson(json, StatisticsElement.class);
        for (Field field : statisticsElement.getClass().getDeclaredFields()) {
            try {
                field.set(this, field.get(statisticsElement));
            }
            catch (Exception ex) {

            }
        }
    }

    @Override
    public String getActionName() {
        return actionName;
    }

    @Override
    public String toString () {
        return gson.toJson(this);
    }
}
