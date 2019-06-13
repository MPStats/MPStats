package com.mpstats.mpstats;

import android.arch.core.util.Function;

import com.mpstats.mpstats.Interfaces.IStatElement;
import com.mpstats.mpstats.Interfaces.IWebRequest;

public class WebRequest implements IWebRequest, IStatElement {
    String _url;
    String _data;
    transient Function<String, String> _dataToEventName;

    public WebRequest (String urll, String datal, Function<String, String> dataToEventNamel) {
        _url = urll;
        _data = datal;
        _dataToEventName = dataToEventNamel;
    }

    @Override
    public String url() {
        return _url;
    }

    @Override
    public String data() {
        return _data;
    }

    @Override
    public String getActionName () {
        return _dataToEventName.apply(_data);
    }
}
