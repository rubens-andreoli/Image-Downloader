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
import rubensandreoli.commons.exceptions.checked.BoundsException;
import rubensandreoli.imagedownloader.tasks.SequentialTask;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JOptionPane;
import rubensandreoli.commons.others.Configuration;

/** 
 * References:
 * https://stackoverflow.com/questions/26848647/custom-bean-class-for-gui-component-in-netbeans
 * https://docs.oracle.com/javase/tutorial/uiswing/components/textarea.html
 */
public class SequencePanel extends DownloadTaskPanel {
    private static final long serialVersionUID = 1L;

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String TITLE = "Sequence";
    private static final int MNEMONIC = KeyEvent.VK_S;
    private static final String DESCRIPTION_MASK = "%s [%d:%d] -> %s\n"; //source, start, end, destination
    
    private static final String INVALID_DESTINATION_TITLE = "Invalid Folder";
    private static final String INVALID_DESTINATION_MSG = "Please verify if the destination folder is valid.\n";
    private static final String INVALID_NUMBER_TITLE = "Invalid Numbering Bounds";
    private static final String INVALID_NUMBER_MSG = "Please verify if the marked file number is lower than the target set.\n";
    private static final String INVALID_URL_TITLE = "Malformed URL";
    private static final String INVALID_URL_MSG = "Please verify if the link provided is valid.\n";
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    private static final int FAIL_THRESHOLD;
    private static final int MIN_FILESIZE;
    static{
        FAIL_THRESHOLD = Configuration.values.get("sequencial:fails_threshold", SequentialTask.DEFAULT_FAIL_THRESHOLD, 0);
        MIN_FILESIZE = Configuration.values.get("sequencial:filesize_min", SequentialTask.DEFAULT_MIN_FILESIZE, 0);
    }
    // </editor-fold>
    
    public SequencePanel() {
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

        txfUrl.setToolTipText("<html>\nSource image <b>URL</b>, with <b>first number</b> of the sequence between <b>curly brackets</b>.<br>\n<i>Eg.: https://www.site.com/image_{003}.jpg</i><br>\n<i>The maximum number of consecutive failed attempts can be set in<br>\nthe configuration file (a value of '0' means no fail threashold).</i>\n</html>");
        txfUrl.setPreferredSize(new java.awt.Dimension(300, 22));

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        txfNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfNumber.setText("50");
        txfNumber.setToolTipText("<html>\n<b>Last number</b> of the sequence.<br>\n<i>Must be greater than the URL number</i>\n</html>");
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
                        .addComponent(txfUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
        try {
            final SequentialTask task = new SequentialTask(txfUrl.getText(), txfNumber.getInt());
            
            try {
                task.setDestination(txfDest.getText());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    INVALID_DESTINATION_MSG+ex.getMessage(), 
                    INVALID_DESTINATION_TITLE, 
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            notify(task, DESCRIPTION_MASK, 
                    task.getMaskedName(), 
                    task.getLowerBound(), 
                    task.getUpperBound(), 
                    task.getDestination()
            );

            txfUrl.setText("");
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, 
                INVALID_URL_MSG+ex.getMessage(), 
                INVALID_URL_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
        } catch (BoundsException ex) {
            JOptionPane.showMessageDialog(this, 
                INVALID_NUMBER_MSG+ex.getMessage(), 
                INVALID_NUMBER_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
        }
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
        return MNEMONIC;
    }

    @Override
    protected int getFailThreshold() {
        return FAIL_THRESHOLD;
    }

    @Override
    protected int getMinFilesize() {
        return MIN_FILESIZE;
    }

}
