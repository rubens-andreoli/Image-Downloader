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

import java.io.File;
import java.io.IOException;
import java.util.List;
import rubensandreoli.commons.others.CachedFile;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.support.ProgressLog.Tag;
import rubensandreoli.imagedownloader.tasks.Searcher.ImageInfo;

public class LargerSubtask implements GoogleSubtask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final int PRIORITY = 0;
    public static final double DEFAULT_FILESIZE_RATIO = 1.00;
    public static final double MIN_FILESIZE_RATIO = 0.1;
    public static final boolean DEFAULT_RETRY_SMALL = true;
    public static final String ATTENTION_SUBFOLDER = "low";

    private static final String NO_BIGGER_LOG_MASK = "No bigger images were found within %d image(s)"; //image count
    private static final String BIGGER_FOUND_LOG_MASK = "Found image with bigger dimensions [%d:%d] > [%d:%d]"; //width, height; source width; source height
    private static final String TRY_OTHER_IMAGE_LOG = "Attempting to find another image";
    private static final String NO_NEW_IMAGES_LOG ="No new images were found";
    private static final String SMALLER_THAN_SOURCE_LOG_MASK = "Image has a smaller file size than desired [%,d bytes] < [%,d bytes]"; //source size; target size
    private static final String BIGGER_SIZE_LOG_MASK = "Image found has a bigger file size also [%,d bytes] > [%,d bytes]"; //downloaded size; source size
    // </editor-fold>
    
    private String destination;
    private boolean retrySmall = DEFAULT_RETRY_SMALL;
    private double filesizeRatio = DEFAULT_FILESIZE_RATIO;

    public LargerSubtask(String folder, String subfolder) throws IOException{
        try{
            destination = FileUtils.createSubfolder(folder, subfolder);
        }catch(IOException ex){
            throw new IOException(String.format("Failed to create subfolder [%s]", destination));
        }
    }    
    
    @Override
    public void processing(GoogleTask task, ImageInfo source, List<ImageInfo> similars) {    
        //LOOK FOR LARGEST GOOGLE IMAGE LARGER THAN SOURCE (not worth sorting list before)
        ImageInfo largest = null;
        for (ImageInfo image : similars) {
            if(image.largerThan(source) && (largest==null || image.largerThan(largest))){
                largest = image;
            }
        }
        if(largest == null){
            task.getCurrentLog().appendLine(Tag.INFO, NO_BIGGER_LOG_MASK, similars.size());
            return; //not found
        }
        
        //FOUND LARGER
        task.getCurrentLog().appendLine(Tag.INFO, BIGGER_FOUND_LOG_MASK, largest.width, largest.height, source.width, source.height);
        final CachedFile cachedFile = task.download(largest.path, largest.getFilename(), largest.getExtension(), task.getCurrentLog(), false);
        if(cachedFile != null){
            if(retrySmall){
                if(cachedFile.length() <= source.getSize()*filesizeRatio){
                    //RETRY
                    task.getCurrentLog().appendLine(Tag.WARNING, SMALLER_THAN_SOURCE_LOG_MASK, cachedFile.length(), (long)(source.getSize()*filesizeRatio));
                    FileUtils.moveFileToChild(cachedFile, ATTENTION_SUBFOLDER);
                    similars.remove(largest);
                    if(similars.isEmpty()){
                        task.getCurrentLog().appendLine(Tag.WARNING, NO_NEW_IMAGES_LOG);
                    }else{
                        task.getCurrentLog().appendLine(Tag.INFO, TRY_OTHER_IMAGE_LOG);
                        processing(task, source, similars);
                    }
                }else{
                    task.getCurrentLog().appendLine(Tag.INFO, BIGGER_SIZE_LOG_MASK, cachedFile.length(), source.getSize());
                    task.increaseProgress();
                }
            }else{
                task.increaseProgress();
            }
        }else{ //FIX: temporary; add boolean failed and do bellow
            similars.remove(largest);
            if(similars.isEmpty()){
                task.getCurrentLog().appendLine(Tag.WARNING, NO_NEW_IMAGES_LOG);
            }else{
                task.getCurrentLog().appendLine(Tag.INFO, TRY_OTHER_IMAGE_LOG);
                processing(task, source, similars);
            }
        }
    }

    
    
    @Override
    public void postProcessing(GoogleTask task) {}

    public void setRetrySmall(boolean b) {
        retrySmall = b;
    }

    public void setFilesizeRatio(double ratio) {
        if(ratio < MIN_FILESIZE_RATIO) throw new IllegalArgumentException("ratio "+ratio+" < "+MIN_FILESIZE_RATIO);
        filesizeRatio = ratio;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public void interrupt() {}

    @Override
    public String getDestination(){
        return destination;
    }
}
