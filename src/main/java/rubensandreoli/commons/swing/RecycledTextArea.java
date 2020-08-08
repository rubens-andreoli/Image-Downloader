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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.text.DefaultCaret;

/** 
 * References:
 * https://stackoverflow.com/questions/9580457/fifo-class-in-java
 * https://stackoverflow.com/questions/19050211/why-linkedlist-doesnt-have-initialcapacity-in-java
 * https://stackoverflow.com/questions/6961356/list-clear-vs-list-new-arraylistinteger
 * https://www.baeldung.com/java-list-iterate-backwards
 * https://stackoverflow.com/questions/2483572/making-a-jscrollpane-automatically-scroll-all-the-way-down
 */
public class RecycledTextArea extends javax.swing.JTextArea{
    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_MAX_SIZE = 120;
    public static final int MIN_SIZE = 1;
    private static final String TOOLTIP = "<html><b>Double click</b> to <b>clear</b> texts.</html>";
    
    private LinkedList<String> texts = new LinkedList<>();;
    private int size;
    private boolean inverted;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RecycledTextArea(int size) {
        this.size = size;
        setRows(1);
        setEditable(false);
        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    clear();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lockCaret(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lockCaret(false);
            }
        });
    }
  

    public RecycledTextArea() {
        this(DEFAULT_MAX_SIZE);
    }
    
    public void enableTooltip(boolean b){
        if(b) setToolTipText(TOOLTIP);
        else setToolTipText(null);
    }

    private void addWithoutPrinting(String text){
        if(texts.size() > size){
            texts.removeFirst();
        }
        texts.add(text);
    }

    public void addText(String text){
        addWithoutPrinting(text);
        printTexts();
    }
    
    public boolean amendText(String text){
        if(texts.isEmpty()) return false;
        texts.set(texts.size()-1, text);
        return true;
    }
    
    public boolean amendText(String text, Predicate<String> condition){
        if(texts.isEmpty()) return false;
        final int index = texts.size()-1;
        if(condition.test(texts.get(index))){
            texts.set(index, text);
        }
        return false;
    }

    @Override
    public void setText(String text) {
        clear();
        texts.add(text);
        printTexts();
    }
    
    public void clear(){
        texts.clear();
        super.setText("");
    }
    
    private void printTexts(){
        final StringBuilder sb = new StringBuilder();
        if(inverted){
            for (int i = texts.size(); i-- > 0; ) {
                sb.append(texts.get(i));
            }
        }else{
            texts.forEach(sb::append); //t -> sb.append(t)
        }
        super.setText(sb.toString());
    }
    
    public void setSize(int size){
        this.size = size<MIN_SIZE? MIN_SIZE : size;
        while(texts.size() > this.size){
            texts.removeFirst();
        }
    }

    public List<String> getTexts() {
        return texts;
    }
    
    public void setTexts(List<String> texts){
        texts.forEach(t -> addWithoutPrinting(t));
        printTexts();
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        lockCaret(inverted);
    }
    
    private void lockCaret(boolean b){
        final DefaultCaret caret = (DefaultCaret) getCaret();
        if(b || inverted){ 
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }else{
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
    }
    
}
