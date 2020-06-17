package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.ProgressLog;
import com.mycompany.imagedownloader.model.Task;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/** References:
 * http://www.java2s.com/Code/Java/Swing-JFC/DisplayingthePercentageDoneonaJProgressBarComponent.htm
 */
public class ImageDownloader extends javax.swing.JFrame implements TaskPanelListener{
    private static final long serialVersionUID = 1L;

    private List<Task> tasks = new ArrayList<>();
    private List<TaskPanel> taskPanels = new ArrayList<>();
    private Task currentTask;
    private boolean running;
    
    public ImageDownloader() {
	initComponents();

//        pnlTab.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        // <editor-fold defaultstate="collapsed" desc=" SSL TRUST MANAGER "> 
	// Create a new trust manager that trust all certificates
	TrustManager[] trustAllCerts = new TrustManager[]{
	    new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		    return null;
		}
		public void checkClientTrusted(
		    java.security.cert.X509Certificate[] certs, String authType) {
		}
		public void checkServerTrusted(
		    java.security.cert.X509Certificate[] certs, String authType) {
		}
	    }
	};
	
	// Activate the new trust manager
	try {
	    SSLContext sc = SSLContext.getInstance("SSL");
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	} catch (Exception ex) {}
        // </editor-fold>
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flcFolder = new javax.swing.JFileChooser();
        btnStart = new javax.swing.JButton();
        pgbTasks = new javax.swing.JProgressBar();
        pnlTab = new javax.swing.JTabbedPane();
        btnStop = new javax.swing.JButton();
        sclLog = new javax.swing.JScrollPane();
        txaLog = new com.mycompany.imagedownloader.view_controller.RecycledTextArea();

        flcFolder.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Image Downloader");
        setResizable(false);

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        pnlTab.setPreferredSize(new java.awt.Dimension(510, 270));

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        sclLog.setBorder(javax.swing.BorderFactory.createTitledBorder("LOG Messages"));

        txaLog.setColumns(20);
        txaLog.setRows(5);
        txaLog.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        sclLog.setViewportView(txaLog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sclLog)
                    .addComponent(pnlTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pgbTasks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStart)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlTab, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnStart)
                        .addComponent(btnStop))
                    .addComponent(pgbTasks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclLog, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTab.getAccessibleContext().setAccessibleName("Tabs");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        if(tasks.isEmpty()) return;
        lockUI();
        
        //PERFORM TASKS IN ANOTHER THREAD AND UPDATE UI
        SwingWorker<Boolean, ProgressLog> worker = new SwingWorker<>() {
            
            private int counter;
            
            @Override
            protected Boolean doInBackground() throws Exception {
		running = true;
                for (Task task : tasks) {
                    if(!running) break;
                    currentTask = task;
                    task.setProgressListener(m -> {
                        publish(m);
                        if(!m.isPartial()) counter++;
                    });
                    task.start();
                }
                return true;
            }

            @Override
            protected void process(List<ProgressLog> chunks) {
                chunks.forEach(m -> {
                    if(m.isPartial()){
                        txaLog.addText(m.getLog());
                    }else{
                        txaLog.addText(m.getLogWithID());
                    }
                });
                pgbTasks.setValue(counter);
                pgbTasks.setToolTipText(pgbTasks.getValue()+"/"+pgbTasks.getMaximum());
            }

            @Override
            protected void done() {
//                get(); //get doInBackground return
                pgbTasks.setValue(pgbTasks.getMaximum());
                pgbTasks.setToolTipText(null);
                JOptionPane.showMessageDialog(
                        ImageDownloader.this,
                        "All available files were downloaded.",
                        "Tasks Completed",
                        JOptionPane.INFORMATION_MESSAGE
                );
                currentTask = null;
                unlockUI();
            }
            
        };
        worker.execute();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        if(currentTask != null){
            currentTask.stop();
            btnStop.setEnabled(false);
        }
    }//GEN-LAST:event_btnStopActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JFileChooser flcFolder;
    private javax.swing.JProgressBar pgbTasks;
    private javax.swing.JTabbedPane pnlTab;
    private javax.swing.JScrollPane sclLog;
    private com.mycompany.imagedownloader.view_controller.RecycledTextArea txaLog;
    // End of variables declaration//GEN-END:variables

    private void lockUI(){
        taskPanels.forEach(p -> p.setEditable(false));
        btnStart.setEnabled(false);
        int processes = 0;
        for (Task task : tasks) {
            processes += task.getProcessesCount();
        }
        pgbTasks.setMaximum(processes);
        pgbTasks.setValue(0);
    }
    
    private void unlockUI(){
        pgbTasks.setValue(0);
        tasks = new ArrayList<>();
        btnStart.setEnabled(true);
        btnStop.setEnabled(true);
        taskPanels.forEach(p -> p.reset());
    }
    
    @Override
    public void taskCreated(Task task) {
        tasks.add(task);
    }
    
    public void addTaskPanel(TaskPanel panel){
        taskPanels.add(panel);
        panel.setTaskListener(this);
        pnlTab.addTab(panel.getTitle(), panel);
    }
    
}
