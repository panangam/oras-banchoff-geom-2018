
package demo.exec;

/**
 * An ExecStatusListener is used to find out the status of the execution
 * thread. Use Exec.addStatusListener(.) to add a listener, and
 * Exec.removeStatusListener(.) to remove one.
 */
public interface ExecStatusListener {
	/**
	 * Called when the status of a exec queue changes.
	 * @param running whether the queue is running or asleep
	 */
	public void execStatusChanged(boolean running);
}
