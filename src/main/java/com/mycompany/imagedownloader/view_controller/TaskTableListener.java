package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.Task;

@FunctionalInterface
public interface TaskTableListener {
    boolean taskRemoved(Task tasks);
}
