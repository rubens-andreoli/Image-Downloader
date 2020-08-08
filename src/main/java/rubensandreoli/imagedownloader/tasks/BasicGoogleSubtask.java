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
import java.util.List;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.tasks.support.Downloader;
import rubensandreoli.imagedownloader.tasks.support.ImageInfo;
import rubensandreoli.imagedownloader.tasks.support.TaskJournal;

public abstract class BasicGoogleSubtask implements GoogleSubtask{

    protected String subfolder;

    public BasicGoogleSubtask(String subfolder) {
        this.subfolder = subfolder;
    }

    @Override
    public void preProcessing(String destination) {
        try {
            subfolder = FileUtils.createSubfolder(destination, subfolder).getPath();
        } catch (IOException ex) {
            subfolder = destination;
        }
    }

    @Override
    public abstract void processing(TaskJournal monitor, Downloader downloader, ImageInfo source, List<ImageInfo> similars);

    @Override
    public void postProcessing(TaskJournal monitor, Downloader downloader) {}
    
}
