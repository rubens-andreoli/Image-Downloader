package rubensandreoli.commons.swing;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import javax.swing.JFileChooser;

/** References:
 https://stackoverflow.com/questions/5931261/java-use-stringbuilder-to-insert-at-the-beginning
 https://stackoverflow.com/questions/12524826/why-should-i-use-deque-over-stack
 https://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
 https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
 https://stackoverflow.com/questions/14189262/fitting-text-to-jtextfield-using
 https://stackoverflow.com/questions/30987866/java-enforce-textfield-format-ux-00000000
 https://docs.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
 https://stackoverflow.com/questions/8075373/path-separator-vs-filesystem-getseparator-vs-system-getpropertyfile-separato
 https://stackoverflow.com/questions/58631724/paths-get-vs-path-of
 https://stackoverflow.com/questions/811248/how-can-i-use-drag-and-drop-in-swing-to-get-path-path
 * http://zetcode.com/tutorials/javaswingtutorial/draganddrop/
 */
public class PathField extends javax.swing.JTextField{
    private static final long serialVersionUID = 1L;
    
    public static final int FILES_ONLY = JFileChooser.FILES_ONLY;
    public static final int DIRECTORIES_ONLY = JFileChooser.DIRECTORIES_ONLY;
    public static final int FILES_AND_DIRECTORIES = JFileChooser.FILES_AND_DIRECTORIES;
    private static final int MIN_LENGTH = 5;
    
    private final int mode;
    private String path = "";
    private int length;
    private static JFileChooser chooser = new JFileChooser();;
    
    public PathField(int mode, int length){
        this.mode = mode;
        this.length = length;
        setEditable(false);
        enableDrag();
    }
    
    public PathField(int mode){
        this(mode, 0);
    }
    
    public PathField(){
        this(FILES_AND_DIRECTORIES, 0);
    }
    
    private void enableDrag(){
        setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        if ((mode==FILES_AND_DIRECTORIES) || 
                                (mode==FILES_ONLY && file.isFile()) ||
                                (mode==DIRECTORIES_ONLY && file.isDirectory())){
                            chooser.setSelectedFile(new File(file, File.separator));
                            setText(file.getAbsolutePath());
                            fireActionPerformed();
                        }
                        break;
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    System.err.println("ERROR: drag and drop failed "+ex.getMessage());
                }
            }
        });
    }

    private String formatText(){
        if(path.isEmpty()) return "";
        if((length < MIN_LENGTH) || (path.length() <= length)) return path;

        String root = Path.of(path).getRoot().toString();

        String formated = path.substring(path.length()-length, path.length());
        formated = formated.replaceFirst("([^\\"+File.separator
                +"]{3}(?=\\"+File.separator
                +"))|(.{3})(?=[^\\"+File.separator
                +"]*$)", "..."); //or only (.{3,}?(?=\/))|(.{3})
        
        if(root != null){
            formated = formated.replaceFirst(".{"+root.length()
                    +",}(\\.{3})|(^.{"+(root.length()+3)
                    +",}?(?=\\"+File.separator+"))", Matcher.quoteReplacement(root)+"...");
            int index = formated.indexOf(".");
            if(index < root.length()) formated = formated.substring(index);
        }

        return formated;
    }
    
    public void clear(){
        super.setText("");
        path = "";
    }
    
    @Override
    public String getText() {
        return path;
    }
    
    @Override
    public void setText(String path){
        if(path==null){ //no need for further testing
            clear();
            return;
        }
        
        if(mode==DIRECTORIES_ONLY && path.contains(".")) 
            throw new IllegalArgumentException("Pathname doesn't represent a folder.");
        if(mode==FILES_ONLY && !path.contains(".")) 
            throw new IllegalArgumentException("Pathname doesn't represent a file.");
        
        this.path = path.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
        
        super.setText(formatText());
    }

    public void setLenght(int length) {
        this.length = length;
        super.setText(formatText());
    }
    
    private boolean selectFile(Component parent, int mode){
        chooser.setFileSelectionMode(mode);
        if(chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
            String selected = chooser.getSelectedFile().getAbsolutePath();
            if(!selected.isBlank()){
                setText(selected);
                return true;
            }
        }
        return false;
    }
        
    public boolean select(Component parent){
        return selectFile(parent, mode);
    }
    
    public static String select(Component parent, int mode){
        chooser.setFileSelectionMode(mode);
        if(chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
            String selected = chooser.getSelectedFile().getAbsolutePath();
            if(!selected.isBlank()){
                return selected;
            }
        }
        return null;
    }

}
