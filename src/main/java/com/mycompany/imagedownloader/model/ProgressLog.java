package com.mycompany.imagedownloader.model;

/** References:
 * https://howtodoinjava.com/java/string/4-ways-to-split-tokenize-strings-in-java/
 */
public class ProgressLog {
    
    public enum Status{
        INFO, WARNING, ERROR, CRITICAL;

        @Override
        public String toString() {
            return this.name()+TAG_DELIMITER;
        }
    }
    
    private static final String TAG_DELIMITER = ": ";

    private int number;
    private boolean partial;
    private StringBuilder log;

    public ProgressLog(int number, boolean isPartial){
        this.number = number;
        this.partial = isPartial;
        log = new StringBuilder();
    }

    public ProgressLog(int number) {
        this(number, false);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void appendToLog(String message){
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

    public int getNumber(){
        return number;
    }

    public boolean isPartial() {
        return partial;
    }
    // </editor-fold>

}
