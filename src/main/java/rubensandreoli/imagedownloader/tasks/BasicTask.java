package rubensandreoli.imagedownloader.tasks;

import java.io.File;
import java.io.IOException;
import rubensandreoli.commons.utils.Configs;

public abstract class BasicTask implements Task {

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String NO_FOLDER_MSG = "No folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found.";
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    protected static final int CONNECTION_MAX_TIMEOUT; //ms
    protected static final int CONNECTION_MIN_TIMEOUT; //ms
    static{
        CONNECTION_MIN_TIMEOUT = Configs.values.get("connection_timeout_min", 500);
        CONNECTION_MAX_TIMEOUT = Configs.values.get("connection_timeout_max", CONNECTION_MIN_TIMEOUT+500, CONNECTION_MIN_TIMEOUT);
    }
    // </editor-fold>
    
    private String destination;
    private ProgressListener listener;
    private volatile Status status = Status.WAITING;
    private int progress, workload = 1;
    
    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public void perform() {
        status = Status.RUNNING;
        run();
        if(status != Status.INTERRUPTED) status = Status.COMPLETED;
    }
    
    protected abstract void run();

    @Override
    public void interrupt() {
        status = Status.INTERRUPTED;
    }
    
    protected boolean isInterrupted(){
        return status == Status.INTERRUPTED;
    }
    
    protected File createFolder(String folder) throws IOException{
        if(folder==null || folder.isBlank()) throw new IOException(NO_FOLDER_MSG);
        try{
            File file = new File(folder);
            if(file.isDirectory()){
                return file;
            }else{
                throw new IOException(String.format(NOT_FOLDER_MSG_MASK, folder));
            }
        }catch(SecurityException ex){
            throw new IOException(String.format(FOLDER_PERMISSION_MSG_MASK, folder));
        }
    }
    
    protected void report(ProgressLog log){
        if(listener != null) listener.progressed(log);
    }
    
    protected void report(String status, boolean progressed, String message, Object...args){
        var log = new ProgressLog(progressed? increaseProgress():getProgress(), getWorkload());
        log.appendLine(status, message, args);
        report(log);
    }
    
    protected void report(String status, String message, Object...args){
        report(status, true, message, args);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setDestination(String folder) throws IOException{
        createFolder(folder);
        this.destination = folder;
    }
    
    protected int increaseProgress(){
        return progress++;
    }

    protected void setWorkload(int workload){
        this.workload = workload;
    }
    
    protected void increaseWorkload(int amout) {
        workload += amout;
    }
    
    protected int increseWorkload(){
        return workload++;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getDestination() {
        return destination;
    }
    
    @Override
    public Status getStatus(){
        return status;
    }

    @Override
    public int getProgress(){
        return progress;
    }
    
    @Override
    public int getWorkload() {
        return workload;
    }
    // </editor-fold>
 
}
