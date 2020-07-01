package rubensandreoli.imagedownloader.gui;

import rubensandreoli.imagedownloader.tasks.Task;

/**
 * This interface should be implemented to listen 
 * when a Task is created by a TaskPanel.
 * 
 * @author Rubens A. Andreoli Jr.
 */
@FunctionalInterface
public interface TaskPanelListener {
    void taskCreated(TaskPanel source, Task task, String description);  
}
