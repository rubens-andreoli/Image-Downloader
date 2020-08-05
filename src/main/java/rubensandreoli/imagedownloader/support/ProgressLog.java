package rubensandreoli.imagedownloader.support;

import java.util.IllegalFormatException;

/** 
 * References:
 * https://howtodoinjava.com/java/string/4-ways-to-split-tokenize-strings-in-java/
 */
public class ProgressLog {
    
    public enum Tag{INFO, WARNING, ERROR, CRITICAL}
    
    public static final String LEVEL_DELIMITER = ": ";

    private final int number;
    private final int workload;
    private final StringBuilder log;

    public ProgressLog(final int number, final int workload){
        this.number = number;
        this.workload = workload;
        log = new StringBuilder();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void append(String message, Object...args){
        if(args.length != 0){
            try{
                message = String.format(message, args);
            } catch (IllegalFormatException ex){
                System.err.println("failed appending log message "+ex.getMessage()); //don't log, development error
            }
        }
        log.append(message);
    }
    
    public void append(Tag level, String message, Object...args){
        log.append(level).append(LEVEL_DELIMITER);
        append(message, args);
    }
    
    public void appendLine(String message, Object...args){
        append(message, args);
        log.append("\r\n");
    }
 
    public void appendLine(Tag level, String message, Object...args){
        log.append(level).append(LEVEL_DELIMITER);
        appendLine(message, args);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getMessages(){
        return log.toString();
    }

    public int getNumber(){
        return number;
    }

    public int getWorkload() {
        return workload;
    }
    // </editor-fold>

}
