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
package rubensandreoli.imagedownloader.tasks;

import java.io.File;
import java.io.IOException;
import rubensandreoli.commons.others.Level;
import rubensandreoli.imagedownloader.tasks.support.DownloadListener;
import rubensandreoli.imagedownloader.tasks.support.Downloader;
import rubensandreoli.imagedownloader.tasks.support.ProgressListener;
import rubensandreoli.imagedownloader.tasks.support.TaskJournal;

public abstract class DownloadTask implements Task, DownloadListener {
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final String DOWNLOAD_TOTAL_LOG_MASK = "%d file(s) downloaded"; //successess
    
    private static final String NO_FOLDER_MSG = "No folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found."; //filepath
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    // </editor-fold>
    
    protected final TaskJournal monitor = new TaskJournal();
    protected Downloader downloader = new Downloader();
    private String destination;
    private boolean reportStatus = true, reportSuccesses = true;
    private int failTreashold = 0; //value '0' won't fail

    @Override
    public boolean perform(){
        if(!monitor.start()) return false;
        downloader.setListener(this);
        if(reportStatus) monitor.reportState();
        
        run();
        
        if(monitor.isRunning()) monitor.setStatus(State.COMPLETED);
        if(reportStatus) monitor.reportState();
        if(reportSuccesses) monitor.report(Level.INFO, DOWNLOAD_TOTAL_LOG_MASK, monitor.getSuccesses());
        return true;
    }
    
    protected abstract void run();
    
    @Override
    public boolean interrupt() {
        return monitor.interrupt();
    }
    
    protected File getWritableFolder(String pathname) throws IOException{
        if(pathname == null || pathname.isBlank()) throw new IOException(NO_FOLDER_MSG);
        try{
            final File file = new File(pathname);
            if(file.isDirectory() && file.canWrite()){
                return file;
            }else{
                throw new IOException(String.format(NOT_FOLDER_MSG_MASK, pathname));
            }
        }catch(SecurityException ex){
            throw new IOException(String.format(FOLDER_PERMISSION_MSG_MASK, pathname));
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS ">    
    @Override
    public void setProgressListener(ProgressListener listener) {
        monitor.setProgressListener(listener);
    }
    
    public void setFailThreshold(int amount) {
        if(amount < 0) throw new IllegalArgumentException(amount+" < 0");
        failTreashold = amount;
    }

    public void setReportStatus(boolean b) {
        this.reportStatus = b;
    }

    public void setReportSuccesses(boolean b) {
        this.reportSuccesses = b;
    }
    
    public void setDestination(String folder) throws IOException {
        destination = getWritableFolder(folder).getPath();
    }

    public void setMinFilesize(int bytes) {
        downloader.setMinFilesize(bytes);
    }
    
    public void setConnection(int connectionTimeout, int readTimeout, int minCooldown, int maxCooldown){
        downloader.setConnectionTimeout(connectionTimeout);
        downloader.setReadTimeout(readTimeout);
        downloader.setCooldown(minCooldown, maxCooldown);
    }
    
    public void silent(boolean b){
        monitor.setSilent(b);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getDestination(){
        return destination;
    }

    protected boolean interrupted(){
        return monitor.isInterrupted();
    }
    
    protected boolean failed() {
        return monitor.fail(failTreashold);
    }
    
    @Override
    public State getStatus() {
        return monitor.getStatus();
    }
    
    public int getSuccesses(){
        return monitor.getSuccesses();
    }
    
    public int getFails(){
        return monitor.getFails();
    }
    // </editor-fold>

    @Override
    public void donwloadStateChanged(int state, String description) {
        Level level = Level.INFO;
        switch(state){
            case Downloader.FAILED:
                level = Level.ERROR;
                break;
            case Downloader.DELETED:
                level = Level.WARNING;
                break;
        }
        this.downloadStateChanged(level, description);
    }
    
    public abstract void downloadStateChanged(Level level, String description);
    
}
