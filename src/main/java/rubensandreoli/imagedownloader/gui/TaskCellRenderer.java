package rubensandreoli.imagedownloader.gui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import rubensandreoli.commons.utils.Utils;
import rubensandreoli.imagedownloader.tasks.Task;

public class TaskCellRenderer implements TableCellRenderer {

    private static final ImageIcon RUNNING_ICON = Utils.loadIcon("download.png");
    private static final ImageIcon INTERRUPTED_ICON = Utils.loadIcon("close.png");
    private static final ImageIcon COMPLETED_ICON = Utils.loadIcon("checkmark.png");

    private final JLabel label = new JLabel();

    public TaskCellRenderer(){
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.BOTTOM);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Task task = (Task) value;
        switch(task.getStatus()){
            case RUNNING:
                label.setIcon(RUNNING_ICON);
                break;
            case INTERRUPTED:
                label.setIcon(INTERRUPTED_ICON);
                break;
            case COMPLETED:
                label.setIcon(COMPLETED_ICON);
                break;
            default:
                label.setIcon(null);
        }
        return label;
    }

}
