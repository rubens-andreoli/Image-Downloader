package rubensandreoli.commons.swing;

import java.awt.Cursor;
import java.awt.Frame;
import java.io.IOException;
import rubensandreoli.commons.utils.FileUtils;

/**
 * https://stackoverflow.com/questions/51660856/open-a-browser-with-java
 */
public class AboutDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;

    public static final String GNU_PUBLIC = "<html><body style=\"text-align:justify\">"
	    + "This program is free software: you can redistribute it and/or modify "
	    + "it under the terms of the GNU General Public License as published by "
	    + "the Free Software Foundation, either version 3 of the License, or "
	    + "any later version."
	    + "<p>This program is distributed in the hope that it will be useful, "
	    + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
	    + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the "
	    + "GNU General Public License for more details.</p>"
	    + "<p>You should have received a copy of the GNU General Public License "
	    + "along with this program.  If not, see http://www.gnu.org/licenses.</p></body></html>";
    
    private StringBuilder atributions;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AboutDialog(Frame parent, String programName, String programDesc, String version, String year, String imageUrl) {
	super(parent);
	initComponents();
        
        setLocationRelativeTo(parent);
        setIconImage(parent.getIconImage());

        lblIcon.setIcon(FileUtils.loadIcon(imageUrl));

        lblProgram.setText(programName);
        lblVersion.setText("Version: "+ version);
        if(programDesc!=null) lblDescription.setText(programDesc);
        lblCopyright.setText("Copyright (C) "+year+"  Rubens A. Andreoli Junior");
        
        txpLicense.setText(
                "<html><body style=\"text-align:justify\">"
                        + GNU_PUBLIC
                        + "</body></html>"
            );
        txpLicense.setCaretPosition(0);
        
        lblHere.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    public AboutDialog(Frame parent, String programName, String version, String year, String imageUrl) {
	this(parent, programName, null, version, year, imageUrl);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblIcon = new javax.swing.JLabel();
        lblProgram = new javax.swing.JLabel();
        spnlLicense = new javax.swing.JScrollPane();
        txpLicense = new javax.swing.JTextPane();
        pnlColor = new javax.swing.JPanel();
        lblDonating = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        lblHere = new javax.swing.JLabel();
        sclAtributions = new javax.swing.JScrollPane();
        txpAtributions = new javax.swing.JTextPane();
        lblCopyright = new javax.swing.JLabel();
        lblVersion = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setModalityType(java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        setResizable(false);

        lblIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lblProgram.setFont(lblProgram.getFont().deriveFont(lblProgram.getFont().getStyle() | java.awt.Font.BOLD, lblProgram.getFont().getSize()+4));

        txpLicense.setEditable(false);
        txpLicense.setContentType("text/html"); // NOI18N
        spnlLicense.setViewportView(txpLicense);

        pnlColor.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.shadow"));

        lblDonating.setForeground(new java.awt.Color(0, 0, 0));
        lblDonating.setText("<html>Please considere <b>donating</b> by clicking</html>");

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        lblHere.setText("<html> <a href=\"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=Q5NUAPVCTC5U4&currency_code=USD&source=url\">here...</a></html>");
        lblHere.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHereMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlColorLayout = new javax.swing.GroupLayout(pnlColor);
        pnlColor.setLayout(pnlColorLayout);
        pnlColorLayout.setHorizontalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDonating, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(lblHere, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addComponent(btnClose)
                .addContainerGap())
        );
        pnlColorLayout.setVerticalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlColorLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(lblDonating, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHere, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        sclAtributions.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(0, -2, 0, -2), javax.swing.BorderFactory.createTitledBorder(null, "Atributions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Times New Roman", 1, 14)))); // NOI18N

        txpAtributions.setBackground(getBackground());
        txpAtributions.setContentType("text/html"); // NOI18N
        txpAtributions.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txpAtributions.setMargin(new java.awt.Insets(0, 6, 2, 6));
        sclAtributions.setViewportView(txpAtributions);

        lblCopyright.setFont(lblCopyright.getFont().deriveFont(lblCopyright.getFont().getSize()-2f));

        lblVersion.setFont(lblVersion.getFont());

        lblDescription.setFont(lblDescription.getFont());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sclAtributions)
                    .addComponent(spnlLicense, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblCopyright, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblProgram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblProgram, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(lblVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCopyright, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnlLicense, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclAtributions, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void lblHereMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHereMouseClicked
        String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=Q5NUAPVCTC5U4&currency_code=USD&source=url";
        String os = System.getProperty("os.name").toLowerCase();
        
        if(os.contains("win")){
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {}
        }else if(os.contains("mac")){
            try {
                Runtime.getRuntime().exec("open " + url);
            } catch (IOException ex) {}
        }else if(os.contains("nix") || os.contains("nux")){
            try {
                Runtime.getRuntime().exec("xdg-open " + url);
            } catch (IOException ex) {}
        }
    }//GEN-LAST:event_lblHereMouseClicked

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JLabel lblCopyright;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblDonating;
    private javax.swing.JLabel lblHere;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblProgram;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlColor;
    private javax.swing.JScrollPane sclAtributions;
    private javax.swing.JScrollPane spnlLicense;
    private javax.swing.JTextPane txpAtributions;
    private javax.swing.JTextPane txpLicense;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setVisible(boolean b) {
	if(b){
            if(atributions!=null){
                //TODO: add to panel
                txpAtributions.setText("<html><body>"+atributions.toString()+"</body></html>");
            }
            btnClose.requestFocus();
        }
        super.setVisible(b);
    }
    
    public AboutDialog addAtribution(String item, String creator, String site){
        if(atributions == null) atributions = new StringBuilder();
        else atributions.append("<br/>");
        atributions.append("<span style=\"font-size:9px;\">")
                .append(item).append(" made by <b>").append(creator)
                .append("</b> from <i>").append(site).append("</i></span>");
        return this;
    }
    
}
