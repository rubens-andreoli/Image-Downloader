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
import rubensandreoli.commons.tools.Configs;
import rubensandreoli.commons.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScraperTask extends DownloadTask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String SUPPORTED_IMAGES_REGEX = ".*\\.jpg|jpeg|bmp|png|gif";
    private static final String SITE_MASK = "%s://%s"; //protocol; authority
    
    private static final String INVALID_URL_MSG = "Invalid URL.";
    private static final String INVALID_BOUNDS_MSG_MASK = "Search depth has a limit of %d";
    
    private static final String CONNECTION_LOG_MASK = "Connected to [%s]"; //url
    private static final String CONNECTION_FAILED_LOG_MASK = "Failed connecting to [%s]"; //url
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    public static final int DEPTH_LIMIT; 
    public static final int MIN_FILESIZE;
    static{
        DEPTH_LIMIT = Configs.values.get("scraper:depth_limit", 3, 0);
        MIN_FILESIZE = Configs.values.get("scraper:filesize_min", 25600, 0);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" PAGE "> 
    public static class Page{

        public final String domain;
        public final String url;
        public final Document document;
        public final Elements imgTags;
        public final Elements aTags;

        public Page(String domain, String url, Document document) {
            this.domain = domain;
            this.url = url;
            this.document = document;
            imgTags = document.getElementsByTag("img");
            aTags = document.getElementsByTag("a");
        }

        public Set<String> parseImages(){
            final Set<String> images = new HashSet<>();
            imgTags.forEach(e -> images.add(e.absUrl("src")));
            for (Element link : aTags) {
                final String linkUrl = link.absUrl("href");
                if(linkUrl.matches(SUPPORTED_IMAGES_REGEX)){
                    images.add(linkUrl);
                }
            }
            return images;
        }

        public Set<String> parseLinks(){
            final Set<String> links = new HashSet<>();
            for (Element link : aTags) {
                final String linkUrl = link.absUrl("href");
                if(linkUrl.startsWith(domain)){ //process only links from same site
                    links.add(linkUrl);
                }
            }
            return links;
        }

    }
    // </editor-fold>
     
    private final String path;
    private final String domain;
    private int depth = 0;
    private final Set<String> processed = new HashSet<>();

    public ScraperTask(String url) throws MalformedURLException{
        try {
            final URL validUrl = new URL(url);
            path = validUrl.toString();
            domain = String.format(SITE_MASK, validUrl.getProtocol(), validUrl.getAuthority());
            
            setSizeThreashold(MIN_FILESIZE);
        } catch (MalformedURLException ex) {
            throw new MalformedURLException(INVALID_URL_MSG);
        }
    }
    
    @Override
    protected void run() {
        processPage(path);
    }
        
    private void processPage(String url){
        if(processed.contains(url)) return;
        processed.add(url);
        try {
            //CONNECTION
            final Document d = connect(url);
            report(ProgressLog.INFO, false, CONNECTION_LOG_MASK, url);
            Page page = new Page(domain, url, d);
            
            //DOWNLOAD
            final Set<String> images = page.parseImages();
            addWorkload(images.size());
            downloadImages(images);
            if(interrupted()) return; //INTERRUPT EXIT POINT

            //CRAWL
            if(depth > 0){
                depth--;
                final Set<String> links = page.parseLinks();
                addWorkload(links.size());
                links.forEach(this::processPage);
            }
        } catch (IOException ex) {
            report(ProgressLog.ERROR, CONNECTION_FAILED_LOG_MASK, url);
        }
    }

    private void downloadImages(Set<String> images){
        for (String image : images) {
            if(interrupted()) break; //INTERRUPT
            
            //CREATE FILE
            final String filename = FileUtils.parseFilename(image);
            final String extension = FileUtils.parseExtension(image, ".jpg");
            final File file = FileUtils.createValidFile(getDestination(), filename, extension);
            
            //DOWNLOAD
            if(download(image, file)){
                increaseSuccesses();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setDepth(int depth) throws BoundsException{
        if(depth < 0 || depth > DEPTH_LIMIT){
            throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, DEPTH_LIMIT));
        }
        this.depth = depth;
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
