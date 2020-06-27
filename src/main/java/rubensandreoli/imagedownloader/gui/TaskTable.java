package rubensandreoli.imagedownloader.gui;

import rubensandreoli.imagedownloader.tasks.Task;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/** References:
 * https://stackoverflow.com/questions/17858132/automatically-adjust-jtable-column-to-fit-content
 * http://www.java2s.com/Tutorial/Java/0240__Swing/Setcolumnwidthbasedoncellrenderer.htm
 */
public class TaskTable extends javax.swing.JTable {
    private static final long serialVersionUID = 1L;
    
    private static final String[] COLUMNS_NAME =  new String [] {"Type", "", "Description"};
    private static final int[] COLUMNS_WIDTH = new int[] {56, 22, 400};
    private static final int ROWS_HEIGHT = 18;
    
    private TableModel model = new TableModel();
    private List<TaskAdapter> tasks = new LinkedList<>();
    
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

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TaskTable(){
        setModel(model);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        
        getColumnModel().getColumn(0).setMinWidth(COLUMNS_WIDTH[0]);
        getColumnModel().getColumn(0).setPreferredWidth(COLUMNS_WIDTH[0]);
        getColumnModel().getColumn(1).setMinWidth(COLUMNS_WIDTH[1]);
        getColumnModel().getColumn(1).setPreferredWidth(COLUMNS_WIDTH[1]);
        getColumnModel().getColumn(2).setPreferredWidth(COLUMNS_WIDTH[2]);
        setRowHeight(ROWS_HEIGHT); 
        
        setDefaultRenderer(Task.class, new TaskCellRenderer());
    }

    public void addTask(TaskAdapter taskAdapter){
        tasks.add(taskAdapter);
        int rows = model.getRowCount();
        model.fireTableRowsInserted(rows, rows);
    }
    
    public void addTask(String title, Task task, String description){
        addTask(new TaskAdapter(title, task, description));
    }
 
    public void refresh(){
        model.fireTableDataChanged();
    }
    
    public void clear(){
       int size = tasks.size();
       tasks = new LinkedList<>();
       model.fireTableRowsDeleted(0, size);
    }

    private List<TaskAdapter> getSelected(int[] rows){
        LinkedList<TaskAdapter> selectedAdapters = new LinkedList<>();
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
                }
            }
        });
    }
 
}
