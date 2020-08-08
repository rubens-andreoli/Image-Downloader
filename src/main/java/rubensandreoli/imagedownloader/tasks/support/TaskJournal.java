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
package rubensandreoli.imagedownloader.tasks.support;

import rubensandreoli.commons.others.Level;
import rubensandreoli.imagedownloader.tasks.Task.State;

public class TaskJournal{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final String TITLE_LOG_MASK = "---|%s|---"; //status
    // </editor-fold>
    
    private ProgressListener listener;
    private volatile State state = State.WAITING;
    private int progress = 0, workload = 0;
    private int successes = 0, fails = 0;
    private ProgressLog currentLog;
    private boolean silent;
 
    //----------STATE----------//
    public boolean start(){
        if(isWaiting()){
            state = State.RUNNING;
            return true;
        }
        return false;
    }
    
    public boolean interrupt(){
        if(isRunning()){
            state = State.INTERRUPTED;
            return true;
        }
        return false;
    }
    
    public boolean fail(int threashold){
        final boolean failed = fails > threashold;
        if(failed) state = State.FAILED;
        return failed;
    }
    
    //----------LOGGING----------//
    public ProgressLog getCurrentLog(){
        return currentLog;
    }
    
    public ProgressLog startNewLog(boolean progressed){
        currentLog = new ProgressLog(progressed? increaseProgress():progress, workload);
        return currentLog;
    }
    
    public void reportCurrentLog(){
        if(currentLog != null) reportLog(currentLog);
    }
    
    public void reportLog(ProgressLog log){
        if(listener != null && !silent) listener.progressed(log);
    }
    
    public void reportState(){
        final var log = new ProgressLog(progress, workload);
        log.appendLine(String.format(TITLE_LOG_MASK, state.toString()));
        reportLog(log);
    }

    public void reportTitle(String title){
        final var log = new ProgressLog(progress, workload);
        log.appendLine(String.format(TITLE_LOG_MASK, title));
        reportLog(log);
    }
    
    public void report(Level level, boolean progressed, String message, Object...args){
        final var log = new ProgressLog(progressed? increaseProgress():progress, workload);
        if(level != null) log.appendLine(level, message, args);
        else log.appendLine(message, args);
        reportLog(log);
    }

    public void report(Level level, String message, Object...args){
        report(level, true, message, args);
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setProgressListener(ProgressListener listener){
        this.listener = listener;
    }

    public void setProgress(int progress){
        this.progress = progress;
    }
        
    public int increaseProgress(){
        return ++progress;
    }
    
    public int addProgress(int amount){
        return progress += amount;
    }

    public void setWorkload(int workload){
        this.workload = workload;
    }
    
    public int increaseWorkload(){
        return ++workload;
    }
    
    public int addWorkload(int amount) {
        return workload += amount;
    }

    public void setStatus(State status){
        this.state = status;
    }

    public void setSuccesses(int successes){
        this.successes = successes;
    }
    
    public int increaseSuccesses(){
        return ++successes;
    }
    
    public int addSuccesses(int amount){
        return successes += amount;
    }

    public void setFails(int fails){
        this.fails = fails;
    }
    
    public int increaseFails(){
        return ++fails;
    }
    
    public int addFails(int amount){
        return fails += amount;
    }
    
    public void resetFails(){
        fails = 0;
    }

    public void setSilent(boolean b) {
        this.silent = b;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public int getProgress(){
        return progress;
    }
    
    public int getWorkload(){
        return workload;
    }

    public State getStatus(){
        return state;
    }
    
    public boolean isInterrupted(){
        return state == State.INTERRUPTED;
    }

    public boolean isWaiting(){
        return state == State.WAITING;
    }
    
    public boolean isRunning(){
        return state == State.RUNNING;
    }

    public int getSuccesses() {
        return successes;
    }

    public int getFails() {
        return fails;
    }
    // </editor-fold>
    
}
