package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.BoundsException;
import com.mycompany.imagedownloader.model.GoogleTask;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

/** References:
 * https://www.codejava.net/java-se/swing/jcheckbox-basic-tutorial-and-examples
 * https://stackoverflow.com/questions/9882845/jcheckbox-actionlistener-and-itemlistener/17576273
 */
public class GooglePanel extends TaskPanel {
    private static final long serialVersionUID = 1L;
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String TITLE = "Google";
    private static final String INVALID_DESTINATION_TITLE = "Invalid Folder";
    private static final String INVALID_DESTINATION_MSG = "Please verify if the destination folder is valid.\n";
    private static final String INVALID_NUMBER_TITLE = "Invalid Numbering Bounds";
    private static final String INVALID_NUMBER_MSG = "Please verify if file index is not negative and it is lower than the number of images in the source folder.\n";
    private static final String INVALID_SOURCE_TITLE = "Invalid/Empty Folder";
    private static final String INVALID_SOURCE_MSG = "Please verify if the source folder is valid and contain supported images.\n";
    private static final String DESCRIPTION_MASK = "%s [%d:%d] -> %s\n"; //source, start, end, destination
    // </editor-fold>

    public GooglePanel() {
        super(TITLE);
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flcFolder = new javax.swing.JFileChooser();
        btnDest = new javax.swing.JButton();
        txfDest = new com.mycompany.imagedownloader.view_controller.PathField(com.mycompany.imagedownloader.view_controller.PathField.DIRECTORIES_ONLY, 50);
        chbSize = new javax.swing.JCheckBox();
        btnSource = new javax.swing.JButton();
        txfSource = new com.mycompany.imagedownloader.view_controller.PathField(com.mycompany.imagedownloader.view_controller.PathField.DIRECTORIES_ONLY, 45);
        txfStart = new com.mycompany.imagedownloader.view_controller.NumberField();
        btnAdd = new javax.swing.JButton();
        sclTasks = new javax.swing.JScrollPane();
        txaTasks = new javax.swing.JTextArea();

        flcFolder.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setPreferredSize(new java.awt.Dimension(510, 270));

        btnDest.setText("Destination");
        btnDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDestActionPerformed(evt);
            }
        });

        chbSize.setSelected(true);
        chbSize.setText("Check size...");
        chbSize.setToolTipText("<html>If checked, images with bigger dimensions but<br>\n<b>smaller filesize</b> will be saved in a <b>subfolder</b>,<br>\nand another try is performed.<br>\n<i>Can generate a lot of copies of the same image</i></html>");
        chbSize.setIconTextGap(6);
        chbSize.setMargin(new java.awt.Insets(2, 0, 2, 0));

        btnSource.setText("Source");
        btnSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceActionPerformed(evt);
            }
        });

        txfSource.setPreferredSize(new java.awt.Dimension(256, 22));

        txfStart.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfStart.setText("0");
        txfStart.setPreferredSize(new java.awt.Dimension(35, 22));

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        txaTasks.setEditable(false);
        txaTasks.setColumns(20);
        txaTasks.setRows(2);
        sclTasks.setViewportView(txaTasks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sclTasks)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txfSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txfStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txfDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                            .addComponent(chbSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDest)
                    .addComponent(txfDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chbSize))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnSource)
                    .addComponent(txfStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txfSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        txfDest.selector(this);
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if(listener == null) return;
        GoogleTask task = new GoogleTask();
        //SET DESTINATION
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

        //SET SOURCE
        try {
            task.setSource(txfSource.getText());
            txfStart.setMaxValue(task.getImageCount());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, 
                INVALID_SOURCE_MSG+ex.getMessage(), 
                INVALID_SOURCE_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        } 

        //SET START INDEX
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

        task.setRetrySmall(chbSize.isSelected());

        //ALL VALID
        txfSource.clear();
        txfStart.clear();
        appendTaskDescription(
                String.format(
                        DESCRIPTION_MASK, 
                        task.getSource(), 
                        task.getStartIndex(), 
                        task.getImageCount(), 
                        task.getDestination()
                ));
        listener.taskCreated(task);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceActionPerformed
        txfSource.selector(this);
    }//GEN-LAST:event_btnSourceActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDest;
    private javax.swing.JButton btnSource;
    private javax.swing.JCheckBox chbSize;
    private javax.swing.JFileChooser flcFolder;
    private javax.swing.JScrollPane sclTasks;
    private javax.swing.JTextArea txaTasks;
    private com.mycompany.imagedownloader.view_controller.PathField txfDest;
    private com.mycompany.imagedownloader.view_controller.PathField txfSource;
    private com.mycompany.imagedownloader.view_controller.NumberField txfStart;
    // End of variables declaration//GEN-END:variables

    private void appendTaskDescription(String taskDescription){
        txaTasks.setText(txaTasks.getText()+taskDescription);
    }
    
    @Override
    public void setEditable(boolean b) {
        btnAdd.setEnabled(b);
    }

    @Override
    public void reset() {
        setEditable(true);
        txaTasks.setText("");
    }
    
}
