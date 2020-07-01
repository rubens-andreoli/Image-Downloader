package rubensandreoli.imagedownloader.gui;

import java.awt.Dimension;

/**
 * TasksPanels act like Task factories, but 
 * instead of asking for an instance from them, 
 * the panel provides one, when created, to the 
 * TaskPanelListener set.
 * 
 * @author Rubens A. A. Andreoli
 */
public abstract class TaskPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;

    private final String title;
    protected TaskPanelListener listener;

    public TaskPanel(String title) {
        this(title, null);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TaskPanel(final String title, TaskPanelListener listener) {
        this.title = title;
        this.listener = listener;
        Dimension min = new Dimension(488, 62);
        setPreferredSize(min);
        setMinimumSize(min);
    }
    
    public void setTaskListener(TaskPanelListener listener) {
        this.listener = listener;
    }

    public String getTitle() {
        return title;
    }
    
}
