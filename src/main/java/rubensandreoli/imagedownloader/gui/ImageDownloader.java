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

import rubensandreoli.commons.swing.RecycledTextArea;
import rubensandreoli.commons.tools.Configs;
import rubensandreoli.imagedownloader.tasks.ProgressLog;
import rubensandreoli.imagedownloader.tasks.Task;
import rubensandreoli.imagedownloader.tasks.Task.Status;
import rubensandreoli.commons.utils.FileUtils;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import rubensandreoli.commons.swing.AboutDialog;

/** 
 * References:
 * http://www.java2s.com/Code/Java/Swing-JFC/DisplayingthePercentageDoneonaJProgressBarComponent.htm
 * https://stackoverflow.com/questions/2973643/shutdown-windows-with-java
 * https://stackoverflow.com/questions/32228345/run-java-function-every-hour
 * https://stackoverflow.com/questions/37117470/how-to-loop-arraylist-from-one-thread-while-adding-to-it-from-other-thread/37117674
 * https://stackoverflow.com/questions/1426754/linkedblockingqueue-vs-concurrentlinkedqueue
 * https://stackoverflow.com/questions/1614772/how-to-change-jframe-icon
 * https://stackoverflow.com/questions/3965336/how-to-minimize-a-jframe-window-from-java
 */
public class ImageDownloader extends javax.swing.JFrame implements TaskPanelListener, TaskTableListener {
    private static final long serialVersionUID = 1L;
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String COMPLETE_TITLE = "Tasks Completed";
    private static final String COMPLETE_MSG = "All available files were downloaded.";
    private static final String ABORTED_TITLE = "Tasks Aborted";
    private static final String ABORTED_MSG = "All remaining tasks were stopped.";
    private static final String LOG_FILE = "history.log";
    private static final int SHUTDOWN_EVENT_CODE = 101;
    private static final int SHUTDOWN_INTERVAL = 10; //seconds
    private static final String LOG_SIZE_KEY = "log_size";
    private static final String LOG_TIMER_KEY = "log_timer";
    private static final int DEFAULT_LOG_TIMER_INTERVAL = 10; //minutes
    private static final String LOG_INVERT_KEY = "log_inverted";
    private static final String PROGRESSBAR_TOOLTIP_MASK = "%d/%d"; //progress, total
    private static final String PROGRAM_ICON = "download_image.png";
    // </editor-fold>
    
    private ConcurrentLinkedDeque<Task> tasks = new ConcurrentLinkedDeque<>();
    private SwingWorker<Void, ProgressLog> worker;
    private Task currentTask;
    private ScheduledExecutorService logger;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ImageDownloader() { 
        initComponents();
        
        setIconImage(FileUtils.loadIcon(PROGRAM_ICON).getImage());
        
        txaLogs.setSize(Configs.values.get(LOG_SIZE_KEY, RecycledTextArea.DEFAULT_MAX_SIZE, RecycledTextArea.MIN_SIZE));
        txaLogs.setInverted(Configs.values.get(LOG_INVERT_KEY));
        loadLog();
        
        int minutes = Configs.values.get(LOG_TIMER_KEY, DEFAULT_LOG_TIMER_INTERVAL, 0);
        if(minutes != 0){ //logger is running even when not task is performed; ineficient
            logger = Executors.newSingleThreadScheduledExecutor();
            logger.scheduleAtFixedRate(this::saveLog, minutes, minutes, TimeUnit.MINUTES);  
        }
        
        // <editor-fold defaultstate="collapsed" desc=" SSL TRUST MANAGER "> 
	// Create a new trust manager that trust all certificates
	TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
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

        pnlOverlay = new javax.swing.JPanel();
        lblAbout = new javax.swing.JLabel();
        pnlTab = new javax.swing.JTabbedPane();
        sclTasks = new javax.swing.JScrollPane();
        tblTasks = new TaskTable();
        tblTasks.setListener(this);
        pnlTools = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        pgbTasks = new javax.swing.JProgressBar();
        chkShutdown = new javax.swing.JCheckBox();
        sclLogs = new javax.swing.JScrollPane();
        txaLogs = new rubensandreoli.commons.swing.RecycledTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Image Downloader");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnlOverlay.setLayout(new javax.swing.OverlayLayout(pnlOverlay));

        lblAbout.setIcon(FileUtils.loadIcon("about.png"));
        lblAbout.setAlignmentX(1.0F);
        lblAbout.setAlignmentY(0.0F);
        lblAbout.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 3));
        lblAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAboutMouseClicked(evt);
            }
        });
        pnlOverlay.add(lblAbout);

        pnlTab.setAlignmentX(1.0F);
        pnlTab.setAlignmentY(0.0F);
        pnlTab.setMinimumSize(new java.awt.Dimension(100, 133));
        pnlOverlay.add(pnlTab);
        pnlTab.getAccessibleContext().setAccessibleName("Tabs");

        sclTasks.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 1), javax.swing.BorderFactory.createEtchedBorder()));
        sclTasks.setViewportView(tblTasks);

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

        chkShutdown.setToolTipText("<html><b>Shutdown computer</b> after completion.<br>\n<i>Shutdown will occur automatically, <br>\nwithout an option to cancel it.</i></html>");
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
                    .addComponent(sclTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(pnlOverlay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlOverlay, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(sclTasks, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(pnlTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sclLogs, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        if(tasks.isEmpty()) return; //TODO: warning no tasks?
        prepare();
        
        //PERFORM TASKS IN ANOTHER THREAD AND UPDATE UI
        worker = new SwingWorker<>() {
            
            @Override
            protected Void doInBackground() throws Exception {
                while(!tasks.isEmpty()){
                    if(isCancelled()) break;
                    currentTask = tasks.removeFirst();
                    currentTask.setProgressListener(l -> publish(l));
                    currentTask.perform();
                }
                return null;
            }

            @Override
            protected void process(List<ProgressLog> logs) {
                boolean changedTask = false;
                for (var log : logs) {
                    if(log.getNumber() == 0) changedTask = true;
                    txaLogs.addText(log.getMessages());
                }
                if(changedTask) tblTasks.refresh();
                ProgressLog log = logs.get(logs.size()-1);
                pgbTasks.setValue(log.getNumber());
                pgbTasks.setMaximum(log.getWorkload());
                pgbTasks.setToolTipText(String.format(PROGRESSBAR_TOOLTIP_MASK, pgbTasks.getValue(), pgbTasks.getMaximum()));
            }

            @Override
            protected void done() {
                currentTask = null;
                clear(isCancelled());
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
        if(logger != null) logger.shutdownNow();
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

    private void lblAboutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAboutMouseClicked
        new AboutDialog(this, "Image Downloader", "1.0", "2020", "logo.png")
                .addAtribution("Program icon", "Good Ware", "www.flaticon.com")
                .addAtribution("Task status icons", "Pixel perfect", "www.flaticon.com")
                .setVisible(true);
    }//GEN-LAST:event_lblAboutMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JCheckBox chkShutdown;
    private javax.swing.JLabel lblAbout;
    private javax.swing.JProgressBar pgbTasks;
    private javax.swing.JPanel pnlOverlay;
    private javax.swing.JTabbedPane pnlTab;
    private javax.swing.JPanel pnlTools;
    private javax.swing.JScrollPane sclLogs;
    private javax.swing.JScrollPane sclTasks;
    private rubensandreoli.imagedownloader.gui.TaskTable tblTasks;
    private rubensandreoli.commons.swing.RecycledTextArea txaLogs;
    // End of variables declaration//GEN-END:variables

    private void stop(boolean interrupt){
        //currentTask and worker are null if not started; no need to stop
        //currentTask null, worker not null if completed; no need to stop
        if(worker != null && currentTask != null){
            currentTask.interrupt();
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
        txaLogs.clear();
        btnStart.setEnabled(false);
        pgbTasks.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    private void clear(boolean isCanceled){
        //BEFORE USER VERIFICATION
        pgbTasks.setCursor(Cursor.getDefaultCursor());
        pgbTasks.setValue(pgbTasks.getMaximum()); //if for some reason don't fill completly
        tblTasks.refresh();
        saveLog();
        if(!chkShutdown.isSelected()) {
            setState(Frame.NORMAL);
            if(!isCanceled){
                JOptionPane.showMessageDialog(
                        ImageDownloader.this,
                        COMPLETE_MSG,
                        COMPLETE_TITLE,
                        JOptionPane.INFORMATION_MESSAGE
                );
            }else{
                JOptionPane.showMessageDialog(
                        ImageDownloader.this,
                        ABORTED_MSG,
                        ABORTED_TITLE,
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
        
        //AFTER USER VERIFICATION
        pgbTasks.setToolTipText(null);
        pgbTasks.setValue(0);
        if(isCanceled) tasks.clear(); //if cancelled remove remaining tasks
        tblTasks.clear();
        btnStart.setEnabled(true);
        btnStop.setEnabled(true);
    }

    public void addTaskPanel(TaskPanel panel){
        panel.setTaskPanelListener(this);
        pnlTab.addTab(panel.getTitle(), panel);
        Integer mnemonic = panel.getMnemonic();
        if(mnemonic != null){
            pnlTab.setMnemonicAt(pnlTab.getTabCount()-1, mnemonic);
        }
    }

    @Override
    public void taskCreated(TaskPanel source, Task task, String description) { //TODO: name from source or selected panel?
        tasks.addLast(task);
        tblTasks.addTask(source.getTitle(), task, description); //add to table afer adding here
    }

    @Override
    public boolean taskRemoved(Task task) {
        boolean removed = false;
        if(task.getStatus() == Status.WAITING){
            tasks.remove(task);
            removed = true;
        }else if(task == currentTask){
            currentTask.interrupt();
        }
        return removed;
    }
     
}
