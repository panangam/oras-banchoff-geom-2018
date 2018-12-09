package demo.util;

public class UnsortedVectorDictionary extends java.util .Dictionary{

    private  java.util .Vector keys, elements;

    public  UnsortedVectorDictionary() {
        super();

            this .keys = new java.util .Vector();
            this .elements = new java.util .Vector();
        }

    public  UnsortedVectorDictionary( int initialCapacity ) {
        super();

            this .keys = new java.util .Vector( initialCapacity );
            this .elements = new java.util .Vector( initialCapacity );
        }

    public  java.util .Enumeration keys() {
        return this .keys .elements();
    }

    public  java.util .Enumeration elements() {
        return this .elements .elements();
    }

    public  Object get( Object key ) {
        for ( int i = 0; i < this .keys .size(); ++i ) {
            if ( this .keys .elementAt( i ) .equals( key ) ) {
                return this .elements .elementAt( i );
            }
        }
        return null;
    }

    public  Object put( Object key, Object value ) {
        for ( int i = 0; i < this .keys .size(); ++i ) {
            if ( this .keys .elementAt( i ) .equals( key ) ) {
                Object oldValue = this .elements .elementAt( i );
                this .elements .setElementAt( value, i );
                return oldValue;
            }
        }
        this .keys .addElement( key );
        this .elements .addElement( value );
        return null;
    }

    public  Object remove( Object key ) {
        for ( int i = 0; i < this .keys .size(); ++i ) {
            if ( this .keys .elementAt( i ) .equals( key ) ) {
                Object oldValue = this .elements .elementAt( i );
                this .keys .removeElementAt( i );
                this .elements .removeElementAt( i );
                return oldValue;
            }
        }
        return null;
    }

    public  int size() {
        return this .keys .size();
    }

    public  boolean isEmpty() {
        return size() == 0;
    }

    public  Object clone() {
        UnsortedVectorDictionary clone = new UnsortedVectorDictionary();
        for ( int i = 0; i < this .keys .size(); ++i ) {
            clone .put( this .keys .elementAt( i ),
                         this .elements .elementAt( i ) );
        }
        return clone;
    }

    /**
        * Returns the keys in this dictionary as an array.
        */
    public  Object[] keysArray() {
        Object[] result = new Object [ size() ];
        int i = 0;
        for ( java.util .Enumeration keys = keys(); keys .hasMoreElements();  ) {
            result[i++] = keys .nextElement();
        }
        return result;
    }

    /**
        * Returns the elements in this dictionary as an array.
        */
    public  Object[] elementsArray() {
        Object[] result = new Object [ size() ];
        int i = 0;
        for ( java.util .Enumeration els = elements(); els .hasMoreElements();  ) {
            result[i++] = els .nextElement();
        }
        return result;
    }

    /**
        * Adds to this dictionary all the elements of the given dictionary.
        */
    public  void add( UnsortedVectorDictionary dictionary ) {
        for ( java.util .Enumeration keys = dictionary .keys(); keys .hasMoreElements();  ) {
            Object key = keys .nextElement();
            put( key, dictionary .get( key ) );
        }
    }

    /**
        * For debugging purposes.
        */
    public  void print() {
        /*for ( java.util .Enumeration keys = keys(); keys .hasMoreElements();  
        ) {
            Object key = keys .nextElement();
            System .out .println( key + ":" + get( key ) );
        }*/
    }


}


