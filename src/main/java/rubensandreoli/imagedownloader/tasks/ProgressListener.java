package rubensandreoli.imagedownloader.tasks;

@FunctionalInterface
public interface ProgressListener {
    void progressed(ProgressLog log);
}
