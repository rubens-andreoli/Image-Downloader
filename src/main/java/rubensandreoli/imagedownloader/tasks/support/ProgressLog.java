package rubensandreoli.imagedownloader.tasks.support;

import java.util.IllegalFormatException;
import rubensandreoli.commons.others.Level;

/** 
 * References:
 * https://howtodoinjava.com/java/string/4-ways-to-split-tokenize-strings-in-java/
 * 
 * @author Rubens A. Andreoli Jr.
 */
public class ProgressLog {
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final String LEVEL_DELIMITER = ": ";
    // </editor-fold>

    private final int number;
    private final int workload;
    private final StringBuilder log = new StringBuilder();

    public ProgressLog(int number, int workload){
        this.number = number;
        this.workload = workload;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public ProgressLog append(String message, Object...args){
        if(args.length != 0){
            try{
                message = String.format(message, args);
            } catch (IllegalFormatException ex){
                System.err.println("failed appending log message "+ex.getMessage()); //don't log, development error
            }
        }
        log.append(message);
        return this;
    }
    
    public ProgressLog append(Level level, String message, Object...args){
        log.append(level).append(LEVEL_DELIMITER);
        return append(message, args);
    }
    
    public ProgressLog appendLine(String message, Object...args){
        append(message, args);
        log.append("\r\n");
        return this;
    }
 
    public ProgressLog appendLine(Level level, String message, Object...args){
        log.append(level).append(LEVEL_DELIMITER);
        return appendLine(message, args);
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
    
    public boolean isFirst() {
        return number == 0;
    }
    
    public boolean isLast() {
        return number == workload;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return getMessages();
    }
 
}
