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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rubensandreoli.commons.others.Level;
import rubensandreoli.imagedownloader.tasks.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.support.Downloader;
import rubensandreoli.imagedownloader.tasks.support.ImageInfo;
import rubensandreoli.imagedownloader.tasks.support.TaskJournal;

public class MoreSubtask extends BasicGoogleSubtask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final int PRIORITY = LargerSubtask.PRIORITY+1;
    public static final int DEFAULT_LOWER_MARGIN = 20;
    public static final int DEFAULT_UPPER_MARGIN = 200;
    public static final int DEFAULT_MIN_DIMENSION = 400;
    public static final int DEFAULT_SEQUENCE_MAX_LENGHT = 1000;
    private static final int OCURRANCE_CONFIRMATION = 2;
    
    private static final String NUMBER_REGEX = "\\d+";
    private static final String VALID_IMAGE_REGEX = ".*"+NUMBER_REGEX+".*";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);
    private static final String CONFIRMED_MARKER = ".";
    
    private static final String TITLE_LOG = "MORE";
    private static final String FOUND_SEQUENCE_LOG_MASK = "Found %d new potential sequence(s)";
    private static final String SEQUENCE_START_LOG_MASK = "Starting sequence [%s] -> [%d:%d]"; //url; start; end
    private static final String SEQUENCE_TOTAL_LOG_MASK = "%d sequential download(s)";
    private static final String SEQUENCE_TOO_BIG_LOG_MASK = "Sequence [%s] is too big [%,d values]"; //url; sequence size
    private static final String SEQUENCE_FAILED_LOG = "Failed starting sequence";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" LINK "> 
    public static class Link{

        public final String start;
        public final String end;

        public Link(String parent, String filenameStart, String filenameEnd, String extension) {
            start = new StringBuilder(parent).append("/").append(filenameStart).append("{").toString();
            end = new StringBuilder("}").append(filenameEnd).append(extension).toString();
        }

        public String getMaskedLink(String middle){
            return new StringBuilder(start).append(middle).append(end).toString();
        }
        
        public String getMaskedLink(String numberMask, int value){
            return getMaskedLink(String.format(numberMask, value));
        }

        @Override
        public String toString() {
            return new StringBuilder(start).append("?").append(end).toString();
        }

        @Override
        public int hashCode() {
            return 97 + Objects.hashCode(this.start);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            return Objects.equals(this.start, ((Link) obj).start);
        }

    }
    // </editor-fold>
    
    private final Map<Link, Set<String>> links = new HashMap<>();
    private int minDimension = DEFAULT_MIN_DIMENSION;
    private int lowerMargin = DEFAULT_LOWER_MARGIN;
    private int upperMargin = DEFAULT_UPPER_MARGIN;
    private int sequenceMaxLenght = DEFAULT_SEQUENCE_MAX_LENGHT;
    
    public MoreSubtask(String subfolder) {
        super(subfolder);
    }
    
    @Override
    public void processing(TaskJournal journal, Downloader downloader, ImageInfo source, List<ImageInfo> similars)  {
        int found = 0;
        for (ImageInfo i : similars) {
            
            //PREPARE VALUES
            final String parent = i.getParent();
            final String filename = i.getFilename().replaceAll("%20", " ");
            final String extension = i.getExtension();

            //TEST AND ADD SEQUENCE
            if(filename.matches(VALID_IMAGE_REGEX) && (i.width > minDimension || i.height > minDimension)){
                found += addSequence(parent, filename, extension);
            }
        }
        journal.addWorkload(found);
        journal.getCurrentLog().appendLine(Level.INFO, FOUND_SEQUENCE_LOG_MASK, found);
    }
    
    private int addSequence(String parent, String filename, String extension){
        int added = 0, found = 0;
        boolean created = false;
        final Matcher matcher = NUMBER_PATTERN.matcher(filename);
        Set<String> numbers = null;
        while(matcher.find()){
            found++;
            final int start = matcher.start();
            final int end = matcher.end();
            final Link link = new Link(
                    parent, 
                    filename.substring(0, start), 
                    filename.substring(end), 
                    extension
            );
            final String number = filename.substring(start, end);

            if((numbers = links.get(link)) != null){
                numbers.add(number);
                if(numbers.size() == OCURRANCE_CONFIRMATION){ //confirm after X ocurrances
                    numbers.add(CONFIRMED_MARKER);
                    added++;
                }
            }else{
                numbers = new HashSet<>();
                numbers.add(number);
                links.put(link, numbers);
                created = true;
            }
        }
        if(numbers != null && found == 1 && created){ //confirm if found only one and link first time
            numbers.add(CONFIRMED_MARKER);
            added++;
        }
        return added;
    }
    
    @Override
    public void postProcessing(TaskJournal journal, Downloader downloader){
        if (links.isEmpty() || journal.isInterrupted()) return;
        
        boolean printTitle = true;
        for (var entry : links.entrySet()) {
            if(journal.isInterrupted()) break; //INTERRUPT EXIT POINT
            final Set<String> numbers = entry.getValue();
            if(!numbers.contains(".")) continue; //not confirmed sequence, don't progress here
            
            //PARSE NUMBERS
            final TreeSet<Integer> values = new TreeSet<>();
            boolean padding = false;
            int endInZero = 0;
            int maxLenght = 0;
            for (String v : numbers) {
                //TODO: number to big for integer -> numberMask_start = 'first-part-of-number'; val = 'parsed-last-index-5'
                if('0' == v.charAt(0)) padding = true;
                if('0' == v.charAt(v.length()-1)) endInZero++;
                if(v.length() > maxLenght) maxLenght = v.length();
                try{
                    values.add(Integer.parseInt(v));
                }catch(NumberFormatException ex){}
            }
            //if failed all conversions; or all end in '0' they are possibly resolutions (letting sequence fail is costlier)
            if(values.isEmpty() || endInZero == values.size()){ 
                journal.increaseProgress();
                continue;
            }
            
            if(printTitle) { //print only for first confirmed and not discarded
                journal.reportTitle(TITLE_LOG);
                printTitle = false;
            } 
            
            //PREPARE TASK VALUES
            final int start = Math.max(values.first()-lowerMargin, 1); 
            final int end = values.last()+upperMargin;
            final int size = end - start;
            if(size > sequenceMaxLenght){ //TODO: do what when max lenght < 0?
                journal.report(Level.WARNING, true, SEQUENCE_TOO_BIG_LOG_MASK, entry.getKey(), size);
                continue;
            }
            final String numberMask = padding? String.format("%%0%dd", maxLenght) : "%d";
            final String maskedLink = entry.getKey().getMaskedLink(numberMask, start);
            
            //SUB-TASK
            journal.report(Level.INFO, false, SEQUENCE_START_LOG_MASK, entry.getKey(), start, end);
            try {
                final var subtask = new SequenceTask(maskedLink, end);
                subtask.silent(true);
                subtask.excludeNumbers(values); //already have copies of the ones found by the reverse search
                subtask.setDestination(subfolder);
                subtask.setSafeThreshold(values.last());
                subtask.run();
                final int downloaded = subtask.getSuccesses();
                journal.addSuccesses(downloaded);
                journal.report(Level.INFO, true, SEQUENCE_TOTAL_LOG_MASK, downloaded);
            } catch (BoundsException | IOException ex) {
                journal.report(Level.ERROR, true, SEQUENCE_FAILED_LOG);
            }

        }

    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setMinDimension(int d) {
        this.minDimension = d;
    }

    public void setLowerMargin(int amount) {
        this.lowerMargin = amount;
    }

    public void setUpperMargin(int amount) {
        this.upperMargin = amount;
    }

    public void setSequenceLimit(int amount) {
        this.sequenceMaxLenght = amount;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    // </editor-fold>

}
