package com.mycompany.imagedownloader.view_controller;

import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JTextArea;

/** References:
 * https://stackoverflow.com/questions/9580457/fifo-class-in-java
 */
public class RecycledTextArea extends JTextArea{
    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_MAX_SIZE = 10;
    
    private LinkedList<String> texts;
    private int size;

    public RecycledTextArea(int size) {
        texts = new LinkedList<>();
        this.size = size;
        setEditable(false);
//        setColumns(20);
//        setRows(5);
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
    
    private void printTexts(){
        final StringBuilder sb = new StringBuilder();
        texts.forEach(t -> sb.append(t));
        super.setText(sb.toString());
    }

    @Override
    public void setText(String text) {
        texts.clear();
        texts.add(text);
        printTexts();
    }
    
    
    
}
