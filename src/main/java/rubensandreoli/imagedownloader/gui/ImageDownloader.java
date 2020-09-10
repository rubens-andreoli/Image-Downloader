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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Taskbar;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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
import rubensandreoli.commons.others.Configuration;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.others.Logger;
import rubensandreoli.commons.swing.AboutDialog;
import rubensandreoli.commons.swing.RecycledTextArea;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.Task;
import rubensandreoli.imagedownloader.tasks.Task.State;
import rubensandreoli.imagedownloader.tasks.support.ProgressLog;

/** 
 * References:
 * http://www.java2s.com/Code/Java/Swing-JFC/DisplayingthePercentageDoneonaJProgressBarComponent.htm
 * https://stackoverflow.com/questions/2973643/shutdown-windows-with-java
 * https://stackoverflow.com/questions/32228345/run-java-function-every-hour
 * https://stackoverflow.com/questions/37117470/how-to-loop-arraylist-from-one-thread-while-adding-to-it-from-other-thread/37117674
 * https://stackoverflow.com/questions/1426754/linkedblockingqueue-vs-concurrentlinkedqueue
 * https://stackoverflow.com/questions/1614772/how-to-change-jframe-icon
 * https://stackoverflow.com/questions/3965336/how-to-minimize-a-jframe-window-from-java
 * https://stackoverflow.com/questions/2167037/windows-7-taskbar-progress-bar-in-java
 * https://vmlens.com/articles/5-ways-to-thread-safe-update-a-field-in-java-2/
 * https://stackoverflow.com/questions/38498399/thread-safe-for-instance-fields#:~:text=If%20your%20variable%20is%20modified,code%20block%20to%20be%20guarded
 */
public class ImageDownloader extends javax.swing.JFrame implements TaskPanelListener, TaskTableListener {
    private static final long serialVersionUID = 1L;
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String PROGRAM_NAME = "Image Downloader";
    private static final String PROGRAM_ICON = "images/download_image.png";
    private static final String PROGRAM_VERSION = "1.0.0";
    private static final String PROGRAM_YEAR = "2020";
    private static final String LOG_FILE = "history.log";
    private static final int SHUTDOWN_INTERVAL = 10; //seconds
    private static final int SHUTDOWN_EVENT_CODE = 101;
    
    private static final String PROGRESSBAR_TOOLTIP_MASK = "%d/%d"; //progress, total
    
    private static final String CLOSING_TITLE = "Close";
    private static final String CLOSING_MSG = "Closing the program while running will stop all current donwloads.\nDo you wish to exit anyway?";
    private static final String SECURITY_TITLE = "Security Error";
    private static final String SECURITY_MSG = "This program doesn't have permission to read/write in the folder where it is located.";
    private static final String EMPTY_MSG = "At least one task must be created before starting.";
    private static final String EMPTY_TITLE = "No Tasks";
    private static final String COMPLETE_TITLE = "Tasks Completed";
    private static final String COMPLETE_MSG = "All available files were downloaded.";
    private static final String ABORTED_TITLE = "Tasks Aborted";
    private static final String ABORTED_MSG = "All remaining tasks were stopped.";
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS ">
    public static final boolean DEGUB;
    public static final int LOG_SIZE;
    public static final boolean LOG_INVERTED;
    public static final Font LOG_FONT;
    public static final int SAVE_INTERVAL;
    static{
        DEGUB = Configuration.values.get("crash_log", true);
        LOG_SIZE = Configuration.values.get("log_size", RecycledTextArea.DEFAULT_MAX_SIZE, RecycledTextArea.MIN_SIZE);
        LOG_INVERTED = Configuration.values.get("log_inverted", false);
        LOG_FONT = new Font("Segoe UI", 0, Configuration.values.get("font_size", 10, 8));
        SAVE_INTERVAL = Configuration.values.get("log_timer", 10, 0);
    }
    // </editor-fold>
      
    private ConcurrentLinkedDeque<Task> tasks = new ConcurrentLinkedDeque<>(); //set by both threads
    private SwingWorker<Void, ProgressLog> worker;
    private volatile Task currentTask; //set by worker thread
    private ScheduledExecutorService logger;
    private Taskbar taskbar = Taskbar.getTaskbar();

    public ImageDownloader() { 
        initComponents();
        Logger.log.setEnabled(DEGUB);
        
        setIconImage(FileUtils.loadIcon(PROGRAM_ICON).getImage());
        lblAbout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        txaLogs.enableTooltip(true);
        txaLogs.setSize(LOG_SIZE);
        txaLogs.setInverted(LOG_INVERTED);
        txaLogs.setFont(LOG_FONT);
        loadLog();
    
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

        lblAbout.setIcon(rubensandreoli.commons.utils.FileUtils.loadIcon("images/about.png"));
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
        if(tasks.isEmpty()) {
            JOptionPane.showMessageDialog(ImageDownloader.this,
                    EMPTY_MSG,
                    EMPTY_TITLE,
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        txaLogs.clear();
        btnStart.setEnabled(false);
        pgbTasks.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(SAVE_INTERVAL != 0){
            logger = Executors.newSingleThreadScheduledExecutor();
            logger.scheduleAtFixedRate(this::saveLog, SAVE_INTERVAL, SAVE_INTERVAL, TimeUnit.MINUTES);  
        }
        
        //PERFORM TASKS IN ANOTHER THREAD AND UPDATE UI
        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while(!tasks.isEmpty()){
                    if(isCancelled()) break;
                    currentTask = tasks.peekFirst();
                    currentTask.setProgressListener(l -> publish(l));
                    currentTask.perform();
                    tasks.removeFirst(); //remove task after it's done
                }
                return null;
            }

            @Override
            protected void process(List<ProgressLog> logs) {
                for(ProgressLog log : logs) {
                    if(log.isFirst()) tblTasks.refresh();
                    txaLogs.addText(log.getMessages());
                }
                final ProgressLog lastLog = logs.get(logs.size()-1);
                final int number = lastLog.getNumber();
                final int workload = lastLog.getWorkload();
                pgbTasks.setValue(number);
                pgbTasks.setMaximum(workload);
                try {taskbar.setWindowProgressValue(ImageDownloader.this, (100*number)/workload);
                } catch(RuntimeException ex){}
                pgbTasks.setToolTipText(String.format(PROGRESSBAR_TOOLTIP_MASK, number, workload));
            }

            @Override
            protected void done() {
                worker = null;
                currentTask = null;
                ImageDownloader.this.saveLog();
                ImageDownloader.this.clear(isCancelled());
                if(logger != null) try{ logger.shutdownNow();}catch(RuntimeException ex){}
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
        if(currentTask != null){
            if(JOptionPane.showConfirmDialog(
                    this, 
                    CLOSING_MSG, 
                    CLOSING_TITLE, 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) 
                return;
        }
        stop(true);
        if(logger != null) logger.shutdownNow();
        saveLog();
        Configuration.values.save();
        if(evt.getID() == SHUTDOWN_EVENT_CODE){
            try {
                Runtime.getRuntime().exec("shutdown -s -t "+SHUTDOWN_INTERVAL);
                dispose();
            } catch (IOException ex) {
                Logger.log.print(Level.SEVERE, "failed auto-shutdown", ex);
            }
        }else{
            dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    private void lblAboutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAboutMouseClicked
        new AboutDialog(this, PROGRAM_NAME, PROGRAM_VERSION, PROGRAM_YEAR, "images/logo.png")
                .addAtribution("Program icon", "Good Ware", "https://www.flaticon.com/authors/good-ware")
                .addAtribution("About icon", "Gregor Cresnar", "https://www.flaticon.com/authors/gregor-cresnar")
                .addAtribution("Other icons", "Pixel perfect", "https://www.flaticon.com/authors/pixel-perfect")
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
        if(worker != null && currentTask != null){
            currentTask.interrupt();
            worker.cancel(interrupt);
        }
    }
    
    private void loadLog(){
        final File log = new File(LOG_FILE);
        try{
            if(log.isFile()){
                try(var is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(log)))){
                    txaLogs.setTexts((LinkedList<String>) is.readObject());
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.log.print(Level.CRITICAL, "failed loading log file "+LOG_FILE, ex);
                }
            }
        }catch(SecurityException ex){
            securityAlert();
        }
    }
    
    private void saveLog(){
        try(var os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(LOG_FILE)))){
            os.writeObject(txaLogs.getTexts());
        } catch (IOException ex) {
            Logger.log.print(Level.CRITICAL, "failed saving log file "+LOG_FILE, ex);
        } catch(SecurityException ex){
            securityAlert();
        }
    }
    
    private void securityAlert(){
        JOptionPane.showMessageDialog(this, SECURITY_MSG, SECURITY_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    private void clear(boolean isCanceled){ 
        //BEFORE USER VERIFICATION
        try{
            if(isCanceled) taskbar.setWindowProgressState(ImageDownloader.this, Taskbar.State.ERROR);
            taskbar.setWindowProgressValue(ImageDownloader.this, 100);
        }catch(RuntimeException ex){}
        pgbTasks.setCursor(Cursor.getDefaultCursor());
        pgbTasks.setValue(pgbTasks.getMaximum()); //if for some reason it's not filled completely
        tblTasks.refresh();
        
        //ALERT USER
        if(!chkShutdown.isSelected()) {
            setState(Frame.NORMAL);
            if(!isCanceled){
                JOptionPane.showMessageDialog(this, COMPLETE_MSG, COMPLETE_TITLE, JOptionPane.INFORMATION_MESSAGE);   
            }else{
                JOptionPane.showMessageDialog(this, ABORTED_MSG, ABORTED_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        //AFTER USER VERIFICATION
        taskbar.setWindowProgressState(ImageDownloader.this, Taskbar.State.OFF);
        pgbTasks.setToolTipText(null);
        pgbTasks.setValue(0);
        if(isCanceled) tasks.clear(); //if cancelled remove remaining tasks
        btnStart.setEnabled(true);
        btnStop.setEnabled(true);
    }

    public void addTaskPanel(TaskPanel panel){
        panel.setTaskPanelListener(this);
        pnlTab.addTab(panel.getTitle(), panel);
        final Integer mnemonic = panel.getMnemonic();
        if(mnemonic != null){
            pnlTab.setMnemonicAt(pnlTab.getTabCount()-1, mnemonic);
        }
    }

    @Override
    public void taskCreated(String type, Task task, String description) {
        if(tasks.isEmpty()) tblTasks.clear();
        tasks.addLast(task);
        tblTasks.addTask(type, task, description);
    }

    @Override
    public boolean taskRemoved(Task task) {
        boolean removed = false;
        if(task.getStatus() == State.WAITING){
            tasks.remove(task);
            removed = true;
        }else if(task == currentTask){
            currentTask.interrupt();
        }
        return removed;
    }

}
