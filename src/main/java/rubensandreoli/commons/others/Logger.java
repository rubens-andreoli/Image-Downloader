package rubensandreoli.commons.others;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {
    
    public enum Level{WARNING, SEVERE, INFO}
    
    public static final String FILENAME = "crash.log";
    
    private static final String SEPARATOR_MASK = "-------------//----------------";
    private static final String LOG_MASK = "[%s]\r\ndate: %s\r\ncomment: %s\r\nmessage: %s\r\nstack trace: %s\r\n"+SEPARATOR_MASK;
    private static final File FILE = new File(FILENAME);
    
    public static final Logger log = new Logger(); //eager initialization;
    
    private boolean enabled;
    
    private Logger(){};

    public void print(Level lvl, String comment, Exception ex){
        if(!enabled) return;
        final StringBuilder trace = new StringBuilder();
        for (StackTraceElement e : ex.getStackTrace()) {
            trace.append(e.toString()).append("\n");
        }
        try(var bw = new BufferedWriter(new FileWriter(FILE, true))){
            bw.write(String.format(LOG_MASK, lvl.toString(), LocalDateTime.now(), comment, ex.getMessage(), trace));
        } catch (IOException ex1) {}
    }
    
    public void print(Level lvl, Exception ex){
        print(lvl, "", ex);
    }
    
    public void print(Exception ex){
        print((ex instanceof RuntimeException? Level.SEVERE : Level.INFO), ex);
    }
    
    public void setEnabled(boolean b){
        this.enabled = b;
    }
    
}
