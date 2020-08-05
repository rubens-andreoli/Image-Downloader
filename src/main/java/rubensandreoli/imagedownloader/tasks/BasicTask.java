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
import rubensandreoli.imagedownloader.support.ProgressListener;
import rubensandreoli.imagedownloader.support.ProgressLog.Tag;

public abstract class BasicTask implements Task{
    
    protected static final String STATUS_LOG_MASK = "---|%s|---";
    
    private ProgressListener listener;
    private volatile Status status = Status.WAITING;
    private int progress = 0;
    private int workload = 1;
    private boolean reportStatus = true;
    
    @Override
    public boolean perform(){
        if(getStatus() != Status.WAITING) return false;
        status = Status.RUNNING;
        if(reportStatus) reportStatus();
        
        run();

        if(status == Status.RUNNING) status = Status.COMPLETED;
        if(reportStatus) reportStatus();
        return true;
    }
    
    protected abstract void run();
    
    protected void reportLog(ProgressLog log){
        if(listener != null) listener.progressed(log);
    }
    
    protected void reportStatus(){
        final var log = new ProgressLog(getProgress(), getWorkload());
        log.appendLine(String.format(STATUS_LOG_MASK, status.toString()));
        reportLog(log);
    }

    protected void report(Tag tag, boolean progressed, String message, Object...args){
        final var log = new ProgressLog(progressed? increaseProgress():getProgress(), getWorkload());
        if(tag != null) log.appendLine(tag, message, args);
        else log.appendLine(message, args);
        reportLog(log);
    }

    protected void report(Tag tag, String message, Object...args){
        report(tag, true, message, args);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    @Override
    public void interrupt(){
        status = Status.INTERRUPTED;
    }
        
    protected void setStatus(Status status){
        this.status = status;
    }
    
    @Override
    public void setProgressListener(ProgressListener listener){
        this.listener = listener;
    }

    protected void setReportStatus(boolean b) {
        reportStatus = b;
    }

    protected void setProgress(int progress){
        this.progress = progress;
    }
        
    protected int increaseProgress(){
        return ++progress;
    }
    
    protected int addProgress(int amount){
        return progress += amount;
    }

    protected void setWorkload(int workload){
        this.workload = workload;
    }
    
    protected int increaseWorkload(){
        return ++workload;
    }
    
    protected int addWorkload(int amount) {
        return workload += amount;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    @Override
    public Status getStatus(){
        return status;
    }
    
    @Override
    public int getProgress(){
        return progress;
    }
    
    @Override
    public int getWorkload(){
        return workload;
    }
    // </editor-fold>

}
