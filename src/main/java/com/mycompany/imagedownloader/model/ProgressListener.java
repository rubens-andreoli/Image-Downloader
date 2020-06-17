package com.mycompany.imagedownloader.model;

@FunctionalInterface
public interface ProgressListener {
    void progressed(ProgressLog message);
}
