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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoreTask extends GoogleTask {
    
    private static final String SEQUENCE_REGEX = ".*[^\\d]\\d{1,4}[^\\d].*";
    
    private boolean google;
    
    public MoreTask(String folder) throws IOException {
        super(folder);
    }

    @Override
    protected void download(List<ImageInfo> googleImages, ImageInfo source) {
        Set<String> images = new HashSet<>();
        googleImages.forEach(i -> {
            if(i.getFilename().matches(SEQUENCE_REGEX)){
                images.add(markFilename(i.path));
            }
        });
        images.forEach(i -> {
            try {
                var task = new SequentialTask(i);
                int success = task.run();
            } catch (MalformedURLException ex) {
                System.err.println("ERROR: failed marking image url " + ex.getMessage());
            }
        });
        if(google) super.download(googleImages, source);
    }
    
    private String markFilename(String link){ //TODO: not implemented
        return link;
    }
    
    
    
}
