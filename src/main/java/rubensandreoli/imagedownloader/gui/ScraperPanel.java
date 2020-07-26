package rubensandreoli.imagedownloader.gui;

import java.awt.event.KeyEvent;
import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.ScraperTask;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JOptionPane;

public class ScraperPanel extends TaskPanel {
    private static final long serialVersionUID = 1L;

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String TITLE = "Scraper";
    private static final String INVALID_DESTINATION_TITLE = "Invalid Folder";
    private static final String INVALID_DESTINATION_MSG = "Please verify if the destination folder is valid.\n";
    private static final String INVALID_DEPTH_TITLE = "Invalid Depth";
    private static final String INVALID_DEPTH_MSG = "Please verify if the depth set is lower then the limit.\n";
    private static final String INVALID_URL_TITLE = "Malformed URL";
    private static final String INVALID_URL_MSG = "Please verify if the link provided is valid.\n";
    private static final String DESCRIPTION_MASK = "%s [%d] -> %s\n"; //source, depth, destination
    // </editor-fold>
    
    public ScraperPanel() {
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

        txfUrl.setToolTipText("<html>\n<b>Root URL</b> from where the scraper will try to crawl downloading images.<br>\n<i>Eg.: https://www.site.com/page.html</i>\n</html>");

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        txfNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfNumber.setText("0");
        txfNumber.setToolTipText("<html>" +
            "<b>Depth of the sub-links</b> that the scraper will crawl to.<br>" +
            "<i>Depth limit can be set in the configurations file (default = "+ScraperTask.DEFAULT_DEPTH_LIMIT+")</i>" +
            "</html>");

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDest)
                    .addComponent(txfDest, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txfUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txfNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        txfDest.select(this);
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        ScraperTask task = new ScraperTask();
        
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
        
        try {
            task.setSource(txfUrl.getText());
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, 
                INVALID_URL_MSG+ex.getMessage(), 
                INVALID_URL_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        try {
            task.setDepth(txfNumber.getInt());
        } catch (BoundsException ex) {
            JOptionPane.showMessageDialog(this, 
                INVALID_DEPTH_MSG+ex.getMessage(), 
                INVALID_DEPTH_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        listener.taskCreated(this, task, 
                String.format(
                        DESCRIPTION_MASK, 
                        task.getPath(),
                        task.getDepth(),
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
        return KeyEvent.VK_P;
    }
  
}
