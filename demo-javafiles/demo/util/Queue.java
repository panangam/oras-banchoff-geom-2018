//
//  Queue.java
//  Demo
//
//  Created by David Eigen on Sun Jun 22 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.util;

public class Queue {

    private QueueNode front_;
    private QueueNode back_;

    public Queue() {
        front_ = back_ = new QueueNode(null);
    }
    
    /**
     * adds the given object to the back of the queue
     */
    public synchronized void enqueue(Object obj) {
        back_.next = new QueueNode(obj);
        back_ = back_.next;
    }

    /**
     * removes the object at the front of the queue, and returns it
     * @return the object at the front of the queue
     */
    public synchronized Object dequeue() {
        Object obj = front_.next.obj;
        front_ = front_.next;
        return obj;
    }

    /**
     * @return the object at the front of the queue
     */
    public synchronized Object peek() {
        return front_.next.obj;
    }

    /**
     * @return whether this queue is empty
     */
    public synchronized boolean isEmpty() {
        return front_ == back_;
    }

    /**
     * @return whether this queue is empty
     */
    public synchronized boolean empty() {
        return front_ == back_;
    }

    /**
     * @return an Enumeration that goes through all elements in this queue,
     *         from fron tot back
     */
    public synchronized java.util.Enumeration elements() {
        return new QueueEnumeration(front_, back_);
    }


    private class QueueNode {
        public Object obj;
        public QueueNode next;
        public QueueNode(Object o) {
            obj = o;  next = null;
        }
    }

    private class QueueEnumeration implements java.util.Enumeration {
        private QueueNode curr, back;
        public QueueEnumeration(QueueNode front, QueueNode back) {
            curr = front; this.back = back;
        }
        public boolean hasMoreElements() {
            return curr != back_;
        }
        public Object nextElement() {
            Object obj = curr.next.obj;
            curr = curr.next;
            return obj;
        }
    }
    

}
