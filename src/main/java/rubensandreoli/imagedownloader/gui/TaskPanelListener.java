package rubensandreoli.imagedownloader.gui;

import rubensandreoli.imagedownloader.tasks.Task;

@FunctionalInterface
public interface TaskPanelListener {
    void taskCreated(TaskPanel source, Task task, String description);  
}
