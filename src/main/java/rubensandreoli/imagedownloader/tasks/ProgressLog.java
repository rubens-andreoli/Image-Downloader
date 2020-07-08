package rubensandreoli.imagedownloader.tasks;

/** References:
 * https://howtodoinjava.com/java/string/4-ways-to-split-tokenize-strings-in-java/
 */
public class ProgressLog {
    
    // <editor-fold defaultstate="collapsed" desc=" TAGS "> 
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String CRITICAL = "CRITICAL";

    public static final String TAG_DELIMITER = ": ";
    // </editor-fold>

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
        if(args != null) message = String.format(message, args);
        log.append(message);
    }
    
    public void appendLine(String message, Object...args){
        append(message, args);
        log.append("\r\n");
    }
    
    public void append(String tag, String message, Object...args){
        log.append(tag).append(TAG_DELIMITER);
        append(message, args);
    }
 
    public void appendLine(String tag, String message, Object...args){
        log.append(tag).append(TAG_DELIMITER);
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
