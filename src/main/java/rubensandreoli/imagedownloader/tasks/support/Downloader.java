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
package rubensandreoli.imagedownloader.tasks.support;

import java.io.IOException;
import java.nio.file.Files;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import rubensandreoli.commons.others.CachedFile;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

public class Downloader {
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final int DONE = 0;
    public static final int FIXED = 1;
    public static final int DELETED = 2;
    public static final int FAILED = 3;
    
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000; //ms
    public static final int DEFAULT_READ_TIMEOUT = 4000; //ms
    public static final int DEFAULT_MIN_COOLDOWN = 600; //ms
    public static final int DEFAULT_MAX_COOLDOWN = 1200; //ms
    
    private static final String TUMBLR_IMAGE_PREFIX = "tumblr_";
    
    private static final String DOWNLOAD_LOG_MASK = "Downloaded [%s]"; //url
    private static final String DELETING_FILE_LOG_MASK = "Deleting unwanted [%s]"; //url
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading [%s]"; //url
    private static final String SUCCESS_TUMBLR_LOG_MASK = "Succeeded resolving Tumblr URL [%s]"; //url
    private static final String FAILED_TUMBLR_LOG_MASK = "Failed resolving Tumblr URL [%s]"; //url
    private static final String FAILED_INVALID_LOG_MASK = "Failed resolving invalid URL [%s]"; //url
    // </editor-fold>
    
    private boolean sleep = true;
    private int minFilesize = 0;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int minCooldown = DEFAULT_MIN_COOLDOWN;
    private int maxCooldown = DEFAULT_MAX_COOLDOWN;
    private DownloadListener listener;

    public CachedFile download(String url, String folder, String filename, String extension){
        try {
            CachedFile cachedFile = new CachedFile(FileUtils.createValidFile(folder, filename, extension));
            FileUtils.downloadToFile(url, cachedFile, connectionTimeout, readTimeout);
            if(cachedFile.length() > minFilesize && !cachedFile.matchSignature((byte)60)){ //[<]!DOCTYPE...>
                fireStateChanged(DONE, DOWNLOAD_LOG_MASK, url);
            } else {
                if(cachedFile.delete()){
                    fireStateChanged(DELETED, DELETING_FILE_LOG_MASK, url);
                }
                if(filename.startsWith(TUMBLR_IMAGE_PREFIX)){
                    cachedFile = resolveTumblr(url, folder, filename, extension);
                }else if(url.contains("?")){
                    cachedFile = resolveInvalid(url, folder);
                }else{
                    return null; //failed resolving
                }
            }
            if(sleep) sleepRandom();
            return cachedFile; //ok
        } catch (IOException ex) {
            fireStateChanged(FAILED, DOWNLOAD_FAILED_LOG_MASK, url);
            return null; //failed download
        }
    }
    
    public void fireStateChanged(int status, String msg, Object...args){
        if(listener != null) listener.donwloadStateChanged(status, String.format(msg, args));
    }
    
    private CachedFile resolveTumblr(String url, String folder, String filename, String extension) throws IOException{
        try(CloseableHttpClient client = HttpUtils.getClient()){
            
            //----------REQUEST----------//
            final HttpGet request = new HttpGet(url);
            request.addHeader("Accept", HttpUtils.ACCEPT_IMAGE);
            request.addHeader("referer", url);
            final HttpResponse response = client.execute(request);
            
            //----------SAVE BYTES----------//
            final byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            if(bytes.length > minFilesize){
                final CachedFile cachedFile = new CachedFile(FileUtils.createValidFile(folder, filename, extension));
                Files.write(cachedFile.toPath(), bytes);
                cachedFile.setSize(bytes.length);
                fireStateChanged(FIXED, SUCCESS_TUMBLR_LOG_MASK, url);
                return cachedFile;
            }
            fireStateChanged(FAILED, FAILED_TUMBLR_LOG_MASK, url);
            return null;
        }
    }
    
    private CachedFile resolveInvalid(String url, String folder) throws IOException {
        url = url.substring(0, url.lastIndexOf("?"));
        final String filename = FileUtils.getFilename(url);
        final String extension = FileUtils.getExtension(url);
        final CachedFile cachedFile = download(url, folder, filename, extension);
        if(cachedFile == null) fireStateChanged(FAILED, FAILED_INVALID_LOG_MASK, url);
        return cachedFile;
    }
    
    private void sleepRandom(){
        try {
            Thread.sleep(IntegerUtils.getRandomBetween(minCooldown, maxCooldown));
        } catch (InterruptedException ex) {}
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public void setMinFilesize(int bytes) {
        if(bytes < 0) throw new IllegalArgumentException(bytes+" < 0");
        this.minFilesize = bytes;
    }

    public void setConnectionTimeout(int ms) {
        if(ms < 0) throw new IllegalArgumentException(ms+" < 0");
        this.connectionTimeout = ms;
    }

    public void setReadTimeout(int ms) {
        if(ms < 0) throw new IllegalArgumentException(ms+" < 0");
        this.readTimeout = ms;
    }
    
    public void setCooldown(int minCooldown, int maxCooldown) {
        if(minCooldown > maxCooldown) throw new IllegalArgumentException("min "+minCooldown+" > max "+maxCooldown);
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }
    // </editor-fold>

}
