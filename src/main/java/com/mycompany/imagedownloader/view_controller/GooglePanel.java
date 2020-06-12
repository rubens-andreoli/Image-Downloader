/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.BoundsException;
import com.mycompany.imagedownloader.model.GoogleTask;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

public class GooglePanel extends TaskPanel {
    private static final long serialVersionUID = 1L;

    private GoogleTask task = new GoogleTask();
    
    public GooglePanel() {
        super("Google");
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flcFolder = new javax.swing.JFileChooser();
        btnDest = new javax.swing.JButton();
        txfDest = new javax.swing.JTextField();
        btnFolder = new javax.swing.JButton();
        txfUrl = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        pnlScroll = new javax.swing.JScrollPane();
        txaTasks = new javax.swing.JTextArea();
        txfNumber = new com.mycompany.imagedownloader.view_controller.NumberField();

        flcFolder.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setPreferredSize(new java.awt.Dimension(510, 270));

        btnDest.setText("Destination");
        btnDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDestActionPerformed(evt);
            }
        });

        txfDest.setEditable(false);

        btnFolder.setText("Folder");
        btnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFolderActionPerformed(evt);
            }
        });

        txfUrl.setEditable(false);
        txfUrl.setPreferredSize(new java.awt.Dimension(300, 22));

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        txaTasks.setEditable(false);
        txaTasks.setColumns(20);
        txaTasks.setRows(5);
        pnlScroll.setViewportView(txaTasks);

        txfNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfNumber.setText("0");
        txfNumber.setPreferredSize(new java.awt.Dimension(35, 22));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txfDest)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txfUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDest)
                    .addComponent(txfDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txfUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd)
                    .addComponent(btnFolder)
                    .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        if(flcFolder.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
	    String dest = flcFolder.getSelectedFile().getAbsolutePath();
            try {
                task.setDestination(dest);
                txfDest.setText(dest);
            } catch (IOException ex) {
                System.err.println("dest error");
            }
	}
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if(listener == null) return;
        try {
            task.setStartIndex(txfNumber.getInt());
            txfUrl.setText("");
            listener.taskCreated(task);
            appendTaskDescription(task.getSource()+" ["+task.getStartIndex()+":"+task.getImageCount()+"] -> "+task.getDestination());
        } catch (BoundsException ex) {
            System.err.println("start error");
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFolderActionPerformed
        if(flcFolder.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
	    String source = flcFolder.getSelectedFile().getAbsolutePath();
            try {
                task.setSource(source);
                txfUrl.setText(source);
                txfNumber.setMaxValue(task.getImageCount());
            } catch (IOException ex) {
                System.err.println("source error");
            }
	}
    }//GEN-LAST:event_btnFolderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDest;
    private javax.swing.JButton btnFolder;
    private javax.swing.JFileChooser flcFolder;
    private javax.swing.JScrollPane pnlScroll;
    private javax.swing.JTextArea txaTasks;
    private javax.swing.JTextField txfDest;
    private com.mycompany.imagedownloader.view_controller.NumberField txfNumber;
    private javax.swing.JTextField txfUrl;
    // End of variables declaration//GEN-END:variables

    private void appendTaskDescription(String taskDescription){
        txaTasks.setText(txaTasks.getText()+taskDescription);
    }
    
    @Override
    public void setEditable(boolean isOpen) {
        btnAdd.setEnabled(isOpen);
    }

    @Override
    public void reset() {
        setEditable(true);
        txaTasks.setText("");
        task = new GoogleTask();
        try {
            task.setDestination(txfDest.getText());
        } catch (IOException ex) {
            System.err.println("dest error");
        }
    }
    
}
