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

import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HttpUtils {
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
    public static final String ACCEPT_IMAGE = "image/webp,image/apng,*/*";
    public static final String ACCEPT_TEXT = "text/html; charset=UTF-8";
    // </editor-fold>
    
    private HttpUtils(){}
    
    public static Document getDocument(String url) throws IOException {
            return Jsoup.connect(url)
                    .header("Accept", ACCEPT_TEXT)
                    .userAgent(USER_AGENT)
                    .get();
    }
    
    public static CloseableHttpClient getClient(){
        return HttpClientBuilder.create().setUserAgent(USER_AGENT).build();
    }

}
