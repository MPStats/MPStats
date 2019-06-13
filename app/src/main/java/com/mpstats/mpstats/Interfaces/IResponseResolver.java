package com.mpstats.mpstats.Interfaces;

public interface IResponseResolver {
    void ReportSuccesfull(IWebRequest request);
    long ReportErrorGetTimeout(IWebRequest request, IQueue<IWebRequest> queue, int errorCode);
}
