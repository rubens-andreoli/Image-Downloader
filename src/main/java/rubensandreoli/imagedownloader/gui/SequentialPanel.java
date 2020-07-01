package rubensandreoli.imagedownloader.gui;

import java.awt.event.KeyEvent;
import rubensandreoli.imagedownloader.tasks.BoundsException;
import rubensandreoli.imagedownloader.tasks.SequentialTask;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/** References:
 * https://stackoverflow.com/questions/26848647/custom-bean-class-for-gui-component-in-netbeans
 * https://docs.oracle.com/javase/tutorial/uiswing/components/textarea.html
 */
public class SequentialPanel extends TaskPanel {
    private static final long serialVersionUID = 1L;

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String TITLE = "Sequential";
    private static final String INVALID_DESTINATION_TITLE = "Invalid Folder";
    private static final String INVALID_DESTINATION_MSG = "Please verify if the destination folder is valid.\n";
    private static final String INVALID_NUMBER_TITLE = "Invalid Numbering Bounds";
    private static final String INVALID_NUMBER_MSG = "Please verify if the marked file number is lower than the target set.\n";
    private static final String INVALID_URL_TITLE = "Malformed URL";
    private static final String INVALID_URL_MSG = "Please verify if the link provided is valid.\n";
    private static final String DESCRIPTION_MASK = "%s [%d:%d] -> %s\n"; //source, start, end, destination
    // </editor-fold>
    
    public SequentialPanel() {
        super(TITLE);
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnDest = new javax.swing.JButton();
        txfUrl = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        txfNumber = new rubensandreoli.commons.swing.NumberField();
        txfDest = new rubensandreoli.commons.swing.PathField(rubensandreoli.commons.swing.PathField.DIRECTORIES_ONLY, 60);

        btnDest.setText("Destination");
        btnDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDestActionPerformed(evt);
            }
        });

        txfUrl.setPreferredSize(new java.awt.Dimension(300, 22));

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        txfNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfNumber.setText("50");
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
                        .addComponent(txfDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txfUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDest)
                    .addComponent(txfDest, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txfUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd)
                    .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        txfDest.select(this);
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        SequentialTask task = new SequentialTask();
        
        try {
            task.setDestination(txfDest.getText());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, 
                INVALID_DESTINATION_MSG+ex.getMessage(), 
                INVALID_DESTINATION_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
        }
        
        try {
            task.setSource(txfUrl.getText());
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(
                this, 
                INVALID_URL_MSG+ex.getMessage(), 
                INVALID_URL_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
        } catch (BoundsException ex) {
            JOptionPane.showMessageDialog(
                this, 
                INVALID_NUMBER_MSG+ex.getMessage(), 
                INVALID_NUMBER_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
        }
        
        try {
            task.setUpperBound(txfNumber.getInt());
        } catch (BoundsException ex) {
            JOptionPane.showMessageDialog(
                this, 
                INVALID_NUMBER_MSG+ex.getMessage(), 
                INVALID_NUMBER_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
        }

        listener.taskCreated(this, task, 
                    String.format(DESCRIPTION_MASK, 
                            task.getPath(), 
                            task.getLowerBound(), 
                            task.getUpperBound(), 
                            task.getDestination()
                    ));
        
        txfUrl.setText("");
    }//GEN-LAST:event_btnAddActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDest;
    private rubensandreoli.commons.swing.PathField txfDest;
    private rubensandreoli.commons.swing.NumberField txfNumber;
    private javax.swing.JTextField txfUrl;
    // End of variables declaration//GEN-END:variables

    @Override
    public Integer getMnemonic() {
        return KeyEvent.VK_S;
    }

}
