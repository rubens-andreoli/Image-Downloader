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
import rubensandreoli.commons.others.Configuration;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.GoogleTask;
import rubensandreoli.imagedownloader.tasks.LargerSubtask;
import rubensandreoli.imagedownloader.tasks.MoreSubtask;
import rubensandreoli.imagedownloader.tasks.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.support.Searcher;

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
    
    private static final String NO_SUBTASK_MSG = "At least one subtask must selected.";
    private static final String NO_SUBTASK_TITLE = "No Subtasks";
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
    private static final int SEQUENCE_LIMIT;
    private static final double DIMENSION_RATIO;
    private static final boolean SOURCE_NAME;
    static{
        FAIL_THREASHOLD = Configuration.values.get("google:fail_threashold", GoogleTask.DEFAULT_FAIL_THRESHOLD, 0);
        RESPONSE_LINK_TEXT = Configuration.values.get("google:link_text_marker", Searcher.DEFAULT_LINK_TEXT);
        MIN_FILESIZE = Configuration.values.get("google:filesize_min", GoogleTask.DEFAULT_MIN_FILESIZE, 0);
        
        SUBFOLDER_LARGER = Configuration.values.get("google-large:subfolder", "copies");
        FILESIZE_RATIO = Configuration.values.get("google-large:filesize_ratio", LargerSubtask.DEFAULT_FILESIZE_RATIO, LargerSubtask.MIN_FILESIZE_RATIO);
        DIMENSION_RATIO = Configuration.values.get("google-large:dimension_ratio", LargerSubtask.DEFAULT_DIMENSION_RATIO, LargerSubtask.MIN_DIMENSION_RATIO);
        SOURCE_NAME = Configuration.values.get("google-large:source_name", LargerSubtask.DEFAULT_SOURCE_NAME);
        
        SUBFOLDER_MORE = Configuration.values.get("google-more:subfolder", "more");
        LOWER_MARGIN = Configuration.values.get("google-more:lower_margin", MoreSubtask.DEFAULT_LOWER_MARGIN, 0);
        UPPER_MARGIN = Configuration.values.get("google-more:upper_margin", MoreSubtask.DEFAULT_UPPER_MARGIN, 0);
        MIN_DIMENSION = Configuration.values.get("google-more:min_dimension", MoreSubtask.DEFAULT_MIN_DIMENSION, MoreSubtask.MIN_MIN_DIMENSION);
        SEQUENCE_LIMIT = Configuration.values.get("google-more:sequence_limit", MoreSubtask.DEFAULT_SEQUENCE_LIMIT, 0);
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
        tbtLarger = new javax.swing.JToggleButton();
        tbtSize = new javax.swing.JToggleButton();
        tbtMore = new javax.swing.JToggleButton();
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

        txfDest.setToolTipText("<html>\n<i>If left empty, the destination will be the source folder</i>\n</html>");

        tbtLarger.setIcon(FileUtils.loadIcon("images/expand.png", 22));
        tbtLarger.setSelected(true);
        tbtLarger.setToolTipText("<html>\nIf checked, a search for a <b>dimensionally larger copy</b> of each<br>\nsource image, will be performed. If found, it will be <b>saved</b><br>\nin a <b>subfolder</b> of the destination.<br>\n</html>");
        tbtLarger.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tbtLargerItemStateChanged(evt);
            }
        });

        tbtSize.setIcon(FileUtils.loadIcon("images/save.png", 22));
        tbtSize.setSelected(true);
        tbtSize.setToolTipText("<html>\nIf checked, images with larger dimensions but <b>smaller filesize</b><br>\nwill be <b>moved</b> to a <b>copies subfolder</b>, and another try is performed.<br>\n<i>Can generate a lot of copies of the same image</i><br>\n</html>");
        tbtSize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tbtSizeItemStateChanged(evt);
            }
        });

        tbtMore.setIcon(FileUtils.loadIcon("images/plus.png", 22));
        tbtMore.setToolTipText("<html>\nIf checked, sequential images, <b>potentially similar</b>, to the ones<br>\nfound in the reverse search, will be <b>saved</b> in a <b>subfolder</b>.<br>\n<i>Can generate unrelated images</i><br>\n</html>");

        javax.swing.GroupLayout btnSettingsLayout = new javax.swing.GroupLayout(btnSettings);
        btnSettings.setLayout(btnSettingsLayout);
        btnSettingsLayout.setHorizontalGroup(
            btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnSettingsLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(tbtLarger, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tbtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(tbtMore, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        btnSettingsLayout.setVerticalGroup(
            btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnSettingsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tbtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(btnSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tbtLarger, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tbtMore, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        btnSource.setText("Source");
        btnSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceActionPerformed(evt);
            }
        });

        txfStart.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txfStart.setToolTipText("<html>\n<b>Start</b> image <b>position</b> within the source folder.<br>\n<i>Images are sorted alphabetically</i>\n</html>");

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
        if(!tbtLarger.isSelected() && !tbtMore.isSelected()) {
            JOptionPane.showMessageDialog(this,
                    NO_SUBTASK_MSG,
                    NO_SUBTASK_TITLE,
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        final String source = txfSource.getText();
        String destination = txfDest.getText();
        if(destination.isBlank()) destination = source;
        
        try {
            final GoogleTask task = new GoogleTask(source, RESPONSE_LINK_TEXT);
            
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
                final LargerSubtask larger = new LargerSubtask(SUBFOLDER_LARGER);
                larger.setRetrySmall(tbtSize.isSelected());
                larger.setFilesizeRatio(FILESIZE_RATIO);
                larger.setDimensionRatio(DIMENSION_RATIO);
                larger.setSourceName(SOURCE_NAME);
                task.addSubtask(larger);
            }
            if(tbtMore.isSelected()){
                final MoreSubtask more = new MoreSubtask(SUBFOLDER_MORE); 
                more.setLowerMargin(LOWER_MARGIN);
                more.setUpperMargin(UPPER_MARGIN);
                more.setMinDimension(MIN_DIMENSION);
                more.setSequenceLimit(SEQUENCE_LIMIT);
                task.addSubtask(more);
            }
            
            fireTaskCreated(task, DESCRIPTION_MASK,
                    task.getSource(),
                    task.getStartIndex(),
                    task.getImageCount()-1, 
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

    private void tbtLargerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tbtLargerItemStateChanged
        tbtSize.setSelected(tbtLarger.isSelected());
    }//GEN-LAST:event_tbtLargerItemStateChanged

    private void tbtSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tbtSizeItemStateChanged
        if(!tbtLarger.isSelected() && tbtSize.isSelected()) tbtLarger.setSelected(true);
    }//GEN-LAST:event_tbtSizeItemStateChanged
    
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
