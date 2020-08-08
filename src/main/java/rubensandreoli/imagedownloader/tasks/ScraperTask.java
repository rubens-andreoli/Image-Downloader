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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.support.HttpUtils;

public class ScraperTask extends DownloadTask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final int DEFAULT_DEPTH_LIMIT = 3;
    public static final int DEFAULT_MIN_FILESIZE = 25600;
    
    private static final String SITE_MASK = "%s://%s"; //protocol; authority
    
    private static final String INVALID_URL_MSG = "Invalid URL.";
    private static final String INVALID_BOUNDS_MSG_MASK = "Search depth has a limit of %d";
    
    private static final String CONNECTION_LOG_MASK = "Connected to [%s]"; //url
    private static final String CONNECTION_FAILED_LOG_MASK = "Failed connecting to [%s]"; //url
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" PAGE "> 
    public static class Webpage{

        public final String domain;
        public final String url;
        public final Document document;
        public final Elements imgTags;
        public final Elements aTags;

        public Webpage(String domain, String url, Document document) {
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
                if(linkUrl.matches(FileUtils.IMAGES_REGEX)){
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
     
    private final String url;
    private final String domain;
    private int depth = 0;
    private final Set<String> processed = new HashSet<>();
    private final int depthLimit;
    private int minFilezise = DEFAULT_MIN_FILESIZE;

    public ScraperTask(String url, int depthLimit) throws MalformedURLException{
        if(depthLimit < 0) throw new IllegalArgumentException(depthLimit+" < 0");
        this.depthLimit = depthLimit;
        try {
            final URL validUrl = new URL(url);
            this.url = validUrl.toString();
            domain = String.format(SITE_MASK, validUrl.getProtocol(), validUrl.getAuthority());
            
            setMinFilesize(minFilezise);
        } catch (MalformedURLException ex) {
            throw new MalformedURLException(INVALID_URL_MSG);
        }
    }
    
    public ScraperTask(String url) throws MalformedURLException{
        this(url, DEFAULT_DEPTH_LIMIT);
    }
    
    @Override
    protected void run() {
        processPage(url);
    }
        
    private void processPage(String url){
        if(processed.contains(url)) return;
        processed.add(url);
        try {
            //CONNECTION
            final Document d = HttpUtils.getDocument(url);
            monitor.report(Level.INFO, false, CONNECTION_LOG_MASK, url);
            Webpage page = new Webpage(domain, url, d);
            
            //DOWNLOAD
            final Set<String> images = page.parseImages();
            monitor.addWorkload(images.size());
            downloadImages(images);
            if(interrupted()) return; //INTERRUPT EXIT POINT

            //CRAWL
            if(depth > 0){
                depth--;
                final Set<String> links = page.parseLinks();
                monitor.addWorkload(links.size());
                links.forEach(this::processPage);
            }
        } catch (IOException ex) {
            monitor.report(Level.ERROR, CONNECTION_FAILED_LOG_MASK, url);
        }
    }

    private void downloadImages(Set<String> urls){
        for (String url : urls) {
            if(interrupted()) break; //INTERRUPT
            
            //CREATE FILE NAME
            final String filename = FileUtils.getFilename(url);
            final String extension = FileUtils.getExtension(url, ".jpg");
            
            //DOWNLOAD
            if(downloader.download(url, getDestination(), filename, extension) != null){
                monitor.increaseSuccesses();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setDepth(int depth) throws BoundsException{
        if(depth < 0 || depth > depthLimit) throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, depthLimit));
        this.depth = depth;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getURL() {
        return url;
    }

    public int getDepth() {
        return depth;
    }
    // </editor-fold>

    @Override
    public void downloadStateChanged(Level level, String description) {
        monitor.report(level, description);
    }
    
}
