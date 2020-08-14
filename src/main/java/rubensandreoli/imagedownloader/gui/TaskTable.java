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

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import rubensandreoli.imagedownloader.tasks.Task;

/** 
 * References:
 * https://stackoverflow.com/questions/17858132/automatically-adjust-jtable-column-to-fit-content
 * http://www.java2s.com/Tutorial/Java/0240__Swing/Setcolumnwidthbasedoncellrenderer.htm
 * https://stackoverflow.com/questions/34862674/java-how-to-prevent-the-horizontal-auto-scrolling-in-a-jtable-when-clicking-th
 */
public class TaskTable extends javax.swing.JTable {
    private static final long serialVersionUID = 1L;
    
    private static final String TOOLTIP = "<html>Select rows and press the <b>delete key</b><br>"
            + " to <b>remove</b> a task that hasn't been started yet,<br>"
            + " or to <b>stop</b> the current task.</html>";
    private static final String[] COLUMNS_NAME =  new String [] {"Type", "", "Description"};
    private static final int[] COLUMNS_WIDTH = new int[] {56, 22, 400};
    private static final int ROWS_HEIGHT = 18;
    
    private TableModel model = new TableModel();
    private List<TaskAdapter> tasks = createList();
    
    // <editor-fold defaultstate="collapsed" desc=" TASK ADAPTER "> 
    public static class TaskAdapter {
        public final String type;
        public final Task task;
        public final String description;

        public TaskAdapter(final String type, final Task task, final String description) {
            this.type = type;
            this.task = task;
            this.description = description;
        }
        
        private TaskAdapter(Task task){
            this(null, task, null);
        }

        @Override
        public int hashCode() {
            return 133 + Objects.hashCode(this.task);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            return Objects.equals(this.task, ((TaskAdapter) obj).task);
        }    
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" TABLE MODEL "> 
    private class TableModel extends AbstractTableModel{
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return tasks.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS_NAME[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch(columnIndex){
                case 0:
                    return String.class;
                case 1:
                    return Task.class;
                case 2:
                    return String.class;
                default:
                    return null;
            }
        }
 
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TaskAdapter t = tasks.get(rowIndex);
            switch(columnIndex){
                case 0:
                    return t.type;
                case 1:
                    return t.task;
                case 2:
                    return t.description;
                default:
                    return null;
            }
        }
       
    }
    // </editor-fold>

    public TaskTable(){
        setModel(model);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        
        getColumnModel().getColumn(0).setMinWidth(COLUMNS_WIDTH[0]);
        getColumnModel().getColumn(0).setPreferredWidth(COLUMNS_WIDTH[0]);
        getColumnModel().getColumn(0).setResizable(false);
        getColumnModel().getColumn(1).setMinWidth(COLUMNS_WIDTH[1]);
        getColumnModel().getColumn(1).setPreferredWidth(COLUMNS_WIDTH[1]);
        getColumnModel().getColumn(1).setResizable(false);
        getColumnModel().getColumn(2).setPreferredWidth(COLUMNS_WIDTH[2]);
        setRowHeight(ROWS_HEIGHT); 
        
        setDefaultRenderer(Task.class, new TaskCellRenderer());
        setToolTipText(TOOLTIP);
    }
    
    private List<TaskAdapter> createList(){
        return new LinkedList<>();
    }

    public void addTask(TaskAdapter taskAdapter){
        tasks.add(taskAdapter);
        final int rows = model.getRowCount();
        if(!tasks.isEmpty()) setAutoResizeMode(JTable.AUTO_RESIZE_OFF); //after first insert
        model.fireTableRowsInserted(rows, rows);
    }
    
    public void addTask(String title, Task task, String description){
        addTask(new TaskAdapter(title, task, description));
    }
 
    public void refresh(){
        model.fireTableDataChanged();
    }
    
    public void clear(){
       final int oldSize = tasks.size();
       tasks = createList();
       setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
       model.fireTableRowsDeleted(0, oldSize);
    }

    private List<TaskAdapter> getSelected(int[] rows){
        final LinkedList<TaskAdapter> selectedAdapters = new LinkedList<>();
        for (int row : rows) {
            selectedAdapters.addFirst(tasks.get(row)); //add backwards so it's removed from last to active task
        }
        return selectedAdapters;
    }

    public void setListener(TaskTableListener listener) {
        if(getKeyListeners().length != 0/* && listener != getKeyListeners()[0]*/){
            removeKeyListener(getKeyListeners()[0]);
        }
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if(evt.getKeyCode() == KeyEvent.VK_DELETE){
                    for (TaskAdapter adapter : getSelected(getSelectedRows())) {
                        if(listener.taskRemoved(adapter.task)){
                            tasks.remove(adapter);
                        }
                    }
                    refresh();
                    if(tasks.isEmpty())setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                }
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        final Component component = super.prepareRenderer(renderer, row, column);
        int rendererWidth = component.getPreferredSize().width;
        final TableColumn tableColumn = getColumnModel().getColumn(column);
        tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
        return component;
    }
    
    @Override
    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        super.changeSelection(row, 0, toggle, extend);
    }

}
