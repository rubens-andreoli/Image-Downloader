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

import rubensandreoli.imagedownloader.support.ProgressLog;
import rubensandreoli.commons.exceptions.checked.BoundsException;
import rubensandreoli.commons.utils.FileUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import rubensandreoli.imagedownloader.support.ProgressLog.Tag;

/** 
 * References:
 * https://javapapers.com/java/glob-with-java-nio/
 * https://stackoverflow.com/questions/5923817/how-to-clone-an-inputstream/5924132
 * https://stackoverflow.com/questions/12107049/how-can-i-make-a-copy-of-a-bufferedreader
 * https://stackoverflow.com/questions/3850074/regex-until-but-not-including
 * https://stackoverflow.com/questions/38581427/why-non-static-final-member-variables-are-not-required-to-follow-the-constant-na/38581517
 */
public class GoogleTask extends DownloadTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final int DEFAULT_FAIL_THRESHOLD = 10;
    public static final int DEFAULT_MIN_FILESIZE = 25600; //bytes
    
    private static final String EMPTY_SOURCE_MSG_MASK = "Source folder [%s] doesn't contain any image file.";
    private static final String INVALID_BOUNDS_MSG_MASK = "Starting index must be greater than 0 and smaller than the number of image files [%d] in the source folder.";
    
    private static final String IMAGE_NUMBER_LOG_MASK = "[%d]"; //index
    private static final String LOADING_IMAGE_LOG_MASK = "Loading [%s]"; //path
    private static final String NO_SIMILAR_LOG = "No similar images were found";
    private static final String FAILED_UPLOADING_LOG= "Failed connecting/uploading image";
    private static final String FAILED_READING_FILE_LOG = "Failed reading file";
    private static final String UNEXPECTED_LOG_MASK = "Unexpected exception [%s]"; //exception message
    // </editor-fold>

    private final String sourceFolder;
    private final Searcher searcher;
    private final List<Path> images;
    private int startIndex = 0;
    private ProgressLog currentLog;
    private Set<GoogleSubtask> subtasks = new TreeSet<>();
    private GoogleSubtask currentSubtask;

    public GoogleTask(String source, Searcher searcher) throws IOException{
        final Path path = getFolderPath(source);
        this.searcher = searcher;
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(path, FileUtils.IMAGES_GLOB)){
            images = new ArrayList<>();
            for (Path file : contents) {
                images.add(file);
            }
            if(images.isEmpty()) throw new IOException(String.format(EMPTY_SOURCE_MSG_MASK, source));
            sourceFolder = source;
        }
        
        setFailThreshold(DEFAULT_FAIL_THRESHOLD);
        setMinFilesize(DEFAULT_MIN_FILESIZE);
    }
    
    @Override
    protected void run() {
        images.sort((p1,p2) -> p1.getFileName().compareTo(p2.getFileName()));
        setWorkload(getImageCount()-startIndex);
        
        for (int i = startIndex; i < images.size(); i++) {
            if(interrupted() || failed()) break; //INTERRUPT EXIT POINT
            Path image = images.get(i);
            currentLog = new ProgressLog(getProgress(), getWorkload());
            currentLog.appendLine(IMAGE_NUMBER_LOG_MASK, i);
            currentLog.appendLine(Tag.INFO, LOADING_IMAGE_LOG_MASK, image.getFileName().toString());
            
            try {
                final var search = searcher.getSearch(image);
                try{
                    search.request();
                    if(search.search() > 0) {
                        //SUBTASKS PROCESSING
                        subtasks.forEach(subtask -> {
                            currentSubtask = subtask;
                            subtask.processing(this, search.getSourceInfo(), search.getSimilarsInfo());
                        });
                    } else {
                        currentLog.appendLine(Tag.WARNING, NO_SIMILAR_LOG);
                    }
                    resetFails();
                    if(search.getDuration() < getMinCooldown()) sleepRandom();
                }catch(IOException ex){
                    currentLog.appendLine(Tag.ERROR, FAILED_UPLOADING_LOG);
                    increaseFails();
                }
            } catch (IOException ex) {
                currentLog.appendLine(Tag.ERROR, FAILED_READING_FILE_LOG);
            } catch (Exception ex) {
                currentLog.appendLine(Tag.CRITICAL, UNEXPECTED_LOG_MASK, ex.getMessage());
            }
            reportLog(currentLog);
            increaseProgress();
        }
        //SUBTASKS POST-PROCESSING
        subtasks.forEach(subtask -> {
            currentSubtask = subtask;
            subtask.postProcessing(this);
        });
    }

    @Override
    public void interrupt() {
        super.interrupt();
        subtasks.forEach(t -> t.interrupt());
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS ">       
    public void setStartIndex(int startIndex) throws BoundsException {
        if(startIndex < 0 || startIndex>images.size()-1){
            throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, images.size()));
        }
        this.startIndex = startIndex;
    }
    
    public boolean addSubtask(GoogleSubtask subtask){
        if(getStatus() != Status.WAITING) return false;
        return subtasks.add(subtask);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public int getImageCount(){
        return images==null? 0 : images.size()-1; //FIX: why -1?
    }

    public String getSource() {
        return sourceFolder;
    }

    public int getStartIndex() {
        return startIndex;
    }
     
    public ProgressLog getCurrentLog() {
        return currentLog;
    }
    
    @Override
    public String getDestination() {
        if(currentSubtask != null) {
            return currentSubtask.getDestination();
        }
        return super.getDestination();
    }
    // </editor-fold>


}
