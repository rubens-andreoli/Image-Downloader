package com.mycompany.imagedownloader.model;

public interface Task {
    
    boolean start(ProgressListener listener);
    void stop();
    
    default int getProcessesCount(){
        return 1;
    }
    
}
