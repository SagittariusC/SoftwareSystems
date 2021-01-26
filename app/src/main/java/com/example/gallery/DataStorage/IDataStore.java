package com.example.gallery.DataStorage;

public interface IDataStore {
    String getState();
    void putState(String state);
}
