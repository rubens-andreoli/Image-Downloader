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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.exceptions.LoadException;
import rubensandreoli.imagedownloader.tasks.exceptions.SearchException;
import rubensandreoli.imagedownloader.tasks.exceptions.UploadException;
import rubensandreoli.imagedownloader.tasks.support.Searcher;

/** 
 * References:
 * https://javapapers.com/java/glob-with-java-nio/
 * https://stackoverflow.com/questions/5923817/how-to-clone-an-inputstream/5924132
 * https://stackoverflow.com/questions/12107049/how-can-i-make-a-copy-of-a-bufferedreader
 * https://stackoverflow.com/questions/3850074/regex-until-but-not-including
 * https://stackoverflow.com/questions/38581427/why-non-static-final-member-variables-are-not-required-to-follow-the-constant-na/38581517
 * 
 * @author Rubens A. Andreoli Jr.
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
    private static final String FAILED_UPLOADING_LOG = "Failed connecting/uploading image";
    private static final String FAILED_READING_FILE_LOG = "Failed reading file";
    // </editor-fold>

    private final Path source;
    private final List<Path> images;
    private final int size;
    private final Searcher searcher;
    private int startIndex = 0;
    private Set<GoogleSubtask> subtasks;

    public GoogleTask(String source, String linkText) throws IOException{
        final Path path = getWritableFolder(source).toPath();
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(path, FileUtils.IMAGES_GLOB)){
            images = new ArrayList<>();
            for (Path file : contents) {
                images.add(file);
            }
            if(images.isEmpty()) throw new IOException(String.format(EMPTY_SOURCE_MSG_MASK, source));
            size = images.size();
            this.source = path;
        }
        this.searcher = new Searcher(linkText);
        
        //----------SUPER----------//
        downloader.setSleep(false);
        setFailThreshold(DEFAULT_FAIL_THRESHOLD);
        setMinFilesize(DEFAULT_MIN_FILESIZE);
    }
    
    @Override
    protected void run() {
        images.sort((p1,p2) -> p1.getFileName().compareTo(p2.getFileName()));
        journal.setWorkload(getImageCount()-startIndex+1); //+1: end inclusive;
        
        //----------SUBTASKS PRE-PROCESSING----------//
        subtasks.forEach(subtask -> subtask.preProcessing(getDestination()));

        for (int i = startIndex; i < getImageCount(); i++) {
            if(interrupted() || failed()) break; //INTERRUPT EXIT POINT
            final Path image = images.get(i);
            final var log = journal.startNewLog(true)
                    .appendLine(IMAGE_NUMBER_LOG_MASK, i)
                    .appendLine(Level.INFO, LOADING_IMAGE_LOG_MASK, image.toString());

            try {
                final var result = searcher.search(image);
                if(result.isEmpty()){
                    log.appendLine(Level.WARNING, NO_SIMILAR_LOG);
                }else{
                    
                    //----------SUBTASKS PROCESSING----------//
                    subtasks.forEach(subtask -> subtask.processing(journal, downloader, result.source, result.images));
                    
                }
                journal.resetFails();
            } catch (LoadException ex) {
                log.appendLine(Level.ERROR, FAILED_READING_FILE_LOG);
            } catch (UploadException | SearchException ex) {
                journal.increaseFails();
                log.appendLine(Level.ERROR, FAILED_UPLOADING_LOG);
            }
            journal.reportCurrentLog();
        }
        
        //----------SUBTASKS POST-PROCESSING----------//
        subtasks.forEach(subtask -> subtask.postProcessing(journal, downloader));
        
    }

    @Override
    protected void close() {
        images.clear();
        subtasks = null;
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS ">
    public void setStartIndex(int startIndex) throws BoundsException {
        if(startIndex < 0 || startIndex>images.size()-1){
            throw new BoundsException(String.format(INVALID_BOUNDS_MSG_MASK, images.size()));
        }
        this.startIndex = startIndex;
    }
    
    public boolean addSubtask(GoogleSubtask subtask){
        if(getStatus() != State.WAITING) return false;
        else if(subtasks == null) subtasks = new TreeSet<>();
        return subtasks.add(subtask);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public int getImageCount(){
        return size;
    }

    public String getSource() {
        return source.toString();
    }

    public int getStartIndex() {
        return startIndex;
    }
    // </editor-fold>

    @Override
    public void downloadStateChanged(Level level, String description) {
        journal.getCurrentLog().appendLine(level, description);
    }

}
