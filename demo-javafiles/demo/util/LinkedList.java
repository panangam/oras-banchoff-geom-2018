package demo.util;
/**
 * The LinkedList class is a linked list of Object. Objects can be added to
 * the end of the list using add(.). Objects in the list can be accessed
 * using the methods of java.util.Enumeration, which LinkedList implements.
 *
 * @author deigen
 */
public class LinkedList extends Object implements java.util .Enumeration{

    // the first node of the list
    private  ListNode first = null;

    // the last node of the list
    private  ListNode last = null;

    // the current node in the list (the one we're up to from enumerating)
    private  ListNode current = null;

    // the number of nodes in the list
    private  int size = 0;
    
    // data entries for the first node of the list (the node just before the first real node)
    private static final Object FIRST_DATA = new Object();

    /**
     * @return the number of objects in the list
     */
    public  int size() {
        return size;
    }

    public  LinkedList() {
        super();
        
            first = new ListNode( FIRST_DATA );
            current = first;
            last = first;
    }

    /**
     * Creates a new list containing the given entry.
     * @param entry the entry to put in the list
     */
    public  LinkedList( Object entry ) {
        this();

            add( entry );
        }

    /**
     * Creates an enumeration of the elements. This enumeration is different from
     * the LinkedList, so reseting the LinkedList will not affect this enumeration.
     * @return an enumeration of the elements in the list
     */
    public  java.util .Enumeration elements() {
        return new LinkedListEnumeration(this);
    }

    /**
     * Copies the given LinkedList into this list.
     * The nodes of the given list are appended to the nodes of this list.
     * @param list the list to append to this list
     */
    public  void append( LinkedList list ) {
        ListNode curr = list.first.next;
        while (curr != null) {
            this.last.next = new ListNode(curr.data);
            this.last = this.last.next;
            curr = curr.next;
        }
        size += list.size;
    }

    /**
     * Adds the given object to the end of the list.
     * @param obj the object to put at the end of the list
     */
    public  void add( Object obj ) {
        // put a new node at the end
        ListNode node = new ListNode( obj );
        this .last .next = node;
        this .last = node;
        size++;
    }

    /**
     * Removes the given object from this list
     * (this is a O(n) operation)
     */
    public void remove(Object obj) {
        ListNode prev = null;
        ListNode curr = first;
        while (curr != null && curr.data != obj) {
            prev = curr;
            curr = curr.next;
        }
        if (curr != null)
            prev.next = curr.next;
    }

    public  boolean hasMoreElements() {
        return current != last;
    }

    public  Object nextElement() {
        current = current .next;
        return current .data;
    }

    /**
     * Resets the enumeration, so nextElement() will return the first element of the list.
     */
    public  void resetEnumeration() {
        current = first;
    }
    

    private class LinkedListEnumeration implements java.util.Enumeration {

        private LinkedList list_;
        private ListNode curr_;

        public LinkedListEnumeration(LinkedList list) {
            list_ = list;
            curr_ = list.first;
        }

        public boolean hasMoreElements() {
            return curr_ != list_.last;
        }

        public Object nextElement() {
            curr_ = curr_.next;
            return curr_.data;
        }

    }


    private class ListNode extends Object{

        public  ListNode next = null;

        public  Object data;

        public  ListNode( Object data ) {
            super();

            this .data = data;
        }

    }
    

}



