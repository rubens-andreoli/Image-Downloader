package com.mycompany.imagedownloader.view_controller;

import java.awt.Dimension;
import javax.swing.JPanel;

public abstract class TaskPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final String title;
    protected TaskPanelListener listener;

    public TaskPanel(String title) {
        this(title, null);
    }

    public TaskPanel(final String title, TaskPanelListener listener) {
        this.title = title;
        this.listener = listener;
        setPreferredSize(new Dimension(510, 270));
    }
    
    public abstract void setEditable(boolean b);
    public abstract void clear();

    public void setTaskListener(TaskPanelListener listener) {
        this.listener = listener;
    }

    public String getTitle() {
        return title;
    }
}
