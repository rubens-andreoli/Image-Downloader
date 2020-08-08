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
package rubensandreoli.commons.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** References:
 * https://stackoverflow.com/questions/3622402/how-to-intercept-keyboard-strokes-going-to-java-swing-jtextfield
 * https://stackoverflow.com/questions/4863850/disable-input-some-symbols-to-jtextfield
 * https://stackoverflow.com/questions/46343616/how-can-i-convert-a-char-to-int-in-java
 * https://stackoverflow.com/questions/4968323/java-parse-int-value-from-a-char
 */
public class NumberField extends javax.swing.JTextField{
    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_INITIAL_VALUE = 0;
    
    private String initialNumber = String.valueOf(DEFAULT_INITIAL_VALUE);
    private String maxNumber;
    private int maxValue;
    
    public NumberField(){
        reset();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                
                if(getText().isEmpty()){ //deleted value
                    clear();
                    return;
                }
                
                final char c = e.getKeyChar();
                if (!Character.isDigit(c)) { //typed is not a number
                    e.consume();
                    return;
                }
                
                final int v = c - '0';
                if(getText().equals("0")) setText("");
                
                if(maxValue > 0){ //max value is set
                    if(v > maxValue){ //typed value is over max
                        e.consume();
                        setText(maxNumber);
                    }else if(Integer.parseInt(getText()+v) > maxValue){ //total is over max
                        setText("");
                    }
                }
            }
        });
    }

    public void reset(){
        setText(initialNumber);
    }
    
    public void clear(){
        setText("0");
    }
    
    public void setValues(int initial, int max){
        if(initial<0 || max <0) throw new IllegalArgumentException("Number fields can only work with positive numbers: "+Math.min(initial, max)+" < 0");
        if(max!=0 && initial>max) throw new IllegalArgumentException("Number fields' initial value must be lower than the maximum: "+initial+" > "+max);
        initialNumber = String.valueOf(initial);
        maxValue = max;
        maxNumber = String.valueOf(max);
    }
    
    public void setValues(String initial, String max){
        this.setValues(Integer.valueOf(initial), Integer.valueOf(max));
    }
    
    public void setMaxValue(int max){
        this.setValues(DEFAULT_INITIAL_VALUE, max);
    }
    
    public void setMaxValue(String max){
        this.setValues(String.valueOf(DEFAULT_INITIAL_VALUE), max);
    }

    public int getInt(){
        return Integer.parseInt(getText());
    }        
    
}
