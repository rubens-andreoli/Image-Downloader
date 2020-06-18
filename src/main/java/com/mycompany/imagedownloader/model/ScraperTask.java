package com.mycompany.imagedownloader.model;

import com.mycompany.imagedownloader.model.ProgressLog.Status;
import static com.mycompany.imagedownloader.model.Utils.USER_AGENT;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ScraperTask implements Task{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String INVALID_URL_MSG = "Invalid URL.";
    private static final String MISSING_DESTINATION_MSG = "Detination folder not found.";
    
    private static final String INVALID_BOUNDS_MSG_MASK = "Search depth has a limit of %d";
    private static final String CONNECTING_LOG_MASK = "Connecting to %s\n"; //url
    private static final String CONNECTION_FAILED_LOG = "Connection failed";
    private static final String IMAGE_COUNT_LOG_MASK = "Found %d images\n"; //num images
    private static final String FAILED_DOWNLOAD_LOG_MASK = "Failed downloading/saving %s\n"; //image url
    // </editor-fold>
    
    public final int depthLimit;
    
    private String path;
    private String root;
    private String destination;
    private int depth;
    
    private ProgressLog log;
    private ProgressListener listener;
    private volatile boolean running;

    public ScraperTask(String path, String destination, int depth) throws MalformedURLException, IOException, BoundsException {
        depthLimit = Configs.values.get("scraper:depth_limit", 3);
        
        if(depth < 0 || depth > depthLimit){
            throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, depthLimit));
        }
        if(!(new File(destination)).isDirectory()){
            throw new IOException(MISSING_DESTINATION_MSG);
        }
        try {
            URL url = new URL(path);
            this.path = url.toString();
            root = url.getAuthority();
            this.destination = destination;
            this.depth = depth;
        } catch (MalformedURLException ex) {
            throw new MalformedURLException(INVALID_URL_MSG);
        }   
    }
    
    @Override
    public void start() {
        running = true;
        download(path, 0, true);
    }
    
    @Override
    public void stop() {
        running = false;
    }
    
    private void download(String url, int progress, boolean isPath){
        if(!running) return;
        
        log = new ProgressLog(progress, !isPath);
        log.appendToLog(String.format(CONNECTING_LOG_MASK, url), Status.INFO);
        
        Document doc = null;
        try{
            //JSOUP CONNECTION
            doc = Jsoup.connect(url)
                    .header("Accept", "text/html; charset=UTF-8")
                    .userAgent(USER_AGENT)
                    .get();
        }catch(IOException ex){
            log.appendToLog(CONNECTION_FAILED_LOG, isPath? Status.CRITICAL:Status.ERROR);
            if(listener != null) listener.progressed(log);
        }
        if(doc == null) return;
        
        //PARSING IMAGES
        Elements images = doc.getElementsByTag("img");
        log.appendToLog(String.format(IMAGE_COUNT_LOG_MASK, images.size()), Status.INFO);
        for (int i = 0; i < images.size(); i++) {
            
            //GET SRC URL
            String imageUrl = images.get(i).absUrl("src");
            int removeIndex = imageUrl.indexOf("?"); //fix some images url with values after extension
            if(removeIndex != -1){
                imageUrl = imageUrl.substring(0, removeIndex);
            }   
            
            //SAVE
            String filename = Utils.parseFilename(imageUrl);
            String extension = Utils.parseExtension(imageUrl);
            File file = Utils.createValidFile(destination, filename, extension);
            try{
                Utils.downloadToFile(imageUrl, file);
            }catch(IOException ex){
                log.appendToLog(String.format(FAILED_DOWNLOAD_LOG_MASK, imageUrl), Status.ERROR);
            }
        }
        
        if(listener != null) listener.progressed(log); //parcial log for each link

        //PARSING LINKS
        if(depth > 0){
            depth--;
            Elements links = doc.getElementsByTag("a"); //TODO: link with image as href
            for (int i = 0; i < links.size(); i++) {
                String linkUrl = links.get(i).absUrl("href");
                if(linkUrl.startsWith(root)){ //TODO: better solution?
                    download(linkUrl, i, false);
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getPath() {
        return path;
    }

    public int getDepth() {
        return depth;
    }

    public String getRoot() {
        return root;
    }
    
    public String getDestination() {
        return destination;
    }

    @Override
    public int getProcessesCount() {
        return getDepth()+1;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
    // </editor-fold>
    
}
