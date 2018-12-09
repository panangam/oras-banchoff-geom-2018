package demo.ui;

import java.awt .*;
import java.awt.event .*;

import demo.util.Set;

public class Slider extends Panel {

    private  double min, max;

    private  boolean round;

    private  double value;

    private  TextField valueField;

    private  SliderCanvas canvas;

    private Set listeners_ = new Set();
    
    public  Slider( int min, int max ) {
        this( min, max, new Integer(max).toString().length() );
    }

    public  Slider( int min, int max, int valueFieldChars ) {
        super();

            this .min = min;
            this .max = max;
            this .round = true;
            this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
            this .canvas = new SliderCanvas( 100, this );
            this .add( canvas );
            this .valueField = new TextField( valueFieldChars );
            if ( valueFieldChars > 0 ) {
                this .add( valueField );
            }
            canvas .setPosition( 0 );
            valueField .setText( String .valueOf( (int) min ) );
        }

    public  Slider( double min, double max ) {
        this( min, max, 0 );
    }

    public 
    Slider( double min, double max, int valueFieldChars ) {
        super();

            this .min = min;
            this .max = max;
            this .round = false;
            this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
            this .canvas = new SliderCanvas( 100, this );
            this .add( canvas );
            this .valueField = new TextField( valueFieldChars );
            if ( valueFieldChars > 0 ) {
                this .add( valueField );
            }
            canvas .setPosition( 0 );
            valueField .setText( String .valueOf( min ) );
        }

    /**
     * Adds an action listener. An action is performed when the value of the slider changes.
     */
    public void addActionListener(ActionListener l) {
        listeners_.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners_.remove(l);
    }

    public  double value() {
        if ( round ) {
            return (int) Math .round( value );
        }
        else {
            return value;
        }
    }

    public  int intValue() {
        return (int) Math .round( value );
    }

    public  void setValue( double value ) {
        if ( round ) {
            this .value = Math .round( value );
            valueField .setText( String .valueOf( (int) (Math .round( this .value * Math .pow( 10, valueField .getColumns() ) ) / Math .pow( 10, valueField .getColumns() )) ) );
        }
        else {
            this .value = value;
            valueField .setText( String .valueOf( Math .round( value * Math .pow( 10, valueField .getColumns() ) ) / Math .pow( 10, valueField .getColumns() ) ) );
        }
        canvas .setPosition( (value - min) / (max - min) );
    }

    public  void enable() {
        canvas .enable();
        if ( round ) {
            valueField .setText( String .valueOf( (int) (Math .round( this .value * Math .pow( 10,  valueField .getColumns() ) ) / Math .pow( 10, valueField .getColumns() )) ) );
        }
        else {
            valueField .setText( String .valueOf( Math .round( value * Math .pow( 10, valueField .getColumns() ) ) / Math .pow( 10, valueField .getColumns() ) ) );
        }
        valueField .enable();
        super .enable();
    }

    public  void disable() {
        canvas .disable();
        valueField .setText( "" );
        valueField .disable();
        super .disable();
    }

    public  boolean action( Event ev, Object obj ) {
        if ( ev .target == valueField ) {
            double newValue;
            try  {
                newValue = new Double( valueField .getText() ) .doubleValue();
            } catch( NumberFormatException ex ) {
                //System .out .println( "Number not formatted properly." );
                return true;
                // put a window here later
            }
            if ( min <= newValue && newValue <= max ) {
                setValue( newValue );
                sendEventToParent();
                notifyListeners();
            }
            else {
                //System .out .println( "Number out of range" );
            }
            return true;
        }
        return false;
    }

    public  void changeValue( double position ) {
        this .value = min + (max - min) * position;
        if ( round ) {
            this .value = Math .round( this .value );
            valueField .setText( String .valueOf( (int) (Math .round( this .value * Math .pow( 10, valueField .getColumns() ) ) / Math .pow( 10, valueField .getColumns() )) ) );
        }
        else {
            valueField .setText( String .valueOf( Math .round( value * Math .pow( 10, valueField .getColumns() ) ) / Math .pow( 10, valueField .getColumns() ) ) );
        }
    }

    public  void sendEventToParent() {
        this .postEvent( new Event( this, Event .ACTION_EVENT, new Double( value ) ) );
    }

    public void notifyListeners() {
        for (java.util.Enumeration lsnrs = listeners_.elements();
             lsnrs.hasMoreElements();)
            ((ActionListener) lsnrs.nextElement()).actionPerformed(new ActionEvent(
                                                                    this,
                                                                    ActionEvent.ACTION_PERFORMED,
                                                                    "SLIDERVALUE"));
    }


}




class SliderCanvas extends Canvas {

    private  Slider myPanel;

    private  boolean enabled = true;

    private  int sliderPosition = 0;

    private  Rectangle sliderRect;

    private  boolean active = false;

    private  int LINE_WIDTH;

    private static final  int SLIDER_WIDTH = 10, SLIDER_HEIGHT = 5;

    private static final  int MARGIN_SIDE = 1, MARGIN_TOPBOTTOM = 1;

    private  Image bufferImage = null;

    private  Graphics bufferGraphics = null;

    private final  Dimension preferredSize;
    

    public  SliderCanvas( int width, Slider slider ) {
        super();

            this .myPanel = slider;
            preferredSize = new Dimension( width + SLIDER_WIDTH + MARGIN_SIDE * 2,
                                           SLIDER_HEIGHT + 1 + MARGIN_TOPBOTTOM * 2 );
            setSize(preferredSize);
            this .LINE_WIDTH = width - 2 * MARGIN_SIDE;
        }

    public  double position() {
        return ((double) (sliderPosition - SLIDER_WIDTH / 2 - MARGIN_SIDE)) / (double) LINE_WIDTH;
    }

    public  void setPosition( double position ) {
        // check for possible underflow
        if ( Math .abs( position ) < 1E-10 ) {
            position = 0;
        }
        sliderPosition = (int) Math .round( position * LINE_WIDTH + SLIDER_WIDTH / 2 + MARGIN_SIDE );
        if ( bufferImage != null ) {
            redraw();
            repaint();
        }
    }

    public  void enable() {
        this .enabled = true;
        if ( bufferImage != null ) {
            redraw();
            repaint();
        }
        super .enable();
    }

    public  void disable() {
        this .active = false;
        this .enabled = false;
        if ( bufferImage != null ) {
            redraw();
            repaint();
        }
        super .disable();
    }

    public  boolean mouseDown( Event ev, int x, int y ) {
        if ( new Rectangle( sliderPosition - SLIDER_WIDTH / 2,
                                  this .MARGIN_TOPBOTTOM, SLIDER_WIDTH + 1,
                                  SLIDER_HEIGHT + 2 ) .inside( x, y ) ) {
            this .active = true;
            return true;
        }
        return false;
    }

    public  boolean mouseDrag( Event ev, int x, int y ) {
        if ( active ) {
            if ( x < MARGIN_SIDE + SLIDER_WIDTH / 2 || x > MARGIN_SIDE + SLIDER_WIDTH / 2 + LINE_WIDTH ) {
                if ( x < MARGIN_SIDE + SLIDER_WIDTH / 2 ) {
                    sliderPosition = MARGIN_SIDE + SLIDER_WIDTH / 2;
                }
                else {
                    sliderPosition = MARGIN_SIDE + SLIDER_WIDTH / 2 + LINE_WIDTH;
                }
            }
            else {
                sliderPosition = x;
            }
            redraw();
            myPanel .changeValue( position() );
            paint(this.getGraphics());
            return true;
        }
        return false;
    }

    public  boolean mouseUp( Event ev, int x, int y ) {
        if ( this .active ) {
            this .active = false;
            redraw();
            repaint();
            myPanel .sendEventToParent();
            myPanel .notifyListeners();
            return true;
        }
        return false;
    }

    private  void redraw() {
        if ( bufferImage == null ) {
            bufferImage = createImage( this .size() .width,
                                               this .size() .height );
            bufferGraphics = bufferImage .getGraphics();
        }
        if ( enabled ) {
            bufferGraphics .setColor( Color .black );
        }
        else {
            bufferGraphics .setColor( Color .gray );
        }
        bufferGraphics .clearRect( 0, 0, this .size() .width,
                                       this .size() .height );
        bufferGraphics .drawLine( MARGIN_SIDE + SLIDER_WIDTH / 2,
                                      MARGIN_TOPBOTTOM,
                                      MARGIN_SIDE + SLIDER_WIDTH / 2 + LINE_WIDTH,
                                      MARGIN_TOPBOTTOM );
        if ( this .active ) {
            bufferGraphics .fillPolygon( new int []{ sliderPosition,
                                                     sliderPosition - SLIDER_WIDTH / 2,
                                                     sliderPosition + SLIDER_WIDTH / 2 },
                                         new int []{ MARGIN_TOPBOTTOM,
                                                     MARGIN_TOPBOTTOM + SLIDER_HEIGHT,
                                                     MARGIN_TOPBOTTOM + SLIDER_HEIGHT },
                                         3 );
        }
        else {
            if ( this .enabled ) {
                bufferGraphics .drawPolygon( new int []{ sliderPosition,
                                                         sliderPosition - SLIDER_WIDTH / 2,
                                                         sliderPosition + SLIDER_WIDTH / 2 },
                                             new int []{ MARGIN_TOPBOTTOM,
                                                         MARGIN_TOPBOTTOM + SLIDER_HEIGHT,
                                                         MARGIN_TOPBOTTOM + SLIDER_HEIGHT },
                                              3 );
            }
        }
    }

    public  void paint( Graphics g ) {
        if ( bufferImage == null ) {
            redraw();
        }
        g .drawImage( bufferImage, 0, 0, this );
    }

    public  void update( Graphics g ) {
        paint( g );
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }
    public Dimension getMinimumSize() {
        return preferredSize;
    }
    public Dimension getMaximumSize() {
        return preferredSize;
    }

}


