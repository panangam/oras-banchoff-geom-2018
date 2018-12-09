//
//  ExecCallback.java
//  Demo
//
//  Created by David Eigen on Tue Jun 24 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.exec;

/**
 * An Exec.Callback can be put on an execution thread's queue.
 * Use Exec.run(Callback) to put a callback on the default execution
 * queue, and to start put the thread.
 */
public abstract class ExecCallback {
    /**
     * Values for the status variable of cleanup(.).
     * Can be used as bit masks. This means to find if the status is
     * either RUNNING or COMPLETE, you can say:
     * if (status & (RUNNING | COMPLETE)) ...
     */
    public static final int QUEUED = 0x1, RUNNING = 0x2, COMPLETE = 0x4;

    /**
     * Performs the action for this callback.
     */
    public abstract void invoke();

    /**
     * Called after the callback was invoked and completed running, or when
     * the execution thread was cancelled and the callback was on the queue.
     * The parameter status is set to one of QUEUED, RUNNING, or COMPLETE,
     * indicating the state at which cleanup was called. If status is
     * QUEUED, then the execution thread was stopped and the queue cleared
     * before the callback got a chance to run. If status is RUNNING, then
     * the execution thread was stopped while the invoke(.) method was
     * running. If status is COMPLETE, then the callback was completed
     * normally, and cleanup is being called for normal cleanup operations.
     * The execution thread will not be cancelled during a call to cleanup.
     * By default, this method does nothing.
     */
    public void cleanup(int status) { cleanup(); }

    /**
     * Alternative to cleanup(int status). If cleanup(int status) is not overridden,
     * cleanup() is called. If cleanup(int status) is overridden, cleanup() will
     * not be called. By default, cleanup() does nothing.
     */
    public void cleanup() { }
}

