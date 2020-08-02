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

/**
 * Tasks can only be performed once. A task progress can be followed
 * with a progress listener or its 'getters'. The task status can also
 * be used to monitor a task.
 * It's recommend clearing implemented task fields when done to free unused memory.
 * 
 * @author Rubens A. Andreoli Jr.
 */
public abstract class Task {
    
    public enum Status {WAITING, RUNNING, COMPLETED, INTERRUPTED, FAILED}
    
    protected static final String STATUS_LOG_MASK = "---|%s|---";
    
    private ProgressListener listener;
    private volatile Status status = Status.WAITING;
    private int progress = 0;
    private int workload = 1;
    private boolean reportStatus = true;
    
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

    protected void report(String status, boolean progressed, String message, Object...args){
        final var log = new ProgressLog(progressed? increaseProgress():getProgress(), getWorkload());
        if(status != null) log.appendLine(status, message, args);
        else log.appendLine(message, args);
        reportLog(log);
    }

    protected void report(String status, String message, Object...args){
        report(status, true, message, args);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void interrupt(){
        status = Status.INTERRUPTED;
    }
        
    protected void setStatus(Status status){
        this.status = status;
    }
    
    public void setProgressListener(ProgressListener listener){
        this.listener = listener;
    }

    public void setReportStatus(boolean b) {
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
    public Status getStatus(){
        return status;
    }
    
    public ProgressListener getProgressListener(){
        return listener;
    }
    
    public boolean isReportingStatus(){
        return reportStatus;
    }
    
    public int getProgress(){
        return progress;
    }
    
    public int getWorkload(){
        return workload;
    }
    // </editor-fold>

}
