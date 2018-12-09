//
//  Exec.java
//  Demo
//
//  Created by David Eigen on Sun Jun 22 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.exec;

import demo.util.*;

/**
 * This class contains the execution thread. Calculations can be queued onto
 * this thread, to allow the currently executing thread to proceed. Thre is
 * one default execution queue and thread. Exec is a singleton class.
 */
public class Exec {

    private ExecThread runThread_;
    
    private static final Exec inst_ = new Exec();

    private Exec() {
        runThread_ = new ExecThread("Demo-ExecQueue");
        runThread_.startup();
    }
    

    /**
     * Makes sure the execution thread is started up and running.
     */
    public static void startup() {
        inst_.runThread_.startup();
    }

    /**
     * Adds a status listener for the default execution queue.
     */
    public static void addStatusListener(ExecStatusListener l) {
        inst_.runThread_.addStatusListener(l);
    }

    /**
     * Removes a status listener for the default execution queue.
     */
    public static void removeStatusListener(ExecStatusListener l) {
        inst_.runThread_.removeStatusListener(l);
    }

    /**
     * Breaks execution and removes everything from the execution queue.
     * This method immediately stops the current thing running
     * on the default execution queue, and clears the queue.
     */
    public static void breakExecution() {
        inst_.runThread_.breakExecution();
    }

    /**
     * Returns the status of the default execution queue.
     * @return true if the default execution queue is running something, false if waiting
     */
    public static boolean getStatus() {
        return inst_.runThread_.getStatus();
    }

    /**
     * Enters an uncancellable section of code. If the current thread is an Exec thread
     * (i.e. the default Exec queue), then it will turn uncancellable. If it is cancelled
     * while uncancellable, it will be cancelled when it becomes cancellable again.
     * begin/end_nocancel blocks may be nested.
     */
    public static void begin_nocancel() {
        if (inst_.runThread_.isCurrThread())
            inst_.runThread_.setUncancellable();
    }

    /**
     * Ends an uncancellable section of code. If the current thread is an Exec thread
     * (i.e. the default Exec queue), then it will turn cancellable. If it was cancelled
     * while uncancellable, it will be cancelled.
     * begin/end_nocancel blocks may be nested.
     */
    public static void end_nocancel() {
        if (inst_.runThread_.isCurrThread())
            inst_.runThread_.setCancellable();
    }

    /**
     * Queues the given callback for running. The callback
     * will be invoked on the given object with the given arguments in the
     * execution thread when this object comes to the top of the execution thread.
     * Wakes the execution thread to start running this method, if it is not already running.
     * @param obj the object to invoke the method on
     * @param method the method to invoke
     * @param args the arguments to supply the method with
     */
    public static void run(ExecCallback callback) {
        inst_.runThread_.add(callback);
    }


}




class ExecThread implements Runnable {

    private Queue runQ_;
    private Set statusListeners_;
    private Thread thr_;
    private volatile boolean status_;
    private boolean cancelled_;
    private int uncancellableLevel_;
    private String thrName_;
    
    public ExecThread(String name) {
        super();
        thrName_ = name;
        runQ_ = new Queue();
        statusListeners_ = new Set();
        status_ = false;
        uncancellableLevel_ = 0;
        cancelled_ = false;
        thr_ = null;
    }

    public void startup() {
        if (thr_ == null || !thr_.isAlive()) {
            thr_ = new Thread(this, thrName_);
            thr_.start();
        }
    }

    public boolean isCurrThread() {
        return thr_ == Thread.currentThread();
    }
    
    public void run() {
        while (true) {
            ExecCallback currCallback = null;
            try {
                synchronized(this) {
                    if (runQ_.isEmpty()) {
                        status_ = false;
                        for (java.util.Enumeration ls = statusListeners_.elements();
                             ls.hasMoreElements();)
                            ((ExecStatusListener) ls.nextElement()).execStatusChanged(false);
                    }
                    while (runQ_.isEmpty()) {
                        try {  this.wait();  }
                        catch (InterruptedException ex) { }
                    }
                }
                status_ = true;
                for (java.util.Enumeration ls = statusListeners_.elements();
                     ls.hasMoreElements();)
                    ((ExecStatusListener) ls.nextElement()).execStatusChanged(true);
                while (!runQ_.isEmpty()) {
                    synchronized(thr_) {
                        currCallback = (ExecCallback) runQ_.dequeue();
                    }
                    currCallback.invoke();
                    synchronized(thr_) {
                        currCallback.cleanup(ExecCallback.COMPLETE);
                        currCallback = null;
                        if (uncancellableLevel_ != 0) {
                            System.err.println("WARNING: Exec thread uncancellable after " +
                                               "job done. Resetting to be cancellable.");
                            uncancellableLevel_ = 1;
                            setCancellable();
                        }
                    }
                }
            }
            catch (Throwable ex) {
                if (ex instanceof ThreadDeath) {
                    // the thread got cancelled: remove everything from the queue, and
                    // call all cleanup handlers
                    if (currCallback != null)
                        currCallback.cleanup(ExecCallback.RUNNING);
                    for (java.util.Enumeration cbs = runQ_.elements(); cbs.hasMoreElements();)
                        ((ExecCallback) cbs.nextElement()).cleanup(ExecCallback.QUEUED);
                    currCallback = null;
                    throw (ThreadDeath) ex; // ThreadDeaths should propagate to the top for cleanup
                }
                System.err.println("Exception occurred in exec queue execution:");
                ex.printStackTrace(System.err);
                // the exception might have been thrown inside an uncancellable block
                // in this case, we are not in an uncancellable block anymore, since the
                // exception propogated to the top level
                uncancellableLevel_ = 0;
                if (cancelled_)
                    breakExecution();
            }
        }
    }

    // adds a call to the queue, and starts running if not already running
    public synchronized void add(ExecCallback call) {
        runQ_.enqueue(call);
        this.notify();
    }

    public void setCancellable() {
        synchronized(thr_) {
            if ((--uncancellableLevel_ == 0) && cancelled_)
                breakExecution();
        }
    }

    public void setUncancellable() {
        synchronized(thr_) {
            ++uncancellableLevel_;
        }
    }

    public void breakExecution() {
        synchronized(thr_) {
            if (!thr_.isAlive())
                return;
            if (uncancellableLevel_ != 0) {
                cancelled_ = true;
                return;
            }
            thr_.stop();
            try {
                thr_.join();
            }
            catch (InterruptedException ex) {}
            cancelled_ = false;
        }
        synchronized(this) {
            while (!runQ_.isEmpty())
                runQ_.dequeue();
        }
        thr_ = new Thread(this, thrName_);
        thr_.start();
    }

    public boolean getStatus() {
        return status_;
    }

    public void addStatusListener(ExecStatusListener l) {
        statusListeners_.add(l);
    }

    public void removeStatusListener(ExecStatusListener l) {
        statusListeners_.remove(l);
    }

}

