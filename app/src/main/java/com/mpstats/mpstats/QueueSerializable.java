package com.mpstats.mpstats;

import android.arch.core.util.Function;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mpstats.mpstats.Data.AppPreferances;
import com.mpstats.mpstats.Data.MPStatsData;
import com.mpstats.mpstats.Interfaces.AbstractIQueueSerializable;
import com.mpstats.mpstats.Interfaces.IQueue;
import com.mpstats.mpstats.Interfaces.IWebRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class QueueSerializable extends AbstractIQueueSerializable implements IQueue<IWebRequest> {

    private ArrayList<IWebRequest> queue;
    private int mbMaxData;
    private Function<HashMap<String, String>, IWebRequest> iWebRequestParser;
    public ArrayList<IWebRequest> asList() {
        return queue;
    }


    public QueueSerializable(int _mbMaxData) {
        super(_mbMaxData);
        mbMaxData = _mbMaxData;
        Load();
    }
    @Override
    public IWebRequest Dequeue() {
        IWebRequest returnMe = queue.get(0);
        queue.remove(0);
        return returnMe;
    }
    @Override
    public void EnqueueFirst (IWebRequest item) {
        Utility.LogError("ENQUEUE FIRST: " + item.data() + " NOW SIZE: " + queue.size());
        queue.add(0, item);
    }
    @Override
    public void Enqueue(IWebRequest item) {
        Utility.LogError("ENQUEUE: " + item.data() + " NOW SIZE: " + queue.size());
        queue.add(item);
    }

    @Override
    public IWebRequest Peek() {
        if (queue.size() < 1) {
            return null;
        }
        return queue.get(0);
    }
    private ArrayList<IWebRequest> tempQueue;
    private boolean CutData (String data) {
        double size = ConvertBytesToMegabytes(data.length());
        double overSize = size - mbMaxData;
        Utility.LogError("CUT DATA. Size:" + size + " OVERSIZE: " + overSize);
        if (overSize > 0) {
            tempQueue = queue;
            tempQueue.subList(queue.size() / 2, queue.size() - 1).clear();
            queue = tempQueue;
            return true;
        }
        return false;
    }
    static double ConvertBytesToMegabytes(long bytes)
    {
        return (bytes / 1024f) / 1024f;
    }
    static Gson gson = new Gson();
    public void Save() {
        Save(10);
    }
    void Save (int safeIterator) {
        String data = gson.toJson(queue);
        if (safeIterator > 0 && CutData(data)) {
            safeIterator--;
            Save(safeIterator);
            return;
        }
        MPStatsData.saveQueue(data);
    }
    public void Load() {
        queue = new ArrayList<IWebRequest>();
        String loadedValue = MPStatsData.loadQueue();
        if (!loadedValue.isEmpty()) {
            Type resolveType = new TypeToken< WebRequest[] >() {}.getType();
            WebRequest[] needBeParsed = gson.fromJson(loadedValue, resolveType);
            for (int i = 0; i < needBeParsed.length; i++) {
                queue.add(needBeParsed[i]);
            }
        }
    }
}