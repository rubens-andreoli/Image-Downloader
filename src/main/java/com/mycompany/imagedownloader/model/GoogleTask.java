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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** References:
 * https://javapapers.com/java/glob-with-java-nio/
 * https://stackoverflow.com/questions/5923817/how-to-clone-an-inputstream/5924132
 * https://stackoverflow.com/questions/12107049/how-can-i-make-a-copy-of-a-bufferedreader
 */
public class GoogleTask implements Task {

    private static final String IMAGE_SUPPORTED_GLOB = "*.{jpg,jpeg,bmp,gif,png}";
    private static final String GOOGLE_URL = "https://www.google.com/searchbyimage/upload";
    private static final int SEARCH_MAX_TIMEOUT = 2000; //ms
    private static final int SEARCH_MIN_TIMEOUT = 1000; //ms
    private static final double MIN_FILESIZE_RATIO = 0.25;
    
    private static final String RESPONSE_LINK_PREFIX_TOKEN = "/search?tbs=simg:";
    private static final String RESPONSE_LINK_TEXT_TOKEN = "Todos os tamanhos";
    private static final String SUB_RESPONSE_SCRIPT_DATA_TOKEN = "AF_initDataCallback";
    private static final String TUMBLR_IMAGE_START_TOKEN = "tumblr_";
    private static final String SUB_RESPONSE_SPLIT_LINK_TOKEN = ",";
    private static final String SUB_RESPONSE_CLEAR_LINK_REGEX = "[\\[\\]\\\"\\\"]";
    private static final String IMAGE_LINK_REGEX = "((\\[\"http.*\\/\\/(?!encrypted)).*\\])"; //"(\\[\"http.*])"
    
    private static final String MISSING_DESTINATION_MSG_MASK = "Detination folder [%s] not found.";
    private static final String MISSING_SOURCE_MSG_MASK = "Source folder [%s] not found.";
    private static final String EMPTY_SOURCE_MSG_MASK = "Source folder [%s] doesn't contain any image file.";
    private static final String INVALID_BOUNDS_MSG = "Starting index must be greater than 0 and smaller than the number of image files in the source folder.";
       
    private Random random = new Random();
    private String source;
    private List<Path> images;
    private String destination;
    private int startIndex;
    
    @Override
    public boolean perform(ProgressListener listener) {
        if(images==null || destination==null || startIndex>images.size()){ //TODO: test last condition
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
                System.err.println("ERROR: UNEXPECTED EXCEPTION "+ex.getMessage());
            }
        }
        return true;
    }
    
    private void searchWithFile(Path file) throws Exception{
        try(var fileStream = new BufferedInputStream(Files.newInputStream(file));  //TODO: test if splitting input stream instead of reading twice is more eficient
                var cache = new ByteArrayOutputStream();){ //TODO: not sure if stream is needed here; use byte[] from fileStream directly?
            fileStream.transferTo(cache);
            byte[] imageBytes = cache.toByteArray();
            
            //GET IMAGE INFO FOR COMPARISON
            int size = imageBytes.length;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            int height = image.getHeight();
            int width = image.getWidth();
            System.out.println("LOG: LOADING IMAGE "+file+" -> "+width+":"+height+" ["+size+"]");
            
            //PREPARE ENTITY
            MultipartEntity entity = new MultipartEntity(); 
            entity.addPart("encoded_image", new InputStreamBody(new ByteArrayInputStream(imageBytes), file.getFileName().toString()));
            HttpPost post = new HttpPost(GOOGLE_URL);
            post.setEntity(entity); 
            
            //POST
            try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
                HttpResponse response = client.execute(post);
                String site = response.getFirstHeader("location").getValue();
//                System.out.println("LOG: REPONSE LINK "+ site);
                List<GoogleImage> googleImages = parseResponse(Jsoup.connect(site).get());
                if(googleImages!=null && !googleImages.isEmpty()){
                    downloadLargest(googleImages, width, height, size);
                } else {
                    System.out.println("WARNING: NO IMAGES FOUND");
                }
            }catch(IOException ex){
                System.err.println("ERROR: FAILED CONNECTING/UPLOADING FILE "+file);
            }
        }catch(IOException ex){
            System.err.println("ERROR: FAILED READING FILE "+file);
        }
    }
    
    private List<GoogleImage> parseResponse(Document doc) throws IOException{    
        //FIND CORRECT LINK
        String responseLink = null;
        for (Element e : doc.getElementsByTag("a")) {
            String ref = e.attr("href");
            if(ref.startsWith(RESPONSE_LINK_PREFIX_TOKEN) && e.text().equals(RESPONSE_LINK_TEXT_TOKEN)){
                responseLink = e.absUrl("href");
                break; //just one
            }
        }
        if(responseLink==null){
            return null; //if null: no similar images found
        }
//        System.out.println("LOG: RESPONSE IMAGES LINK "+responseLink);

        //GET IMAGE LINKS
        List<GoogleImage> googleImages = new ArrayList<>();
        Pattern p = Pattern.compile(IMAGE_LINK_REGEX);
        Document subdoc = Jsoup.connect(responseLink).get();
        subdoc.getElementsByTag("script").forEach(s -> {
            String script = s.data();
            if(script.startsWith(SUB_RESPONSE_SCRIPT_DATA_TOKEN)){
                Matcher m = p.matcher(script);
                while(m.find()) {
                    String[] info = m.group(0) //first group of the pattern regex
                            .replaceAll(SUB_RESPONSE_CLEAR_LINK_REGEX, "")
                            .split(SUB_RESPONSE_SPLIT_LINK_TOKEN);
                    if(info.length == 3){
//                        System.out.println("LOG: LINK " + info[0]);
                        googleImages.add(new GoogleImage(info[0], info[1], info[2]));
                    }/*else{
                        System.out.println("LOG: DISCARDED LINK "+Arrays.asList(info));
                    }*/
                    
                }
            }
        });
        return googleImages; //if empty: failed finding images urls
    }
    
    private void downloadLargest(List<GoogleImage> googleImages, int width, int height, int size) {    
        //LOOK FOR IMAGE BIGGER THAN SOURCE IMAGE AND GOOGLE IMAGES
        GoogleImage biggest = null;
        for (GoogleImage image : googleImages) {
            if(image.width>width && image.height>height){
                if(biggest==null || (image.width>biggest.width && image.height>biggest.height)){
                    biggest = image;
                }
            }
        }
        if(biggest == null) return;
        System.out.println("LOG: BIGGER IMAGE FOUND "+biggest.url);

        //SAVE FILE AND CHECK SIZE
        File file = Utils.generateFile(destination, biggest.getFilename(), biggest.getExtension());
        try{
            Utils.saveFileFromURL(biggest.url, file);
            if(file.length() < (size*MIN_FILESIZE_RATIO)){
                System.out.println("ERROR: FILE ["+file.getAbsolutePath()+"] SAVED WITH ERRORS ["+file.length()+"] vs ["+size+"]");
                if(biggest.getFilename().startsWith(TUMBLR_IMAGE_START_TOKEN) && resolveTumblr(biggest)) return;
                throw new IOException();  //try other image
            }
        }catch(IOException ex){
            googleImages.remove(biggest);
            if(!googleImages.isEmpty()){
                downloadLargest(googleImages, width, height, size);
            }
        }
    }

    private boolean resolveTumblr(GoogleImage image){
        try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
            HttpGet requisicao = new HttpGet(image.url);
            requisicao.addHeader("Accept", "image/webp,image/apng,*/*");
            requisicao.addHeader("referer", image.url);
//            requisicao.addHeader("sec-fetch-dest", "image");
//            requisicao.addHeader("sec-fetch-mode", "no-cors");
//            requisicao.addHeader("sec-fetch-site", "same-origin");
            HttpResponse resposta = client.execute(requisicao);
            byte[] bytes = EntityUtils.toByteArray(resposta.getEntity());
            Path filepath = Path.of(Utils.generateFilepath(destination, image.getFilename(), image.getExtension()));
            Files.write(filepath, bytes);
        } catch (IOException ex) {
            System.out.println("ERROR: FAILED TO CONNECT/DOWNLOAD TUMBLR IMAGE "+image.url);
            return false;
        }
        return true;
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSource(String folder)throws IOException{
        Path f = Paths.get(folder);
        if(!Files.exists(f) || !Files.isDirectory(f)){
            throw new IOException(String.format(MISSING_SOURCE_MSG_MASK, folder));
        }
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(f, IMAGE_SUPPORTED_GLOB)){
            List<Path> paths = new ArrayList<>();
            for (Path file : contents) {
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

    public String getDestination() { //not used
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
