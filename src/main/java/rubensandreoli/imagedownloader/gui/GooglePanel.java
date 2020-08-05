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
import rubensandreoli.imagedownloader.tasks.GoogleTask;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import rubensandreoli.commons.others.Configuration;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.LargerSubtask;
import rubensandreoli.imagedownloader.tasks.MoreSubtask;
import static rubensandreoli.imagedownloader.tasks.MoreSubtask.DEFAULT_LOWER_MARGIN;
import static rubensandreoli.imagedownloader.tasks.MoreSubtask.DEFAULT_MIN_DIMENSION;
import static rubensandreoli.imagedownloader.tasks.MoreSubtask.DEFAULT_UPPER_MARGIN;
import rubensandreoli.imagedownloader.tasks.Searcher;

/** 
 * References:
 https://www.codejava.net/java-se/swing/jcheckbox-basic-tutorial-and-examples
 https://stackoverflow.com/questions/9882845/jcheckbox-actionlistener-and-itemlistener/17576273
 https://stackoverflow.com/questions/17858132/automatically-adjust-jtable-column-to-fit-content
 https://stackoverflow.com/questions/4577792/how-to-reset-jtable/4578501
 https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
 https://stackoverflow.com/questions/1019062/how-to-programmatically-deselect-the-currently-selected-row-in-a-jtable-swing
 */
public class GooglePanel extends DownloadTaskPanel {
    private static final long serialVersionUID = 1L;

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String TITLE = "Google";
    private static final int MNEMONIC = KeyEvent.VK_G;
    private static final String DESCRIPTION_MASK = "%s [%d:%d] -> %s\n"; //source, start, end, destination
    
    private static final String INVALID_DESTINATION_TITLE = "Invalid Folder";
    private static final String INVALID_DESTINATION_MSG = "Please verify if the destination folder is valid.\n";
    private static final String INVALID_NUMBER_TITLE = "Invalid Numbering Bounds";
    private static final String INVALID_NUMBER_MSG = "Please verify if file index is not negative and it is lower than the number of images in the source folder.\n";
    private static final String INVALID_SOURCE_TITLE = "Invalid/Empty Folder";
    private static final String INVALID_SOURCE_MSG = "Please verify if the source folder is valid and contain supported images.\n";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    private static final int FAIL_THREASHOLD;
    private static final int MIN_FILESIZE; //bytes
    private static final double FILESIZE_RATIO; //to source image
    private static final String RESPONSE_LINK_TEXT; //pt-br
    private static final String SUBFOLDER_LARGER;
    private static final String SUBFOLDER_MORE;
    private static final int LOWER_MARGIN;
    private static final int UPPER_MARGIN;
    private static final int MIN_DIMENSION;
    static{
        FAIL_THREASHOLD = Configuration.values.get("google:fail_threashold", GoogleTask.DEFAULT_FAIL_THRESHOLD, 1);
        RESPONSE_LINK_TEXT = Configuration.values.get("google:link_text_marker", Searcher.DEFAULT_LINK_TEXT);
        MIN_FILESIZE = Configuration.values.get("google:filesize_min", GoogleTask.DEFAULT_MIN_FILESIZE, 0);
        
        SUBFOLDER_LARGER = Configuration.values.get("google-large:subfolder_name", "copies");
        FILESIZE_RATIO = Configuration.values.get("google-large:filesize_ratio", LargerSubtask.DEFAULT_FILESIZE_RATIO, LargerSubtask.MIN_FILESIZE_RATIO);
        
        SUBFOLDER_MORE = Configuration.values.get("google-more:subfolder", "more");
        LOWER_MARGIN = Configuration.values.get("google-more:lower_margin", DEFAULT_LOWER_MARGIN, 0);
        UPPER_MARGIN = Configuration.values.get("google-more:upper_margin", DEFAULT_UPPER_MARGIN, 0);
        MIN_DIMENSION = Configuration.values.get("google-more:min_dimension", DEFAULT_MIN_DIMENSION, 0);
    }
    // </editor-fold>

    public GooglePanel() {
        super(TITLE);
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnDest = new javax.swing.JButton();
        txfDest = new rubensandreoli.commons.swing.PathField(rubensandreoli.commons.swing.PathField.DIRECTORIES_ONLY, 60);
        btnSettings = new javax.swing.JPanel();
        tbtSize = new javax.swing.JToggleButton();
        tbtMore = new javax.swing.JToggleButton();
        tbtLarger = new javax.swing.JToggleButton();
        btnSource = new javax.swing.JButton();
        txfSource = new rubensandreoli.commons.swing.PathField(rubensandreoli.commons.swing.PathField.DIRECTORIES_ONLY, 45);
        txfStart = new rubensandreoli.commons.swing.NumberField();
        btnAdd = new javax.swing.JButton();

        btnDest.setText("Destination");
        btnDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDestActionPerformed(evt);
            }
        });

        tbtSize.setIcon(FileUtils.loadIcon("save.png", 22));
        tbtSize.setSelected(true);
        tbtSize.setToolTipText("<html>\nIf checked, images with bigger dimensions but<br>\n<b>smaller filesize</b> will be <b>saved</b> in a <b>subfolder</b>,<br>\nand another try is performed.<br\n<i>Can generate a lot of copies of the same image</i>\n</html>");

        tbtMore.setIcon(FileUtils.loadIcon("plus.png", 22));
        tbtMore.setSelected(true);

        tbtLarger.setIcon(FileUtils.loadIcon("expand.png", 22));
        tbtLarger.setSelected(true);

        javax.swing.GroupLayout btnSettingsLayout = new javax.swing.GroupLayout(btnSettings);
        btnSettings.setLayout(btnSettingsLayout);
        btnSettingsLayout.setHorizontalGroup(
            btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnSettingsLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(tbtLarger, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tbtMore, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tbtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        btnSettingsLayout.setVerticalGroup(
            btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnSettingsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbtLarger, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbtMore, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        btnSource.setText("Source");
        btnSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceActionPerformed(evt);
            }
        });

        txfStart.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfStart.setToolTipText("<html>\n<b>Depth of the sub-links</b> that the scraper will crawl to.<br>\n<i>Depth limit can be set in the configurations file</i>\n</html>");

        btnAdd.setText("Add Task");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txfSource, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfStart, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txfDest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDest)
                    .addComponent(txfDest, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txfStart, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSource)
                        .addComponent(txfSource, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceActionPerformed
        txfSource.select(this);
    }//GEN-LAST:event_btnSourceActionPerformed

    private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
        txfDest.select(this);
    }//GEN-LAST:event_btnDestActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if(!tbtLarger.isSelected() && !tbtMore.isSelected()) return; //TODO: warning
        
        final String source = txfSource.getText();
        String destination = txfDest.getText();
        if(destination.isBlank()){
            destination = source;
            txfDest.setText(destination);
        }
        
        try {
            final GoogleTask task = new GoogleTask(source, new Searcher(RESPONSE_LINK_TEXT));
            
            try {
                task.setDestination(destination);
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
            
            if(tbtLarger.isSelected()){
                final LargerSubtask larger = new LargerSubtask(destination, SUBFOLDER_LARGER);
                larger.setRetrySmall(tbtSize.isSelected());
                larger.setFilesizeRatio(FILESIZE_RATIO);
                task.addSubtask(larger);
            }
            if(tbtMore.isSelected()){
                MoreSubtask more = new MoreSubtask(destination, SUBFOLDER_MORE); 
                more.setLowerMargin(LOWER_MARGIN);
                more.setUpperMargin(UPPER_MARGIN);
                task.addSubtask(more);
            }
            
            notify(task, DESCRIPTION_MASK,
                    task.getSource(),
                    task.getStartIndex(),
                    task.getImageCount(), 
                    task.getDestination()
            );
        
            txfSource.clear();
            txfStart.reset();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this,
                INVALID_SOURCE_MSG+ex.getMessage(),
                INVALID_SOURCE_TITLE, 
                JOptionPane.ERROR_MESSAGE
            );
        } 
    }//GEN-LAST:event_btnAddActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDest;
    private javax.swing.JPanel btnSettings;
    private javax.swing.JButton btnSource;
    private javax.swing.JToggleButton tbtLarger;
    private javax.swing.JToggleButton tbtMore;
    private javax.swing.JToggleButton tbtSize;
    private rubensandreoli.commons.swing.PathField txfDest;
    private rubensandreoli.commons.swing.PathField txfSource;
    private rubensandreoli.commons.swing.NumberField txfStart;
    // End of variables declaration//GEN-END:variables

    @Override
    public Integer getMnemonic() {
        return MNEMONIC;
    }

    @Override
    protected int getFailThreshold() {
        return FAIL_THREASHOLD;
    }

    @Override
    protected int getMinFilesize() {
        return MIN_FILESIZE;
    }
      
}
