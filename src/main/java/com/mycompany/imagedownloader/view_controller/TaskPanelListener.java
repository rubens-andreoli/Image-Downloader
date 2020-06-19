package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.Task;

public interface TaskPanelListener {
    void taskCreated(Task task);  
    void taskDeleted(Task task);
}
