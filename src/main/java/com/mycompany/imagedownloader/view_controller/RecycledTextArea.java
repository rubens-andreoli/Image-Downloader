package com.mycompany.imagedownloader.view_controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.JTextArea;

/** References:
 * https://stackoverflow.com/questions/9580457/fifo-class-in-java
 * https://stackoverflow.com/questions/19050211/why-linkedlist-doesnt-have-initialcapacity-in-java
 * https://stackoverflow.com/questions/6961356/list-clear-vs-list-new-arraylistinteger
 */
public class RecycledTextArea extends JTextArea{
    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_MAX_SIZE = 30;
    private static final String TOOLTIP = "Double click to clear.";
    
    private String title;
    private LinkedList<String> texts;
    private int size;

    public RecycledTextArea(int size) {
        texts = new LinkedList<>();
        this.size = size;
        setEditable(false);
        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    clear();
                }
            }
        });
        setToolTipText(TOOLTIP);
    }

    public RecycledTextArea() {
        this(DEFAULT_MAX_SIZE);
    }

    public void addText(String text){
        if(texts.size() > size){
            texts.removeFirst();
        }
        texts.add(text);
        printTexts();
    }

    @Override
    public void setText(String text) {
        clear();
        texts.add(text);
        printTexts();
    }
    
    public void clear(){
        texts = new LinkedList<>(); //texts.clear();
        super.setText("");
    }
    
    private void printTexts(){
        final StringBuilder sb = new StringBuilder();
        if(title != null) sb.append(title).append("\n");
        texts.forEach(t -> sb.append(t));
        super.setText(sb.toString());
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
       
}
