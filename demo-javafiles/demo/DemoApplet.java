package demo;

import java.applet .Applet;
import java.awt .*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * The DemoApplet class converts the Demo application into an applet.
 * The applet tag can contain stuff to put into the demo.
 *
 * @author deigen
 */
public class DemoApplet extends Applet implements ActionListener {

    // this button launches the demo
    private  Button goBtn = new Button( "Demo" );
        
    /**
     * The instance of Demo produced after the user clicks the "go" button.
     */
    public  Demo demo;

    private boolean unsupportedSystem() {
        return System.getProperty("java.vendor").startsWith("Netscape") &&
        System.getProperty("os.name").equals("Mac OS");
    }
    
    public  void init() {
        if (unsupportedSystem()) {
            this.setLayout(new GridLayout(1,1));
            TextArea message = new TextArea("You are running this applet on\nan unsupported system. If you\nare using Netscape for Mac,\ninstall the MRJPlugin or use\nInternet Explorer.", 5, 30);
            message.setEditable(false);
            this.add(message);
            return;
        }
        this .setLayout( new GridLayout(1,1) );
        String buttonLabel = getParameter( "NAME" );
        if ( buttonLabel != null ) {
            goBtn .setLabel( buttonLabel );
        }
        this .add( goBtn );
        goBtn.addActionListener(this);
        goBtn .setEnabled(true);
    }


    public void actionPerformed(ActionEvent ev) {
        if ( ev.getSource() == goBtn ) {
            if ((ev.getModifiers() & ActionEvent.SHIFT_MASK) != 0 &&
                (ev.getModifiers() & ActionEvent.CTRL_MASK)  != 0)
                showDataTag();
            else
                startDemo();
        }
    }
    
    /**
     * Starts the demo by making a new Demo and disabling the "go" button.
     */
    public  void startDemo() {
        goBtn .setEnabled(false);
        demo = new Demo( this, getParameter( "DATA" ) );
    }
    
    /**
     * Ends the demo by disposing the Demo and enabling the "go" button.
     */
    public  void endDemo() {
        demo = null;
        goBtn .setEnabled(true);
    }

    public void stop() {
        // the applet is stopping: stop the demo (necessary to free up stuff)
        if (demo != null)
            demo.quit();
        super.stop();
    }
    
    // shows the data tag
    private  void showDataTag() {
        String data = getParameter( "DATA" );
        if ( data == null ) {
            new DataWindow( "" );
        }
        else {
            int start = 0, index = 0;
            String newdata = "";
            while ((index = data.indexOf("<br>", start)) != -1) {
                newdata += data.substring(start, index) + " \n";
                start = index + 4;
            }
            newdata += data.substring(start);
            data = newdata;
            newdata = "";
            index = start = 0;
            while ((index = data.indexOf("<BR>", start)) != -1) {
                newdata += data.substring(start, index) + " \n";
                start = index + 4;
            }
            newdata += data.substring(start);
            data = newdata;
            new DataWindow( data );
        }
    }

}


/**
 * Displays the "DATA" parameter info. after the user clicks
 * the "Show Data" button. (in version 1 & 2 -- out in v3)
 *
 * @author deigen
 */
class DataWindow extends demo.ui.DemoFrame{

    // closes the window
    private  java.awt .Button closeButton;
    
    /**
     * @param text the String to display in the window
     */
    public  DataWindow( String text ) {
        super( "Applet Tag" );

            setLayout( new java.awt .BorderLayout() );
            java.awt .TextArea area = new java.awt .TextArea( text, 24, 36 );
            area .setEditable( false );
            add( new java.awt .Label( "Copy and paste the following data in a newly launched application to load this demo." ), "North" );
            add( area, "Center" );
            add( this .closeButton = new java.awt .Button( "Close" ), "South" );
            pack();
            setVisible(true);
        }

    public boolean action( java.awt .Event event, Object object ) {
        if ( event .target == this .closeButton ) {
            setVisible(false);
            dispose();
            return true;
        }
        else {
            return super .action( event, object );
        }
    }

    public  boolean handleEvent( java.awt .Event event ) {
        if ( event .id == java.awt .Event .WINDOW_DESTROY ) {
            setVisible(false);
            dispose();
            return true;
        }
        else {
            return super .handleEvent( event );
        }
    }


}


