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
package rubensandreoli.commons.utils;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.others.Logger;

public class SwingUtils {
    
    private SwingUtils(){}
    
    public static void addClickableLink(String url, Component c, boolean log){
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String os = System.getProperty("os.name").toLowerCase();
                final Runtime runtime = Runtime.getRuntime();
                IOException exception = null;

                if(os.contains("win")){
                    try { runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                    } catch (IOException ex) {exception = ex;}
                }else if(os.contains("mac")){
                    try { runtime.exec("open " + url);
                    } catch (IOException ex) {exception = ex;}
                }else if(os.contains("nix") || os.contains("nux")){
                    try { runtime.exec("xdg-open " + url);
                    } catch (IOException ex) {exception = ex;}
                }

                if(exception != null && log) Logger.log.print(Level.WARNING, "failed opening donate link on "+os, exception);
            }
        });
    }
    
}
