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
package rubensandreoli.imagedownloader.tasks.support;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Element;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.others.Logger;
import rubensandreoli.imagedownloader.tasks.exceptions.LoadException;
import rubensandreoli.imagedownloader.tasks.exceptions.SearchException;
import rubensandreoli.imagedownloader.tasks.exceptions.UploadException;

/**
 * References:
 * https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Content_negotiation/List_of_default_Accept_values
 * https://stackoverflow.com/questions/6440010/modifier-static-is-only-allowed-in-constant-variable-declarations
 * 
 * @author Rubens A. Andreoli Jr.
 */
public class Searcher {
   
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final String GOOGLE_LINK = "https://www.google.com/searchbyimage/upload";
    public static final String DEFAULT_LINK_TEXT = "Todos os tamanhos";

    private static final String RESPONSE_LINK_PREFIX = "/search?tbs=simg:";
    private static final String RESPONSE_SCRIPT_PREFIX = "AF_initDataCallback";
    private static final String IMAGE_LINK_CLEAR_REGEX = "[\\[\\]\\\"\\\"]";
    private static final String IMAGE_LINK_DELIMITER = ",";
    private static final String IMAGE_LINK_REGEX = "((\\[\"http.*\\/\\/(?!encrypted)).*\\])";
    private static final Pattern IMAGE_LINK_PATTERN = Pattern.compile(IMAGE_LINK_REGEX);
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" SEARCH "> 
    public class Search{

        private final Path image;
        
        //----------LOAD----------//
        private ImageInfo sourceInfo;
        private byte[] sourceBytes;
        
        //----------UPLOAD----------//
        private String responseLink;
        private long start;
        
        //----------SEARCH----------//
        private List<ImageInfo> googleImages = new ArrayList<>();
        private long duration;

        private Search(Path image){
            this.image = image;
        }
        
        public void load() throws LoadException{
            try(var imageStream = new BufferedInputStream(Files.newInputStream(image))){
                sourceBytes = imageStream.readAllBytes();
                final var bufferedImage = ImageIO.read(new ByteArrayInputStream(sourceBytes));
                if(bufferedImage == null) throw new IOException("failed reading image from file");
                sourceInfo = new ImageInfo(image.toString(), bufferedImage.getWidth(), bufferedImage.getHeight());
                sourceInfo.setSize(sourceBytes.length);
            } catch (IOException ex) {
                throw new LoadException(ex);
            }
        }

        public void upload() throws UploadException{
            start = System.currentTimeMillis();        
            final MultipartEntity entity = new MultipartEntity(); 
            entity.addPart("encoded_image", new InputStreamBody(new ByteArrayInputStream(sourceBytes), sourceInfo.getName()));
            final HttpPost post = new HttpPost(GOOGLE_LINK);
            post.setEntity(entity); 

            try(CloseableHttpClient client = HttpUtils.getClient()){
                final HttpResponse response = client.execute(post);
                sourceBytes = null; //free memory
                final Header header = response.getFirstHeader("location");
                if(header == null) throw new UploadException("location header not foound");
                responseLink = header.getValue();
            } catch (IOException ex) {
                throw new UploadException(ex);
            }
        }

        public void search() throws SearchException{
            try{            
                //FIND LINK
                String similarLink = null;
                for (Element e : HttpUtils.getDocument(responseLink).getElementsByTag("a")) {
                    final String ref = e.attr("href");
                    if(ref.startsWith(RESPONSE_LINK_PREFIX) && e.text().toLowerCase().equals(linkText)){
                        similarLink = e.absUrl("href");
                        break; //just one
                    }
                }

                //FIND IMAGES
                if(similarLink != null){
                    HttpUtils.getDocument(similarLink).getElementsByTag("script").forEach(s -> {
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
            }catch(IOException ex){
                throw new SearchException(ex);
            }finally{
                duration = System.currentTimeMillis() - start;
            }
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" SEARCH RESULT "> 
    public static class SearchResult{
    
        public final ImageInfo source;
        public final long duration;
        public final List<ImageInfo> images;

        public SearchResult(ImageInfo source, long duration, List<ImageInfo> images) {
            this.source = source;
            this.duration = duration;
            this.images = images;
        }

        public boolean isEmpty(){
            return images.isEmpty();
        }
        
    }
    // </editor-fold>
    
    private final String linkText;

    public Searcher(String linkText){
        this.linkText = linkText.trim().toLowerCase();
    }
    
    public Searcher(){
        this(DEFAULT_LINK_TEXT);
    }

    public SearchResult search(Path image) throws LoadException, UploadException, SearchException{
        final Search search = new Search(image);
        search.load();
        search.upload();
        search.search();
        return new SearchResult(search.sourceInfo, search.duration, search.googleImages);
    }
    
}
