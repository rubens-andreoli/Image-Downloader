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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.nodes.Element;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

/**
 * References:
 * https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Content_negotiation/List_of_default_Accept_values
 */
public class Searcher {

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final String DEFAULT_LINK_TEXT = "Todos os tamanhos";
    public static final String GOOGLE_LINK = "https://www.google.com/searchbyimage/upload";
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" IMAGE INFO "> 
    public static class ImageInfo implements Comparable<ImageInfo>{

        public final String path;
        public final int width;
        public final int height;

        private String parent;
        private String name;
        private String filename;
        private String extension;
        private long size;

        public ImageInfo(String path, int width, int height) {
            this(path, null, width, height);
        }
        
        public ImageInfo(Path path, int width, int height) {
            this(path.toString(), path.getFileName().toString(), width, height);
        }
        
        private ImageInfo(String path, String name, int width, int height){
            this.path = path;
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public ImageInfo(String path, String width, String height) {
            this(path, IntegerUtils.parseInteger(width), IntegerUtils.parseInteger(height));
        }
        
        public String getParent(){
            if(parent == null) parent = FileUtils.getParent(path);
            return parent;
        }
        
        public String getName(){
            if(name == null) name = FileUtils.getName(path);
            return name;
        }

        public String getFilename() {
            if(filename == null) filename = FileUtils.getFilename(path);
            return filename;
        }

        public String getExtension() {
            if(extension == null) extension = FileUtils.getExtension(path, ".jpg");
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

    // <editor-fold defaultstate="collapsed" desc=" SEARCH "> 
    public static class Search{
        
        // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
        private static final String RESPONSE_LINK_PREFIX = "/search?tbs=simg:";
        private static final String RESPONSE_SCRIPT_PREFIX = "AF_initDataCallback";
        private static final String IMAGE_LINK_CLEAR_REGEX = "[\\[\\]\\\"\\\"]";
        private static final String IMAGE_LINK_DELIMITER = ",";
        private static final String IMAGE_LINK_REGEX = "((\\[\"http.*\\/\\/(?!encrypted)).*\\])";
        private static final Pattern IMAGE_LINK_PATTERN = Pattern.compile(IMAGE_LINK_REGEX);
        // </editor-fold>
        
        private String linkText;
        private ImageInfo sourceInfo;
        private byte[] sourceBytes;
        private List<ImageInfo> googleImages = new ArrayList<>();
        private long start, duration;
        private String responseLink;

        private Search(Path image) throws IOException{
            try(var imageStream = new BufferedInputStream(Files.newInputStream(image))){
                sourceBytes = imageStream.readAllBytes();
                final var bufferedImage = ImageIO.read(new ByteArrayInputStream(sourceBytes));
                sourceInfo = new ImageInfo(image.toString(), bufferedImage.getWidth(), bufferedImage.getHeight());
                sourceInfo.setSize(sourceBytes.length);
            }
        }

        public void request() throws IOException{
            start = System.currentTimeMillis();        
            final MultipartEntity entity = new MultipartEntity(); 
            entity.addPart("encoded_image", new InputStreamBody(new ByteArrayInputStream(sourceBytes), sourceInfo.getName()));
            final HttpPost post = new HttpPost(GOOGLE_LINK);
            post.setEntity(entity); 

            try(CloseableHttpClient client = HttpClientBuilder.create().setUserAgent(DownloadTask.USER_AGENT).build()){
                final HttpResponse response = client.execute(post);
                sourceBytes = null; //free memory
                responseLink = response.getFirstHeader("location").getValue();
            }
        }

        public int search() throws IOException{
            if(responseLink == null) throw new IllegalStateException("request before searching");
            //FIND LINK
            String similarLink = null;
            for (Element e : DownloadTask.request(responseLink).getElementsByTag("a")) {
                final String ref = e.attr("href");
                if(ref.startsWith(RESPONSE_LINK_PREFIX) && e.text().equals(linkText)){
                    similarLink = e.absUrl("href");
                    break; //just one
                }
            }

            //FIND IMAGES
            if(similarLink != null){
                DownloadTask.request(similarLink).getElementsByTag("script").forEach(s -> {
                    final String script = s.data();
                    if(script.startsWith(RESPONSE_SCRIPT_PREFIX)){
                        final Matcher m = IMAGE_LINK_PATTERN.matcher(script);
                        while(m.find()) {
                            final String[] info = m.group(0) //first group of the pattern regex
                                    .replaceAll(IMAGE_LINK_CLEAR_REGEX, "")
                                    .split(IMAGE_LINK_DELIMITER);
                            if(info.length == 3){ //contains width and height information
                                googleImages.add(new ImageInfo(info[0], info[2], info[1]));
                            }
                        }
                    }
                });
            }
            duration = System.currentTimeMillis() - start;
            return googleImages.size();
        }

        public long getDuration() {
            return duration;
        }

        public ImageInfo getSourceInfo() {
            return sourceInfo;
        }

        public List<ImageInfo> getSimilarsInfo() {
            return googleImages;
        }

        public void setLinkText(String linkText) {
            this.linkText = linkText;
        }
        
    }
    // </editor-fold>

    private String linkText;

    public Searcher(String linkText){
        this.linkText = linkText;
    }

    public Search getSearch(Path image) throws IOException{
        Search rv = new Search(image);
        rv.setLinkText(linkText);
        return rv;
    }

    public List<ImageInfo> search(Path image) throws IOException{
        Search rv = new Search(image);
        rv.setLinkText(linkText);
        rv.request();
        rv.search();
        return rv.getSimilarsInfo();
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

}
