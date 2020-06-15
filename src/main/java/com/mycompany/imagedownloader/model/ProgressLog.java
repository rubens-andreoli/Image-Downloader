package com.mycompany.imagedownloader.model;

public class ProgressLog {
    
    public enum Status{
        MSG, WARNING, ERROR, CRITICAL;

        @Override
        public String toString() {
            return this.name()+": ";
        }
        
    }
    
    private static final String DEFAULT_MASK = "[%d]\n%s";

    private final int id;
    private StringBuilder log;

    public ProgressLog(final int id) {
        this.id = id;
        log = new StringBuilder();
    }
    
    public void appendToLog(String message){
        log.append(message);
    }
    
    public void appendToLog(String message, Status tag){
        log.append(tag.toString());
        appendToLog(message);
    }
    
    public void setLog(String log){
        setLog(log, null);
    }
    
    public void setLog(String log, Status tag){
        this.log = new StringBuilder(tag!=null? tag.toString():"");
        appendToLog(log);
    }
    
    public String getLog(){
        return log.toString();
    }
    
    public String getLogWithID(){
        return getLogWithID(DEFAULT_MASK);
    }
    
    public String getLogWithID(String mask){
        return String.format(mask, id, getLog());
    }
    
    public int getId(){
        return id;
    }
       
}
