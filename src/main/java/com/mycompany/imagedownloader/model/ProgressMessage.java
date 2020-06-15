package com.mycompany.imagedownloader.model;

public class ProgressMessage {
    
    private final int id;
    private StringBuilder text;

    public ProgressMessage(final int id) {
        this.id = id;
        text = new StringBuilder();
    }
    
    public void appendToText(String part){
        text.append(part);
    }
    
    public void setText(String text){
        this.text = new StringBuilder(text);
    }
    
    public String getText(){
        return text.toString();
    }
    
    public String getTextWithID(){
        return "["+id+"]\n"+getText();
    }
    
    public int getId(){
        return id;
    }
    
}
