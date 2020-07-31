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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import org.jsoup.nodes.Element;
import rubensandreoli.commons.utils.IntegerUtils;

/** 
 * References:
 * https://javapapers.com/java/glob-with-java-nio/
 * https://stackoverflow.com/questions/5923817/how-to-clone-an-inputstream/5924132
 * https://stackoverflow.com/questions/12107049/how-can-i-make-a-copy-of-a-bufferedreader
 * https://stackoverflow.com/questions/3850074/regex-until-but-not-including
 * https://stackoverflow.com/questions/38581427/why-non-static-final-member-variables-are-not-required-to-follow-the-constant-na/38581517
 */
public class GoogleTask extends DownloadTask{

    // <editor-fold defaultstate="collapsed" desc=" IMAGE INFO "> 
    public static class ImageInfo implements Comparable<ImageInfo>{

        public final String path;
        public final int width;
        public final int height;

        private String filename;
        private String extension;
        private long size;

        public ImageInfo(String path, int width, int height) {
            this.path = path;
            this.width = width;
            this.height = height;
        }

        public ImageInfo(String path, String width, String height) {
            this(path, IntegerUtils.parseInteger(width), IntegerUtils.parseInteger(height));
        }

        public String getFilename() {
            if(filename == null) filename = FileUtils.parseFilename(path);
            return filename;
        }

        public String getExtension() {
            if(extension == null) extension = FileUtils.parseExtension(path);
            return extension;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean largerThan(ImageInfo i){
            return compareTo(i)>0;
        }

        public boolean smallerThan(ImageInfo i){
            return compareTo(i)<0;
        }

        @Override
        public int hashCode() {
            return 61 + Objects.hashCode(this.path);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            return Objects.equals(this.path, ((ImageInfo) obj).path);
        }

        @Override
        public int compareTo(ImageInfo i) { //image is larger only if proportionally
            if(width > i.width && height > i.height) return 1;
            return (width == i.width && height == i.height)? 0: -1;
        }

    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String IMAGE_SUPPORTED_GLOB = "*.{jpg,jpeg,bmp,gif,png}";
    private static final String GOOGLE_URL = "https://www.google.com/searchbyimage/upload";
    private static final String ATENTION_FOLDER = "low";
    
    private static final String RESPONSE_LINK_PREFIX = "/search?tbs=simg:";
    private static final String RESPONSE_SCRIPT_PREFIX = "AF_initDataCallback";
    private static final String TUMBLR_IMAGE_PREFIX = "tumblr_";
    private static final String IMAGE_LINK_CLEAR_REGEX = "[\\[\\]\\\"\\\"]";
    private static final String IMAGE_LINK_DELIMITER = ",";
    private static final String IMAGE_LINK_REGEX = "((\\[\"http.*\\/\\/(?!encrypted)).*\\])";
    
    private static final String EMPTY_SOURCE_MSG_MASK = "Source folder [%s] doesn't contain any image file.";
    private static final String INVALID_BOUNDS_MSG_MASK = "Starting index must be greater than 0 and smaller than the number of image files [%d] in the source folder.";
    private static final String FAILED_DESTINATION_MSG_MASK = "Failed creating a subfolder inside folder:\n%s\nTry setting a destination manually.";
    
    private static final String IMAGE_NUMBER_LOG_MASK = "[%s]"; //index
    private static final String LOADING_IMAGE_LOG = "Loading image...";
    private static final String NO_SIMILAR_LOG = "No similar images were found";
    private static final String FAILED_UPLOADING_LOG= "Failed connecting/uploading file";
    private static final String FAILED_READING_FILE_LOG = "Failed reading file";
    private static final String NO_BIGGER_LOG_MASK = "No bigger images were found within %d image(s)"; //image count
    private static final String BIGGER_FOUND_LOG_MASK = "Found image with bigger dimensions [%d:%d] > [%d:%d]"; //width, height, source width, source height
    private static final String CORRUPTED_FILE_LOG_MASK = "Downloaded image may be corrupted [%,d bytes] %s"; //size, path
    private static final String FAILED_DOWNLOADING_LOG = "Failed downloading/saving file";
    private static final String TRY_OTHER_IMAGE_LOG = "Attempting to find another image";
    private static final String FAILED_TUMBLR_LOG_MASK = "Failed resolving Tumblr image %s"; //url
    private static final String SUCCESS_TUMBLR_LOG_MASK = "Succeeded resolving Tumblr image %s"; //url
    private static final String UNEXPECTED_LOG_MASK = "Unexpected exception %s"; //exception message
    private static final String DELETING_FILE_LOG_MASK = "Deleting corrupted file [%,d bytes]";
    private static final String SMALLER_THAN_SOURCE_LOG = "Image has a smaller file size than source";
    private static final String BIGGER_SIZE_LOG_MASK = "Image found has a bigger file size also [%,d bytes] > [%,d bytes]";
    private static final String NO_NEW_IMAGES_LOG ="No new images were found";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    private static final int DISCONNECTED_THREASHOLD;
    private static final double MIN_FILESIZE_RATIO; //to source image
    private static final int MIN_FILESIZE; //bytes
    private static final String RESPONSE_LINK_TEXT; //pt-br
    private static final String SUBFOLDER;
    static{
        DISCONNECTED_THREASHOLD = Configs.values.get("google:fail_threashold", 10, 1);
        MIN_FILESIZE = Configs.values.get("google:filesize_min", 25600);
        MIN_FILESIZE_RATIO = Configs.values.get("google:filesize_suspect", 0.25, 0.1, 1);
        RESPONSE_LINK_TEXT = Configs.values.get("google:link_text_marker", "Todos os tamanhos");
        SUBFOLDER = Configs.values.get("google:subfolder_name", "copies");
    }
    // </editor-fold>
    
    private final String sourceFolder;
    private final List<Path> images;
    private int startIndex;
    private boolean retrySmall;
    private ProgressLog log;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public GoogleTask(String folder) throws IOException{
        Path path = getFolderPath(folder);
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(path, IMAGE_SUPPORTED_GLOB)){
            images = new ArrayList<>();
            for (Path file : contents) {
                images.add(file);
            }
            if(images.isEmpty()) throw new IOException(String.format(EMPTY_SOURCE_MSG_MASK, folder));
            sourceFolder = folder;
        }
    }
    
    @Override
    protected void run() {
        images.sort((p1,p2) -> p1.getFileName().compareTo(p2.getFileName()));
        setWorkload(getImageCount()-startIndex);
        
        for (int i = startIndex; i < images.size(); i++) {
            if(isInterrupted() || getFails() >= DISCONNECTED_THREASHOLD) break; //INTERRUPT EXIT POINT
            log = new ProgressLog(increaseProgress(), getWorkload());
            log.appendLine(IMAGE_NUMBER_LOG_MASK, i);
            processImage(images.get(i));
            report(log);
        }
    }
    
    private void processImage(Path path){
        try(var imageStream = new BufferedInputStream(Files.newInputStream(path))){
            //LOAD IMAGE
            byte[] imageBytes = imageStream.readAllBytes();
            var bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            log.appendLine(ProgressLog.INFO, LOADING_IMAGE_LOG);
            log.appendLine(ProgressLog.INFO, path.toString());
            ImageInfo sourceInfo = new ImageInfo(path.toString(), bufferedImage.getWidth(), bufferedImage.getHeight());
            sourceInfo.setSize(imageBytes.length);
            
            //PREPARE ENTITY
            MultipartEntity entity = new MultipartEntity(); 
            entity.addPart("encoded_image", new InputStreamBody(new ByteArrayInputStream(imageBytes), path.getFileName().toString()));
            HttpPost post = new HttpPost(GOOGLE_URL);
            post.setEntity(entity); 
            
            //POST
            try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
                HttpResponse response = client.execute(post);
                String responseLink = response.getFirstHeader("location").getValue();
                long start = System.currentTimeMillis();
                
                //PROCESS RESPONSE
                List<ImageInfo> googleImages = processResponse(responseLink);
                if(googleImages != null){
                    downloadLargest(googleImages, sourceInfo);
                }else{
                    log.appendLine(ProgressLog.WARNING, NO_SIMILAR_LOG);
                }
                if(System.currentTimeMillis() - start < CONNECTION_MIN_COOLDOWN){ //sleep if succeded and not enough time has passed
                    sleepRandom();
                }
            } catch (IOException ex){
                log.appendLine(ProgressLog.ERROR, FAILED_UPLOADING_LOG);
                increaseFails();
            }
        } catch (IOException ex) {
            log.appendLine(ProgressLog.ERROR, FAILED_READING_FILE_LOG);
        } catch (Exception ex) {
            log.appendLine(ProgressLog.CRITICAL, UNEXPECTED_LOG_MASK, ex.getMessage());
        }
    }

    private List<ImageInfo> processResponse(String responseLink) throws IOException{    
        //FIND CORRECT LINK
        String similarLink = null;
        for (Element e : connect(responseLink).getElementsByTag("a")) {
            String ref = e.attr("href");
            if(ref.startsWith(RESPONSE_LINK_PREFIX) && e.text().equals(RESPONSE_LINK_TEXT)){
                similarLink = e.absUrl("href");
                break; //just one
            }
        }
        if(similarLink == null){
            return null; //no similar images found
        }

        List<ImageInfo> googleImages = new ArrayList<>();
        Pattern p = Pattern.compile(IMAGE_LINK_REGEX);
        connect(similarLink).getElementsByTag("script").forEach(s -> {
            String script = s.data();
            if(script.startsWith(RESPONSE_SCRIPT_PREFIX)){
                Matcher m = p.matcher(script);
                while(m.find()) {
                    String[] info = m.group(0) //first group of the pattern regex
                            .replaceAll(IMAGE_LINK_CLEAR_REGEX, "")
                            .split(IMAGE_LINK_DELIMITER);
                    if(info.length == 3){ //contains width and height information
                        googleImages.add(new ImageInfo(info[0], info[2], info[1]));
                    }
                }
            }
        });
        return googleImages.isEmpty()? null:googleImages; //if empty: failed finding images urls
    }
    
    protected void downloadLargest(List<ImageInfo> googleImages, ImageInfo source) {    
        //LOOK FOR LARGEST GOOGLE IMAGE LARGER THAN SOURCE (not worth sorting list before)
        ImageInfo largest = null;
        for (ImageInfo image : googleImages) {
            if(image.largerThan(source) && (largest==null || image.largerThan(largest))){
                largest = image;
            }
        }
        if(largest == null){
            log.appendLine(ProgressLog.INFO, NO_BIGGER_LOG_MASK, googleImages.size());
            return;
        }
        log.appendLine(ProgressLog.INFO, BIGGER_FOUND_LOG_MASK, largest.width, largest.height, source.width, source.height);
        log.appendLine(ProgressLog.INFO, largest.path);
        
        boolean failed = false;
        try{
            //SAVE FILE
            File file = FileUtils.createValidFile(getDestination(), largest.getFilename(), largest.getExtension());
            largest.setSize(FileUtils.downloadToFile(largest.path, file, CONNECTION_TIMEOUT, READ_TIMEOUT));
            
            //TESTS
            boolean corrupt = reviseCorrupt(file, largest.getSize(), source.getSize());
            if(corrupt && largest.getFilename().startsWith(TUMBLR_IMAGE_PREFIX)){
                File tmp = resolveTumblr(largest, source.getSize());
                if(tmp != null){
                    file = tmp;
                    corrupt = false;
                }
            }
            boolean small = (retrySmall && !corrupt)? reviseSmall(file, largest.getSize(), source.getSize()):false;
            if(corrupt || small) failed = true;
            
        }catch(IOException ex){
            log.appendLine(ProgressLog.ERROR, FAILED_DOWNLOADING_LOG);
            //LAST RESORT (rarely solves the problem if failed because of path)
            if(largest.path.contains("?")){ 
                googleImages.add(new ImageInfo(
                        largest.path.substring(0, largest.path.lastIndexOf("?")), 
                        largest.width, 
                        largest.height)
                );
            }
            failed = true;
        }
        
        //RETRY
        if(failed){
            googleImages.remove(largest);
            if(googleImages.isEmpty()){
                log.appendLine(ProgressLog.WARNING, NO_NEW_IMAGES_LOG);
            }else{
                log.appendLine(ProgressLog.INFO, TRY_OTHER_IMAGE_LOG);
                downloadLargest(googleImages, source);
            }
        }else{
            increaseSuccesses();
            resetFails(); //successive fails
        }
    }
    
    private boolean reviseSmall(File file, long size, long sourceSize){
        if(size < sourceSize){
            log.appendLine(ProgressLog.WARNING, SMALLER_THAN_SOURCE_LOG);
            FileUtils.moveFileToChild(file, ATENTION_FOLDER);
            return true; //even if it failed to move, try other images
        }
        log.appendLine(ProgressLog.INFO, BIGGER_SIZE_LOG_MASK, size, sourceSize);
        return false;
    }
    
    private boolean reviseCorrupt(File file, long size, long sourceSize){
        //BELOW FILESIZE THRESHOLD
        if(size < MIN_FILESIZE){
            if(FileUtils.deleteFile(file)){
               log.appendLine(ProgressLog.WARNING, DELETING_FILE_LOG_MASK, size);
            }
            return true;
        }
        
        //TOO SMALL COMPARED TO SOURCE
        if (!retrySmall && size < (sourceSize*MIN_FILESIZE_RATIO)){
            log.appendLine(ProgressLog.WARNING, CORRUPTED_FILE_LOG_MASK, file.length(), file.getAbsolutePath());
            FileUtils.moveFileToChild(file, ATENTION_FOLDER);
            return true;
        }
        return false;
    }
    
    private boolean reviseCorrupt(File file, long sourceSize){
        return reviseCorrupt(file, FileUtils.getFileSize(file), sourceSize);
    }

    private File resolveTumblr(ImageInfo image, long sourceSize){
        File file = null;
        try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(USER_AGENT).build()){
            //REQUEST
            HttpGet requisicao = new HttpGet(image.path);
            requisicao.addHeader("Accept", "image/webp,image/apng,*/*");
            requisicao.addHeader("referer", image.path);
            HttpResponse response = client.execute(requisicao);
            //SAVE BYTES
            byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            Path filepath = Path.of(FileUtils.createValidFile(getDestination(), image.getFilename(), image.getExtension()).getAbsolutePath());
            Files.write(filepath, bytes);
            file = filepath.toFile();
        } catch (IOException ex) {
            System.err.println("ERROR: Failed connecting Tumblr "+ex.getMessage());
        }
        
        //TEST IF CORRUPT AND LOG
        if(file != null && !reviseCorrupt(file, sourceSize)){
            log.appendLine(ProgressLog.INFO, SUCCESS_TUMBLR_LOG_MASK, image.path);
        }else{
            log.appendLine(ProgressLog.ERROR, FAILED_TUMBLR_LOG_MASK, image.path);
        }
        return file;
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS ">    
    @Override
    public void setDestination(String folder) throws IOException {
        if(folder == null || folder.isEmpty()){ //TODO: change to panel? create if don't exist (delete if canceled not started?
            File subfolder = new File(sourceFolder, SUBFOLDER);
            if(!subfolder.isDirectory()){
                if(!subfolder.mkdir()){
                   throw new IOException(String.format(FAILED_DESTINATION_MSG_MASK, sourceFolder));
                }
            }
            folder = subfolder.getAbsolutePath();
        }
        super.setDestination(folder);
    }
    
    public void setStartIndex(int startIndex) throws BoundsException {
        if(startIndex < 0 || startIndex>images.size()-1){
            throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, images.size()));
        }
        this.startIndex = startIndex;
    }

    public void setRetrySmall(boolean b) {
        retrySmall = b;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public int getImageCount(){
        return images==null?0:images.size()-1;
    }

    public String getSource() {
        return sourceFolder;
    }

    public Set<Path> getSourceImages(int fromIndex){
        return new HashSet<>(images.subList(fromIndex, images.size()));
    }

    public int getStartIndex() {
        return startIndex;
    }
    
    public ProgressLog getCurrentLog() {
        return log;
    }
    // </editor-fold>

}
