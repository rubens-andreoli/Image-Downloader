package com.mycompany.imagedownloader.model;

import static com.mycompany.imagedownloader.model.Utils.USER_AGENT;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** References:
 * https://javapapers.com/java/glob-with-java-nio/
 * https://stackoverflow.com/questions/5923817/how-to-clone-an-inputstream/5924132
 * https://stackoverflow.com/questions/12107049/how-can-i-make-a-copy-of-a-bufferedreader
 * @author Morus
 */
public class GoogleTask implements Task {

    private static final String IMAGE_SUPPORTED_GLOB = "*.{jpg,jpeg,bmp,gif,png}";
    private static final String GOOGLE_URL = "https://www.google.com/searchbyimage/upload";
    private static final int SEARCH_MAX_TIMEOUT = 2000; //ms
    private static final int SEARCH_MIN_TIMEOUT = 1000; //ms
    private static final double MIN_FILESIZE_RATIO = 0.3;
    
    private static final String RESPONSE_LINK_PREFIX_MARKER = "/search?tbs=simg:";
    private static final String RESPONSE_LINK_TEXT_MARKER = "Todos os tamanhos";
    private static final String SUB_RESPONSE_SCRIPT_TEXT_MARKER = "AF_initDataCallback";
    private static final String SUB_RESPONSE_CLEAR_LINK_REGEX = "[\\[\\]\\\"\\\"]";
    private static final String SUB_RESPONSE_SPLIT_LINK_MARKER = ",";
    private static final String IMAGE_LINK_REGEX = "((\\[\"http.*\\/\\/(?!encrypted)).*\\])"; //"(\\[\"http.*])"
    
    private static final String MISSING_DESTINATION_MSG_MASK = "Detination folder [%s] not found.";
    private static final String MISSING_SOURCE_MSG_MASK = "Source folder [%s] not found.";
    private static final String EMPTY_SOURCE_MSG_MASK = "Source folder [%s] doesn't contain any image file.";
    private static final String INVALID_BOUNDS_MSG = "Starting index must be greater than 0 and smaller than the number of image files in the source folder.";
    private static final String CONNECTION_FAILED_MASK = "Failed to connect to google while processing image: %s";
       
    private Random random = new Random();
    private String source;
    private List<Path> images;
    private String destination;
    private int startIndex;
    
    @Override
    public boolean perform(ProgressListener listener) {
        if(images==null || destination==null || startIndex>images.size()){
            return false;
        }
        images.sort((p1,p2) -> p1.getFileName().compareTo(p2.getFileName()));
        for (int i = startIndex; i < images.size(); i++) {
            listener.progress();
            try {
                Thread.sleep((int) ((random.nextDouble()*(SEARCH_MAX_TIMEOUT-SEARCH_MIN_TIMEOUT))+SEARCH_MIN_TIMEOUT));
            } catch (Exception ex) {}
            try {
                searchWithFile(images.get(i));
            } catch (Exception ex) {
                System.err.println("FAILED READING FILE: "+images.get(i));
            }
        }
        return true;
    }
    
    private void searchWithFile(Path file) throws Exception{
        try(var fileStream = new BufferedInputStream(Files.newInputStream(file));  //TODO: test if splitting input stream instead of reading twice is more eficient
                var baos = new ByteArrayOutputStream();){ //throw failed to read file
            fileStream.transferTo(baos);
            byte[] imageBytes = baos.toByteArray();
            
            //GET IMAGE INFO FOR COMPARISON
            int size = imageBytes.length;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream((imageBytes)));
            int height = image.getHeight();
            int width = image.getWidth();
            System.out.println("IMAGE: "+file+" -> "+width+":"+height+" ["+size+"]");
            
            //PREPARE ENTITY
            MultipartEntity entity = new MultipartEntity(); 
            entity.addPart("encoded_image", new InputStreamBody(new ByteArrayInputStream(imageBytes), file.getFileName().toString()));
            HttpPost post = new HttpPost(GOOGLE_URL);
            post.setEntity(entity); 
            
            //POST
            try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
                HttpResponse response = client.execute(post);
                String site = response.getFirstHeader("location").getValue();
//                System.out.println("REPONSE LINK: "+ site);
                List<GoogleImage> imagesUrl = parseResponse(Jsoup.connect(site).get());
                if(imagesUrl==null || imagesUrl.isEmpty()){
                    System.out.println("NO IMAGES FOUND");
                    return;
                }
                downloadLargest(imagesUrl, width, height, size);
            }catch(IOException ex){
                throw new IOException(String.format(CONNECTION_FAILED_MASK, file));
            }
        }
    }
    
    private List<GoogleImage> parseResponse(Document doc) throws IOException{    
        //FIND CORRECT LINK
        String prefix = RESPONSE_LINK_PREFIX_MARKER;
        String responseLink = null;
        for (Element e : doc.getElementsByTag("a")) {
            String ref = e.attr("href");
            if(ref.startsWith(prefix) && e.text().equals(RESPONSE_LINK_TEXT_MARKER)){
                responseLink = e.absUrl("href");
                break; //just one
            }
        }
        if(responseLink==null){ //no images found
            return null;
        }
//        System.out.println("IMAGES LINK: "+responseLink);

        //GET IMAGE LINKS
        List<GoogleImage> imagesUrl = new ArrayList<>();
        Pattern p = Pattern.compile(IMAGE_LINK_REGEX);
        Document subdoc = Jsoup.connect(responseLink).get();
        subdoc.getElementsByTag("script").forEach(s -> {
            String script = s.data();
            if(script.startsWith(SUB_RESPONSE_SCRIPT_TEXT_MARKER)){
                Matcher m = p.matcher(script);
                while(m.find()) {
                    String[] info = m.group(0)
                            .replaceAll(SUB_RESPONSE_CLEAR_LINK_REGEX, "")
                            .split(SUB_RESPONSE_SPLIT_LINK_MARKER);
                    if(info.length == 3){
//                        System.out.println("LINK: " + info[0]);
                        imagesUrl.add(new GoogleImage(info[0], info[1], info[2]));
                    }/*else{
                        System.out.println("DISCARDED: "+Arrays.asList(info));
                    }*/
                    
                }
            }
        });
        return imagesUrl; //empty = failed finding images urls
    }
    
    private void downloadLargest(List<GoogleImage> images, int width, int height, int size) {    
        //LOOK FOR IMAGE BIGGER THAN SOURCE IMAGE
        GoogleImage biggest = null;
        for (GoogleImage image : images) {
            if(image.width>width && image.height>height){
                if(biggest==null || (image.width>biggest.width && image.height>image.height)){
                    biggest = image;
                }
            }
        }
        if(biggest == null){
            return;
        }
        
        //GENERATE FILENAME
        File file = Utils.generateFile(destination, biggest.getFilename(), biggest.getExtension());
        System.out.println("BIGGER IMAGE FOUND: "+file.getAbsolutePath());
        //SAVE FILE
        try{
            Utils.saveFileFromURL(biggest.url, file);
//            FileUtils.copyURLToFile(new URL(biggest.url), file, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
            if(file.length() < (size*MIN_FILESIZE_RATIO)){
                System.out.println("FILE SAVED WITH ERRORS ["+file.length()+"] vs ["+size+"]");
                throw new IOException();
            }
        }catch(IOException ex){
//            System.err.println("Failed downloading and saving: "+ biggest.url);
            images.remove(biggest);
            if(!images.isEmpty()){
                downloadLargest(images, width, height, size);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSource(String folder)throws IOException{
        Path f = Paths.get(folder);
        if(!Files.exists(f) || !Files.isDirectory(f)){
            throw new IOException(String.format(MISSING_SOURCE_MSG_MASK, folder));
        }
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(f, IMAGE_SUPPORTED_GLOB)){ //TODO: ordered?
            List<Path> paths = new ArrayList<>();
            for (Path file : contents) {
//                System.out.println(file.getFileName());
                paths.add(file);
            }
            if(paths.isEmpty()){
                throw new IOException(String.format(EMPTY_SOURCE_MSG_MASK, folder));
            }
            images = paths;
            source = folder;
        }
    }
 
    public void setDestination(String folder) throws IOException {
        Path f = Paths.get(folder);
        if(!Files.exists(f) || !Files.isDirectory(f)){
            throw new IOException(String.format(MISSING_DESTINATION_MSG_MASK, folder));
        }
        this.destination = folder;
    }

    public void setStartIndex(int startIndex) throws BoundsException {
        if(startIndex < 0 || (images!=null && startIndex>images.size())){
            throw new BoundsException(INVALID_BOUNDS_MSG);
        }
        this.startIndex = startIndex;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public int getImageCount(){
        return images==null?0:images.size();
    }

    public String getDestination() {
        return destination;
    }

    public Set<Path> getSourceImages(int fromIndex){
        try{
            return new HashSet<>(images.subList(fromIndex, images.size()));
        }catch(RuntimeException ex){
            return null;
        }
    }

    public String getSource() {
        return source;
    }

    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getProcessesCount() {
        return getImageCount()-startIndex;
    }
    // </editor-fold>

}
