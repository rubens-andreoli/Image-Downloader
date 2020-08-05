package rubensandreoli.imagedownloader.support;

@FunctionalInterface
public interface ProgressListener {
    void progressed(ProgressLog log);
}
