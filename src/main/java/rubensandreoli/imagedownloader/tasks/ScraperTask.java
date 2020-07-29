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

import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.commons.utils.Configs;
import rubensandreoli.commons.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScraperTask extends DownloadTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">    
    private static final String INVALID_URL_MSG = "Invalid URL.";
    private static final String INVALID_BOUNDS_MSG_MASK = "Search depth has a limit of %d";
    
    private static final String CONNECTION_LOG_MASK = "Connected to [%s]"; //url
    private static final String CONNECTION_FAILED_LOG_MASK = "Failed connecting to [%s]"; //url
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    public static final int DEFAULT_DEPTH_LIMIT = 3;
    
    private static final int DEPTH_LIMIT;
    private static final int MIN_FILESIZE; //bytes
    static{
        DEPTH_LIMIT = Configs.values.get("scraper:depth_limit", DEFAULT_DEPTH_LIMIT, 0);
        MIN_FILESIZE = Configs.values.get("scraper:filesize_min", 25600, 0);
    }
    // </editor-fold>
     
    private final String path;
    private final String domain;
    private int depth;
    private int success;

    public ScraperTask(String url) throws MalformedURLException{
        try {
            URL validUrl = new URL(url);
            path = validUrl.toString();
            domain = validUrl.getAuthority();
        } catch (MalformedURLException ex) {
            throw new MalformedURLException(INVALID_URL_MSG);
        }
    }
    
    @Override
    protected int run() {
        processPage(path);
        return success;
    }
        
    private void processPage(String url){
        try {
            //CONNECTION
            Document d = connect(url);
            report(ProgressLog.INFO, false, CONNECTION_LOG_MASK, url);
            
            //DOWNLOAD
            downloadImages(d);
            if(isInterrupted()) return; //INTERRUPT EXIT POINT

            //PROCESS LINKS
            if(depth-- > 0) processLinks(d);
        } catch (IOException ex) {
            report(ProgressLog.ERROR, CONNECTION_FAILED_LOG_MASK, url);
        }
    }

    private void downloadImages(Document d){
        Elements images = d.getElementsByTag("img");
        increaseWorkload(images.size());
        int successPartial = 0;
        for (Element image : images) {
            if(isInterrupted()) break; //INTERRUPT PROCCESS
            
            //FIX URL
            String imageUrl = image.absUrl("src");
            int removeIndex = imageUrl.indexOf("?"); //fix some images url with values after extension
            if(removeIndex > 0){
                imageUrl = imageUrl.substring(0, removeIndex);
            }
            
            //OUTPUT FILE
            String filename = FileUtils.parseFilename(imageUrl);
            String extension = FileUtils.parseExtension(imageUrl);
            File file = FileUtils.createValidFile(getDestination(), filename, extension);
            
            //DOWNLOAD TO FILE
            if(download(imageUrl, file, MIN_FILESIZE, null)){
                successPartial++;
            }
        }
        success += successPartial;
    }
    
    private void processLinks(Document d){
        Elements links = d.getElementsByTag("a");
        increaseWorkload(links.size());
        for (Element link : links) {
            if(isInterrupted()) break; //INTERRUPT PROCCESS
            String linkUrl = link.absUrl("href");
            if(linkUrl.startsWith(domain)){ //process only links from same site
                processPage(linkUrl);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setDepth(int depth) throws BoundsException{
        if(depth < 0 || depth > DEPTH_LIMIT){
            throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, DEPTH_LIMIT));
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getPath() {
        return path;
    }

    public int getDepth() {
        return depth;
    }

    public String getRoot() {
        return domain;
    }
    // </editor-fold>

}
