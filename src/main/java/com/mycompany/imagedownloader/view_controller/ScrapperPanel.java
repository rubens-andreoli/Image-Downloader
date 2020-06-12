package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.BoundsException;
import com.mycompany.imagedownloader.model.ScrapperTask;
import static com.mycompany.imagedownloader.model.ScrapperTask.DEPTH_LIMIT;
import com.mycompany.imagedownloader.model.Task;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ScrapperPanel extends TaskPanel {
    private static final long serialVersionUID = 1L;

    public ScrapperPanel() {
        super("Scrapper");
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flcFolder = new javax.swing.JFileChooser();
        btnDest = new javax.swing.JButton();
        txfDest = new javax.swing.JTextField();
        txfUrl = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        pnlScroll = new javax.swing.JScrollPane();
        txaTasks = new javax.swing.JTextArea();
        txfNumber = new com.mycompany.imagedownloader.view_controller.NumberField();
        txfNumber.setMaxValue(DEPTH_LIMIT);

        flcFolder.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setPreferredSize(new java.awt.Dimension(510, 270));

        btnDest.setText("Destination");
        btnDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDestActionPerformed(evt);
            }
        });

        txfDest.setEditable(false);

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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnDest)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txfDest))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txfUrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE))
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
                    .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        if(flcFolder.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
	    txfDest.setText(flcFolder.getSelectedFile().getAbsolutePath());
	}
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if(listener == null) return;
        String url = txfUrl.getText();
	String dest = txfDest.getText();
        int val = txfNumber.getInt();
        try{
            ScrapperTask t = new ScrapperTask(url, dest, val);
            txfUrl.setText("");
            listener.taskCreated(t);
            appendTaskDescription(url, val, dest);
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(
                this, 
                ex.getMessage()+"\nPlease verify if the link provided is valid.", 
                "Malformed URL", 
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, 
                ex.getMessage()+"\nPlease verify if the destination folder is valid.", 
                "Invalid Folder", 
                JOptionPane.ERROR_MESSAGE
            );
        } catch (BoundsException ex) {
            JOptionPane.showMessageDialog(
                this, 
                ex.getMessage()+"\nPlease verify if the depth set is lower then the limit: "+ScrapperTask.DEPTH_LIMIT, 
                "Invalid Depth",
                JOptionPane.ERROR_MESSAGE
            );
        }

    }//GEN-LAST:event_btnAddActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDest;
    private javax.swing.JFileChooser flcFolder;
    private javax.swing.JScrollPane pnlScroll;
    private javax.swing.JTextArea txaTasks;
    private javax.swing.JTextField txfDest;
    private com.mycompany.imagedownloader.view_controller.NumberField txfNumber;
    private javax.swing.JTextField txfUrl;
    // End of variables declaration//GEN-END:variables

    private void appendTaskDescription(String url, int val, String destination){
        StringBuilder sb = new StringBuilder(txaTasks.getText());
        sb.append(url).append(" [").append(val).append("] -> ").append(destination).append("\n");
        txaTasks.setText(sb.toString());
    }
    
    @Override
    public void setEditable(boolean isOpen) {
        btnAdd.setEnabled(isOpen);
    }

    @Override
    public void reset() {
        setEditable(true);
        txaTasks.setText("");
    }
    
}
