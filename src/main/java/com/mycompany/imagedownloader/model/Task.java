package com.mycompany.imagedownloader.model;

public interface Task {
    
    boolean perform(ProgressListener listener);
    
    default int getProcessesCount(){
        return 1;
    }
    
}
