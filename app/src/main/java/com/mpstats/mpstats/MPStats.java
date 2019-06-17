package com.mpstats.mpstats;

import android.app.Activity;
import android.app.ActivityManager;
import android.arch.core.util.Function;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mpstats.mpstats.Data.DefaultEventNames;
import com.mpstats.mpstats.Data.MPStatsData;
import com.mpstats.mpstats.Interfaces.IStatElement;
import com.mpstats.mpstats.Interfaces.IWebRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;


public class MPStats {
    static int maxMbDataSize = 1; // 1mb
    static boolean initialized = false;
    QueueSerializable queue;
    ResponseResolver resolver;
    DataSender dataSender;
    boolean mBound;
    public QueueSerializable getQueue () {
        return queue;
    }
    public ResponseResolver getResolver () {
        return resolver;
    }

    private static final MPStats ourInstance = new MPStats();

    public static MPStats getInstance() {
        return ourInstance;
    }
    private Context _context;
    public Context getContext() { return _context; }



    private static void activate (Context context, MPStatsConfig config) {
        if (initialized) {
            return;
        }
        getInstance()._context = context;
        MPStatsData.config = config;
        getInstance().CreateServices(context);
        getInstance().SendInstallIfNotYet();
        getInstance().SendStartIfNotYet();
        initialized = true;
    }
    public static void activate (Activity activity, String appName) {
        SetDeepLink(activity);
        //BillingController controller = new BillingController();
        activate(activity, new MPStatsConfig(appName));
    }
    static void SetDeepLink (Activity activity) {
        try {
            if (activity.getIntent().getData() != null) {
                String dataString = activity.getIntent().getData().toString();
                MPStatsData.setDeepLink(dataString);
            }
        }
        catch (Exception ex) {

        }
    }
    void CreateServices (Context context) {
        queue = new QueueSerializable(maxMbDataSize);
        resolver = new ResponseResolver(
                new ParseWebRequest(),
                new GetInstallRequest(),
                new GetInstallAttemptLimitReached()
        );

        Intent serviceIntent = new Intent(context, DataSender.class);
        context.startService(serviceIntent);

        new Thread(bindServiceWhenCan).start();
        //new Thread(debug).start();
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DataSender.LocalBinder binder = (DataSender.LocalBinder) service;
            dataSender = binder.getService();
            dataSender.Initialize(queue, resolver, this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    Runnable debug = new Runnable() {
        @Override
        public void run() {
            while (true) {
                String output = new String();
                for (int i = 0; i < queue.asList().size(); i++) {
                    output += new ParseWebRequest().apply(queue.asList().get(i)).getActionName() + ",";
                }
                Utility.LogError(output);
                try {
                    Thread.sleep(500);
                }
                catch (Exception ex) {}
            }
        }
    };
    Runnable bindServiceWhenCan = new Runnable() {
        @Override
        public void run() {
            while (DataSender.getContext() == null) {
                try {
                    Thread.sleep(500);
                }
                catch (Exception ex) {}
            }
            Intent serviceIntent = new Intent(getContext(), DataSender.class);
            DataSender.getContext().bindService(serviceIntent, mConnection, Context.BIND_IMPORTANT);
        }
    };


    public static void ReportPurchasePage (String purchaseSKU) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), DefaultEventNames.PurchasePage, purchaseSKU);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    public static void ReportPurchasePressed (String purchaseSKU) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), DefaultEventNames.PurchasePressed, purchaseSKU);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    public static void ReportPurchaseDone (String receipt) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), receipt, false);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }


    public static void ReportSubscribePage (String subscribeSKU) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), DefaultEventNames.SubscribePage, subscribeSKU);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    public static void ReportSubscribePressed (String subscribeSKU) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), DefaultEventNames.PurchasePressed, subscribeSKU);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    public static void ReportSubscribeDone (String receipt) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), receipt, false);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    public static void ReportEvent (String message) {
        if (initialized) {
            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), message);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    public static void ReportEvent (String message, String actionValue) {
        if (initialized) {

            StatisticsElement statElement = new StatisticsElement(MPStatsData.config.getApiKey(), message, actionValue);
            getInstance().Send(MPStatsData.getEndPoint(), statElement.toString(), false);
        }
    }
    void SendInstallIfNotYet () {
        if (!MPStatsData.getInstallSent()) {
            if (getQueue().Peek() != null) {
                if (!FirstIsInstall()) {
                    // First event in queue is not install
                    ReportInstall();
                }
            }
            else {
                ReportInstall();
            }
        }
    }

    boolean FirstIsInstall () {
        return getQueue().Peek() != null && new ParseWebRequest().apply(getQueue().Peek()).getActionName().equals(DefaultEventNames.Install);
    }

    void SendStartIfNotYet () {
        if (!MPStatsData.startAdded) {
            MPStatsData.startAdded = true;
            ReportStart();
        }
    }
    private void ReportInstall () {
        String statElement = GetInstallStatElement();
        Send(MPStatsData.getEndPoint(), statElement, true);
    }
    private void ReportStart () {
        String statElement = GetStartStatElement();
        Send(MPStatsData.getEndPoint(), statElement, false);
    }
    private void Send (String url, String data, boolean addToBegin) {
        if (addToBegin) {
            getQueue().EnqueueFirst(new WebRequest(url, data, new ParseDataToEventName()));
        }
        else {
            getQueue().Enqueue(new WebRequest(url, data, new ParseDataToEventName()));
        }
    }
    private String GetStartStatElement () {
        return new StatisticsElement(MPStatsData.config.getApiKey(), DefaultEventNames.Start.toString()).toString();
    }
    private String GetInstallStatElement () {
        return new StatisticsElement(MPStatsData.config.getApiKey(), true).toString();
    }
}

class ParseWebRequest implements Function<IWebRequest, IStatElement> {

    @Override
    public IStatElement apply(IWebRequest input) {
        return new StatisticsElement(input.data());
    }
    StatisticsElement getStartEvent () {
        return new StatisticsElement(MPStatsData.config.getApiKey(), false);
    }
}
class GetInstallRequest implements Function<Void, IWebRequest> {

    @Override
    public IWebRequest apply(Void input) {
        return new WebRequest(MPStatsData.getEndPoint(), getInstallEvent().toString(), new ParseDataToEventName());
    }
    StatisticsElement getInstallEvent () {
        return new StatisticsElement(MPStatsData.config.getApiKey(), true);
    }
}
class ParseDataToEventName implements Function<String, String> {

    @Override
    public String apply(String input) {
        return new StatisticsElement(input).getActionName();
    }
}
class GetInstallAttemptLimitReached implements Function<Void, IWebRequest> {

    @Override
    public IWebRequest apply(Void input) {
        return new WebRequest(MPStatsData.getEndPoint(), new StatisticsElement(MPStatsData.config.getApiKey(), DefaultEventNames.InstallAttemptLimit).toString(), new ParseDataToEventName());
    }
    StatisticsElement getStartEvent () {
        return new StatisticsElement(MPStatsData.config.getApiKey(), true);
    }
}
class IWebRequestFromString implements Function<HashMap<String, String>, IWebRequest> {
    static Gson gson = new Gson();
    @Override
    public IWebRequest apply(HashMap<String, String> input) {
        String url = input.get("_url");
        String data = input.get("_data");
        return new WebRequest(url, data, new ParseDataToEventName());
    }
}