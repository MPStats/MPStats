package com.mpstats.mpstats;

import android.app.Service;
import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.mpstats.mpstats.Data.MPStatsData;
import com.mpstats.mpstats.Interfaces.IQueue;
import com.mpstats.mpstats.Interfaces.IResponseResolver;
import com.mpstats.mpstats.Interfaces.IWebRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class DataSender extends Service {

    private static IQueue<IWebRequest> queue;
    private static IResponseResolver resolver;
    private static long smallDelay = 500;
    private static long bigDelay = 10000;
    private static long hugeDelay = 60000;

    static Context context;
    ServiceConnection serviceConnection;
    public static Context getContext () {
        return context;
    }
    @Override
    public void onCreate() {
        context = this;
    }
    @Override
    public void onDestroy() {
        queue.Save();
        unbindService(serviceConnection);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Initialize(MPStats.getInstance().getQueue(), MPStats.getInstance().getResolver(), new GetInstallRequest(), MPStatsData.getContext());
        return START_NOT_STICKY;
    }




    private final IBinder mBinder = new LocalBinder();
    // Random number generator

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        DataSender getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataSender.this;
        }
    }
    @Override
    public void onTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void Initialize (IQueue<IWebRequest> _queue, IResponseResolver _resolver, ServiceConnection _serviceConnection) {
        queue = _queue;
        resolver = _resolver;
        serviceConnection = _serviceConnection;
        new Thread(senderLoop).start();
    }

    public Runnable senderLoop = new Runnable() {
        public void run() {
            long waitMe = 100;
            PowerManager powerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
            try {
                Thread.sleep(1000);
            }catch (Exception ex){}
            while (true) {
                if (MPStatsData.getStatisticDisabled()) {
                    break;
                }
                if (queue.Peek() != null) {
                    int resultCode = SendData(queue.Peek());
                    if (resultCode == HttpsURLConnection.HTTP_OK) {
                        // success
                        resolver.ReportSuccesfull(queue.Peek());
                        queue.Dequeue();
                    }
                    else {
                        // error
                        waitMe = resolver.ReportErrorGetTimeout(queue.Peek(), queue, resultCode);
                    }
                    if (waitMe != 0) {
                        try {
                            Thread.sleep(waitMe);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        waitMe = 0;
                    }
                }
                boolean isScreenOff;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    isScreenOff = !powerManager.isInteractive();
                }
                else {
                    isScreenOff = !powerManager.isScreenOn();
                }
                try {
                    if (waitMe == Long.MAX_VALUE) {
                        stopSelf();
                    }
                    Thread.sleep(smallDelay);
                    if (isScreenOff ^ queue.Peek() == null) {
                        Thread.sleep(bigDelay);
                    }
                    else if (isScreenOff && queue.Peek() == null) {
                        Thread.sleep(hugeDelay);
                    }
                } catch (InterruptedException e) {}
            }
        }
    };

    public static int SendData( IWebRequest request )
    {
        String postString = request.data();
        String requestURL = request.url();

        HttpsURLConnection conn = null;
        InputStream inStream = null;
        int responseCode = 0;

        // detect post vs get requests
        boolean postRequest = !TextUtils.isEmpty( postString );
        if ( TextUtils.isEmpty( requestURL ) )
        {
            return responseCode;
        }

        try
        {
            URL url = new URL( requestURL );
            conn = ( HttpsURLConnection ) url.openConnection();
            conn.setReadTimeout( 5000 );
            conn.setConnectTimeout( 5000 );
            if ( postRequest )
            {
                conn.setRequestMethod( "POST" );
                conn.setDoOutput( true );
            }
            else
            {
                conn.setRequestMethod( "GET" );
                conn.setDoOutput( false );
            }
            conn.setDoInput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/json" );

            // JSON
            if ( postRequest )
            {
                byte[] outputBytes = postString.getBytes( "UTF-8" );
                OutputStream os = conn.getOutputStream();
                os.write( outputBytes );
                os.close();
            }
            else
            {
                conn.connect();
            }

            responseCode = conn.getResponseCode();

            if ( responseCode == HttpsURLConnection.HTTP_OK )
            {
                inStream = conn.getInputStream();
            }

            try
            {
                if ( inStream != null )
                {
                    inStream.close();
                }
            }
            catch ( Exception e )
            {
                Log.e( TAG, "On close exception: " + requestURL + " " + e.getMessage() );
            }
        }
        catch ( Exception e )
        {
            Log.e( TAG, requestURL + " " + e.getMessage() );
        }

        try
        {
            if ( conn != null )
            {
                conn.disconnect();
            }
        }
        catch ( Exception e )
        {
            Log.e( TAG, "On disconnect exception: " + requestURL + " " + e.getMessage() );
        }

        return responseCode;
    }


}
