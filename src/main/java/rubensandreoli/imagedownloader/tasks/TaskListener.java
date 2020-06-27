package rubensandreoli.imagedownloader.tasks;

@FunctionalInterface
public interface TaskListener {
    void progressed(ProgressLog log);
}
