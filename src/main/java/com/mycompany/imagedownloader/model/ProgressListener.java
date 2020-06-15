package com.mycompany.imagedownloader.model;

public interface ProgressListener {
    
    void progress(ProgressLog message, boolean isParcial);
    
    default void progress(ProgressLog message){
        progress(message, false);
    }
}
