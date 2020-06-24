package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.Configs;
import com.mycompany.imagedownloader.model.ProgressLog;
import com.mycompany.imagedownloader.model.Task;
import com.mycompany.imagedownloader.model.Task.Status;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** References:
 * http://www.java2s.com/Code/Java/Swing-JFC/DisplayingthePercentageDoneonaJProgressBarComponent.htm
 * https://stackoverflow.com/questions/2973643/shutdown-windows-with-java
 * https://stackoverflow.com/questions/32228345/run-java-function-every-hour
 */
public class ImageDownloader extends javax.swing.JFrame implements TaskPanelListener, TaskTableListener {
    private static final long serialVersionUID = 1L;
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String COMPLETE_TITLE = "Tasks Completed";
    private static final String COMPLETE_MSG = "All available files were downloaded.";
    private static final String LOG_FILE = "history.log";
    private static final int SHUTDOWN_EVENT_CODE = 101;
    private static final int SHUTDOWN_INTERVAL = 10; //seconds
    private static final String LOG_SIZE_KEY = "log_size";
    private static final String LOG_TIMER_KEY = "log_timer";
    private static final int DEFAULT_LOG_TIMER_INTERVAL = 10; //minutes
    private static final String PROGRESSBAR_TOOLTIP_MASK = "%d/%d";
    private static final String LOG_INVERT_KEY = "log_inverted";
    // </editor-fold>
    
    private List<Task> tasks = new LinkedList<>();
    private List<TaskPanel> taskPanels = new ArrayList<>();
    private SwingWorker<Void, ProgressLog> worker;
    private Task currentTask;
    private ScheduledExecutorService logger;

    public ImageDownloader() { 
        initComponents();
        txaLogs.setSize(Configs.values.get(LOG_SIZE_KEY, RecycledTextArea.DEFAULT_MAX_SIZE, RecycledTextArea.MIN_SIZE));
        txaLogs.setInverted(Configs.values.get(LOG_INVERT_KEY));
        loadLog();
        int minutes = Configs.values.get(LOG_TIMER_KEY, DEFAULT_LOG_TIMER_INTERVAL, 0);
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

        pnlTab = new javax.swing.JTabbedPane();
        pnlTools = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        pgbTasks = new javax.swing.JProgressBar();
        chkShutdown = new javax.swing.JCheckBox();
        tblTasks = new TaskTable(this);
        sclLogs = new javax.swing.JScrollPane();
        txaLogs = new com.mycompany.imagedownloader.view_controller.RecycledTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Image Downloader");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnlTab.setMinimumSize(new java.awt.Dimension(100, 133));

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        chkShutdown.setToolTipText("<html><b>Shutdown computer</b> after completion.<br>\n<i>Shutdown will occur automatically, <br>\nwithout any warning.</i></html>");
        chkShutdown.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        chkShutdown.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        chkShutdown.setMargin(new java.awt.Insets(2, 2, 2, 0));

        javax.swing.GroupLayout pnlToolsLayout = new javax.swing.GroupLayout(pnlTools);
        pnlTools.setLayout(pnlToolsLayout);
        pnlToolsLayout.setHorizontalGroup(
            pnlToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToolsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(btnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pgbTasks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShutdown)
                .addGap(0, 0, 0))
        );
        pnlToolsLayout.setVerticalGroup(
            pnlToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToolsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(pnlToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnStart, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chkShutdown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStop)
                    .addComponent(pgbTasks, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        sclLogs.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, -1), javax.swing.BorderFactory.createTitledBorder("LOG Messages")));

        txaLogs.setColumns(20);
        txaLogs.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        sclLogs.setViewportView(txaLogs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTools, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sclLogs, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tblTasks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlTab, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tblTasks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlTab.getAccessibleContext().setAccessibleName("Tabs");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        if(tasks.isEmpty()) return; //TODO: warning no tasks
        prepare();
        
        //PERFORM TASKS IN ANOTHER THREAD AND UPDATE UI
        worker = new SwingWorker<>() {
            
            private int counter; //TODO: remove this
            
            @Override
            protected Void doInBackground() throws Exception { //return value not being used
                for (Task task : tasks) {
                    if(isCancelled()) break;
                    currentTask = task;
                    task.setProgressListener(l -> {
                        publish(l);
                        if(!l.isPartial()) counter++;
                    });
                    task.start();
                }
                return null;
            }

            @Override
            protected void process(List<ProgressLog> logs) {
                logs.forEach(l -> txaLogs.addText(l.getLog()));
                tblTasks.refresh(); //not efficient
//                ProgressLog lastLog = logs.get(logs.size());
//                if(!lastLog.isPartial()){
//                    pgbTasks.setMaximum(lastLog.getWorkload());
                    pgbTasks.setValue(counter);
                    pgbTasks.setToolTipText(String.format(PROGRESSBAR_TOOLTIP_MASK, pgbTasks.getValue(), pgbTasks.getMaximum()));
//                }
            }

            @Override
            protected void done() {
                currentTask = null;
                cleanup();
                if(!isCancelled() && chkShutdown.isSelected()){
                    ImageDownloader.this.formWindowClosing(new WindowEvent(ImageDownloader.this, SHUTDOWN_EVENT_CODE));
                }
            }
        };
        worker.execute();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        stop(false);
    }//GEN-LAST:event_btnStopActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        stop(true);
        if(logger != null) logger.shutdownNow(); //logger is running even when not task is performed; ineficient
        saveLog();
        Configs.values.save();
        if(evt.getID() == SHUTDOWN_EVENT_CODE){
            try {
                Runtime.getRuntime().exec("shutdown -s -t "+SHUTDOWN_INTERVAL);
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
    private javax.swing.JProgressBar pgbTasks;
    private javax.swing.JTabbedPane pnlTab;
    private javax.swing.JPanel pnlTools;
    private javax.swing.JScrollPane sclLogs;
    private com.mycompany.imagedownloader.view_controller.TaskTable tblTasks;
    private com.mycompany.imagedownloader.view_controller.RecycledTextArea txaLogs;
    // End of variables declaration//GEN-END:variables

    private void stop(boolean interrupt){
        if(worker != null && currentTask != null){
            currentTask.stop();
            worker.cancel(interrupt);
        }
    }
    
    private void loadLog(){
        try(var is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(LOG_FILE)))){
            txaLogs.setTexts((LinkedList<String>) is.readObject());
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("ERROR: Failed loading log file "+ex.getMessage());
        }
    }
    
    private void saveLog(){
        try(var os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(LOG_FILE)))){
            os.writeObject(txaLogs.getTexts());
        } catch (IOException ex) {
            System.err.println("ERROR: Failed saving log file "+ex.getMessage());
        }
    }

    private void prepare(){
        //CLEAR LOGS
        txaLogs.clear();
        //PREVENT CHANGE TO TASKS
        taskPanels.forEach(p -> p.setEditable(false));
        btnStart.setEnabled(false);
        tblTasks.clearSelection();
        tblTasks.setEditable(false);
        //SET PROGRESS BAR
        int processes = 0;
        for (Task task : tasks) {
            processes += task.getProcessesCount();
        }
        pgbTasks.setMaximum(processes);
        pgbTasks.setValue(0);
        pgbTasks.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    private void cleanup(){
        pgbTasks.setCursor(Cursor.getDefaultCursor());
        pgbTasks.setValue(pgbTasks.getMaximum());
        pgbTasks.setToolTipText(null);
        saveLog();
        JOptionPane.showMessageDialog( //TODO: diferent msg is cancelled is true
                ImageDownloader.this,
                COMPLETE_MSG,
                COMPLETE_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
        pgbTasks.setValue(0);
        tasks = new LinkedList<>();
        btnStart.setEnabled(true);
        tblTasks.setEditable(true);
        tblTasks.clear();
        btnStop.setEnabled(true);
        taskPanels.forEach(p -> p.reset());
    }

    public void addTaskPanel(TaskPanel panel){
        taskPanels.add(panel);
        panel.setTaskListener(this);
        pnlTab.addTab(panel.getTitle(), panel);
    }

    @Override
    public void taskCreated(TaskPanel source, Task task, String description) {
        tasks.add(task);
        tblTasks.addTask(source.getTitle(), task, description);
//        System.out.println(taskPanels.get(pnlTab.getSelectedIndex()).getTitle());
    }

    @Override
    public boolean taskRemoved(Task task) {
        if(tasks.contains(task)){ //should contain always
            if(task.getStatus() == Status.WAITING){
                this.tasks.remove(task);
                return true;
            }
            if(task == currentTask){
                currentTask.stop();
            }
        }
        return false;
    }
     
}
