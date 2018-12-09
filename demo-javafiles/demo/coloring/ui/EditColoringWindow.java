package demo.coloring.ui;

import java.awt.*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;
import demo.ui.*;
import demo.util.*;
import demo.coloring.*;
import demo.depend.*;
import demo.Demo;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.ste.SymbolTableEntry;
import demo.expr.ste.STEInterval;

public class EditColoringWindow extends DemoFrame {

        // constants used in the panels array that is passed into the constructor
        public static final String  CONSTANT	= "Constant Color", 
                                    EXPRESSION	= "Expression Spectrum",
                                    GRADIENT	= "Gradient",
                                    CHECKER	= "Checkered" ;

    private boolean animateResize_ = false;
    
    
    public EditColoringWindow( String[] panels, Demo demo, DialogListener listener, DemoFrame parentFrame ) {
        this( panels, demo, listener, null, parentFrame, demo.environment() );
    }
    

    public EditColoringWindow( String[] panels, Demo demo, DialogListener listener, DemoFrame parentFrame, Environment expressionEnvironment ) {
        this( panels, demo, listener, new ColoringConstant(new double[]{1,1,1,1}), parentFrame, expressionEnvironment );
    }

    public EditColoringWindow( String[] panels, Demo demo, DialogListener listener, Coloring coloring, DemoFrame parentFrame ) {
        this(panels, demo, listener, coloring, parentFrame, demo.environment());
    }

	public 
	EditColoringWindow( String[] panels, Demo demo, DialogListener listener, Coloring coloring, DemoFrame parentFrame, Environment expressionEnvironment ) {
		super( ); //parentFrame );

			//System .out .println( "in EditColorFrame constructor. demo is: " + demo );
			this .demo = demo;
                        this .listener = listener;
			this .setLayout( new BorderLayout() );
                        this .expressionEnvironment = expressionEnvironment;
                        this .parentFrame = parentFrame;
                        parentFrame.addOpenDialog(this);
                        popupMenu.add(copyMenuItem);
                        popupMenu.add(pasteMenuItem);
                        this.add(popupMenu);
			northPanel = new Panel();
			northPanel .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			northPanel .add( new Label( "Coloring Type: " ) );
			northPanel .add( panelChoice );
			this .add( northPanel, "North" );
			southPanel = new Panel();
			southPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
			southPanel .add( cancelBtn );
			southPanel .add( okBtn );
			this .add( southPanel, "South" );
			for ( int i = 0; i < panels .length; i++ ) {
				panelChoice .addItem( new String(panels[i]) );
			}
			set(coloring);
                        setResizable(false);
                        setVisible(true);
                        animateResize_ = true;
                        selectPanel( panelChoice .getSelectedItem() );
		}

	private  void makeColoring() {
		this.coloring = ((EditColoringPanel) panels.get(panelChoice .getSelectedItem())) .coloring();
	}

	private  Demo demo;

        private Environment expressionEnvironment;

	private  Panel northPanel, southPanel;

	private  DialogListener listener;

        private  DemoFrame parentFrame;



	private static final 
	int CONSTANT_INDEX = 0, EXPRESSION_INDEX = 1, GRADIENT_INDEX = 2,
		 CHECKER_INDEX = 3;

	private static final 
	String[] choiceItems = new String []{ CONSTANT,
                                              EXPRESSION,
                                              GRADIENT,
                                              CHECKER };

	private  
	java.util.Dictionary panels = new java.util.Hashtable(5);

	private  EditColoringPanel selectedPanel = null;

	private  Choice panelChoice = new Choice();

	private 
	Button cancelBtn = new Button( "Cancel" ),
			okBtn = new Button( "OK" );
                        
        private Coloring coloring = null;
        
        private PopupMenu popupMenu = new PopupMenu();
        private MenuItem copyMenuItem = new MenuItem("Copy");
        private MenuItem pasteMenuItem = new MenuItem("Paste");
        
        public  Coloring coloring() {
            return coloring;
        }

	public  boolean action( Event e, Object o ) {
            if ( e .target == panelChoice ) {
                selectPanel( panelChoice .getSelectedItem() );
            }
            else if ( e .target == okBtn ) {
                makeColoring();
                if ( this.coloring != null ) {
                    listener.dialogOKed(this);
                    setVisible(false);
                    dispose();
                }
            }
            else if ( e .target == cancelBtn ) {
                coloring = null;
                listener.dialogCanceled(this);
                setVisible(false);
                dispose();
            }
            else if ( e .target == copyMenuItem ) {
                makeColoring();
                if ( this.coloring != null ) {
                    demo.setClipboard(this.coloring);
                    this.coloring.dispose(); // quick hack: don't want dependencies
                }
            }
            else if ( e .target == pasteMenuItem ) {
                Object cObj = demo.cloneClipboard(expressionEnvironment);
                if (cObj instanceof Coloring) {
                    Coloring c = (Coloring) cObj;
                    boolean checkColoring = true;
                    boolean isAllowedType = false;
                    while (checkColoring) {
                        checkColoring = false;
                        String type = (c instanceof ColoringConstant)   ?   CONSTANT    :
                                      (c instanceof ColoringExpression) ?   EXPRESSION  :
                                      (c instanceof ColoringGradient)   ?   GRADIENT    :
                                      (c instanceof ColoringChecker)    ?   CHECKER     :
                                      "@NOTATYPE";
                        for (int i = 0; i < panelChoice.getItemCount(); ++i)
                            if (panelChoice.getItem(i).equals(type))
                                isAllowedType = true;
                        if ( !isAllowedType
                             && (c instanceof ColoringGroup)
                             && ((ColoringGroup) c).colorings().length == 1 ) {
                            c = ((ColoringGroup) c).colorings()[0];
                            checkColoring = true;
                        }
                    }
                    if (isAllowedType)
                        set(c);
                    else
                        demo.showError("You cannot paste that type of coloring here.");
                    
                }
                else demo.showError("You can only paste a coloring here.");
            }
            else {
                return false;
            }
            return true;
	}

	public  boolean handleEvent( Event e, Object o ) {
		if ( e .id == java.awt .Event .WINDOW_DESTROY ) {
                        coloring = null;
                        listener.dialogCanceled(this);
                        setVisible(false);
			dispose();
			return true;
		}
		else {
			return super .handleEvent( e );
		}
	}

	private  void set( Coloring coloring ) {
                String panelType = null;
                if ( coloring instanceof ColoringConstant ) {
			if ( this .panels.get(CONSTANT) == null ) {
				initPanel( CONSTANT );
			}
			((EditColorConstantPanel) this .panels.get(CONSTANT)) .setColor( 
                                    ((ColoringConstant) coloring) .color() );
			panelType = CONSTANT;
		}
		else if ( coloring instanceof ColoringExpression ) {
			if ( this .panels.get(EXPRESSION) == null ) {
				initPanel( EXPRESSION );
			}
			((EditColorExpressionPanel) this .panels.get(EXPRESSION)) .setExpressionString(
                                    ((ColoringExpression) coloring) .expressionDefinitionString() );
			panelType = EXPRESSION;
		}
		else if ( coloring instanceof ColoringGradient ) {
			if ( this .panels.get(GRADIENT) == null ) {
				initPanel( GRADIENT );
			}
			((EditColorGradientPanel) this .panels.get(GRADIENT)) .set( (ColoringGradient) coloring );
			panelType = GRADIENT;
		}
		else if ( coloring instanceof ColoringChecker ) {
			if ( this .panels.get(CHECKER) == null ) {
				initPanel( CHECKER );
			}
			((EditColorCheckerPanel) this .panels.get(CHECKER)) .set( (ColoringChecker) coloring );
			panelType = CHECKER;
		}
                selectPanel(panelType);
	}

	private  void initPanel( String panel ) {
                if ( panel.equals( CONSTANT ) ) {
			panels.put(CONSTANT, new EditColorConstantPanel());
                }
		else if ( panel.equals( EXPRESSION ) ) {
			panels.put(EXPRESSION, new EditColorExpressionPanel( demo, expressionEnvironment ));
                }
		else if ( panel.equals( GRADIENT ) ) {
			panels.put(GRADIENT, new EditColorGradientPanel( demo, expressionEnvironment ));
			((EditColorGradientPanel) this .panels.get(GRADIENT)) .set(
                                new ColoringGradient( demo.recognizeExpression("0"),
                                    new Expression []{ demo.recognizeExpression("- 1"),
                                                       demo.recognizeExpression("1") },
                                    new Coloring []{ new ColoringConstant(new double []{ 0, 0, 0, 0 } ),
                                                     new ColoringConstant(new double []{ 0, 0, 0, 1 } )
                                                                           } ) );
                }		
		else if ( panel.equals( CHECKER ) ) {
			panels.put(CHECKER, new EditColorCheckerPanel( demo, expressionEnvironment ));
                }
        }

	private  void selectPanel( String choice ) {
                panelChoice .select( choice );
		if ( panels.get(choice) == null ) {
			initPanel( choice );
		}
		if ( selectedPanel != null ) {
			this .remove( selectedPanel );
		}
		selectedPanel = (EditColoringPanel) panels.get(choice);
		this .add( selectedPanel, "Center" );
		this .pack();
	}

        
        public void setSize(Dimension d) {
            setSize(d.width, d.height);
        }

        public void setSize(int w, int h) {
            if (this.isVisible() && animateResize_) {
                final int STEPS = 5, DELAY = 15;
                int oldw = getSize().width, oldh = getSize().height;
                int diffw = w - oldw, diffh = h - oldh;
                double currw = oldw, currh = oldh;
                double incrw = diffw/(double)STEPS, incrh = diffh/(double)STEPS;
                try {
                    for (int steps = 0; steps < STEPS; ++steps) {
                        currw += incrw;
                        currh += incrh;
                        super.setSize((int) currw, (int) currh);
                        Thread.sleep(DELAY);
                    }
                }
                catch (InterruptedException ex) {}
            }
            super.setSize(w,h);
        }

        public void dispose() {
            parentFrame.removeOpenDialog(this);
            super.dispose();
        }

        public boolean mouseDown(Event e, int x, int y) {
            try {
                if (e.controlDown()) {
                    pasteMenuItem.setEnabled(
                                demo.clipboardInstanceof(Class.forName("demo.coloring.Coloring")));
                    popupMenu.show(this, x, y);
                    return true;
                }
            } catch (ClassNotFoundException ex) {}
            return false;
        }

        public void setVisible(boolean b) {
            Dimension d = getSize();
            super.setVisible(b);
            Dimension d2 = getSize();
            if (d.width != d2.width || d.height != d2.height) {
                boolean anim = animateResize_;
                animateResize_ = false;
                setSize(d);
                animateResize_ = anim;
            }
        }
        
}




 abstract class EditColoringPanel extends Panel{

	public  abstract  Coloring coloring() ;

	public  void paint( Graphics g ) {
		g .setColor( Color .black );
		g .drawRect( 3, 1, this .size() .width - 7,
					  this .size() .height - 3 );
		super .paint( g );
	}


}









class EditColorConstantPanel extends EditColoringPanel{

	public  EditColorConstantPanel() {
		super();

			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			this .add( new Label( "Color: " ) );
			this .add( colorChoice );
		}

	private 
	ColorChoice colorChoice = new ColorChoice( Color .green );

	public  Coloring coloring() {
		return new ColoringConstant( colorChoice .selectedColor() );
	}

	public  void setColor( Color color ) {
		colorChoice .select( color );
	}

	public  void setColor( double[] color ) {
		colorChoice .select( color );
	}


}




class EditColorExpressionPanel extends EditColoringPanel{

	public  EditColorExpressionPanel( Demo demo, Environment environment ) {
		super();

			this .demo = demo;
                        this .environment = environment;
			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			this .add( new Label( "Expression:" ) );
			this .add( this .expressionFld );
		}

	private  Demo demo;

        private Environment environment;
    
	public  Coloring coloring() {
		Expression expression = demo .recognizeScalarExpression( expressionFld .getText(), environment );
		if ( expression == null )
			return null;
		return new ColoringExpression(expression);
	}

	public  void setExpressionString( String expression ) {
		this .expressionFld .setText( expression );
	}

	private  TextField expressionFld = new TextField( 20 );


}




class EditColorCheckerPanel extends EditColoringPanel{

	public  EditColorCheckerPanel( Demo demo, Environment env ) {
		super();

			this .demo = demo;
                        this .environment = env;
			this .setLayout( new BorderLayout() );
			Panel northPanel = new EditColorBorderNorth( this );
			northPanel .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			northPanel .add( new Label( "Intervals:" ) );
			northPanel .add( intervalsField );
			this .add( northPanel, "North" );
			this .add( new EditColorBorderEast( this ), "East" );
			this .add( new EditColorBorderWest( this ), "West" );
			Panel centerPanel = new Panel();
			centerPanel .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			centerPanel .add( new Label( "Color 1:" ) );
			centerPanel .add( color1Choice );
			this .add( centerPanel, "Center" );
			Panel southPanel = new EditColorBorderSouth( this );
			southPanel .setLayout( new FlowLayout( FlowLayout .CENTER ) );
			southPanel .add( new Label( "Color 2:" ) );
			southPanel .add( color2Choice );
			this .add( southPanel, "South" );
		}

	public  void set( ColoringChecker coloring ) {
		// set intervals field
		String intervalsText = "";
		STEInterval[] intervals = coloring .intervals();
		for ( int i = 0; i < intervals .length; i++ ) {
			intervalsText += intervals[i] .name();
			if ( i < intervals .length - 1 ) {
				intervalsText += ", ";
			}
		}
		intervalsField .setText( intervalsText );
		// set colors
		Coloring coloring1 = coloring .coloring1();
		Coloring coloring2 = coloring .coloring2();
		if ( coloring1 instanceof ColoringConstant ) {
			this .color1Choice .select( ((ColoringConstant) coloring1) .color() );
		}
		if ( coloring2 instanceof ColoringConstant ) {
			this .color2Choice .select( ((ColoringConstant) coloring2) .color() );
		}
	}

	public  Coloring coloring() {
		// get intervals
		java.util .Vector intervalStrings = new java.util .Vector();
		String text = intervalsField .getText();
		int commaIndex = text .indexOf( ',' );
		while ( commaIndex >= 0 ) {
			intervalStrings .addElement( text .substring( 0, commaIndex ) .trim() );
			text = text .substring( commaIndex + 1 < text .length() ? commaIndex + 1 : commaIndex );
			commaIndex = text .indexOf( ',' );
		}
                text = text.trim();
                if (text.length() > 0)
                    intervalStrings .addElement( text );
		// get interval table entries
		STEInterval[] intervalEntries = new STEInterval [ intervalStrings .size() ];
                if (intervalEntries.length == 0) {
                    demo.showError("No intervals have been entered.");
                    return null;
                }
                for ( int i = 0; i < intervalStrings .size(); i++ ) {
			String intervalStr = (String) intervalStrings .elementAt( i );
			// get the interval
                        Object entry;
                        try {
                            entry = environment.lookup( intervalStr );
                        }
			catch (mathbuild.VariableNotFoundException ex) {
				demo .showError(ex);
				return null;
			}
			if ( ! (entry instanceof STEInterval) ) {
                            if (entry instanceof SymbolTableEntry)
                                demo .showError(((SymbolTableEntry) entry).name() + " is not an interval." );
                            else
                                demo .showError("That is not an interval.");
                            return null;
			}
			intervalEntries[i] = (STEInterval) entry;
		}
		ColoringChecker coloring = new ColoringChecker( intervalEntries,
								new ColoringConstant( color1Choice .selectedColor() ),
								new ColoringConstant( color2Choice .selectedColor() ) );
		return coloring;
	}

	private  Demo demo;

        private Environment environment;

	private 
	ColorChoice color1Choice = new ColorChoice( java.awt .Color .black ),
                    color2Choice = new ColorChoice( java.awt .Color .white );

	private  TextField intervalsField = new TextField( 20 );


}




class EditColorGradientPanel extends EditColoringPanel {

	public  EditColorGradientPanel( Demo demo, Environment environment ) {
		super();

			this .demo = demo;
                        this .environment = environment;
			this .canvas = new EditColorGradientCanvas( this );
			this .setLayout( new BorderLayout() );
			Panel northPanel = new EditColorBorderNorth( this );
			northPanel .add( new Label( "Expression:" ) );
			northPanel .add( expressionField );
			this .add( northPanel, "North" );
			Panel eastPanel = new EditColorBorderEast( this );
			eastPanel .add( addBtn );
			this .add( eastPanel, "East" );
			Panel westPanel = new EditColorBorderWest( this );
			westPanel .add( new Label( "" ) );
			this .add( westPanel, "West" );
			this .add( this .canvas, "Center" );
			Panel southPanel = new EditColorBorderSouth( this );
			southPanel .add( new Label( "Value:" ) );
			southPanel .add( valueField );
			southPanel .add( colorChoice );
			southPanel .add( removeBtn );
			this .add( southPanel, "South" );
			updateSelectedData( null );
		}

	public  Coloring coloring() {
		Coloring[] colorings = new Coloring [ canvas .colors .size() ];
		String[] valueDefs = new String [ canvas .colors .size() ];
		for ( int i = 0; i < canvas .colors .size(); i++ ) {
			colorings[i] = new ColoringConstant( 
                                ((EditColorGradientData) canvas .colors .elementAt( i )) .color );
			valueDefs[i] = ((EditColorGradientData) canvas .colors .elementAt( i )) .valueDefinition;
		}
		// get the expressions
		Expression expression = demo .recognizeScalarExpression( expressionField .getText(), environment );
		if ( expression == null )
			return null;
		// get the expressions for the value of pure colors
		Expression[] valueExpressions = new Expression [ canvas .colors .size() ];
		for ( int i = 0; i < valueExpressions .length; i++ ) {
                        
			valueExpressions[i] = demo .recognizeScalarExpression( 
                                ((EditColorGradientData) canvas .colors .elementAt( i )) .valueDefinition, environment );
                        
			if ( valueExpressions[i] == null ) {
				return null;
			}
		}
		return new ColoringGradient(expression, valueExpressions, colorings);
	}

	public  void set( ColoringGradient coloring ) {
		Coloring[] colorings = coloring .colorings();
		Expression[] valueExpressions = coloring .pureColorExpressions();
		String[] valueDefinitions = coloring .pureColorExpressionDefinitions();
		if ( colorings .length < 2 ) {
			if ( colorings .length == 1 ) {
				if ( colorings[0] instanceof ColoringConstant ) {
					this .canvas .colors .setElementAt(
                                                new EditColorGradientData( valueDefinitions[0],
                                                                           valueExpressions[0],
                                                                           ((ColoringConstant) colorings[0]) .color() ),
						0 );
					this .canvas .colors .setElementAt(
                                                new EditColorGradientData( valueDefinitions[0],
									   valueExpressions[0],
									   ((ColoringConstant) colorings[0]) .color() ),
                                                1 );
				}
			}
		}
		else {
			this .canvas .colors = new java.util .Vector();
			for ( int i = 0; i < colorings .length; i++ ) {
				if ( colorings[i] instanceof ColoringConstant ) {
					this .canvas .colors .addElement(
                                                new EditColorGradientData( valueDefinitions[i],
                                                                           valueExpressions[i],
									   ((ColoringConstant) colorings[i]) .color() ) );
				}
				else {
					this .canvas .colors .addElement(
                                                new EditColorGradientData( valueDefinitions[i],
                                                                           valueExpressions[i],
									   new double []{ 0, 0, 0, 0 } ) );
				}
			}
		}
		this .expressionField .setText( coloring .expressionDefinitionString() );
	}

	private  Demo demo;

        private Environment environment;
    
	public  EditColorGradientCanvas canvas;

	private  TextField expressionField = new TextField( 20 );

	private  TextField valueField = new TextField( 5 );

	private 
	ColorChoice colorChoice = new ColorChoice( Color .black );

	private  Button removeBtn = new Button( "Remove" );

	private  Button addBtn = new Button( "Add Color" );

	private  EditColorGradientData selectedData = null;

	public 
	void updateSelectedData( EditColorGradientData selectedData ) {
		this .selectedData = selectedData;
		if ( selectedData == null ) {
			valueField .setText( "" );
			valueField .disable();
			colorChoice .disable();
			removeBtn .disable();
		}
		else {
			// if the value text is a number, we don't want too many digits
			try  {
				selectedData .valueDefinition = String .valueOf( 
                                        (double) Math .round( new Double( selectedData .valueDefinition ) .doubleValue()
                                                                * Math .pow( 10, 3 ) ) / 
                                                (double) Math .pow( 10, 3 ) );
			} catch( NumberFormatException ex ) {
				// it's not a number. That's OK. Don't do anything.
			}
			valueField .setText( selectedData .valueDefinition );
			colorChoice .select( selectedData .color );
			valueField .enable();
			colorChoice .enable();
			if ( canvas .colors .size() > 2 ) {
				removeBtn .enable();
			}
		}
	}

	public  boolean action( Event ev, Object obj ) {
		if ( ev .target == valueField ) {
			// update the value
			String valueDef = valueField .getText();
			IntervalExpression valueExpr = demo .recognizeIntervalExpression( valueDef, environment );
			if ( valueExpr != null ) {
                                if ( !valueExpr.returnsScalar() ) {
                                        demo .showError( "A value for a color must be a scalar." );
                                }
				// make sure it's not dependent on any intervals
				else if ( valueExpr .numIntervals() > 0 ) {
					demo .showError( "A value for a color cannot contain any intervals." );
				}
				else {
					this .selectedData .setData( valueDef, valueExpr );
					canvas .redraw();
					canvas .hiliteSelectedData();
				}
                                valueExpr.dispose();
			}
		}
		else {
			if ( ev .target == addBtn ) {
				// add a new color
				double maxLength = - 1;
				int maxIndex = 0;
				for ( int i = 0; i < canvas .colors .size() - 1; i++ ) {
					if ( ((EditColorGradientData) canvas .colors .elementAt( i + 1 )) .value
                                                 - ((EditColorGradientData) canvas .colors .elementAt( i )) .value
                                              >  maxLength ) {
						maxLength = ((EditColorGradientData) canvas .colors .elementAt( i + 1 )) 
                                                            .value 
                                                            - ((EditColorGradientData) canvas .colors .elementAt( i )) 
                                                              .value;
						maxIndex = i + 1;
					}
				}
				// insert right before the max index
				canvas .colors .insertElementAt( new EditColorGradientData( (((EditColorGradientData) canvas .colors .elementAt( maxIndex )) .value + ((EditColorGradientData) canvas .colors .elementAt( maxIndex - 1 )) .value) / 2, Color .black ), maxIndex );
				canvas .redraw();
				canvas .selectColoringAtIndex( maxIndex );
			}
			else {
				if ( ev .target == removeBtn ) {
					// remove the selected color
					// find which color is the selected color
					for ( int i = 0; i < canvas .colors .size(); i++ ) {
						if ( selectedData == canvas .colors .elementAt( i ) ) {
							canvas .colors .removeElementAt( i );
							updateSelectedData( null );
							canvas .redraw();
							break;
						}
					}
				}
				else {
					if ( ev .target == colorChoice ) {
						// change the color of the selected color
						this .selectedData .color = colorChoice .selectedColor();
						canvas .redraw();
						canvas .hiliteSelectedData();
					}
					else {
						return false;
					}
				}
			}
		}
		this .canvas .repaint();
		return true;
	}


}




class EditColorGradientCanvas extends Canvas{

	public  EditColorGradientCanvas( EditColorGradientPanel panel ) {
		super();

			this .myPanel = panel;
			// make sure there's something in here
			colors .addElement( new EditColorGradientData( - 1, Color .black ) );
			colors .addElement( new EditColorGradientData( 1, Color .black ) );
                        setSize(getPreferredSize());
		}

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    
        public Dimension getPreferredSize() {
            return new Dimension(260, 55);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public 
	java.util .Vector colors = new java.util .Vector();

	private  EditColorGradientPanel myPanel;

	private static final  Color BGCOLOR = Color .white;

	private static final  int BG_STRIPE_WIDTH = 5;

	private  int marginSide, marginTop;

	private  int previewBarWidth, previewBarHeight = 40;

	private  double pixelsPerUnit;

	private  int arrowWidth = 10, arrowHeight = 5;

	private  int imageWidth = - 7, imageHeight = - 7;

	private  Image bufferImage = null;

	private  Graphics bufferGraphics = null;

	private  EditColorGradientData selectedData = null;

	private  boolean dragging = false;

	private  int mouseDownX;

	public  void selectColoringAtIndex( int index ) {
		unhilite( selectedData );
		if ( index >= 0 ) {
			selectedData = (EditColorGradientData) colors .elementAt( index );
			hilite( selectedData );
		}
		else {
			selectedData = null;
		}
		myPanel .updateSelectedData( selectedData );
		repaint();
	}

	public  void hiliteSelectedData() {
		hilite( selectedData );
	}

	public  void redraw() {
		sort( colors );
		if ( imageWidth != this .size() .width || imageHeight != this .size() .height ) {
			imageWidth = this .size() .width;
			imageHeight = this .size() .height;
			bufferImage = createImage( imageWidth, imageHeight );
			bufferGraphics = bufferImage .getGraphics();
			marginSide = arrowWidth / 2 + 10;
			marginTop = 5;
			previewBarWidth = this .size() .width - 2 * marginSide;
		}
		bufferGraphics .setColor( BGCOLOR );
		bufferGraphics .fillRect( 0, 0, this .size() .width, this .size() .height );
		pixelsPerUnit = previewBarWidth / (((EditColorGradientData) colors .elementAt( colors .size() - 1 )) .value - ((EditColorGradientData) colors .elementAt( 0 )) .value);
		int prevColorValuePixels = 0;
		double firstValue = ((EditColorGradientData) colors .elementAt( 0 )) .value;
		double[] prevColor = ((EditColorGradientData) colors .elementAt( 0 )) .color;
		int nextIndex = 1;
		int nextColorValuePixels = (int) Math .round( (((EditColorGradientData) colors .elementAt( nextIndex )) .value - firstValue) * pixelsPerUnit );
		double[] nextColor = ((EditColorGradientData) colors .elementAt( nextIndex )) .color;
		int pixelIncrement = 2;
		for ( int pixel = 0; pixel < previewBarWidth; pixel += pixelIncrement ) {
			if ( pixel > nextColorValuePixels && nextIndex + 1 < colors .size() ) {
				prevColorValuePixels = nextColorValuePixels;
				prevColor = nextColor;
				++nextIndex;
				nextColorValuePixels = (int) Math .round( (((EditColorGradientData) colors .elementAt( nextIndex )) .value - firstValue) * pixelsPerUnit );
				nextColor = ((EditColorGradientData) colors .elementAt( nextIndex )) .color;
				pixel -= pixelIncrement;
				continue;
			}
			double coefficientPrev, coefficientNext;
			if ( nextColorValuePixels == prevColorValuePixels ) {
				coefficientPrev = 0;
				coefficientNext = 1;
			}
			else {
				coefficientPrev = ((double) (nextColorValuePixels - pixel)) / (double) (nextColorValuePixels - prevColorValuePixels);
				coefficientNext = ((double) (pixel - prevColorValuePixels)) / (double) (nextColorValuePixels - prevColorValuePixels);
			}
			int red = (int) Math .round( coefficientPrev * colorStripeColor( prevColor, pixel ) .getRed() + coefficientNext * colorStripeColor( nextColor, pixel ) .getRed() );
			int green = (int) Math .round( coefficientPrev * colorStripeColor( prevColor, pixel ) .getGreen() + coefficientNext * colorStripeColor( nextColor, pixel ) .getGreen() );
			int blue = (int) Math .round( coefficientPrev * colorStripeColor( prevColor, pixel ) .getBlue() + coefficientNext * colorStripeColor( nextColor, pixel ) .getBlue() );
			bufferGraphics .setColor( new java.awt .Color( red, green, blue ) );
			bufferGraphics .fillRect( pixel + marginSide, marginTop,
                                                  pixelIncrement, previewBarHeight );
		}
		bufferGraphics .setColor( Color .black );
		for ( int i = 0; i < colors .size(); i++ ) {
			int xDistance = (int) Math .round( marginSide + (((EditColorGradientData) colors .elementAt( i )) .value - firstValue) * pixelsPerUnit );
			int yDistance = marginTop + previewBarHeight;
			bufferGraphics .drawPolygon( new int []{ xDistance,
								 xDistance - arrowWidth / 2,
								 xDistance + arrowWidth / 2 },
                                                     new int []{ yDistance,
								 yDistance + arrowHeight,
								 yDistance + arrowHeight },
                                                     3 );
		}
	}

	private 
	Color colorStripeColor( double[] color, int pixel ) {
		double[] bgcolor;
		if ( ((double) pixel) / ((double) BG_STRIPE_WIDTH) % 2 == 0 ) {
			bgcolor = new double []{ ((double) BGCOLOR .darker() .getRed()) / (double) 255,
                                                 ((double) BGCOLOR .darker() .getGreen()) / (double) 255,
						 ((double) BGCOLOR .darker() .getBlue()) / (double) 255 };
		}
		else {
			bgcolor = new double []{ ((double) BGCOLOR .getRed()) / (double) 255,
						 ((double) BGCOLOR .getGreen()) / (double) 255,
						 ((double) BGCOLOR .getBlue()) / (double) 255 };
		}
		double red = color[0] + (1 - color[3]) * bgcolor[0];
		double green = color[1] + (1 - color[3]) * bgcolor[1];
		double blue = color[2] + (1 - color[3]) * bgcolor[2];
		return new java.awt .Color( (float) red, (float) green, (float) blue );
	}

	public  void paint( Graphics g ) {
		if ( bufferImage == null || imageWidth != this .size() .width || imageHeight != this .size() .height ) {
			redraw();
		}
		g .drawImage( bufferImage, 0, 0, this );
	}

	public  void update( Graphics g ) {
		paint( g );
	}

	public  boolean mouseDown( Event ev, int x, int y ) {
		this .mouseDownX = x;
		selectColoringAtIndex( getIndex( x, y ) );
		return true;
	}

	public  boolean mouseDrag( Event ev, int x, int y ) {
		if ( ! dragging ) {
			// make sure the user dragged enough for a drag
			dragging = Math .abs( x - mouseDownX ) > 4;
		}
		if ( dragging && selectedData != null 
                     && selectedData != colors .elementAt( 0 ) 
                     && selectedData != colors .elementAt( colors .size() - 1 ) ) {
			// if x is beyond the end, change it so we don't go over the edge
			if ( x <= marginSide ) {
				x = marginSide + 1;
			}
			else {
				if ( x >= imageWidth - marginSide ) {
					x = imageWidth - marginSide - 1;
				}
			}
			// set the value of the selected index to the value at pixel x
			selectedData .value = (1 / pixelsPerUnit) * 
                                              (x - marginSide) + ((EditColorGradientData) colors .elementAt( 0 )) .value;
			selectedData .valueDefinition = String .valueOf( selectedData .value );
			redraw();
			hilite( selectedData );
                        paint(this.getGraphics());
			myPanel .updateSelectedData( selectedData );
			return true;
		}
		return false;
	}

	public  boolean mouseUp( Event ev, int x, int y ) {
		dragging = false;
		return true;
	}

	private  int getIndex( int x, int y ) {
		double firstValue = ((EditColorGradientData) colors .elementAt( 0 )) .value;
		int i;
		for ( i = 0; i < colors .size(); i++ ) {
			int xDistance = (int) Math .round( marginSide + (((EditColorGradientData) colors .elementAt( i )) .value - firstValue) * pixelsPerUnit );
			int yDistance = marginTop + previewBarHeight;
			if ( new Rectangle( xDistance - arrowWidth / 2, yDistance,
                                            arrowWidth + 1, arrowHeight + 2 ) .inside( x, y ) ) {
				// we found it!
				break;
			}
		}
		if ( i < colors .size() ) {
			return i;
		}
		else {
			return - 1;
		}
	}

	private  void hilite( EditColorGradientData data ) {
		if ( data != null ) {
			int xDistance = (int) Math .round( marginSide + (data .value - ((EditColorGradientData) colors .elementAt( 0 )) .value) * pixelsPerUnit );
			int yDistance = marginTop + previewBarHeight;
			bufferGraphics .setColor( Color .black );
			bufferGraphics .fillPolygon( new int []{ xDistance,
                                                                 xDistance - arrowWidth / 2,
                                                                 xDistance + arrowWidth / 2 }, 
                                                     new int []{ yDistance,
                                                                 yDistance + arrowHeight,
                                                                 yDistance + arrowHeight },
                                                     3 );
		}
	}

	private  void unhilite( EditColorGradientData data ) {
		if ( data != null && colors .contains( data ) ) {
			int xDistance = (int) Math .round( marginSide + (data .value - ((EditColorGradientData) colors .elementAt( 0 )) .value) * pixelsPerUnit );
			int yDistance = marginTop + previewBarHeight;
			bufferGraphics .setColor( BGCOLOR );
			bufferGraphics .fillPolygon( new int []{ xDistance,
                                                                 xDistance - arrowWidth / 2, 
                                                                 xDistance + arrowWidth / 2 }, 
                                                     new int []{ yDistance,
								 yDistance + arrowHeight,
								 yDistance + arrowHeight },
                                                     3 );
			bufferGraphics .setColor( Color .black );
			bufferGraphics .drawPolygon( new int []{ xDistance,
								 xDistance - arrowWidth / 2,
								 xDistance + arrowWidth / 2 },
                                                     new int []{ yDistance,
								 yDistance + arrowHeight,
								 yDistance + arrowHeight },
                                                     3 );
		}
	}

	// sorts the vector, assuming there is at most one value out of order.
	private  void sort( java.util .Vector data ) {
		// find the value that is out of order
		int index;
		if ( ((EditColorGradientData) data .elementAt( 0 )) .value > ((EditColorGradientData) data .elementAt( 1 )) .value ) {
			// the zeroth element is out of order
			if ( data .size() == 2 ) {
				// this is the fewest possible number of entries
				// it's a special case
				Object element = data .elementAt( 0 );
				data .removeElementAt( 0 );
				data .insertElementAt( element, 1 );
				return ;
			}
			// the out of place index is 0
			index = 0;
		}
		else {
			// go through each element to see which one isn't in order
			for ( index = 1; index < data .size(); index++ ) {
				if ( ((EditColorGradientData) data .elementAt( index )) .value < ((EditColorGradientData) data .elementAt( index - 1 )) .value ) {
					// it's out of order
					break;
				}
			}
			if ( index >= data .size() ) {
				// nothing to do: it's sorted
				return ;
			}
		}
		// take the out of place value out
		EditColorGradientData movingData = (EditColorGradientData) data .elementAt( index );
		data .removeElementAt( index );
		double value = movingData .value;
		// a special case is when it's less than the first element
		if ( value < ((EditColorGradientData) data .elementAt( 0 )) .value ) {
			data .insertElementAt( movingData, 0 );
			return ;
		}
		// do a binary search for where it's supposed to go
		int greaterIndex = data .size() - 1, lessIndex = 0;
		int guessIndex = greaterIndex / 2;
		while ( greaterIndex - lessIndex > 1 ) {
			if ( value < ((EditColorGradientData) data .elementAt( guessIndex )) .value ) {
				greaterIndex = guessIndex;
				guessIndex = (greaterIndex + lessIndex) / 2;
			}
			else {
				if ( value > ((EditColorGradientData) data .elementAt( guessIndex )) .value ) {
					lessIndex = guessIndex;
					guessIndex = (greaterIndex + lessIndex) / 2;
				}
				else {
					// they're equal
					greaterIndex = guessIndex;
					break;
				}
			}
		}
		data .insertElementAt( movingData, greaterIndex );
	}


}




class EditColorBorderNorth extends Panel{

	public  EditColorBorderNorth( EditColoringPanel panel ) {
		super();

			this .myPanel = panel;
			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
		}

	private  EditColoringPanel myPanel;

	public  boolean action( Event e, Object o ) {
		return myPanel .action( e, o );
	}

	public  void paint( Graphics g ) {
		myPanel .paint( g );
	}


}




class EditColorBorderEast extends Panel{

	public  EditColorBorderEast( EditColoringPanel panel ) {
		super();

			this .myPanel = panel;
			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
		}

	private  EditColoringPanel myPanel;

	public  boolean action( Event e, Object o ) {
		return myPanel .action( e, o );
	}

	public  void paint( Graphics g ) {
		g .translate( this .size() .width - myPanel .size() .width, - 10 );
		myPanel .paint( g );
	}


}




class EditColorBorderWest extends Panel{

	public  EditColorBorderWest( EditColoringPanel panel ) {
		super();

			this .myPanel = panel;
			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
		}

	private  EditColoringPanel myPanel;

	public  boolean action( Event e, Object o ) {
		return myPanel .action( e, o );
	}

	public  void paint( Graphics g ) {
		g .translate( 0, - 10 );
		myPanel .paint( g );
	}


}




class EditColorBorderSouth extends Panel{

	public  EditColorBorderSouth( EditColoringPanel panel ) {
		super();

			this .myPanel = panel;
			this .setLayout( new FlowLayout( FlowLayout .CENTER ) );
		}

	private  EditColoringPanel myPanel;

	public  boolean action( Event e, Object o ) {
		return myPanel .action( e, o );
	}

	public  void paint( Graphics g ) {
		g .translate( 0, this .size() .height - myPanel .size() .height );
		myPanel .paint( g );
	}


}




class EditColorGradientData extends Object{

	public  EditColorGradientData( double value, Color color ) {
		this( value,
			  new double []{ ((double) color .getRed()) / 255.0,
					 ((double) color .getGreen()) / 255.0,
					 ((double) color .getBlue()) / 255.0,
					 1 } );
	}

	public  EditColorGradientData( double value, double[] color ) {
		super();

			this .value = value;
			this .color = color;
			this .valueDefinition = String .valueOf( value );
		}

	public 
	EditColorGradientData( String definition, Expression expression,
							  double[] color ) {
		super();

			this .color = color;
			setData( definition, expression );
		}

	public 
	void setData( String definition, Expression expression ) {
		this .valueDefinition = definition;
		this .value = ((ValueScalar) expression.evaluate()).number();
	}

	public  String valueDefinition;

	public  double value;

	public  double[] color;


}
