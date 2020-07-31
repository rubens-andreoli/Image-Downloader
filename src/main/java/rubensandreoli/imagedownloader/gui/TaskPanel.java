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
