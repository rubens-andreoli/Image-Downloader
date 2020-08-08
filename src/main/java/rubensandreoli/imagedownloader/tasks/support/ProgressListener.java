package rubensandreoli.imagedownloader.tasks.support;

@FunctionalInterface
public interface ProgressListener {
    void progressed(ProgressLog log);
}
