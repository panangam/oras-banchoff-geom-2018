package demo.expr.ui;
import java.awt.*;
import demo.ui.FlowVerticalLayout;

public class TapeDeck extends Panel{

    private  VariablePanel vp;

    private  int resolution = 0, current = 0;

    private  java.awt .Button backward, dec, inc, forward;

    private  java.awt .TextField readout;

    private  TapeDeckAnimator thread;

    public  TapeDeck( VariablePanel vp ) {
        super();

            this .vp = vp;
            this .thread = null;
            Panel backPanel = new Panel();
            backPanel.setLayout(new FlowVerticalLayout(FlowVerticalLayout.RIGHT));
            backPanel.add( this .dec = new java.awt .Button( "<" ) );
            backPanel.add( this .backward = new java.awt .Button( "<<" ) );
            Panel forwPanel = new Panel();
            forwPanel.setLayout(new FlowVerticalLayout(FlowVerticalLayout.LEFT));
            forwPanel.add( this .inc = new java.awt .Button( ">" ) );
            forwPanel.add( this .forward = new java.awt .Button( ">>" ) );
            add( backPanel );
            add( this .readout = new java.awt .TextField( 4 ) );
            add( forwPanel );
        }

    public  void setResolution( int resolution ) {
        this .resolution = resolution;
        if ( this .current > this .resolution ) {
            this .vp .nowAt( this .current = this .resolution );
        }
    }

    public 
    boolean action( java.awt .Event event, Object object ) {
        if ( event .target == this .dec )
            decrement();
        else if ( event .target == this .inc )
            increment();
        else if ( event .target == this .backward ) {
            if ( this .thread == null ) {
                this .vp .suspendGraphs();
                setPauseButtonLabels();
                (this .thread = new TapeDeckAnimator( this, false )) .start();
            }
            else {
                done( true );
            }
        }
        else if ( event .target == this .forward ) {
            if ( this .thread == null ) {
                this .vp .suspendGraphs();
                setPauseButtonLabels();
                (this .thread = new TapeDeckAnimator( this, true )) .start();
            }
            else {
                done( true );
            }
        }
        else if ( event .target == this .readout )
            this .vp .setVariableValue( readout .getText() );
        else
            return super .action( event, object );
        return true;
    }

    public  boolean decrement() {
        if ( this .current > 0 ) {
            this .vp .nowAt( --this .current );
            return true;
        }
        else {
            return false;
        }
    }

    public  boolean increment() {
        if ( this .current < this .resolution ) {
            this .vp .nowAt( ++this .current );
            return true;
        }
        else {
            return false;
        }
    }

    public  void setStep( int step ) {
        this .current = step;
    }

    public  void done( boolean kill ) {
        synchronized(this) {
            if ( kill ) {
                this .thread .end();
                this .notify(); // make the thread run so it can stop
                return;
            }
            this .thread = null;
            this .vp .unsuspendGraphs();
            resetButtonLabels();
            this.notify();
        }
    }

    public  void readout( String value ) {
        this .readout .setText( value );
    }

    private  void setPauseButtonLabels() {
        this .backward .setLabel( "||" );
        this .forward .setLabel( "||" );
    }

    private  void resetButtonLabels() {
        this .backward .setLabel( "<<" );
        this .dec .setLabel( "<" );
        this .inc .setLabel( ">" );
        this .forward .setLabel( ">>" );
    }


}


class TapeDeckAnimator extends Thread{

    private  TapeDeck deck;
    private  boolean stop;
    private  boolean direction;

    // min time for an animation step in milliseconds
    private  static final long MIN_STEP_TIME = 50;

    public  TapeDeckAnimator( TapeDeck deck, boolean direction ) {
        super();

            this .deck = deck;
            this .direction = direction;
        }

    public  void run() {
        stop = false;
        try  {
            synchronized(deck) {
                long startTime = System.currentTimeMillis();
                while ( !stop &&
                        (this .direction ? this .deck .increment() : this .deck .decrement()) ) {
                    deck.wait();
                    if (stop) break;
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTime < MIN_STEP_TIME)
                        Thread.sleep(MIN_STEP_TIME - (endTime - startTime));
                    startTime = System.currentTimeMillis();
                }
                deck.done(false);
            }
        }
        catch( InterruptedException exception ) { }
    }

    public void end() {
        this.stop = true;
    }


}