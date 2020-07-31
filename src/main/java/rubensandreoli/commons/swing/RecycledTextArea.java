package rubensandreoli.commons.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.DefaultCaret;

/** References:
 * https://stackoverflow.com/questions/9580457/fifo-class-in-java
 * https://stackoverflow.com/questions/19050211/why-linkedlist-doesnt-have-initialcapacity-in-java
 * https://stackoverflow.com/questions/6961356/list-clear-vs-list-new-arraylistinteger
 * https://www.baeldung.com/java-list-iterate-backwards
 * https://stackoverflow.com/questions/2483572/making-a-jscrollpane-automatically-scroll-all-the-way-down
 */
public class RecycledTextArea extends javax.swing.JTextArea{
    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_MAX_SIZE = 30;
    public static final int MIN_SIZE = 1;
    private static final String TOOLTIP = "<html><b>Double click</b> to <b>clear</b> texts.</html>";
    
//    private String title;
    private LinkedList<String> texts;
    private int size;
    private boolean inverted;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RecycledTextArea(int size) {
        texts = new LinkedList<>();
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
        });
        setToolTipText(TOOLTIP);
    }

    public RecycledTextArea() {
        this(DEFAULT_MAX_SIZE);
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
    
    public void amendText(String text){
        if(texts.isEmpty()) return;
        texts.set(texts.size()-1, text);
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
//        if(title != null) sb.append(title).append("\r\n");
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
        this.size = size<MIN_SIZE? MIN_SIZE:size;
        while(texts.size() > this.size){
            texts.removeFirst();
        }
    }
    
//    public void setTitle(String title) {
//        this.title = title;
//    }

    public List<String> getTexts() {
        return texts;
    }
    
    public void setTexts(List<String> texts){
        texts.forEach(t -> addWithoutPrinting(t));
        printTexts();
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        DefaultCaret caret = (DefaultCaret) getCaret();
        if(inverted){ 
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }else{
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
    }
    
}