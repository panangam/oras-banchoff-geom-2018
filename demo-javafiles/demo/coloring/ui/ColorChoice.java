package demo.coloring.ui;
import java.awt.*;
import demo.ui.*;

public class ColorChoice extends Panel{

	public  ColorChoice( java.awt .Color defaultColor ) {
		this( new double []{ defaultColor .getRed() / 255.0,
                                     defaultColor .getGreen() / 255.0,
                                     defaultColor .getBlue() / 255.0,
                                     1 } );
	}

	public  ColorChoice( double[] defaultColor ) {
		super();

			previewCanvas = new ColorPreviewCanvas( 10, 10 );
			previewCanvas .setColor( defaultColor );
			colorChoice = new Choice();
			for ( int i = 0; i < colornames .length; ++i ) {
				colorChoice .addItem( colornames[i] );
			}
			int colorIndex;
			for ( colorIndex = 0; colorIndex < colors .length; ++colorIndex ) {
				if ( defaultColor[0] == colors[colorIndex][0] 
                                     && defaultColor[1] == colors[colorIndex][1] 
                                     && defaultColor[2] == colors[colorIndex][2]
                                     && defaultColor[3] == colors[colorIndex][3] ) {
					colorChoice .select( colorIndex );
					break;
				}
			}
			if ( colorIndex == colors .length ) {
				// it's an other color
				colorChoice .select( OTHER_INDEX );
			}
			// set other color to the default color
			colors[colorIndex][0] = defaultColor[0];
			colors[colorIndex][1] = defaultColor[1];
			colors[colorIndex][2] = defaultColor[2];
			colors[colorIndex][3] = defaultColor[3];
			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			this .add( previewCanvas );
			this .add( colorChoice );
		}

	public  double[] selectedColor() {
		return colors[colorChoice .getSelectedIndex()];
	}

	private 
	String[] colornames = { "Blue",
                                "Cyan",
                                "Green",
                                "Yellow",
                                "Orange",
                                "Red",
                                "Pink",
                                "Magenta",
                                "White",
                                "Gray - Light",
                                "Gray",
                                "Gray - Dark",
                                "Black",
                                "Other..." };

	private 
	double[][] colors = {   { 0, 0, 1, 1 },
                                { 0, 1, 1, 1 },
                                { 0, 1, 0, 1 },
                                { 1, 1, 0, 1 },
                                { 1, .8, 0, 1 },
                                { 1, 0, 0, 1 },
                                { 1, .7, .7, 1 },
                                { 1, 0, 1, 1 },
                                { 1, 1, 1, 1 },
                                { .75, .75, .75, 1 },
                                { .5, .5, .5, 1 },
                                { .25, .25, .25, 1 },
                                { 0, 0, 0, 1 },
                                { 0, 0, 0, .5 }   };

	private  int OTHER_INDEX = colornames .length - 1;

	private  ColorPreviewCanvas previewCanvas;

	private  Choice colorChoice;

	public  boolean action( Event e, Object o ) {
		if ( e .target == colorChoice ) {
			previewCanvas .setColor( selectedColor() );
			if ( colorChoice .getSelectedIndex() == OTHER_INDEX ) {
				// make a color selection frame
				new ColorSelectionFrame( colors[OTHER_INDEX], this );
				return true;
			}
			else {
				// set the other color as this color (in case they want to edit)
				colors[OTHER_INDEX] = new double []{
                                    colors[colorChoice .getSelectedIndex()][0],
                                    colors[colorChoice .getSelectedIndex()][1],
                                    colors[colorChoice .getSelectedIndex()][2],
                                    colors[colorChoice .getSelectedIndex()][3] };
                            // we have a new selection, so send the event to the parent
				e .target = this;
				return false;
			}
		}
		if ( e .target instanceof ColorSelectionFrame ) {
			// a color selection for the other color is finished
			select( ((ColorSelectionFrame) e .target) .color() );
			// send an event to the parent
			e .target = this;
			return false;
		}
		return false;
	}

	public  void select( Color color ) {
		select( new double []{ color .getRed() / 255.0,
                                       color .getGreen() / 255.0,
                                       color .getBlue() / 255.0,
                                       1 } );
	}

	public  void select( double[] color ) {
		colors[OTHER_INDEX] = color;
		previewCanvas .setColor( color );
		// select the color
		for ( int colorIndex = 0; colorIndex < colors .length; ++colorIndex ) {
			if ( color[0] == colors[colorIndex][0] 
                             && color[1] == colors[colorIndex][1] 
                             && color[2] == colors[colorIndex][2] 
                             && color[3] == colors[colorIndex][3] ) {
				colorChoice .select( colorIndex );
				return ;
			}
		}
		colorChoice .select( OTHER_INDEX );
	}

	public  void enable() {
		colorChoice .enable();
		previewCanvas .enable();
		super .enable();
	}

	public  void disable() {
		colorChoice .disable();
		previewCanvas .disable();
		super .disable();
	}


}



class ColorSelectionFrame extends DemoFrame{

	public 
	ColorSelectionFrame( double[] defaultColor, Component handler ) {
		super( "Select Color" );

			this .handler = handler;
			this .color = new double []{ defaultColor[0],
                                                     defaultColor[1],
                                                     defaultColor[2],
                                                     defaultColor[3] };
			this .setLayout( new BorderLayout() );
			Panel centerPanel = new Panel();
			centerPanel .setLayout( new GridLayout( 0, 1 ) );
			Panel redPanel = new Panel();
			redPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
			Panel greenPanel = new Panel();
			greenPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
			Panel bluePanel = new Panel();
			bluePanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
			Panel alphaPanel = new Panel();
			alphaPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
			redPanel .add( new Label( "Red:" ) );
			redPanel .add( redSlider );
			greenPanel .add( new Label( "Green:" ) );
			greenPanel .add( greenSlider );
			bluePanel .add( new Label( "Blue:" ) );
			bluePanel .add( blueSlider );
			alphaPanel .add( new Label( "Opacity:" ) );
			alphaPanel .add( alphaSlider );
                        if ( defaultColor[3] == 0 ) {
                            redSlider .setValue(0);
                            greenSlider .setValue(0);
                            blueSlider .setValue(0);
                            alphaSlider .setValue(0);
                        }
                        else {
                            redSlider .setValue( defaultColor[0] / defaultColor[3] * (MAX_VALUE - MIN_VALUE) + MIN_VALUE );
                            greenSlider .setValue( defaultColor[1] / defaultColor[3] * (MAX_VALUE - MIN_VALUE) + MIN_VALUE );
                            blueSlider .setValue( defaultColor[2] / defaultColor[3] * (MAX_VALUE - MIN_VALUE) + MIN_VALUE );
                            alphaSlider .setValue( defaultColor[3] * (MAX_VALUE - MIN_VALUE) + MIN_VALUE );
                        }
			centerPanel .add( redPanel );
			centerPanel .add( greenPanel );
			centerPanel .add( bluePanel );
			centerPanel .add( alphaPanel );
			this .add( centerPanel, "Center" );
			Panel buttonPanel = new Panel();
			buttonPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
			buttonPanel .add( cancelBtn );
			buttonPanel .add( okBtn );
			this .add( buttonPanel, "South" );
			previewCanvas = new ColorPreviewCanvas( 40, 40 );
			previewCanvas .setColor( defaultColor );
			Panel previewCanvasPanel = new Panel();
			previewCanvasPanel .setLayout( new FlowLayout() );
			previewCanvasPanel .add( previewCanvas );
			this .add( previewCanvasPanel, "East" );
			this .pack();
			previewCanvas .repaint();
                        this .setLocation(200,100);
                        setResizable(false);
                        this .setVisible(true);
		}

	private  Component handler;

	private  ColorPreviewCanvas previewCanvas;

	private  int MIN_VALUE = 0, MAX_VALUE = 100;

	private 
	Slider redSlider = new Slider( MIN_VALUE, (int) MAX_VALUE, 3 ),
			greenSlider = new Slider( MIN_VALUE, (int) MAX_VALUE, 3 ),
			blueSlider = new Slider( MIN_VALUE, (int) MAX_VALUE, 3 ),
			alphaSlider = new Slider( MIN_VALUE, (int) MAX_VALUE, 3 );

	private  double[] color;

	private 
	Button cancelBtn = new Button( "Cancel" ),
			okBtn = new Button( "OK" );

	public  double[] color() {
		return color;
	}

	public  boolean action( Event ev, Object obj ) {
		if ( ev .target == cancelBtn ) {
                    setVisible(false);
                    dispose();
                    return true;
                }
		if ( ev .target == okBtn ) {
			// send ok to handler
			handler .deliverEvent( new Event( this, Event .ACTION_EVENT, null ) );
			setVisible(false);
                        dispose();
			return true;
		}
		if ( ev .target instanceof Slider ) {
			// refresh the preview canvas
			calculateColor();
			return true;
		}
		return false;
	}

	public  boolean handleEvent( Event e ) {
		if ( e .id == java.awt .Event .WINDOW_DESTROY ) {
                    setVisible(false);
                    dispose();
                    return true;
		}
		else {
                    return super .handleEvent( e );
		}
	}

	private  void calculateColor() {
		double red = (redSlider .value() - MIN_VALUE) / (MAX_VALUE - MIN_VALUE);
		double green = (greenSlider .value() - MIN_VALUE) / (MAX_VALUE - MIN_VALUE);
		double blue = (blueSlider .value() - MIN_VALUE) / (MAX_VALUE - MIN_VALUE);
		double alpha = (alphaSlider .value() - MIN_VALUE) / (MAX_VALUE - MIN_VALUE);
		this .color = new double []{ red*alpha,
                                             green*alpha,
                                             blue*alpha,
                                             alpha };
		previewCanvas .setColor( color );
		previewCanvas .repaint();
	}


}



class ColorPreviewCanvas extends Canvas{

    private int width_, height_;

    public  ColorPreviewCanvas( int width, int height ) {
        super();
        width_ = width; height_ = height;
        this .setSize( width, height );
    }

    public  void setColor( Color color ) {
        setColor( new double []{ color .getRed() / 255.0,
            color .getGreen() / 255.0,
            color .getBlue() / 255.0,
            1 } );
    }

    public  void setColor( double[] color ) {
        this .color = color;
        repaint();
    }

    public  void enable() {
        this .enabled = true;
        repaint();
    }

    public  void disable() {
        this .enabled = false;
        repaint();
    }

    private  double[] color;

    private  boolean enabled = true;

    public  void paint( Graphics g ) {
        if ( color[3] == 1 ) {
            // we can fill the rect
            g .setColor( new java.awt .Color( (float) color[0],
                                              (float) color[1],
                                              (float) color[2] ) );
            g .fillRect( 0, 0, this .size() .width, this .size() .height );
            if ( ! enabled ) {
                // dim out the rect
                g .setColor( Color .lightGray );
                for ( int i = 0; i < this .size() .width; i += 2 ) {
                    for ( int j = 0; j < this .size() .height; j += 2 ) {
                        g .drawLine( i, j, i, j );
                    }
                }
            }
        }
        else {
            // we need to draw in squares and stuff
            double[] darkBGColor = new double []{ .7, .7, .7 };
            double[] bgColor = new double []{ 1, 1, 1 };
            double red = color[0] + (1 - color[3]) * bgColor[0];
            double green = color[1] + (1 - color[3]) * bgColor[1];
            double blue = color[2] + (1 - color[3]) * bgColor[2];
            java.awt .Color lightColor = new java.awt .Color(
                                            (float) (color[0] + (1 - color[3]) * bgColor[0]),
                                            (float) (color[1] + (1 - color[3]) * bgColor[1]),
                                            (float) (color[2] + (1 - color[3]) * bgColor[2]) );
            java.awt .Color darkColor = new java.awt .Color(
                                            (float) (color[0] + (1 - color[3]) * darkBGColor[0]),
                                            (float) (color[1] + (1 - color[3]) * darkBGColor[1]),
                                            (float) (color[2] + (1 - color[3]) * darkBGColor[2]) );
            int squareSize;
            if ( this .size() .width < 20 || this .size() .height < 20 ) {
                squareSize = 5;
            }
            else {
                squareSize = 10;
            }
            for ( int x = 0; x < this .size() .width; x += squareSize ) {
                for ( int y = 0; y < this .size() .height; y += squareSize ) {
                    if ( (x / squareSize + y / squareSize) % 2 == 0 ) {
                        g .setColor( lightColor );
                    }
                    else {
                        g .setColor( darkColor );
                    }
                    g .fillRect( x, y, squareSize, squareSize );
                }
            }
        }
    }

    public  void update( Graphics g ) {
        paint( g );
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(width_, height_);
    }

}

