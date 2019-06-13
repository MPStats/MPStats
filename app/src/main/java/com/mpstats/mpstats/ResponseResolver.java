package com.mpstats.mpstats;


import android.arch.core.util.Function;

import com.mpstats.mpstats.Data.MPStatsData;
import com.mpstats.mpstats.Data.DefaultEventNames;
import com.mpstats.mpstats.Interfaces.IQueue;
import com.mpstats.mpstats.Interfaces.IResponseResolver;
import com.mpstats.mpstats.Interfaces.IStatElement;
import com.mpstats.mpstats.Interfaces.IWebRequest;

import java.util.ArrayList;

public class ResponseResolver implements IResponseResolver {
    private Function<IWebRequest, IStatElement> parseRequest;
    private Function<Void, IWebRequest> getInstallRequest;
    private Function<Void, IWebRequest> getInstallAttemptLimitReachedWebRequest;
    private long middleTimeout = 1000;
    private long bigTimeout = 5000;

    @Override
    public void ReportSuccesfull (IWebRequest request) {
        Utility.LogInfo("sent success: " + parseRequest.apply(request).getActionName());
        if (MPStatsData.getInstallSent()) {
            return;
        }
        if (parseRequest.apply(request).getActionName().equals(DefaultEventNames.Install)) {
            // Succesfull send install event.
            MPStatsData.setInstallSent(true);
        }
    }

    private int sameEventCurrent;
    private final int sameEventMax = 10;

    private int droppedEventCurrent;
    private final int droppedEventMax = 10;

    private String lastEvent;

    private static ArrayList<String> errorCodesMustResend = new ArrayList<String>() {
        {
            add("0");
        }
    };
    private void CheckSameEventError (IWebRequest errorRequest, IQueue<IWebRequest> queue, int errorCode) {
        if (errorCodesMustResend.contains(String.valueOf(errorCode))) {
            return;
        }

        String currentEvent = errorRequest.url() + errorRequest.data();
        if (currentEvent.equals(lastEvent)) {
            sameEventCurrent++;
            if (sameEventCurrent > sameEventMax) {
                sameEventCurrent = 0;
                queue.Dequeue();
                droppedEventCurrent++;
                Utility.LogError("Can't send: " + parseRequest.apply(errorRequest).getActionName() + " to: " + errorRequest.url() + " because of error code: " + errorCode);
            }
        }
        else {
            lastEvent = currentEvent;
        }
    }
    private void CheckCantSentInstall (String action) {
        if (action.equals(DefaultEventNames.InstallAttemptLimit)) {
            Utility.LogError("STAT DISABLED! Send install attempt limit reached!");
            MPStatsData.setStatisticDisabled(true);
        }
    }
    public long ReportErrorGetTimeout (IWebRequest request, IQueue<IWebRequest> queue, int errorCode) {
        Utility.LogInfo("sent error: " + errorCode + " " + parseRequest.apply(request).getActionName());
        String actionName = parseRequest.apply(request).getActionName();
        CheckSameEventError(request, queue, errorCode);
        if (droppedEventCurrent >= droppedEventMax) {
            return Long.MAX_VALUE;
        }
        actionName = parseRequest.apply(queue.Peek()).getActionName();
        CheckCantSentInstall(actionName);
        if (!MPStatsData.getInstallSent()) {
            // SDK think that install NOT sent
            if (actionName.equals(DefaultEventNames.Install)) {
                // Current event is install event
            }
            else {
                // Current event not install.
                queue.EnqueueFirst(getInstallRequest.apply(null));
            }
            return middleTimeout;
        }
        else {
            // SDK think that install sucessfull sent
            if (errorCode == 590) {
                // Need send install again.
                if (MPStatsData.getInstallSentAttemptsLeft() > 0) {
                    // Has some more attempts.
                    queue.EnqueueFirst(getInstallRequest.apply(null));
                    MPStatsData.setInstallSentAttemptsLeft(MPStatsData.getInstallSentAttemptsLeft() - 1);
                    return bigTimeout;
                }
                else {
                    // Attempt limit reached. DISABLE STAT
                    queue.EnqueueFirst(getInstallAttemptLimitReachedWebRequest.apply(null));
                    return bigTimeout;
                }
            }
        }
        return middleTimeout;
    }

    public ResponseResolver (Function<IWebRequest, IStatElement> _parseRequest, Function<Void, IWebRequest> _getInstallRequest, Function<Void, IWebRequest> _getInstallAttemptLimitReachedWebRequest) {
        parseRequest = _parseRequest;
        getInstallRequest = _getInstallRequest;
        getInstallAttemptLimitReachedWebRequest = _getInstallAttemptLimitReachedWebRequest;
    }
}
