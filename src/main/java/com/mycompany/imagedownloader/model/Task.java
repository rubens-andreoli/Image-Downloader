package com.mycompany.imagedownloader.model;

public interface Task {
    
    void setProgressListener(ProgressListener listener);
    void start();
    void stop();
    
    default int getProcessesCount(){
        return 1;
    }
    
}
