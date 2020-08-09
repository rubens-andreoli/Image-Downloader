/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rubensandreoli.imagedownloader.gui;

import java.awt.Dimension;
import rubensandreoli.imagedownloader.tasks.Task;

/**
 * TasksPanels act like Task factories, but 
 * instead of asking for an instance from them, 
 * the panel provides one, when created, to the 
 * TaskPanelListener set.
 * 
 * @param <T> type of {@code Task} this panel generates
 * @see Task
 * 
 * @author Rubens A. A. Andreoli
 */
public abstract class TaskPanel<T extends Task> extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;

    private final String title;
    private TaskPanelListener listener;

    public TaskPanel(String title) {
        this(title, null);
    }

    public TaskPanel(final String title, TaskPanelListener listener) {
        this.title = title;
        this.listener = listener;
        final Dimension min = new Dimension(488, 62);
        setPreferredSize(min);
        setMinimumSize(min);
    }
    
    protected boolean fireTaskCreated(T task, String description, Object...args){
        if(listener == null) return false;
        listener.taskCreated(title, task, String.format(description, args));
        return true;
    }
    
    public void setTaskPanelListener(TaskPanelListener listener) {
        this.listener = listener;
    }

    public String getTitle() {
        return title;
    }
    
    public Integer getMnemonic(){
        return null;
    }
    
}
