package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.Task;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/** References:
 * https://stackoverflow.com/questions/16343098/resize-a-picture-to-fit-a-jlabel
 * @author Morus
 */
public class TaskTable extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    
    private static final String[] columns =  new String [] {"Type", "", "Description"};
    private TableModel model = new TableModel();
    private List<TaskAdapter> tasks = new LinkedList<>();
    private TaskTableListener listener;
    
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
    public class TableModel extends AbstractTableModel{
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
            return columns[column];
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
    
    // <editor-fold defaultstate="collapsed" desc=" TABLE CELL RENDERER "> 
    public static class TaskCellRenderer implements TableCellRenderer {

        private JLabel label;
        
        public TaskCellRenderer(){
            label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.BOTTOM);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Task task = (Task) value;
            switch(task.getStatus()){
                case RUNNING:
                    label.setIcon(new ImageIcon(getClass().getResource("/download.png")));
                    break;
                case INTERRUPTED:
                    label.setIcon(new ImageIcon(getClass().getResource("/close.png")));
                    break;
                case COMPLETED:
                    label.setIcon(new ImageIcon(getClass().getResource("/checkmark.png")));
                    break;
                default:
                    label.setIcon(null);
            }
            return label;
        }
    
    }
    // </editor-fold>
    
    public TaskTable(){
        this(null);   
    }
    
    public TaskTable(TaskTableListener listener) {
        this.listener = listener;
        initComponents();
        tblTasks.getColumnModel().getColumn(0).setMinWidth(60);
        tblTasks.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblTasks.getColumnModel().getColumn(1).setMinWidth(22);
        tblTasks.getColumnModel().getColumn(1).setPreferredWidth(22);
        tblTasks.getColumnModel().getColumn(2).setPreferredWidth(400);
        tblTasks.setDefaultRenderer(Task.class, new TaskCellRenderer());
        tblTasks.setRowHeight(18);
    }
     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sclTasks = new javax.swing.JScrollPane();
        tblTasks = new javax.swing.JTable();

        sclTasks.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 1), javax.swing.BorderFactory.createEtchedBorder()));
        sclTasks.setToolTipText("<html>Select rows and press the <br>\n<b>delete key to remove</b> a task.</html>");

        tblTasks.setModel(model);
        tblTasks.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblTasks.getTableHeader().setReorderingAllowed(false);
        tblTasks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tblTasksKeyPressed(evt);
            }
        });
        sclTasks.setViewportView(tblTasks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(sclTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sclTasks, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblTasksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblTasksKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_DELETE){
            for (TaskAdapter adapter : getSelected(tblTasks.getSelectedRows())) {
                if(listener.taskRemoved(adapter.task)){
                    tasks.remove(adapter);
                }
            }
            refresh();
        }
    }//GEN-LAST:event_tblTasksKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane sclTasks;
    private javax.swing.JTable tblTasks;
    // End of variables declaration//GEN-END:variables

    public void addTask(TaskAdapter taskAdapter){
        tasks.add(taskAdapter);
        int rows = model.getRowCount();
        model.fireTableRowsInserted(rows, rows);
    }
    
    public void addTask(String title, Task task, String description){
        addTask(new TaskAdapter(title, task, description));
    }
    
    public void setEditable(boolean b){
//        tblTasks.setRowSelectionAllowed(b);
        tblTasks.setEnabled(b);
    }
    
    public void refresh(){
        model.fireTableDataChanged();
    }
    
    public void clear(){
       int size = tasks.size();
       tasks = new LinkedList<>();
       model.fireTableRowsDeleted(0, size);
    }
    
    public void clearSelection() {
        tblTasks.clearSelection();
    }
    
    private List<TaskAdapter> getSelected(int[] rows){
        List<TaskAdapter> selectedAdapters = new ArrayList<>();
        for (int row : rows) {
            selectedAdapters.add(tasks.get(row));
        }
        return selectedAdapters;
    }

    public void setListener(TaskTableListener listener) {
        this.listener = listener;
    }
}
