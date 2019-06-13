package com.mpstats.mpstats.Interfaces;

public interface IQueue<T> {
    T Dequeue();
    void Enqueue(T data);
    void EnqueueFirst(T data);
    T Peek();
    public void Save();
    public void Load();
}
