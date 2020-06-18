package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.Task;

@FunctionalInterface
public interface TaskPanelListener {
    void taskCreated(Task task);  
}
