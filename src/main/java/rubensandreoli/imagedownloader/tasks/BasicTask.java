package rubensandreoli.imagedownloader.tasks;

import java.io.File;
import java.io.IOException;

public abstract class BasicTask implements Task {

    private static final String NO_FOLDER_MSG = "No destination folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found.";
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    
    private String destination;
    
    protected ProgressListener listener;
    protected volatile Status status = Status.WAITING;
    protected int progress; //TODO: private
    
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
    
    public void setDestination(String folder) throws IOException{
        createFolder(folder);
        this.destination = folder;
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

    protected int increaseProgress(){
        return ++progress;
    }
    
    @Override
    public Status getStatus(){
        return status;
    }

    @Override
    public int getProgress(){
        return progress;
    }

    public String getDestination() {
        return destination;
    }
    
    protected boolean isInterrupted(){
        return status == Status.INTERRUPTED;
    }
    
}
