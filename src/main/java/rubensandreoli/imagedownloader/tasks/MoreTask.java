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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.commons.tools.Configs;
import rubensandreoli.commons.utils.FileUtils;

public class MoreTask extends GoogleTask {
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String NUMBER_REGEX = "\\d+";
    private static final String ONE_NUMBER_REGEX = "^[\\D]*\\d+[\\D]*$";
    private static final String CONFIRMED_MARKER = ".";
    private static final String VALID_IMAGE_REGEX = ".*"+NUMBER_REGEX+".*";
    private static final String SEQUENCE_LINK_MASK = "%s/%s{%%s}%s%s"; //parent; filename start; filename end; extension
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);
    private static final String MORE_SUBFOLDER = "more";
    
    private static final String FOUND_SEQUENCE_LOG_MASK = "Found %d potential sequence(s)";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    private static final int LOWER_MARGIN;
    private static final int UPPER_MARGIN;
    private static final boolean RETRY_SMALL;
    static{
        LOWER_MARGIN = Configs.values.get("more:lower_margin", 10, 0);
        UPPER_MARGIN = Configs.values.get("more:upper_margin", 100, 0);
        RETRY_SMALL = Configs.values.get("more:larger_retry_small", true);
    }
    // </editor-fold>
    
    private boolean larger;
    private final Map<String, Set<String>> links = new HashMap<>() {
        private Set<String> putOrAdd(String maskedLink, String number) {
            Set<String> numbers;
            if((numbers = links.get(maskedLink)) != null){
                numbers.add(number);
                return numbers;
            }else{
                numbers = new TreeSet<>();
                numbers.add(number);
                links.put(maskedLink, numbers);
                return null;
            }   
        } 
    };
    private SequentialTask currentSubtask;
    private ImageInfo lastImage;
    
    public MoreTask(String folder) throws IOException {
        super(folder);
    }

    @Override
    protected void run() {
        setRetrySmall(RETRY_SMALL);
        super.run();
        if (!interrupted() && !links.isEmpty()){
            var log = new ProgressLog(getProgress(), getWorkload());
            log.appendLine(String.format(STATUS_LOG_MASK, "MORE"));
            reportLog(log);
            downloadMore();
        }
    }
    
    @Override
    protected void downloadLargest(List<ImageInfo> googleImages, ImageInfo source) {
        if(!source.equals(lastImage)){ //TODO: temporary solution for repeating when retrying
            lastImage = source;
            int found = 0;
            for (ImageInfo i : googleImages) {
                //PREPARE VALUES
                String path = i.path;
                final int index = path.lastIndexOf('?');
                if(index > 0){
                    path = path.substring(0, index);
                }
                final String filename = FileUtils.parseFilename(path);
                final String parent = FileUtils.parseParent(path);
                final String extension = i.getExtension();
                
                //TEST AND ADD SEQUENCE
                if(filename.matches(VALID_IMAGE_REGEX)){
                    found += addSequence(parent, filename, extension);
                }
            }
            getCurrentLog().appendLine(ProgressLog.INFO, FOUND_SEQUENCE_LOG_MASK, found);    
        }
        if(larger) super.downloadLargest(googleImages, source);
    }
    
    private int addSequence(String site, String filename, String extension){
        int added = 0, found = 0;
        final Matcher matcher = NUMBER_PATTERN.matcher(filename);
        Set<String> numbers = null;
        while(matcher.find()){
            found++;
            final int start = matcher.start();
            final int end = matcher.end();
            final String maskedLink = String.format(SEQUENCE_LINK_MASK, site, filename.substring(0, start), filename.substring(end), extension);
            final String number = filename.substring(start, end);

            if((numbers = links.get(maskedLink)) != null){
                numbers.add(number);
                if(numbers.size() == 2){
                    numbers.add(CONFIRMED_MARKER);
                    increaseWorkload();
                    added++;
                }
            }else{
                numbers = new TreeSet<>();
                numbers.add(number);
                links.put(maskedLink, numbers);
            }
        }
        if(numbers != null && found == 1){
            numbers.add(CONFIRMED_MARKER);
            increaseWorkload();
            added++;
        }
        return added;
    }
    
    private void downloadMore(){
        for (var entry : links.entrySet()) {
            if(interrupted()) break; //INTERRUPT EXIT POINT
            final Set<String> values = entry.getValue();
            if(/*values.size() <= 1 || */!values.contains(".")) continue; //not confirmed sequence
            
            //PARSE NUMBERS
            final List<Integer> numbers = new ArrayList<>();
            boolean padding = false;
            int endZero = 0;
            int maxLenght = 0;
            for (String v : values) {
                if('0' == v.charAt(0)) padding = true;
                if('0' == v.charAt(v.length()-1)) endZero++;
                if(v.length() > maxLenght) maxLenght = v.length();
                try{
                    numbers.add(Integer.parseInt(v));
                }catch(NumberFormatException ex){}
            }
            if(endZero == numbers.size()) continue; //possibly resolutions
            
            //PREPARE TASK VALUES
            int start = numbers.get(0)-LOWER_MARGIN;
            if(start < 1) start = 1;
            final int end = numbers.get(numbers.size()-1)+UPPER_MARGIN;
            String numberMask = "%d";
            if(padding) numberMask = "%0"+maxLenght+"d";
            String link = String.format(entry.getKey(), numberMask);
            link = String.format(link, start);
            
            //START SUB-TASK
            report(ProgressLog.INFO, false, "Starting sequence %s [%d:%d]", link, start, end);
            int downloaded = 0;
            try {
                currentSubtask = new SequentialTask(link);
                currentSubtask.setUpperBound(end);
                String destination = super.getDestination();
                if(larger){
                    final File subfolder = new File(destination, MORE_SUBFOLDER);
                    if(!subfolder.isDirectory()) subfolder.mkdirs();
                    destination = subfolder.getAbsolutePath();
                }
                currentSubtask.excludeNumbers(numbers); //already have similar to found by google
                currentSubtask.setDestination(destination);
                currentSubtask.setSafeThreshold(numbers.get(numbers.size()-1));
                currentSubtask.run();
                downloaded = currentSubtask.getSuccesses();
                addSuccesses(downloaded);
            } catch (MalformedURLException ex) {
                System.err.println("ERROR: failed marking image url " + ex.getMessage());
            } catch (BoundsException ex) {
                System.err.println("ERROR: failed setting upper bound " + ex.getMessage());
            } catch (IOException ex) {
                System.err.println("ERROR: failed setting source/destination " + ex.getMessage());
                ex.printStackTrace();
            }
            report(ProgressLog.INFO, "%d downloaded from sequence", downloaded);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(currentSubtask != null) currentSubtask.interrupt();
    }

    public void downloadLarger(boolean b) {
        this.larger = b;
    }
      
}
