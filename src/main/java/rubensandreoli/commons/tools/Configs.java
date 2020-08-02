package rubensandreoli.commons.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import rubensandreoli.commons.exceptions.CastException;
import rubensandreoli.commons.utils.BooleanUtils;

public class Configs {
    
    private static final String COMMENT = "CONFIGURATIONS";
    private static final String FILENAME = "config.xml";
    
    public static final Configs values = new Configs(); //eager initialization;
    
    private Properties p;
    private boolean changed;
    
    private Configs(){
        p = new Properties();
        try(var bis = new BufferedInputStream(new FileInputStream(new File(FILENAME)))){
            p.loadFromXML(bis);
        } catch (IOException ex) {
            System.err.println("ERROR: Failed loading config file "+ex.getMessage());
            
        }
    }
    
    public String get(String key, String defaultValue){
        String v = p.getProperty(key);
        if(v == null){
            put(key, defaultValue);
            v = defaultValue;
        }
        return v;
    }
    
    public Integer get(String key, int defaultValue){
        String v = get(key, String.valueOf(defaultValue));
        try{
            return Integer.parseInt(v);
        }catch(NumberFormatException ex){
            return defaultValue;
        }
    }
    
    public Integer get(String key, int defaultValue, int lowerBound){
        int v = get(key, defaultValue);
        if(v<lowerBound) v = defaultValue;
        return v;
    }
    
    public Integer get(String key, int defaultValue, int lowerBound, int upperBound){
        int v = get(key, defaultValue);
        if(v<lowerBound || v>upperBound) v = defaultValue;
        return v;
    }
    
    public Double get(String key, double defaultValue){
        String v = get(key, String.valueOf(defaultValue));
        try{
            return Double.parseDouble(v);
        }catch(NumberFormatException ex){
            return defaultValue;
        }
    }
    
    public Double get(String key, double defaultValue, double lowerBound){
        double v = get(key, defaultValue);
        if(v<lowerBound) v = defaultValue;
        return v;
    }
    
    public Double get(String key, double defaultValue, double lowerBound, double upperBound){
        double v = get(key, defaultValue);
        if(v<lowerBound || v>upperBound) v = defaultValue;
        return v;
    }
    
    public Boolean get(String key){
        String v = p.getProperty(key);
        if(v == null){
            put(key, "false");
        }
        return Boolean.valueOf(v);
    }
    
    public Boolean get(String key, boolean defaultValue) {
        String v = get(key, String.valueOf(defaultValue));
        try{
            return BooleanUtils.parseBoolean(v);
        }catch(CastException ex){
            return defaultValue;
        }
    }
    
    public void put(String key, String value) {
        p.put(key, value);
        changed = true;
    }

    public boolean save(){
        if(changed){
            try(var bos = new BufferedOutputStream(new FileOutputStream(new File(FILENAME)))){
                p.storeToXML(bos, COMMENT);
                return true;
            } catch (IOException ex) {
                System.err.println("ERROR: Failed saving config file "+ex.getMessage());
            }
        }
        return false;
    }
    
    public boolean hasChanged(){
        return changed;
    }
 
}
