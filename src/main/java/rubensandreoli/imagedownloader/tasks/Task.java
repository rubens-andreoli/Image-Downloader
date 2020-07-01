package rubensandreoli.imagedownloader.tasks;

public interface Task {
    
    public enum Status {
        WAITING, RUNNING, INTERRUPTED, COMPLETED
    }
    
    void setProgressListener(ProgressListener listener);
    void perform();
    void interrupt();
    
    Status getStatus();
    int getProgress();
    default int getWorkload(){
        return 1;
    }
    
}
