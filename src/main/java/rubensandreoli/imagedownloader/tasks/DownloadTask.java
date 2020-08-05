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

import rubensandreoli.imagedownloader.support.ProgressLog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import rubensandreoli.commons.others.CachedFile;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;
import rubensandreoli.imagedownloader.support.ProgressLog.Tag;

public abstract class DownloadTask extends BasicTask {

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000; //ms
    public static final int DEFAULT_READ_TIMEOUT = 4000; //ms
    public static final int DEFAULT_COOLDOWN = 500; //ms
    private static final String TUMBLR_IMAGE_PREFIX = "tumblr_";
    
    private static final String NO_FOLDER_MSG = "No folder selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found."; //filepath
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    
    private static final String DOWNLOAD_LOG_MASK = "Downloaded [%s]"; //url
    private static final String DELETING_FILE_LOG_MASK = "Deleting unwanted file [%,d bytes]"; //size
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading [%s]"; //url
    private static final String DOWNLOAD_TOTAL_LOG_MASK = "%d file(s) downloaded"; //downloaded images
    private static final String SUCCESS_TUMBLR_LOG_MASK = "Succeeded resolving Tumblr URL [%s]"; //url
    private static final String FAILED_TUMBLR_LOG_MASK = "Failed resolving Tumblr URL [%s]"; //url
    private static final String FAILED_INVALID_LOG_MASK = "Failed resolving invalid URL [%s]"; //url
    // </editor-fold>
    
    private String destination;
    private int successes, fails = 0;
    private int failTreashold, minFilesize = 0;
    private boolean reportTotal = true;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int minCooldown = DEFAULT_COOLDOWN;
    private int maxCooldown = DEFAULT_COOLDOWN*2;
 
    @Override
    public boolean perform() {
        final boolean performed = super.perform();
        if(reportTotal) reportTotal();
        return performed;
    }
    
    protected void reportTotal(){
        final var log = new ProgressLog(getProgress(), getWorkload());
        log.appendLine(Tag.INFO, DOWNLOAD_TOTAL_LOG_MASK, successes);
        log.appendLine(String.format(BasicTask.STATUS_LOG_MASK, ""));
        reportLog(log);
    }

    protected CachedFile download(String url, String filename, String extension){
        return download(url, filename, extension, null, true);
    }

    protected CachedFile download(String url, String filename, String extension, ProgressLog log, boolean sleep){
        try {
            CachedFile cachedFile = new CachedFile(FileUtils.createValidFile(getDestination(), filename, extension));
            FileUtils.downloadToFile(url, cachedFile, connectionTimeout, readTimeout);
            if(cachedFile.length() > minFilesize && !cachedFile.matchSignature((byte)60)){ //[<]!DOCTYPE...>
                report(log, Tag.INFO, DOWNLOAD_LOG_MASK, url);
            } else {
                if(cachedFile.delete()){
                    report(log, Tag.WARNING, DELETING_FILE_LOG_MASK, cachedFile.length());
                }
                if(filename.startsWith(TUMBLR_IMAGE_PREFIX)){
                    cachedFile = resolveTumblr(url, filename, extension, log);
                }else if(url.contains("?")){
                    cachedFile = resolveInvalid(url, log);
                }else{
                    cachedFile = null;
                }
            }
            if(cachedFile != null && sleep) sleepRandom();
            return cachedFile;
        } catch (IOException ex) {
            report(log, Tag.ERROR, DOWNLOAD_FAILED_LOG_MASK, url);
            return null;
        }
    }

    private CachedFile resolveInvalid(String url, ProgressLog log) throws IOException {
        url = url.substring(0, url.lastIndexOf("?"));
        final String filename = FileUtils.getFilename(url);
        final String extension = FileUtils.getExtension(url);
        final CachedFile file = download(url, filename, extension, log, false);
        if(file == null){
            report(log, Tag.INFO, FAILED_INVALID_LOG_MASK, url);
        }
        return file;
    }
    
    private CachedFile resolveTumblr(String url, String filename, String extension, ProgressLog log) throws IOException{
        try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
            //REQUEST
            final HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "image/webp,image/apng,*/*");
            request.addHeader("referer", url);
            final HttpResponse response = client.execute(request);
            //SAVE BYTES
            final byte[] bytes = EntityUtils.toByteArray(response.getEntity()); //TODO: use streams with buffer?
            if(bytes.length > minFilesize){
                CachedFile cachedFile = new CachedFile(FileUtils.createValidFile(getDestination(), filename, extension));
                Files.write(cachedFile.toPath(), bytes);
                cachedFile.setSize(bytes.length);
                report(log, Tag.INFO, SUCCESS_TUMBLR_LOG_MASK, url);
                return cachedFile;
            }
            report(log, Tag.INFO, FAILED_TUMBLR_LOG_MASK, url);
            return null;
        }
    }
       
    protected void sleepRandom(){
        try {
            Thread.sleep(IntegerUtils.getRandomBetween(minCooldown, maxCooldown));
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
    
    private void report(ProgressLog log, Tag tag, String message, Object...args){
        if(log == null) report(tag, message, args);
        else log.appendLine(tag, message, args);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS ">    
    protected void setReportTotal(boolean b){
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

    public void setFailThreshold(int amount) {
        if(amount < 0) throw new IllegalArgumentException(amount+" < 0");
        failTreashold = amount;
    }

    public void setMinFilesize(int bytes) {
        if(bytes < 0) throw new IllegalArgumentException(bytes+" < 0");
        minFilesize = bytes;
    }
    
    public void setConnection(int connectionTimeout, int readTimeout, int minCooldown, int maxCooldown){
        setConnectionTimeout(connectionTimeout);
        setReadTimeout(readTimeout);
        setCooldown(minCooldown, maxCooldown);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setCooldown(int minCooldown, int maxCooldown) {
        if(minCooldown > maxCooldown) throw new IllegalArgumentException("min "+minCooldown+" > max "+maxCooldown);
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
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

    protected boolean failed() {
        final boolean failed = fails > failTreashold;
        if(failed) setStatus(Status.FAILED);
        return failed;
    }

    protected int getConnectionTimeout() {
        return connectionTimeout;
    }

    protected int getReadTimeout() {
        return readTimeout;
    }

    protected int getMaxCooldown() {
        return maxCooldown;
    }

    protected int getMinCooldown() {
        return minCooldown;
    }
    
    protected int getMinFilesize() {
        return minFilesize;
    }
    // </editor-fold>

    public static Document request(String url) throws IOException{
        return Jsoup.connect(url)
                    .header("Accept", "text/html; charset=UTF-8")
                    .userAgent(USER_AGENT)
                    .get();
    }
    
}
