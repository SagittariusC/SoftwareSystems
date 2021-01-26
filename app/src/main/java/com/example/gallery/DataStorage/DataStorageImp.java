package com.example.gallery.DataStorage;

public class DataStorageImp implements IDataStore {
    String state = null;
    public void putState(String state)
    {
        this.state = state;
    }
    public String getState()
    {
        return state;
    }
}
