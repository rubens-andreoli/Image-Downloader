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

import java.util.List;
import rubensandreoli.commons.others.CachedFile;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.support.Downloader;
import rubensandreoli.imagedownloader.tasks.support.ImageInfo;
import rubensandreoli.imagedownloader.tasks.support.TaskJournal;

public class LargerSubtask extends BasicGoogleSubtask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final int PRIORITY = 0;
    public static final double DEFAULT_DIMENSION_RATIO = 1.05;
    public static final double MIN_DIMENSION_RATIO = 0.5;
    public static final double DEFAULT_FILESIZE_RATIO = 1.0;
    public static final double MIN_FILESIZE_RATIO = 1.0;
    public static final boolean DEFAULT_RETRY_SMALL = true;
    public static final String ATTENTION_SUBFOLDER = "low";
    public static final boolean DEFAULT_SOURCE_NAME = false;

    private static final String NO_LARGER_LOG_MASK = "No larger images were found within %d image(s)"; //image count
    private static final String LARGER_FOUND_LOG_MASK = "Found image with desired dimensions [%d:%d] >= [%d:%d]"; //width, height; source width; source height
    private static final String TRY_OTHER_IMAGE_LOG = "Attempting to find another image";
    private static final String NO_NEW_IMAGES_LOG ="No new images were found";
    private static final String SMALLER_THAN_SOURCE_LOG_MASK = "Image has a smaller file size than desired [%,d bytes] < [%,d bytes]"; //source size; target size
    private static final String BIGGER_SIZE_LOG_MASK = "Image found has desired file size [%,d bytes] >= [%,d bytes]"; //downloaded size; source size
    // </editor-fold>
    
    private boolean sourceName = DEFAULT_SOURCE_NAME;
    private boolean retrySmall = DEFAULT_RETRY_SMALL;
    private double sizeRatio = DEFAULT_FILESIZE_RATIO;
    private double dimensionRatio = DEFAULT_DIMENSION_RATIO;

    public LargerSubtask(String subfolder) {
        super(subfolder);
    }

    @Override
    public void processing(TaskJournal journal, Downloader downloader, ImageInfo source, List<ImageInfo> similars) {    
        final var log = journal.getCurrentLog();

        //LOOK FOR LARGEST IMAGE LARGER THAN SOURCE (not worth sorting list before)
        ImageInfo largest = null;
        for (ImageInfo image : similars) {
            if(image.largerOrEqualTo(source, dimensionRatio) && (largest==null || image.largerThan(largest))){
                largest = image;
            }
        }
        if(largest == null){ //not found
            log.appendLine(Level.INFO, NO_LARGER_LOG_MASK, similars.size());
            return;
        }
        
        //FOUND LARGER
        log.appendLine(Level.INFO, LARGER_FOUND_LOG_MASK, largest.width, largest.height, (long)(source.width*dimensionRatio), (long)(source.height*dimensionRatio));
        final CachedFile cachedFile = downloader.download(largest.path, subfolder, sourceName? source.getFilename():largest.getFilename(), largest.getExtension());
        boolean failed = (cachedFile == null);
        if(!failed){
            if(retrySmall){
                long desiredSize = (long)(source.getSize()*sizeRatio);
                if(Long.compare(cachedFile.length(), desiredSize) < 0){
                    log.appendLine(Level.WARNING, SMALLER_THAN_SOURCE_LOG_MASK, cachedFile.length(), desiredSize);
                    FileUtils.moveFileToChild(cachedFile, ATTENTION_SUBFOLDER);
                    failed = true;
                }else{
                    log.appendLine(Level.INFO, BIGGER_SIZE_LOG_MASK, cachedFile.length(), desiredSize);
                    journal.increaseSuccesses();
                }
            }else{
                journal.increaseSuccesses();
            }
        }
        
        if(failed){ //retry
            similars.remove(largest);
            if(similars.isEmpty()){
                log.appendLine(Level.WARNING, NO_NEW_IMAGES_LOG);
            }else{
                log.appendLine(Level.INFO, TRY_OTHER_IMAGE_LOG);
                processing(journal, downloader, source, similars);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setRetrySmall(boolean b) {
        retrySmall = b;
    }

    public void setFilesizeRatio(double ratio) {
        if(ratio < MIN_FILESIZE_RATIO) throw new IllegalArgumentException("ratio "+ratio+" < "+MIN_FILESIZE_RATIO);
        sizeRatio = ratio;
    }

    public void setDimensionRatio(double ratio) {
        if(ratio < MIN_DIMENSION_RATIO) throw new IllegalArgumentException("ratio "+ratio+" < "+MIN_DIMENSION_RATIO);
        this.dimensionRatio = ratio;
    }

    public void setSourceName(boolean sourceName) {
        this.sourceName = sourceName;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    // </editor-fold>

}
