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
import java.util.function.Predicate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import rubensandreoli.commons.tools.Configs;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

public abstract class DownloadTask extends Task {

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">   
    private static final String NO_FOLDER_MSG = "No folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found."; //filepath
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    
    private static final String DOWNLOAD_LOG_MASK = "Downloaded [%s]"; //url //TODO: add file to log msg?
    private static final String DELETING_FILE_LOG_MASK = "Deleting unwanted file [%,d bytes]"; //size
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading [%s]"; //url
    private static final String DOWNLOAD_TOTAL_LOG_MASK = "%d file(s) downloaded"; //downloaded images
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    protected static final String USER_AGENT;
    protected static final int CONNECTION_TIMEOUT; //ms
    protected static final int READ_TIMEOUT; //ms
    protected static final int MIN_COOLDOWN; //ms
    protected static final int MAX_COOLDOWN; //ms
    static{
        USER_AGENT = Configs.values.get("user_agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36");
        CONNECTION_TIMEOUT = Configs.values.get("connection_timeout", 2000, 500);
        READ_TIMEOUT = Configs.values.get("read_timout", 4000, 1000);
        MIN_COOLDOWN = Configs.values.get("connection_cooldown_min", 500, 0);
        MAX_COOLDOWN = Configs.values.get("connection_cooldown_max", MIN_COOLDOWN*2, MIN_COOLDOWN);
    }
    // </editor-fold> 
    
    private String destination;
    private int successes, fails = 0;
    private int failTreashold, minFilesize = 0;
    private boolean reportTotal = true;

    @Override
    public boolean perform() {
        final boolean performed = super.perform();
        if(reportTotal) reportTotal();
        return performed;
    }
    
    protected void reportTotal(){
        final var log = new ProgressLog(getProgress(), getWorkload());
        log.appendLine(ProgressLog.INFO, DOWNLOAD_TOTAL_LOG_MASK, successes);
        log.appendLine(String.format(Task.STATUS_LOG_MASK, ""));
        reportLog(log);
    }

    protected boolean download(String url, File file){
        return download(url, file, null);
    }

    protected boolean download(String url, File file, ProgressLog log, Predicate<File>...conditions){
        boolean success = false;
        try {
            final long size = FileUtils.downloadToFile(url, file, CONNECTION_TIMEOUT, READ_TIMEOUT);
            if(size > minFilesize){
                success = true;
                for (int i = 0; success && i < conditions.length; i++) { //break if failed one
                    success = conditions[i].test(file) && success;
                }
            }
            if(success){
                sleepRandom();
                report(log, ProgressLog.INFO, DOWNLOAD_LOG_MASK, url);
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
       
    protected void sleepRandom(){
        try {
            Thread.sleep(IntegerUtils.getRandomBetween(MIN_COOLDOWN, MAX_COOLDOWN));
        } catch (InterruptedException ex) {}
    }

    protected File getFolderFile(String folder) throws IOException{
        if(folder==null || folder.isBlank()) throw new IOException(NO_FOLDER_MSG);
        try{
            final File file = new File(folder);
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
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS ">    
    public void setReportTotal(boolean b){
        reportTotal = b;
    }
    
    public void setDestination(String folder) throws IOException{
        getFolderFile(folder);
        this.destination = folder;
    }
    
    protected int increaseSuccesses(){
        return ++successes;
    }
    
    protected int addSuccesses(int amount){
        return successes += amount;
    }

    protected int increaseFails(){
        return ++fails;
    }
    
    protected void resetFails(){
        fails = 0;
    }

    public void setFailTreashold(int amout) {
        failTreashold = amout;
    }

    public void setSizeThreashold(int bytes) {
        minFilesize = bytes;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getDestination() {
        return destination;
    }

    public int getSuccesses() {
        return successes;
    }
    
    protected boolean interrupted(){
        return getStatus() == Status.INTERRUPTED;
    }

    public boolean failed() {
        final boolean failed = fails > failTreashold;
        if(failed) setStatus(Status.FAILED);
        return failed;
    }
    // </editor-fold>

}
