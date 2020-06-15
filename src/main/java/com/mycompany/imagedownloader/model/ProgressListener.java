package com.mycompany.imagedownloader.model;

public interface ProgressListener<T> {
    
    void progress(ProgressMessage message);
}
