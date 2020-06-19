package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.Configs;
import com.mycompany.imagedownloader.model.ProgressLog;
import com.mycompany.imagedownloader.model.Task;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/** References:
 * http://www.java2s.com/Code/Java/Swing-JFC/DisplayingthePercentageDoneonaJProgressBarComponent.htm
 * https://stackoverflow.com/questions/2973643/shutdown-windows-with-java
 * https://stackoverflow.com/questions/32228345/run-java-function-every-hour
 */
public class ImageDownloader extends javax.swing.JFrame implements TaskPanelListener {
    private static final long serialVersionUID = 1L;

    private static final String COMPLETE_TITLE = "Tasks Completed";
    private static final String COMPLETE_MSG = "All available files were downloaded.";
    private static final String LOG_FILE = "history.log";
    private static final int SHUTDOWN_EVENT = 101;
    private static final int SHUTDOWN_SECONDS = 10;
    
    private List<Task> tasks = new ArrayList<>();
    private List<TaskPanel> taskPanels = new ArrayList<>();
    private SwingWorker<Boolean, ProgressLog> worker;
    private Task currentTask;
    private boolean running;
    private ScheduledExecutorService logger;

    public ImageDownloader() { 
        initComponents();
        txaLog.setSize(Configs.values.get("log_size", RecycledTextArea.DEFAULT_MAX_SIZE, RecycledTextArea.MIN_SIZE));
        loadLog();
        int minutes = Configs.values.get("log_timer", 10, 0);
        if(minutes != 0){
            logger = Executors.newSingleThreadScheduledExecutor();
            logger.scheduleAtFixedRate(this::saveLog, minutes, minutes, TimeUnit.MINUTES);
        }
        
        // <editor-fold defaultstate="collapsed" desc=" SSL TRUST MANAGER "> 
	// Create a new trust manager that trust all certificates
	TrustManager[] trustAllCerts = new TrustManager[]{
	    new X509TrustManager() {
                @Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		    return null;
		}
                @Override
		public void checkClientTrusted(
		    java.security.cert.X509Certificate[] certs, String authType) {
		}
                @Override
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
        chkShutdown = new javax.swing.JCheckBox();

        flcFolder.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Image Downloader");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

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

        chkShutdown.setToolTipText("<html><b>Shutdown</b> after completion.<br>\n<i>Shutdown will occur automatically, <br>\nwithout any warning.</i></html>");
        chkShutdown.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        chkShutdown.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sclLog)
                    .addComponent(pnlTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnStart)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pgbTasks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkShutdown)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlTab, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnStart, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chkShutdown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStop)
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
        if(tasks.isEmpty()) return; //TODO: warning no tasks
        txaLog.clear();
        lockUI();
        
        //PERFORM TASKS IN ANOTHER THREAD AND UPDATE UI
        worker = new SwingWorker<>() {
            
            private int counter;
            
            @Override
            protected Boolean doInBackground() throws Exception {
		running = true;
                for (Task task : tasks) {
                    if(!running) return false;
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
                boolean canceled = true;
                try {
                    canceled = !get();
                } catch (InterruptedException | ExecutionException ex) {
                    System.err.println("ERROR: Failed getting worker result "+ex.getMessage());
                }
                pgbTasks.setValue(pgbTasks.getMaximum());
                pgbTasks.setToolTipText(null);
                saveLog();
                JOptionPane.showMessageDialog(
                        ImageDownloader.this,
                        COMPLETE_MSG,
                        COMPLETE_TITLE,
                        JOptionPane.INFORMATION_MESSAGE
                );
                currentTask = null;
                unlockUI();
                if(!canceled && chkShutdown.isSelected()){
                    ImageDownloader.this.formWindowClosing(new WindowEvent(ImageDownloader.this, SHUTDOWN_EVENT));
                }
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

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(worker != null) worker.cancel(true);
        if(logger != null) logger.shutdownNow();
        saveLog();
        Configs.values.save();
        if(evt.getID() == SHUTDOWN_EVENT){
            try {
                Runtime.getRuntime().exec("shutdown -s -t "+SHUTDOWN_SECONDS);
                dispose();
            } catch (IOException ex) {
                System.err.println("ERROR: Failed to shutdown "+ex.getMessage());
            }
        }else{
            dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JCheckBox chkShutdown;
    private javax.swing.JFileChooser flcFolder;
    private javax.swing.JProgressBar pgbTasks;
    private javax.swing.JTabbedPane pnlTab;
    private javax.swing.JScrollPane sclLog;
    private com.mycompany.imagedownloader.view_controller.RecycledTextArea txaLog;
    // End of variables declaration//GEN-END:variables

    private void loadLog(){
        try(var is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(LOG_FILE)))){
            txaLog.setTexts((LinkedList<String>) is.readObject());
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("ERROR: Failed loading log file "+ex.getMessage());
        }
    }
    
    private void saveLog(){
        try(var os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(LOG_FILE)))){
            os.writeObject(txaLog.getTexts());
        } catch (IOException ex) {
            System.err.println("ERROR: Failed saving log file "+ex.getMessage());
        }
    }

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
