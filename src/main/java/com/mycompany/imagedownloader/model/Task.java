package com.mycompany.imagedownloader.model;

public interface Task {
    
    public enum Status {
        WAITING, RUNNING, INTERRUPTED, COMPLETED
    }
    
    void setProgressListener(TaskListener listener);
    void start();
    void stop();
    Status getStatus();
    
    int getProgress();
    default int getWorkload(){
        return 1;
    }
    
}
