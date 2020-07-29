/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rubensandreoli.imagedownloader.tasks;

import java.util.IllegalFormatException;

/** 
 * References:
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
        if(args.length != 0){
            try{
                message = String.format(message, args);
            } catch (IllegalFormatException ex){}
        }
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
