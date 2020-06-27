package rubensandreoli.imagedownloader.tasks;

/** References:
 * https://howtodoinjava.com/java/string/4-ways-to-split-tokenize-strings-in-java/
 */
public class ProgressLog {
    
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String CRITICAL = "CRITICAL";

    private static final String TAG_DELIMITER = ": ";

    private int workload;
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
    
    public void appendToLog(String message, String tag){
        log.append(tag).append(TAG_DELIMITER).append(message);
    }
    
    public void setLog(String log, String tag){
        this.log = new StringBuilder();
        appendToLog(log, tag);
    }

    public void setPartial(boolean b) {
        this.partial = b;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getMessages(){
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
