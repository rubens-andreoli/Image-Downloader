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
package rubensandreoli.imagedownloader.gui;

import rubensandreoli.commons.others.Configuration;
import rubensandreoli.imagedownloader.tasks.DownloadTask;
import rubensandreoli.imagedownloader.tasks.support.Downloader;

public abstract class DownloadTaskPanel extends TaskPanel<DownloadTask>{
    private static final long serialVersionUID = 1L;

    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS ">
    public static final int CONNECTION_TIMEOUT;
    public static final int READ_TIMEOUT;
    public static final int MIN_COOLDOWN;
    public static final int MAX_COOLDOWN;
    static{
        CONNECTION_TIMEOUT = Configuration.values.get("connection_timeout", Downloader.DEFAULT_CONNECTION_TIMEOUT, 500);
        READ_TIMEOUT = Configuration.values.get("read_timout",  Downloader.DEFAULT_READ_TIMEOUT, 1000);
        MIN_COOLDOWN = Configuration.values.get("connection_cooldown_min", Downloader.DEFAULT_MIN_COOLDOWN, 0);
        MAX_COOLDOWN = Configuration.values.get("connection_cooldown_max", Downloader.DEFAULT_MAX_COOLDOWN, MIN_COOLDOWN);
    }
    // </editor-fold>
    
    public DownloadTaskPanel(String title) {
        super(title);
    }

    public DownloadTaskPanel(String title, TaskPanelListener listener) {
        super(title, listener);
    }

    @Override
    protected boolean notify(DownloadTask task, String description, Object... args) {
        task.setConnection(CONNECTION_TIMEOUT, READ_TIMEOUT, MIN_COOLDOWN, MAX_COOLDOWN);
        task.setFailThreshold(getFailThreshold());
        task.setMinFilesize(getMinFilesize());
        return super.notify(task, description, args);
    }
    
    protected abstract int getFailThreshold();
    protected abstract int getMinFilesize();
    
}
