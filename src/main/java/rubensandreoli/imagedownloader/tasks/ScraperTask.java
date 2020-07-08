package rubensandreoli.imagedownloader.tasks;

import rubensandreoli.commons.utils.Configs;
import rubensandreoli.commons.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScraperTask extends BasicTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String INVALID_URL_MSG = "Invalid URL.";
    private static final String INVALID_BOUNDS_MSG_MASK = "Search depth has a limit of %d";
    
    private static final String CONNECTION_LOG_MASK = "Connected to %s"; //url
    private static final String CONNECTION_FAILED_LOG_MASK = "Failed connecting to %s"; //url
    private static final String DOWNLOAD_LOG_MASK = "Downloaded image to %s"; //file
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading/saving from %s"; //url
    private static final String DOWNLOAD_TOTAL_LOG_MASK = "%s image(s) downloaded"; //success count
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    public static final int DEPTH_LIMIT;
    static{
        DEPTH_LIMIT = Configs.values.get("scraper:depth_limit", 3);
    }
    // </editor-fold>
     
    //SOURCE
    private String path;
    private String root;
    
    private int depth;

    @Override
    protected void run() {
        processPage(path);
    }
        
    private void processPage(String url){
        try {
            //CONNECTION
            Document d = Utils.connect(url);
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
        int success = 0;
        for (Element image : images) {
            if(isInterrupted()) break; //INTERRUPT PROCCESS
            
            //FIX URL
            String imageUrl = image.absUrl("src");
            int removeIndex = imageUrl.indexOf("?"); //fix some images url with values after extension
            if(removeIndex > 0){
                imageUrl = imageUrl.substring(0, removeIndex);
            }
            
            //OUTPUT FILE
            String filename = Utils.parseFilename(imageUrl);
            String extension = Utils.parseExtension(imageUrl);
            File file = Utils.createValidFile(getDestination(), filename, extension);
            
            //DOWNLOAD TO FILE
            try {
                Utils.downloadToFile(imageUrl, file);
                report(ProgressLog.INFO, DOWNLOAD_LOG_MASK, file);
                success++;
                Utils.sleepRandom(CONNECTION_MIN_TIMEOUT, CONNECTION_MAX_TIMEOUT);
            } catch (IOException ex) {
                report(ProgressLog.ERROR, DOWNLOAD_FAILED_LOG_MASK, imageUrl);
            }
        }
        report(ProgressLog.INFO, DOWNLOAD_TOTAL_LOG_MASK, success);
    }
    
    private void processLinks(Document d){
        Elements links = d.getElementsByTag("a");
        increaseWorkload(links.size());
        for (Element link : links) {
            if(isInterrupted()) break; //INTERRUPT PROCCESS
            String linkUrl = link.absUrl("href");
            if(linkUrl.startsWith(root)){ //TODO: better solution?
                processPage(linkUrl);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSource(String path) throws MalformedURLException{
        try {
            URL url = new URL(path);
            this.path = url.toString();
            root = url.getAuthority();
        } catch (MalformedURLException ex) {
            throw new MalformedURLException(INVALID_URL_MSG);
        }
    }
    
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
        return root;
    }
    // </editor-fold>

}
