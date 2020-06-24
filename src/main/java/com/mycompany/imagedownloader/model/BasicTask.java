package com.mycompany.imagedownloader.model;

import java.io.File;
import java.io.IOException;

public abstract class BasicTask implements Task {

    private static final String NO_FOLDER_MSG = "No destination folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found.";
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    
    private String destination;
    
    protected TaskListener listener;
    protected volatile Status status = Status.WAITING;
    
    @Override
    public void setProgressListener(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        status = Status.RUNNING;
        run();
        if(status != Status.INTERRUPTED) status = Status.COMPLETED;
    }
    
    protected abstract void run();

    @Override
    public void stop() {
        status = Status.INTERRUPTED;
    }

    @Override
    public Status getStatus(){
        return status;
    }

    public String getDestination() {
        return destination;
    }
    
    protected File getFolder(String folder) throws IOException{
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
    
    public void setDestination(String folder) throws IOException{
        getFolder(folder);
        this.destination = folder;
    }
    
    protected boolean isInterrupted(){
        return status == Status.INTERRUPTED;
    }
    
}
