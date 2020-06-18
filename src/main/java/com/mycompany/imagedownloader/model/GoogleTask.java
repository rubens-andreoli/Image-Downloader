package com.mycompany.imagedownloader.model;

import com.mycompany.imagedownloader.model.ProgressLog.Status;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
 * https://stackoverflow.com/questions/3850074/regex-until-but-not-including
 * https://stackoverflow.com/questions/38581427/why-non-static-final-member-variables-are-not-required-to-follow-the-constant-na/38581517
 */
public class GoogleTask implements Task {

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String IMAGE_SUPPORTED_GLOB = "*.{jpg,jpeg,bmp,gif,png}";
    private static final String GOOGLE_URL = "https://www.google.com/searchbyimage/upload";
    
    private static final String RESPONSE_LINK_PREFIX = "/search?tbs=simg:";
    private static final String RESPONSE_SCRIPT_PREFIX = "AF_initDataCallback";
    private static final String TUMBLR_IMAGE_PREFIX = "tumblr_";
    private static final String IMAGE_LINK_CLEAR_REGEX = "[\\[\\]\\\"\\\"]";
    private static final String IMAGE_LINK_DELIMITER = ",";
    private static final String IMAGE_LINK_REGEX = "((\\[\"http.*\\/\\/(?!encrypted)).*\\])";
    
    private static final String NO_FOLDER_MSG = "No folder was selected.";
    private static final String NOT_FOLDER_MSG_MASK = "Path [%s] is not a folder, or couldn't be found.";
    private static final String FOLDER_PERMISSION_MSG_MASK = "You don't have folder [%s] read/write permissions.";
    private static final String EMPTY_SOURCE_MSG_MASK = "Source folder [%s] doesn't contain any image file.";
    private static final String INVALID_BOUNDS_MSG = "Starting index must be greater than 0 and smaller than the number of image files in the source folder.";
    
    private static final String LOADING_IMAGE_LOG = "Loading image...\n";
    private static final String NO_SIMILAR_LOG = "No similar images were found\n";
    private static final String FAILED_UPLOADING_LOG = "Failed connecting/uploading file\n";
    private static final String FAILED_READING_FILE_LOG = "Failed reading file\n";
    private static final String NO_BIGGER_LOG_MASK = "No bigger images were found within %d image(s)\n"; //image count
    private static final String BIGGER_FOUND_LOG_MASK = "Found image with bigger dimensions [%d:%d] > [%d:%d]\n"; //width, height, source width, source height
    private static final String CORRUPTED_FILE_LOG_MASK = "Downloaded image may be corrupted [%,d bytes] %s\n"; //size, path
    private static final String FAILED_DOWNLOADING_LOG = "Failed downloading/saving file\n";
    private static final String TRY_OTHER_IMAGE_LOG = "Attempting to find another image\n";
    private static final String FAILED_TUMBLR_LOG_MASK = "Failed resolving Tumblr image %s\n"; //url
    private static final String SUCCESS_TUMBLR_LOG_MASK = "Succeeded resolving Tumblr image %s\n"; //url
    private static final String UNEXPECTED_LOG_MASK = "Unexpected exception %s\n"; //exception message
    private static final String DELETING_FILE_LOG_MASK = "Deleting corrupted file [%,d bytes]\n";
    private static final String SMALLER_THAN_SOURCE_LOG = "Image has a smaller file size than source\n";
    private static final String BIGGER_SIZE_LOG_MASK = "Image found has a bigger file size also [%,d bytes] > [%,d bytes]\n";
    private static final String NO_NEW_IMAGES_LOG ="No new images were found\n";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    private final int searchMaxTimeout; //ms
    private final int searchMinTimeout; //ms
    private final double minFilesizeRatio; //to source image
    private final int minFilesize; //bytes
    private final String responseLinkText; //pt-br
    private final String subfolder;
    // </editor-fold>
    
    private String source;
    private List<Path> images;
    private int startIndex;
    private String destination;
    private boolean retrySmall;
    
    private ProgressListener listener;
    private volatile boolean running;
    private ProgressLog log;
    
    public GoogleTask(){
        searchMinTimeout = Configs.values.get("google:timout_min", 500);
        int max = Configs.values.get("google:timeout_max", 1000);
        searchMaxTimeout = max<searchMinTimeout? searchMinTimeout:max;
        minFilesize = Configs.values.get("google:filesize_min", 25600);
        minFilesizeRatio = Configs.values.get("google:filesize_suspect", 0.25, 0.1, 1);
        responseLinkText = Configs.values.get("google:link_text_marker", "Todos os tamanhos");
        subfolder = Configs.values.get("google:subfolder_name", "low");
    }
    
    @Override
    public void start() {
        if(images==null || destination==null || startIndex>images.size()){ //TODO: test last condition
            return;
        }
        running = true;
        images.sort((p1,p2) -> p1.getFileName().compareTo(p2.getFileName()));
        for (int i = startIndex; i < images.size(); i++) {
            if(!running) break;
            Utils.sleepRandom(searchMinTimeout, searchMaxTimeout);
            log = new ProgressLog(i);
            searchWithFile(images.get(i));
            if(listener!= null) listener.progressed(log);
        }
        running = false; //not really needed
    }
    
    @Override
    public void stop() {
        running = false;
    }
    
    private void searchWithFile(Path file){
        try(var fileStream = new BufferedInputStream(Files.newInputStream(file));  //TODO: test if splitting input stream instead of reading twice is more eficient
                var cache = new ByteArrayOutputStream();){ //TODO: not sure if stream is needed here; use byte[] from fileStream directly?
            fileStream.transferTo(cache);
            byte[] imageBytes = cache.toByteArray();
            
            //GET IMAGE INFO FOR COMPARISON
            int size = imageBytes.length;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            int height = image.getHeight();
            int width = image.getWidth();
            log.appendToLog(LOADING_IMAGE_LOG, Status.INFO);
            log.appendToLog(file.toString()+"\n", Status.INFO);
            
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
                    log.appendToLog(NO_SIMILAR_LOG, Status.WARNING);
                }
            }catch(IOException ex){
                log.appendToLog(FAILED_UPLOADING_LOG, Status.ERROR);
            }
        }catch(IOException ex){
            log.appendToLog(FAILED_READING_FILE_LOG, Status.ERROR);
        } catch (Exception ex) {
            log.appendToLog(String.format(UNEXPECTED_LOG_MASK, ex.getMessage()), Status.CRITICAL);
        }
    }
    
    private List<GoogleImage> parseResponse(Document doc) throws IOException{    
        //FIND CORRECT LINK
        String responseLink = null;
        for (Element e : doc.getElementsByTag("a")) {
            String ref = e.attr("href");
            if(ref.startsWith(RESPONSE_LINK_PREFIX) && e.text().equals(responseLinkText)){
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
            if(script.startsWith(RESPONSE_SCRIPT_PREFIX)){
                Matcher m = p.matcher(script);
                while(m.find()) {
                    String[] info = m.group(0) //first group of the pattern regex
                            .replaceAll(IMAGE_LINK_CLEAR_REGEX, "")
                            .split(IMAGE_LINK_DELIMITER);
                    if(info.length == 3){
//                        System.out.println("LOG: LINK " + info[0]);
                        googleImages.add(new GoogleImage(info[0], info[2], info[1]));
                    }/*else{
                        System.out.println("LOG: DISCARDED LINK "+Arrays.asList(info));
                    }*/
                    
                }
            }
        });
        return googleImages; //if empty: failed finding images urls
    }
    
    private void downloadLargest(List<GoogleImage> googleImages, int sourceWidth, int sourceHeight, int sourceSize) {    
        //LOOK FOR BIGGEST GOOGLE IMAGE BIGGER THAN SOURCE
        GoogleImage biggest = null;
        for (GoogleImage image : googleImages) { //TODO: should I sort list by size before?
            if(image.width>sourceWidth && image.height>sourceHeight){
                if(biggest==null || (image.width>biggest.width && image.height>biggest.height)){
                    biggest = image;
                }
            }
        }
        if(biggest == null){
            log.appendToLog(String.format(NO_BIGGER_LOG_MASK, googleImages.size()), Status.INFO);
            return;
        }
        log.appendToLog(String.format(BIGGER_FOUND_LOG_MASK, biggest.width, biggest.height, sourceWidth, sourceHeight), Status.INFO);
        log.appendToLog(biggest.url+"\n", Status.INFO);
        
        boolean failed = false;
        try{
            //SAVE FILE
            File file = Utils.createValidFile(destination, biggest.getFilename(), biggest.getExtension());
            long imgSize = Utils.downloadToFile(biggest.url, file);
            
            //TESTS
            boolean corrupt = reviseCorrupt(file, imgSize, sourceSize);
            if(corrupt && biggest.getFilename().startsWith(TUMBLR_IMAGE_PREFIX)){
                File tmp = resolveTumblr(biggest, sourceSize);
                if(tmp != null){
                    file = tmp;
                    corrupt = false;
                }
            }
            boolean small = (retrySmall && !corrupt)? reviseSmall(file, imgSize, sourceSize):false;
            if(corrupt || small) failed = true;
            
        }catch(IOException ex){
            log.appendToLog(FAILED_DOWNLOADING_LOG, Status.ERROR);
            //LAST RESORT (rarely solves the problem if failed because of url)
            if(biggest.url.contains("?")){ 
                googleImages.add(new GoogleImage(
                        biggest.url.substring(0, biggest.url.lastIndexOf("?")), 
                        biggest.width, 
                        biggest.height)
                );
            }
            failed = true;
        }
        
        //RETRY
        if(failed){
            googleImages.remove(biggest);
            if(!googleImages.isEmpty()){
                log.appendToLog(TRY_OTHER_IMAGE_LOG, Status.INFO);
                downloadLargest(googleImages, sourceWidth, sourceHeight, sourceSize);
            }else{
                log.appendToLog(NO_NEW_IMAGES_LOG, Status.WARNING);
            }
        }
    }
    
    private boolean reviseSmall(File file, long size, long sourceSize){
        if(size < sourceSize){
            log.appendToLog(SMALLER_THAN_SOURCE_LOG, Status.WARNING);
            Utils.moveFileToChild(file, subfolder);
            return true; //even if it failed to move, try other images
        }
        log.appendToLog(String.format(BIGGER_SIZE_LOG_MASK, size, sourceSize), Status.INFO);
        return false;
    }
    
    private boolean reviseCorrupt(File file, long size, long sourceSize){
        //BELOW FILESIZE THRESHOLD
        if(size < minFilesize){
            if(Utils.deleteFile(file)){
               log.appendToLog(String.format(DELETING_FILE_LOG_MASK, size), Status.WARNING);
            }
            return true;
        }
        //TOO SMALL COMPARED TO SOURCE
        if (!retrySmall && size < (sourceSize*minFilesizeRatio)){
            log.appendToLog(String.format(CORRUPTED_FILE_LOG_MASK, file.length(), file.getAbsolutePath()), Status.WARNING);
            Utils.moveFileToChild(file, subfolder);
            return true;
        }
        return false;
    }
    
    private boolean reviseCorrupt(File file, long sourceSize){
        return GoogleTask.this.reviseCorrupt(file, Utils.getFileSize(file), sourceSize);
    }

    private File resolveTumblr(GoogleImage image, long sourceSize){
        File file = null;
        try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
            //REQUEST
            HttpGet requisicao = new HttpGet(image.url);
            requisicao.addHeader("Accept", "image/webp,image/apng,*/*");
            requisicao.addHeader("referer", image.url);
            HttpResponse response = client.execute(requisicao);
            //SAVE BYTES
            byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            Path filepath = Path.of(Utils.createValidFilepath(destination, image.getFilename(), image.getExtension()));
            Files.write(filepath, bytes);
            file = filepath.toFile();
        } catch (IOException ex) {
            System.err.println("ERROR: Failed connecting Tumblr "+ex.getMessage());
        }
        
        //TEST IF CORRUPT AND LOG
        if(file != null && !reviseCorrupt(file, sourceSize)){
            log.appendToLog(String.format(SUCCESS_TUMBLR_LOG_MASK, image.url), Status.INFO);
        }else{
            log.appendToLog(String.format(FAILED_TUMBLR_LOG_MASK, image.url), Status.ERROR);
        }
        return file;
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSource(String folder)throws IOException{
        Path path = getFolder(folder).toPath();
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(path, IMAGE_SUPPORTED_GLOB)){
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
        getFolder(folder);
        this.destination = folder;
    }
    
    private File getFolder(String folder) throws IOException{
        if(folder==null || folder.isBlank()) throw new IOException(NO_FOLDER_MSG);
        try{
            File file = new File(folder);
            if(file.isDirectory()){
                return file;
            }else{
                throw new IOException(String.format(NOT_FOLDER_MSG_MASK, folder));
            }
        }catch(SecurityException ex){
            throw new IOException(String.format(FOLDER_PERMISSION_MSG_MASK, folder));
        }
    } 

    public void setStartIndex(int startIndex) throws BoundsException {
        if(startIndex < 0 || (images!=null && startIndex>images.size())){
            throw new BoundsException(INVALID_BOUNDS_MSG);
        }
        this.startIndex = startIndex;
    }
    
    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    public void setRetrySmall(boolean b) {
        if(!running) retrySmall = b;
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
