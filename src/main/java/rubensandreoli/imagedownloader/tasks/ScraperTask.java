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
     
    private String path;
    private String root;
    private int depth;
    private int successTotal;

    @Override
    protected int run() {
        processPage(path);
        return successTotal;
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
            if(download(imageUrl, file, MIN_FILESIZE, null)){
                success++;
            }
        }
        successTotal += success;
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
