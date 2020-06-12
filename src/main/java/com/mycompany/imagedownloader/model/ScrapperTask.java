package com.mycompany.imagedownloader.model;

import com.mycompany.imagedownloader.view_controller.BoundsException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapperTask implements Task{

    public static final int CONNECTION_TIMEOUT = 2000; //ms
    public static final int DEPTH_LIMIT = 3;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
    private static final String FILENAME_MASK = "%s/%s%s";
    private static final String DUPLICATED_FILENAME_MASK = "%s/%s (%d)%s";
    
    private static final String URL_REGEX = "^(https*:\\/\\/)([^\\/]+\\/)+[^\\/\\s]*\\s$";
    
    private static final String INVALID_URL_MSG = "Invalid URL.";
    private static final String MISSING_DESTINATION_MSG = "Detination folder not found.";
    private static final String INVALID_BOUNDS_MSG = "Search depth has a limit of "+DEPTH_LIMIT;
    
    private String path;
    private String root;
    private String destination;
    private int depth;

    public ScrapperTask(String path, String dest, int depth) throws MalformedURLException, IOException, BoundsException {
        if(depth < 0 || depth > DEPTH_LIMIT){
            throw new BoundsException(INVALID_BOUNDS_MSG);
        }
        if(path.matches(URL_REGEX)){
            throw new MalformedURLException(INVALID_URL_MSG);
        }
        File folder = new File(dest);
        if(!folder.exists() || !folder.isDirectory()){
            throw new IOException(MISSING_DESTINATION_MSG);
        }
        
        this.depth = depth;
        this.path = path;
        int startIndex = path.indexOf(":")+3;
        int index = path.substring(startIndex).indexOf("/")+startIndex;
        root = path.substring(0, index); //TODO: remove www. or other static. ?
//        System.out.println(root);
        destination = dest;
    }
    
    @Override
    public void perform() {
        download(path);
    }
    
    private void download(String url){
        try {
            //JSOUP CONNECTION
            Document doc = Jsoup.connect(url)
//                    .header("Accept", "text/html; charset=UTF-8")
                    .userAgent(USER_AGENT)
                    .get();

            //PARSING IMAGES
            Elements images = doc.getElementsByTag("img");
            for (Element image : images) {
                String imageUrl = image.absUrl("src");
                int removeIndex = imageUrl.indexOf("?"); //fix some images url with values after extension
                if(removeIndex != -1){
                    imageUrl = imageUrl.substring(0, removeIndex);
                }
//                    System.out.println(imageUrl);
                String tempFilename = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
                int extIndex = tempFilename.lastIndexOf(".");
                if(extIndex < 1) return;
                String filename = tempFilename.substring(0, extIndex);
                String extension = tempFilename.substring(extIndex);
                File file = new File(String.format(FILENAME_MASK, destination,filename,extension));
                for(int n=1; file.exists(); n++){
                    file = new File(String.format(DUPLICATED_FILENAME_MASK, destination,filename,n,extension));
                }
//                System.out.println(file.getAbsolutePath());
                try{
                    FileUtils.copyURLToFile(new URL(imageUrl), file, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
                }catch(IOException ex){
                    System.err.println("Failed downloading: "+ imageUrl);
                }
            }

            //PARSING LINKS
            if(depth > 0){
                depth--;
                Elements links = doc.getElementsByTag("a"); //TODO: link with image as href
                for (Element link : links) {
                    String linkUrl = link.absUrl("href");
//                    System.out.println(linkUrl);
                    if(linkUrl.startsWith(root)){ //TODO: better solution?
                        download(linkUrl);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Failed connecting: "+ url);
        }
    }

    public String getPath() {return path;}
    public String getRoot() {return root;}
    public String getDestination() {return destination;}
    
}
