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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.Task;

public class TaskCellRenderer implements TableCellRenderer {

    private static final ImageIcon RUNNING_ICON = FileUtils.loadIcon("images/download.png");
    private static final ImageIcon INTERRUPTED_ICON = FileUtils.loadIcon("images/close.png");
    private static final ImageIcon COMPLETED_ICON = FileUtils.loadIcon("images/checkmark.png");
    private static final ImageIcon CRASHED_ICON = FileUtils.loadIcon("images/attention.png");

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
            case CRASHED:
                label.setIcon(CRASHED_ICON);
            default:
                label.setIcon(null);
        }
        return label;
    }

}
