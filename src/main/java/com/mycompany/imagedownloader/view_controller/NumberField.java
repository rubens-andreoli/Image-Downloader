package com.mycompany.imagedownloader.view_controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;

/** References:
 * https://stackoverflow.com/questions/3622402/how-to-intercept-keyboard-strokes-going-to-java-swing-jtextfield
 * https://stackoverflow.com/questions/4863850/disable-input-some-symbols-to-jtextfield
 * https://stackoverflow.com/questions/46343616/how-can-i-convert-a-char-to-int-in-java
 * https://stackoverflow.com/questions/4968323/java-parse-int-value-from-a-char
 */
public final class NumberField extends JTextField{
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_VALUE = "0";
    private int maxValue;
    
    public NumberField() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                
                if(getText().isEmpty()){ //deleted value
                    setText(DEFAULT_VALUE);
                    return;
                }
                
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) { //typed is not a number
                    e.consume();
                    return;
                }
                
                if(maxValue > 0){
                    try{
                        int number = Integer.parseInt(getText());
                        if(Integer.parseInt(number+""+c) > maxValue){
                            if((c - '0') > maxValue){
                                e.consume();
                            }else{
                                setText("");
                            }
                        }else if(getText().equals(DEFAULT_VALUE)){ //remove 0
                            setText("");
                        }
                    }catch(NumberFormatException ex){
                        System.err.println("Can't parse field value "+ ex.getMessage());
//                        setText(DEFAULT_VALUE);
                    }
                }else if(getText().equals(DEFAULT_VALUE)){ //remove 0
                    setText("");
                }
            }
        });
    }
    
    public NumberField(int maxValue){
        this();
        setMaxValue(maxValue);
    }
    
    public void clear(){
        setText(DEFAULT_VALUE);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue>=0? maxValue:0;
    }
    
    public int getInt(){
        return Integer.parseInt(getText());
    }        
    
}
