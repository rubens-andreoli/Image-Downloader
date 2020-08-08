/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package rubensandreoli.commons.others;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import rubensandreoli.commons.exceptions.CastException;
import rubensandreoli.commons.utils.BooleanUtils;

public class Configuration {
    
    private static final String COMMENT = "CONFIGURATIONS";
    public static final String FILENAME = "config.xml";
    
    public static final Configuration values = new Configuration(); //eager initialization;
    
    private Properties p;
    private boolean changed;
    
    private Configuration(){
        p = new Properties();
        try(var bis = new BufferedInputStream(new FileInputStream(new File(FILENAME)))){
            p.loadFromXML(bis);
        } catch (IOException ex) {
            Logger.log.print(Level.WARNING, "failed loading config file", ex);
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
        final String v = get(key, String.valueOf(defaultValue));
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
        final String v = get(key, String.valueOf(defaultValue));
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
    
    @Deprecated
    public Boolean get(String key){
        final String v = p.getProperty(key);
        if(v == null){
            put(key, "false");
        }
        return Boolean.valueOf(v);
    }
    
    public Boolean get(String key, boolean defaultValue) {
        final String v = get(key, String.valueOf(defaultValue));
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
                Logger.log.print(Level.WARNING, "failed saving config file", ex);
            }
        }
        return false;
    }
    
    public boolean hasChanged(){
        return changed;
    }
 
}
