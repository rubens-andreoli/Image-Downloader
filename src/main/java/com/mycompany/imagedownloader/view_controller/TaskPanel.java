package com.mycompany.imagedownloader.view_controller;

import java.awt.Dimension;

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
        setMaximumSize(min);
    }
    
    public abstract void setEditable(boolean b);
    
    public void reset() {
        setEditable(true);
    }

    public void setTaskListener(TaskPanelListener listener) {
        this.listener = listener;
    }

    public String getTitle() {
        return title;
    }
    
}
