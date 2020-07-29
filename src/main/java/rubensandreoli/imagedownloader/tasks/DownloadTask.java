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
import java.nio.file.Path;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import rubensandreoli.commons.utils.Configs;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

public abstract class DownloadTask implements Task {

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String NO_FOLDER_MSG = "No folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found."; //filepath
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    
    private static final String DOWNLOAD_LOG_MASK = "Downloaded [%s]"; //url
    private static final String DELETING_FILE_LOG_MASK = "Deleting corrupted file [%,d bytes]"; //size
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading/saving [%s]"; //url
    private static final String DOWNLOAD_TOTAL_LOG_MASK = "%d file(s) downloaded"; //downloaded images
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    protected static final String USER_AGENT;
    protected static final int CONNECTION_TIMEOUT; //ms
    protected static final int READ_TIMEOUT; //ms
    protected static final int CONNECTION_MIN_COOLDOWN; //ms
    protected static final int CONNECTION_MAX_COOLDOWN; //ms
    static{
        USER_AGENT = Configs.values.get("user_agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36");
        CONNECTION_TIMEOUT = Configs.values.get("connection_timeout", 2000, 500);
        READ_TIMEOUT = Configs.values.get("read_timout", 4000, 1000);
        CONNECTION_MIN_COOLDOWN = Configs.values.get("connection_cooldown_min", 500, 0);
        CONNECTION_MAX_COOLDOWN = Configs.values.get("connection_cooldown_max", CONNECTION_MIN_COOLDOWN+500, CONNECTION_MIN_COOLDOWN);
    }
    // </editor-fold>

    private String destination;
    private ProgressListener listener;
    private volatile Status status = Status.WAITING;
    private int progress, workload = 1;

    @Override
    public void perform() {
        status = Status.RUNNING;
        report(ProgressLog.INFO, false, DOWNLOAD_TOTAL_LOG_MASK, run());
        if(status != Status.INTERRUPTED) status = Status.COMPLETED;
    }
    
    protected abstract int run();

    protected boolean download(String url, File file){
        return download(url, file, 0, null);
    }

    protected boolean download(String url, File file, int minFilesize, ProgressLog log){
        boolean success = false;
        try {
            long size = FileUtils.downloadToFile(url, file, CONNECTION_TIMEOUT, READ_TIMEOUT);
            if(size > minFilesize){
                sleepRandom();
                report(log, ProgressLog.INFO, DOWNLOAD_LOG_MASK, url);
                success = true;
            }else{
                if(FileUtils.deleteFile(file)) {
                    report(log, ProgressLog.WARNING, DELETING_FILE_LOG_MASK, size);
                }
            }
        } catch (IOException ex) {
            report(log, ProgressLog.ERROR, DOWNLOAD_FAILED_LOG_MASK, url);
        }
        return success;
    }
        
    @Override
    public void interrupt() {
        status = Status.INTERRUPTED;
    }
    
    protected boolean isInterrupted(){
        return status == Status.INTERRUPTED;
    }
    
    protected File getFolderFile(String folder) throws IOException{
        if(folder==null || folder.isBlank()) throw new IOException(NO_FOLDER_MSG);
        try{
            File file = new File(folder);
            if(file.isDirectory()){
                return file;
            }else{
                throw new IOException(String.format(NOT_FOLDER_MSG_MASK, folder));
            }
        }catch(SecurityException ex){
            throw new IOException(String.format(FOLDER_PERMISSION_MSG_MASK, folder));
        }
    }
    
    protected Path getFolderPath(String folder) throws IOException{
        return this.getFolderFile(folder).toPath();
    }
    
    protected void report(ProgressLog log){
        if(listener != null) listener.progressed(log);
    }

    protected void report(String status, boolean progressed, String message, Object...args){
        var log = new ProgressLog(progressed? increaseProgress():getProgress(), getWorkload());
        log.appendLine(status, message, args);
        report(log);
    }
    
    protected void report(String status, String message, Object...args){
        report(status, true, message, args);
    }
    
    private void report(ProgressLog log, String status, String message, Object...args){
        if(log == null) report(status, message, args);
        else log.appendLine(status, message, args);
    }
    
    protected Document connect(String url) throws IOException{
        return Jsoup.connect(url)
                    .header("Accept", "text/html; charset=UTF-8")
                    .userAgent(USER_AGENT)
                    .get();
    }
    
    protected void sleepRandom(){
        try {
            Thread.sleep(IntegerUtils.getRandomBetween(CONNECTION_MIN_COOLDOWN, CONNECTION_MAX_COOLDOWN));
        } catch (InterruptedException ex) {}
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
    
    public void setDestination(String folder) throws IOException{
        getFolderFile(folder);
        this.destination = folder;
    }
    
    protected int increaseProgress(){
        return progress++;
    }

    protected void setWorkload(int workload){
        this.workload = workload;
    }
    
    protected void increaseWorkload(int amout) {
        workload += amout;
    }
    
    protected int increseWorkload(){
        return workload++;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getDestination() {
        return destination;
    }
    
    @Override
    public Status getStatus(){
        return status;
    }

    @Override
    public int getProgress(){
        return progress;
    }
    
    @Override
    public int getWorkload() {
        return workload;
    }
    // </editor-fold>
 
}
