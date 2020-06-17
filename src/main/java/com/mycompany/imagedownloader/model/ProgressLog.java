package com.mycompany.imagedownloader.model;

/** References:
 * https://howtodoinjava.com/java/string/4-ways-to-split-tokenize-strings-in-java/
 */
public class ProgressLog {
    
    public enum Status{
        INFO, WARNING, ERROR, CRITICAL;

        @Override
        public String toString() {
            return this.name()+TAG_DELIMITER+" ";
        }
    }
    
    private static final String DEFAULT_ID_MASK = "[%s]\n%s";
    private static final char TAG_DELIMITER = ':';

    private String id;
    private boolean partial;
    private StringBuilder log;

    public ProgressLog(String id, boolean isPartial){
        this.id = id;
        this.partial = isPartial;
        log = new StringBuilder();
    }
    
    public ProgressLog(String id){
        this(id, false);
    }
    
    public ProgressLog(int id, boolean isPartial){
        this(String.valueOf(id), false);
    }
    
    public ProgressLog(int id) {
        this(String.valueOf(id), false);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void appendToLog(String message){
        int i = message.indexOf(TAG_DELIMITER);
        if(i != -1){
            int tag = Utils.parseInteger(message.substring(0, i));
            log.append((Status.values()[tag]).toString());
            log.append(message.substring(i+1));
        }
        log.append(message);

    }
    
    public void setLog(String log){
        this.log = new StringBuilder();
        appendToLog(log);
    }
    
    public void appendToLog(String message, Status tag){
        log.append(tag.toString());
        log.append(message);
    }
    
    public void setLog(String log, Status tag){
        this.log = new StringBuilder();
        appendToLog(log, tag);
    }

    public void setPartial(boolean b) {
        this.partial = b;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getLog(){
        return log.toString();
    }
    
    public String getLogWithID(){
        return getLogWithID(DEFAULT_ID_MASK);
    }
    
    public String getLogWithID(String mask){
        return String.format(mask, id, getLog());
    }
    
    public String getId(){
        return id;
    }

    public boolean isPartial() {
        return partial;
    }
    // </editor-fold>

}
