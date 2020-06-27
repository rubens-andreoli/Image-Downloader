package rubensandreoli.imagedownloader.gui;

import rubensandreoli.imagedownloader.tasks.Task;

@FunctionalInterface
public interface TaskTableListener {
    boolean taskRemoved(Task tasks);
}
