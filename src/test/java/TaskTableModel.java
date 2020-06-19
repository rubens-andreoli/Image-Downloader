

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class TaskTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    public static final int COLUMN_COUNT = 4;
    private static final String[] DEFAULT_HEADERS = {"Source", "From", "To", "Destination"};
    
    private List<TaskTableAdapter> tasks;
    private String[] headers;
    
    public TaskTableModel(){
        this(DEFAULT_HEADERS);
    }
    
    public TaskTableModel(String[] headers){
        this.headers = headers;
        tasks = new ArrayList<>();
    }
    
    public void addTask(TaskTableAdapter taskAdapter){
        tasks.add(taskAdapter);
    }
    
    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int col) {
        return headers[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        TaskTableAdapter task = tasks.get(row);
        switch(col){
            case 0:
                return task.getSource();
            case 1:
                return task.getStartValue();
            case 2:
                return task.getEndValue();
            case 3:
                return task.getDestination();
        }
        return null;
    }
    
}

//        ((TaskTableModel) tblTasks.getModel()).addTask(new TaskTableAdapter() {
//            @Override
//            public String getSource() {
//                return task.getSource();
//            }
//
//            @Override
//            public int getStartValue() {
//                return task.getStartIndex();
//            }
//
//            @Override
//            public int getEndValue() {
//                return task.getImageCount();
//            }
//
//            @Override
//            public String getDestination() {
//                return task.getDestination();
//            }
//        });
//        ((TaskTableModel) tblTasks.getModel()).fireTableDataChanged();