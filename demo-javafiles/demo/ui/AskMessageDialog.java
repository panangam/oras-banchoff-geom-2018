package demo.ui;
import java.awt .*;
    
/**
 * A dialog box that displays a message and a field for a user response.
 * Used for getting information from the user.
 *
 * @author deigen
 */
public class AskMessageDialog extends Dialog{
    
    /**
    * @param text the text to display to the user
    * @param defaultAnswer the default text that first appears in the field
    * @param parent the parent frame for this dialog box
    * @param handler the Component that will handle the action delivered from
    *     this dialog when the OK button is pressed
    * @param eventArguments an Object passed as the arguments in the Event 
    *    delivered to the handler
    */
    public AskMessageDialog( String text, String defaultAnswer, Frame parent,
                                    Component handler, Object eventArguments ) {
                super( parent );

                this .eventArguments = eventArguments;
                this .handler = handler;
                this .defaultResponce = defaultAnswer;
                this .answerFld = new TextField( defaultAnswer );
                this .setLayout( new BorderLayout() );
                this .add( new Label( text ), "North" );
                this .add( answerFld, "Center" );
                Panel buttonPanel = new Panel();
                buttonPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
                buttonPanel .add( cancelBtn );
                buttonPanel .add( okBtn );
                this .add( buttonPanel, "South" );
                this .pack();
                this .setVisible(true);
    }
    
        /**
        * @param text the text to display to the user
        * @param defaultAnswer the default text that first appears in the field
        * @param parent the parent frame for this dialog box
        * @param handler the Component that will handle the action delivered from
        *     this dialog when the OK button is pressed
        */
        public AskMessageDialog( String text, String defaultAnswer, Frame parent,
                                    Component handler ) {
        this( text, defaultAnswer, parent, handler, null );
    }
        
        
    /**
    * returns the text in the field that the user responded with
    * @return the text of the user response
    */ 
    public  String getResponce() {
        return answerFld .getText();
    }

    private  String defaultResponce;

    private  Object eventArguments;

    private  Component handler;

    private  TextField answerFld;

    private Button okBtn = new Button( "OK" ), cancelBtn = new Button( "Cancel" );

    public  boolean action( Event e, Object obj ) {
        if ( e .target == okBtn ) {
            // send event to handler
            handler .deliverEvent( new Event( this, Event .ACTION_EVENT, eventArguments ) );
            setVisible(false);
            dispose();
            return true;
        }
        else {
            if ( e .target == cancelBtn ) {
                this .answerFld .setText( defaultResponce );
                setVisible(false);
                dispose();
                return true;
            }
            else {
                if ( e .target == answerFld ) {
                    // the pressed return in the answer field, so send an OK event to me
                    return action( new Event( okBtn, Event .ACTION_EVENT, null ),
                                        null );
                }
            }
        }
        return super .action( e, obj );
    }

    public  boolean keyDown( Event e, int key ) {
        if ( (key == 10 || key == 3) && ! (e .target instanceof TextComponent) ) {
            // enter or return key pressed: send OK event
            return action( new Event( okBtn, Event .ACTION_EVENT, null ),
                                null );
        }
        return super .keyDown( e, key );
    }


}


