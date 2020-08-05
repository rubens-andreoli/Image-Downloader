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

import rubensandreoli.imagedownloader.support.ProgressListener;

/**
 * Tasks can only be performed once. A task progress can be followed
 * with a progress listener or its 'getters'. The task status can also
 * be used to monitor a task.
 * It's recommend clearing implemented task fields when done to free unused memory.
 * 
 * @author Rubens A. Andreoli Jr.
 */
public interface Task {
    
    public enum Status {WAITING, RUNNING, COMPLETED, INTERRUPTED, FAILED}
    
    boolean perform();
    void interrupt();
    
    void setProgressListener(ProgressListener listener);
    Status getStatus();
    int getProgress();
    int getWorkload();
    
}
