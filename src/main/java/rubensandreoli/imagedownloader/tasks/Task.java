package rubensandreoli.imagedownloader.tasks;

public interface Task {
    
    public enum Status {
        WAITING, RUNNING, INTERRUPTED, COMPLETED
    }
    
    void perform();
    void interrupt();
    
    void setProgressListener(ProgressListener listener);
    
    Status getStatus();
    int getProgress();
    default int getWorkload(){
        return 1;
    }
    
}
