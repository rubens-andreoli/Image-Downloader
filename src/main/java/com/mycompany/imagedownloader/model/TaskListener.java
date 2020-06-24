package com.mycompany.imagedownloader.model;

@FunctionalInterface
public interface TaskListener {
    void progressed(ProgressLog log);
}
