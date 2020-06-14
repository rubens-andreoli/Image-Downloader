package com.mycompany.imagedownloader.view_controller;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/** References:
 * https://stackoverflow.com/questions/5931261/java-use-stringbuilder-to-insert-at-the-beginning
 * https://stackoverflow.com/questions/12524826/why-should-i-use-deque-over-stack
 * https://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
 * https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
 * https://stackoverflow.com/questions/14189262/fitting-text-to-jtextfield-using
 * https://stackoverflow.com/questions/30987866/java-enforce-textfield-format-ux-00000000
 * https://docs.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
 * https://stackoverflow.com/questions/8075373/file-separator-vs-filesystem-getseparator-vs-system-getpropertyfile-separato
 * https://stackoverflow.com/questions/58631724/paths-get-vs-path-of
 */
public class FileField extends JTextField{
    private static final long serialVersionUID = 1L;
    
    private static final int MIN_LENGTH = 5;
    
    private String file;
    private int length;
    private static JFileChooser chooser;
    
    public FileField(int length){
        this.length = length;
        setEditable(false);
    }
    
    public FileField(){
        this(0);
    }

    private String formatText(){
        if(file == null) return "";
        if((length < MIN_LENGTH) || (file.length() <= length)) return file;

        String root = Path.of(file).getRoot().toString();
//        if(folder.contains("/")){
//            for (int i = 0; i < folder.length(); i++) {
//                if(folder.charAt(i) == '/' && i>2){
//                    if(i+1 < folder.length() && folder.charAt(i+1) == '/'){
//                        root = folder.substring(0, i+2);
//                    }else{
//                        root = folder.substring(0, i+1);
//                    }
//                    break;
//                }
//            }
//        }

        String formated = file.substring(file.length()-length, file.length());
//        System.out.println(formated);
        formated = formated.replaceFirst("([^\\"+File.separator
                +"]{3}(?=\\"+File.separator
                +"))|(.{3})(?=[^\\"+File.separator
                +"]*$)", "..."); //or only (.{3,}?(?=\/))|(.{3})
//        System.out.println(formated);
        if(root != null){
//            System.out.println(root);
            formated = formated.replaceFirst(".{"+root.length()
                    +",}(\\.{3})|(^.{"+(root.length()+3)
                    +",}?(?=\\"+File.separator+"))", Matcher.quoteReplacement(root)+"...");
        }
//        System.out.println(formated);
        return formated;
    }
    
    public void clear(){
        super.setText("");
        file = null;
    }
    
    @Override
    public String getText() {
        return file;
    }
    
    @Override
    public void setText(String file){
        this.file = file.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
        super.setText(formatText());
    }

    public void setLenght(int length) {
        this.length = length;
        super.setText(formatText());
    }
    
    public String setFile(Component parent, int mode){
        if (chooser == null) chooser = new JFileChooser();
        chooser.setFileSelectionMode(mode);
        if(chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
            file = chooser.getSelectedFile().getAbsolutePath();
            return file;
        }
        return null;
    }
    
    public String setFolder(Component parent){
        return setFile(parent, JFileChooser.DIRECTORIES_ONLY);
    }
    
    public String setFile(Component parent){
        return setFile(parent, JFileChooser.FILES_ONLY);
    }
      
}
