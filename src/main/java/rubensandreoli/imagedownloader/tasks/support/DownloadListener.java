package rubensandreoli.imagedownloader.tasks.support;

@FunctionalInterface
public interface DownloadListener {
    void donwloadStateChanged(int state, String description);
}
