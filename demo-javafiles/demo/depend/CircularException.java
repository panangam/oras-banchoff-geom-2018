package demo.depend;

/**
 * A CircularException is thrown when a circular definition is detected.
 * The only time this should happen is when the user defines or edits
 * something.
 *
 * @author deigen
 */
public class CircularException extends demo.DemoRuntimeException {

    /**
     * @param str a string describing the exception
     */
    public  CircularException( String str ) {
        super( str );
    }


}


