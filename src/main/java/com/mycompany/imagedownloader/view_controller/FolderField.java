package com.mycompany.imagedownloader.view_controller;

import javax.swing.JTextField;

/** References:
 * https://stackoverflow.com/questions/5931261/java-use-stringbuilder-to-insert-at-the-beginning
 * https://stackoverflow.com/questions/12524826/why-should-i-use-deque-over-stack
 * https://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
 * https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
 * https://stackoverflow.com/questions/14189262/fitting-text-to-jtextfield-using
 * https://stackoverflow.com/questions/30987866/java-enforce-textfield-format-ux-00000000
 * https://docs.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
 */
public class FolderField extends JTextField{
    private static final long serialVersionUID = 1L;
    
    private static final int MIN_LENGTH = 5;
    private static final int DEFAULT_MAX_LENGTH = 40;
    
    private String folder;
    private int length;
    
    public FolderField(int length){
        this.length = ajustLength(length);
        setEditable(false);
    }
    
    public FolderField(){
        this(DEFAULT_MAX_LENGTH);
    }
    
    private int ajustLength(int length){
        return length > MIN_LENGTH? length:MIN_LENGTH;
    }
    
    @Override
    public void setText(String folder){
        this.folder = folder.replaceAll("[\\\\]", "/");
        super.setText(formatFolder());
    }

    @Override
    public String getText() {
        return folder;
    }

    private String formatFolder(){
        System.out.println("folder: "+folder);
        if(folder.length()<=length){
            return folder;
        }
        
        String root = null;
        if(folder.contains("/")){
            for (int i = 0; i < folder.length(); i++) {
                if(folder.charAt(i) == '/' && i>2){
                    if(i+1 < folder.length() && folder.charAt(i+1) == '/'){
                        root = folder.substring(0, i+2);
                    }else{
                        root = folder.substring(0, i+1);
                    }
                    break;
                }
            }
        }

        String formated = folder.substring(folder.length()-length, folder.length());
        System.out.println(formated);
        formated = formated.replaceFirst("([^/]{3}(?=/))|(.{3})(?=[^/]*$)", "..."); //or only (.{3,}?(?=\/))|(.{3})
        if(root != null){
            formated = formated.replaceFirst(".{"+root.length()+",}(\\.{3})|(^.{"+(root.length()+3)+",}?(?=/))", root+"...");
        }
        System.out.println(formated);
        return formated;
    }
    
    public void clear(){
        super.setText("");
        folder = null;
    }

    public void setLenght(int length) {
        this.length = ajustLength(length);
        super.setText(formatFolder());
    }
      
}
