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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {

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
