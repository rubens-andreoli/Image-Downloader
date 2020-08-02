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

import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.JOptionPane;
import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.MoreTask;

/** 
 * References:
 * https://www.codejava.net/java-se/swing/jcheckbox-basic-tutorial-and-examples
 * https://stackoverflow.com/questions/9882845/jcheckbox-actionlistener-and-itemlistener/17576273
 * https://stackoverflow.com/questions/17858132/automatically-adjust-jtable-column-to-fit-content
 * https://stackoverflow.com/questions/4577792/how-to-clear-jtable/4578501
 * https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
 * https://stackoverflow.com/questions/1019062/how-to-programmatically-deselect-the-currently-selected-row-in-a-jtable-swing
 */
public class MorePanel extends TaskPanel {
    private static final long serialVersionUID = 1L;
        
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String TITLE = "More";
    private static final int MNEMONIC = KeyEvent.VK_M;
    private static final String INVALID_DESTINATION_TITLE = "Invalid Folder";
    private static final String INVALID_DESTINATION_MSG = "Please verify if the destination folder is valid.\n";
    private static final String INVALID_NUMBER_TITLE = "Invalid Numbering Bounds";
    private static final String INVALID_NUMBER_MSG = "Please verify if file index is not negative and it is lower than the number of images in the source folder.\n";
    private static final String INVALID_SOURCE_TITLE = "Invalid/Empty Folder";
    private static final String INVALID_SOURCE_MSG = "Please verify if the source folder is valid and contain supported images.\n";
    private static final String DESCRIPTION_MASK = "%s [%d:%d] -> %s\n"; //source, start, end, destination
    // </editor-fold>

    public MorePanel() {
        super(TITLE);
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnDest = new javax.swing.JButton();
        txfDest = new rubensandreoli.commons.swing.PathField(rubensandreoli.commons.swing.PathField.DIRECTORIES_ONLY, 50);
        btnSource = new javax.swing.JButton();
        txfSource = new rubensandreoli.commons.swing.PathField(rubensandreoli.commons.swing.PathField.DIRECTORIES_ONLY, 45);
        txfStart = new rubensandreoli.commons.swing.NumberField();
        btnAdd = new javax.swing.JButton();
        chbLarger = new javax.swing.JCheckBox();

        btnDest.setText("Destination");
        btnDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDestActionPerformed(evt);
            }
        });

        btnSource.setText("Source");
        btnSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceActionPerformed(evt);
            }
        });

        txfStart.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfStart.setText("0");
        txfStart.setToolTipText("<html>\n<b>Start</b> image <b>position</b> within the source folder<br>\n<i>Images as sorted alphabetically</i>\n</html>");

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        chbLarger.setSelected(true);
        chbLarger.setText("Larger...");
        chbLarger.setToolTipText("<html>\nIf checked, will also attempt to download <b>larger copies</b><br>\nof the <b>images</b> in the folder.\n</html>");
        chbLarger.setIconTextGap(6);
        chbLarger.setMargin(new java.awt.Insets(2, 0, 2, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDest)
                    .addComponent(btnSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txfSource, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfStart, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txfDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chbLarger, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txfDest, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chbLarger, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnDest))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnSource)
                    .addComponent(txfStart, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txfSource, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        txfDest.select(this);
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        try {
            MoreTask task = new MoreTask(txfSource.getText());
            
            try {
                task.setDestination(txfDest.getText());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    this, 
                    INVALID_DESTINATION_MSG+ex.getMessage(), 
                    INVALID_DESTINATION_TITLE, 
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            } 
            
            try {
                task.setStartIndex(txfStart.getInt());
            } catch (BoundsException ex) {
                JOptionPane.showMessageDialog(
                    this, 
                    INVALID_NUMBER_MSG+ex.getMessage(), 
                    INVALID_NUMBER_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            task.downloadLarger(chbLarger.isSelected());
            
            txfSource.clear();
            txfStart.clear();
            listener.taskCreated(this, task, 
                    String.format(
                            DESCRIPTION_MASK, 
                            task.getSource(), 
                            task.getStartIndex(), 
                            task.getImageCount(), 
                            task.getDestination()
                    ));
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, 
                INVALID_SOURCE_MSG+ex.getMessage(), 
                INVALID_SOURCE_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
        } 
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceActionPerformed
        txfSource.select(this);
    }//GEN-LAST:event_btnSourceActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDest;
    private javax.swing.JButton btnSource;
    private javax.swing.JCheckBox chbLarger;
    private rubensandreoli.commons.swing.PathField txfDest;
    private rubensandreoli.commons.swing.PathField txfSource;
    private rubensandreoli.commons.swing.NumberField txfStart;
    // End of variables declaration//GEN-END:variables

    @Override
    public Integer getMnemonic() {
        return MNEMONIC;
    }
      
}
