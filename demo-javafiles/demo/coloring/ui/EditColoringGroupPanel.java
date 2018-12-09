package demo.coloring.ui;
//
//  EditColoringGroupPanel.java
//  Demo
//
//  Created by David Eigen on Sun Jun 09 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;

import demo.ui.*;
import demo.util.*;
import demo.coloring.*;
import demo.depend.*;
import demo.exec.*;
import demo.Demo;
import demo.expr.ste.STEInterval;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

public class EditColoringGroupPanel extends Panel {

    private  DemoFrame myWindow;

    private EditColoringListener myListener;

    private String[] allowedColorings;

    private  Demo demo;

    private Environment expressionEnvironment;

    private PopupMenu popupMenu = new PopupMenu();
    private MenuItem copyMenuItem = new MenuItem("Copy");
    private MenuItem pasteMenuItem = new MenuItem("Paste");

    private static final  int MAX_ALPHAVALUE = 100, MAX_MIXVALUE = 100;

    private
        Slider alphaSlider = new Slider( 0, MAX_ALPHAVALUE, 3 ),
        mixSlider = new Slider( 0, MAX_MIXVALUE, 3 );

    private  EditColorCanvas canvas;

    private
        Button addBtn = new Button( "Add Coloring..." ),
        removeBtn = new Button( "Remove Coloring" ),
        editBtn = new Button( "Edit Coloring..." );

    private  Coloring defaultColoring;


    /**
     * Creates a new EditColoringGroupPanel. An EditColoringGroupPanel is the user 
     * interface for editing ColoringGroups.
     * @param demo the demo class instance for this demo
     * @param myWindow the parent window containing this component
     * @param myListener the listener for this EditColoringGroupPanel
     * @param defaultColoring the initial coloring shown to the user
     * @param allowedColorings colorings that are allowed for the colorings in the group.
     *        See EditColoringWindow for constants that can be put into this parameter.
     * @param expressionEnvironment the environment in which to recognize expressions
     */
    public
    EditColoringGroupPanel( Demo demo, DemoFrame myWindow, EditColoringListener myListener,
                            Coloring defaultColoring, String[] allowedColorings, Environment expressionEnvironment ) {
        super();

        this .myWindow = myWindow;
        this .myListener = myListener;
        this .demo = demo;
        this .expressionEnvironment = expressionEnvironment;
        this .defaultColoring = defaultColoring;
        this .allowedColorings = allowedColorings;

        popupMenu.add(copyMenuItem);
        popupMenu.add(pasteMenuItem);
        this.add(popupMenu);
        
        Panel buttonsPanel = new Panel();
        buttonsPanel .setLayout( new GridLayout( 0, 1 ) );
        buttonsPanel .add( addBtn );
        buttonsPanel .add( editBtn );
        buttonsPanel .add( removeBtn );
        
        Panel slidersPanel = new Panel();
        slidersPanel .setLayout( new GridLayout( 2, 1 ) );
        Panel alphaSliderPanel = new Panel();
        alphaSliderPanel .setLayout( new FlowLayout( FlowLayout.RIGHT ) );
        alphaSliderPanel .add( new Label( "Opacity:" ) );
        alphaSliderPanel .add( alphaSlider );
        Panel mixSliderPanel = new Panel();
        mixSliderPanel .setLayout( new FlowLayout( FlowLayout.RIGHT ) );
        mixSliderPanel .add( new Label( "Blending:" ) );
        mixSliderPanel .add( mixSlider );
        alphaSlider .setValue( 100 );
        mixSlider .setValue( 0 );
        slidersPanel .add( alphaSliderPanel );
        slidersPanel .add( mixSliderPanel );
        
        canvas = new EditColorCanvas( demo, this, myWindow, allowedColorings, expressionEnvironment );
        canvas .set( defaultColoring );

        Panel internalPanel = new Panel();
        internalPanel.setLayout(new BorderLayout());
        internalPanel .add( slidersPanel, "North" );
        internalPanel .add( canvas, "Center" );
        internalPanel .add( buttonsPanel, "South" );
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.add(internalPanel);
    }

    public  Coloring coloring() {
        return canvas .coloring();
    }

    public  Coloring defaultColoring() {
        return defaultColoring;
    }

    public  void setSize() {
        setSize(getPreferredSize());
    }

    public  void setColoringSelectedState( boolean isSelected ) {
        if ( isSelected ) {
            // a coloring is selected
            editBtn .enable();
            removeBtn .enable();
        }
        else {
            // no coloring is selected
            editBtn .disable();
            removeBtn .disable();
        }
    }

    public  void setAlphaValue( double value ) {
        this .alphaSlider .setValue( value * MAX_ALPHAVALUE );
    }

    public  void setMixValue( double value ) {
        this .mixSlider .setValue( value * MAX_ALPHAVALUE );
    }

    public  boolean action( Event ev, Object obj ) {
        if ( ev .target == addBtn ) {
            canvas .addNewColoring();
        }
        if ( ev .target == editBtn ) {
            canvas .editSelectedColoring();
        }
        else if ( ev .target == removeBtn ) {
            canvas .removeSelectedColoring();
            setSize();
            coloringChanged();
        }
        else if ( ev .target == alphaSlider ) {
            canvas .setSelectedColoringAlphaValue( alphaSlider .value() / MAX_ALPHAVALUE );
            coloringChanged();
        }
        else if ( ev .target == mixSlider ) {
            canvas .setSelectedColoringMixValue( mixSlider .value() / MAX_MIXVALUE );
            coloringChanged();
        }
        else if ( ev .target == copyMenuItem ) {
            demo.setClipboard(coloring());
        }
        else if ( ev .target == pasteMenuItem ) {
            Object c = demo.cloneClipboard(expressionEnvironment);
            if (!(c instanceof Coloring))
                demo.showError("Clipboard contents is not a coloring.");
            else {
                canvas.set((Coloring) c);
                setSize();
                canvas.redraw();
                coloringChanged();
                canvas.repaint();
                canvas.unselect();
            }
        }
        else {
            return false;
        }
        return true;
    }
    
    public  void coloringChanged() {
        myListener.coloringChanged(coloring());
    }

    public  void disable() {
        canvas .disable();
        addBtn .disable();
        editBtn .disable();
        removeBtn .disable();
        alphaSlider .disable();
        mixSlider .disable();
        super .disable();
    }

    public  void enable() {
        canvas .enable();
        addBtn .enable();
        editBtn .enable();
        removeBtn .enable();
        alphaSlider .enable();
        mixSlider .enable();
        super .enable();
    }

    public  void disableSelectionButtons() {
        editBtn .disable();
        removeBtn .disable();
        alphaSlider .disable();
        mixSlider .disable();
    }

    public  void enableSelectionButtons() {
        editBtn .enable();
        removeBtn .enable();
        alphaSlider .enable();
        mixSlider .enable();
    }

    public  void enableWindow() {
        myWindow .enable();
    }

    public  void disableWindow() {
        myWindow .disable();
    }

    public void popupPopupMenu(int x, int y) {
        try {
            pasteMenuItem.setEnabled(demo.clipboardInstanceof(Class.forName("demo.coloring.Coloring")));
            popupMenu.show(this, x, y);
        }
        catch (ClassNotFoundException ex) {}
    }

    public boolean mouseDown(Event e, int x, int y) {
        if (e.controlDown()) {
            popupPopupMenu(x,y);
            return true;
        }
        return false;
    }
    
    public  void dispose() {
        canvas .dispose();
    }

}




class EditColorCanvas extends Canvas implements Dependable, DialogListener {
    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    public  DependencyNode dependencyNode() { return myDependencyNode_; }

    // stores the dialog currently open and the index of the coloring it's editing (or -1 if new)
    int currEditColoringIndex = -2;
    Window currEditColoringDialog = null;

    private  Demo demo;

    private  Environment expressionEnvironment;
    
    private  EditColoringGroupPanel myPanel;

    private  DemoFrame myWindow;

    private  long DOUBLE_CLICK_TIME = 500;

    private  int prevMouseDownX, prevMouseDownY;

    private  int mouseDownX, mouseDownY;

    private  long mouseUpTime = 0;

    private  boolean dragging = false;
    
    private  Image bufferImage;

    private  Graphics bufferGraphics;

    private  Image cleanBufferImage;

    public  int IMAGE_WIDTH = 250, IMAGE_HEIGHT = 50;

    private  int maxImages = 4;

    public static final  Color BGCOLOR = Color .white;

    private  boolean enabled = true;
    
    private
        java.util .Vector colorings = new java.util .Vector(),
        coloringImages = new java.util .Vector();

    private
        java.util .Vector alphaValues = new java.util .Vector(),
        mixValues = new java.util .Vector();

    // the index of the place to drop in the selected index
    private  int betweenIndex = - 1;
    
    // colorings allowed to be in the coloring selection window
    private String[] allowedColorings;

    public EditColorCanvas( Demo demo, EditColoringGroupPanel panel, DemoFrame myWindow, String[] allowedColorings, Environment expressionEnvironment ) {
        super();
        this .demo = demo;
        this .expressionEnvironment = expressionEnvironment;
        this .myPanel = panel;
        this .myWindow = myWindow;
        this .allowedColorings = allowedColorings;
        IMAGE_WIDTH = Demo.getFontMetrics().stringWidth( "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ) * 3 / 2;
        IMAGE_HEIGHT = Demo.getFontMetrics().getHeight() * 3;
        myPanel .disableSelectionButtons();
    }

    public  void set( Coloring coloring ) {
        // remove old colorings
        colorings = new java.util .Vector();
        coloringImages = new java.util .Vector();
        alphaValues = new java.util .Vector();
        mixValues = new java.util .Vector();
        // make sure the coloring is a coloring group
        if ( coloring instanceof ColoringGroup ) {
            Coloring[] groupColorings = ((ColoringGroup) coloring) .colorings();
            double[] groupAlphaValues = ((ColoringGroup) coloring) .alphaValues();
            double[] groupMixValues = ((ColoringGroup) coloring) .mixValues();
            for ( int i = 0; i < groupColorings .length - 1; i++ ) {
                addColoring( groupColorings[i], groupAlphaValues[i],
                             groupMixValues[i] );
            }
            if ( groupColorings[groupColorings .length - 1] instanceof ColoringBase ) {
                //this.baseColoring = (ColoringBase) groupColorings[groupColorings.length-1];
                addColoring(((ColoringBase) groupColorings[groupColorings.length-1]). coloring(), 1, 0);
            }
            else {
                addColoring( groupColorings[groupColorings .length - 1],
                             groupAlphaValues[groupColorings .length - 1],
                             groupMixValues[groupColorings .length - 1] );
            }
        }
        else {
            // if it's not a group, just add it.
            addColoring( coloring );
        }
    }

    public  void addColoring( Coloring coloring ) {
        addColoring( coloring, 1, 0 );
    }

    public void addColoring( Coloring coloring, double alphaValue, double mixValue ) {
        colorings .addElement( coloring );
        alphaValues .addElement( new Double( alphaValue ) );
        mixValues .addElement( new Double( mixValue ) );
        coloringImages .addElement( null );
        if ( colorings .size() > this .maxImages ) {
            // update maxImages to be able to draw more
            this .maxImages *= 2;
            // set the buffer image to null so it will get recreated
            bufferImage = null;
            repaint();
        }
        invalidate();
    }

    public  Coloring coloring() {
        return new ColoringGroup( this .colorings,
                                  this .alphaValues,
                                  this .mixValues );
    }

    public  void addNewColoring() {
        this .myPanel .disableWindow();
        this .currEditColoringIndex = -1;
        this .currEditColoringDialog = new EditColoringWindow(
                    allowedColorings, demo, this, myWindow, expressionEnvironment );
    }

    public  void editSelectedColoring() {
        this .myPanel .disableWindow();
        this .currEditColoringIndex = selectedIndex;
        this .currEditColoringDialog =  new EditColoringWindow(
            allowedColorings, demo, this, (Coloring) colorings.elementAt(selectedIndex), myWindow, expressionEnvironment );
    }

    public  void setSelectedColoringAlphaValue( double value ) {
        if ( selectedIndex >= 0 ) {
            this .alphaValues .setElementAt( new Double( value ), selectedIndex );
        }
    }

    public  void setSelectedColoringMixValue( double value ) {
        if ( selectedIndex >= 0 ) {
            this .mixValues .setElementAt( new Double( value ), selectedIndex );
        }
    }

    public  void removeSelectedColoring() {
        if ( selectedIndex >= 0 ) {
            hiliteImage( selectedIndex );
            colorings .removeElementAt( selectedIndex );
            alphaValues .removeElementAt( selectedIndex );
            mixValues .removeElementAt( selectedIndex );
            ((EditColorCanvasImage) coloringImages.elementAt(selectedIndex)).dispose();
            coloringImages .removeElementAt( selectedIndex );
            selectedIndex = - 1;
            myPanel .disableSelectionButtons();
            redraw();
            repaint();
            invalidate();
        }
    }

    public  void setColoring( int index, Coloring coloring ) {
        if ( index < 0 || index >= colorings .size() ) {
            addColoring( coloring, 1, 0 );
        }
        else {
            // set the coloring
            DependencyManager.removeDependency(this, ((Coloring) colorings .elementAt(index)));
            ((Coloring) colorings .elementAt(index)).dispose();
            colorings .setElementAt( coloring, index );
            coloringImages .setElementAt( null, index );
        }
        unselect();
    }

    private
        void editColoringFrameDisposed( ) {
            this .currEditColoringIndex = -2;
            this .currEditColoringDialog = null;
            this .myPanel .enableWindow();
            this .myPanel .disableSelectionButtons();
            this .unselect();
            redraw();
            repaint();
        }

    public  void dialogOKed(Window dialog) {
        if (dialog == this.currEditColoringDialog) {
            if ( this.currEditColoringIndex < 0 ) {
                // add a new coloring
                addColoring( ((EditColoringWindow) dialog).coloring() );
                myPanel.setSize();
            }
            else {
                // coloring was changed
                setColoring( this.currEditColoringIndex, ((EditColoringWindow) dialog).coloring() );
            }
            editColoringFrameDisposed();
            coloringChanged();
        }
    }

    public  void dialogCanceled(Window dialog) {
        if (dialog == this.currEditColoringDialog)
            editColoringFrameDisposed();
    }

    public  void coloringChanged() {
        this .myPanel .coloringChanged();
    }

    public  void update( Graphics g ) {
        paint( g );
    }

    public  void paint( Graphics g ) {
        if ( bufferImage == null ) {
            myPanel.setSize();
            bufferImage = createImage( IMAGE_WIDTH,
                                       IMAGE_HEIGHT * this .maxImages + 2 );
            bufferGraphics = bufferImage .getGraphics();
            redraw();
        }
        g .drawImage( bufferImage, 0, 0, this );
    }

    public  void redraw() {
        if ( bufferGraphics != null && bufferImage != null ) {
            bufferGraphics .setColor( BGCOLOR );
            bufferGraphics .fillRect( 0, 0, this .size() .width, this .size() .height );
            for ( int coloringIndex = 0; coloringIndex < coloringImages .size(); coloringIndex++ ) {
                if ( coloringImages .elementAt( coloringIndex ) == null ) {
                    coloringImages .setElementAt(
                                                 makeImage( (Coloring) colorings .elementAt( coloringIndex ),
                                                            IMAGE_WIDTH,
                                                            IMAGE_HEIGHT ),
                                                 coloringIndex );
                }
                bufferGraphics .drawImage( ((EditColorCanvasImage) coloringImages
                                            .elementAt( coloringIndex ))
                                           .image(), 0, IMAGE_HEIGHT * coloringIndex, this );
                bufferGraphics .setColor( Color .black );
                bufferGraphics .drawRect( 0, IMAGE_HEIGHT * coloringIndex,
                                          IMAGE_WIDTH - 1, IMAGE_HEIGHT );
            }
            if ( cleanBufferImage == null ) {
                cleanBufferImage = createImage( this .size() .width,
                                                this .size() .height );
            }
            cleanBufferImage .getGraphics() .drawImage( bufferImage, 0, 0, this );
            if ( this .selectedIndex >= 0 ) {
                hiliteImage( selectedIndex );
            }
            if ( ! this .enabled ) {
                // dim the image
                this .disable();
            }
        }
    }

    private EditColorCanvasImage makeImage( Coloring coloring, int imageWidth, int imageHeight ) {
        EditColorCanvasImage image;
        if ( coloring instanceof ColoringConstant )
            image = new EditColorImageConstant( (ColoringConstant) coloring );
        else if ( coloring instanceof ColoringExpression )
            image = new EditColorImageExpression( (ColoringExpression) coloring );
        else if ( coloring instanceof ColoringGradient )
            image = new EditColorImageGradient( (ColoringGradient) coloring );
        else if ( coloring instanceof ColoringChecker )
            image = new EditColorImageChecker( (ColoringChecker) coloring );
        else
            image = new EditColorImageUnknown( coloring );
        image.draw( imageWidth, imageHeight, this );
        DependencyManager.setDependency(this, image);
        return image;
    }

    public  void dispose() {
        // dispose images
        for ( int i = 0; i < coloringImages .size(); i++ ) {
            ((EditColorCanvasImage) coloringImages.elementAt(i)).dispose();
        }
    }

    public  boolean mouseDown( Event e, int x, int y ) {
        if ( enabled ) {
            prevMouseDownX = mouseDownX;
            prevMouseDownY = mouseDownY;
            mouseDownX = x;
            mouseDownY = y;
            if (e.controlDown()) {
                // popup menu for the panel: let the panel get the event
                return false;
            }
            setSelection( x, y );
            repaint();
            this .cleanBufferImage = createImage( this .size() .width, this .size() .height );
            cleanBufferImage .getGraphics() .drawImage( bufferImage, 0, 0, this );
        }
        return true;
    }

    public  boolean mouseDrag( Event e, int x, int y ) {
        if ( enabled ) {
            if ( ! dragging ) {
                // make sure the user dragged enough for a drag
                dragging = Math .abs( y - mouseDownY ) > 7;
            }
            if ( dragging && selectedIndex >= 0 ) {
                bufferGraphics .drawImage( cleanBufferImage, 0, 0, this );
                this .betweenIndex = getIndex( x, y + IMAGE_HEIGHT / 2 );
                if ( betweenIndex < 0 || betweenIndex > this .coloringImages .size() ) {
                    // the index is out of bounds
                    betweenIndex = selectedIndex;
                }
                bufferGraphics .setColor( Color .black );
                bufferGraphics .fillRect( 0, betweenIndex * IMAGE_HEIGHT - 2,
                                          IMAGE_WIDTH - 1, 5 );
                bufferGraphics .setColor( Color .gray );
                bufferGraphics .drawRect( 0,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT),
                                          IMAGE_WIDTH - 1,
                                          IMAGE_HEIGHT );
                bufferGraphics .drawLine( 0,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT)
                                          + IMAGE_HEIGHT / 3,
                                          IMAGE_WIDTH - 1,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT)
                                          + IMAGE_HEIGHT / 3 );
                bufferGraphics .drawLine( 0,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT)
                                          + IMAGE_HEIGHT * 2 / 3,
                                          IMAGE_WIDTH - 1,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT)
                                          + IMAGE_HEIGHT * 2 / 3 );
                bufferGraphics .drawLine( IMAGE_WIDTH / 3,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT),
                                          IMAGE_WIDTH / 3,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT) + IMAGE_HEIGHT );
                bufferGraphics .drawLine( IMAGE_WIDTH * 2 / 3,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT),
                                          IMAGE_WIDTH * 2 / 3,
                                          y - (mouseDownY - selectedIndex * IMAGE_HEIGHT) + IMAGE_HEIGHT );
                repaint();
                return true;
            }
        }
        return false;
    }

    public  boolean mouseUp( Event e, int x, int y ) {
        if ( enabled ) {
            dragging = false;
            long currTime = System .currentTimeMillis();
            bufferGraphics .drawImage( cleanBufferImage, 0, 0, this );
            repaint();
            if ( currTime < mouseUpTime + DOUBLE_CLICK_TIME
                 && Math .abs( x - prevMouseDownX ) < 3
                 && Math .abs( y - prevMouseDownY ) < 3 ) {
                // double click
                mouseUpTime = - DOUBLE_CLICK_TIME;
                mouseDoubleClick( x, y );
                return true;
            }
            else {
                mouseUpTime = currTime;
            }
            if ( betweenIndex >= 0 ) {
                if ( betweenIndex != selectedIndex ) {
                    changeColoringPosition( selectedIndex, betweenIndex );
                    coloringChanged();
                }
                selectedIndex = - 1;
                betweenIndex = - 1;
                myPanel .disableSelectionButtons();
                redraw();
                repaint();
            }
            return true;
        }
        return true;
    }

    public  void mouseDoubleClick( int x, int y ) {
        if ( enabled ) {
            if ( selectedIndex >= 0 ) {
                // edit the selected coloring
                editSelectedColoring();
            }
        }
    }

    public  void disable() {
        this .enabled = false;
        // make the canvas look disabled (dim it out)
        bufferGraphics .setColor( BGCOLOR );
        for ( int i = 0; i < this .size() .width; i += 2 ) {
            for ( int j = 0; j < this .size() .height; j += 2 ) {
                bufferGraphics .drawLine( i, j, i, j );
            }
        }
        repaint();
        super .disable();
    }

    public  void enable() {
        // undim the canvas
        bufferGraphics .drawImage( cleanBufferImage, 0, 0, this );
        super .enable();
        repaint();
        this .enabled = true;
    }

    public  Coloring selection() {
        return (Coloring) colorings .elementAt( this .selectedIndex );
    }

    private  void setSelection( int x, int y ) {
        if ( this .selectedIndex >= 0 ) {
            hiliteImage( this .selectedIndex );
        }
        this .selectedIndex = getIndex( x, y );
        if ( selectedIndex < 0 || selectedIndex >= this .coloringImages .size() ) {
            // the index is out of bounds
            selectedIndex = - 1;
            myPanel .disableSelectionButtons();
        }
        else {
            hiliteImage( this .selectedIndex );
            myPanel .setAlphaValue( ((Double) this .alphaValues .elementAt( selectedIndex )) .doubleValue() );
            myPanel .setMixValue( ((Double) this .mixValues .elementAt( selectedIndex )) .doubleValue() );
            myPanel .enableSelectionButtons();
        }
    }

    public  void unselect() {
        if ( selectedIndex >= 0 ) {
            hiliteImage( selectedIndex );
            selectedIndex = - 1;
            myPanel .disableSelectionButtons();
        }
    }

    private  int selectedIndex = - 1;

    /** Returns the index of the coloring/image that contains
        * the coord x,y.
        */
    private  int getIndex( int x, int y ) {
        return y / (int) IMAGE_HEIGHT;
    }

    private
        void changeColoringPosition( int index, int destIndex ) {
            colorings .insertElementAt( colorings .elementAt( index ), destIndex );
            alphaValues .insertElementAt( alphaValues .elementAt( index ), destIndex );
            mixValues .insertElementAt( mixValues .elementAt( index ), destIndex );
            coloringImages .insertElementAt( coloringImages .elementAt( index ), destIndex );
            if ( index < destIndex ) {
                colorings .removeElementAt( index );
                alphaValues .removeElementAt( index );
                mixValues .removeElementAt( index );
                coloringImages .removeElementAt( index );
            }
            else {
                colorings .removeElementAt( index + 1 );
                alphaValues .removeElementAt( index + 1 );
                mixValues .removeElementAt( index + 1 );
                coloringImages .removeElementAt( index + 1 );
            }
        }

    private  java.awt .Point getImageLocation( int index ) {
        return new java.awt .Point( 0, index * IMAGE_HEIGHT );
    }

    /**
     * Returns the image that contains the coords x,y.
     * That is, the one the user clicks on.
     */
    private  Image getImage( int x, int y ) {
        return ((EditColorCanvasImage) coloringImages .elementAt( getIndex( x, y ) )) .image();
    }

    private  void hiliteImage( int index ) {
        bufferGraphics .setColor( Color .black );
        bufferGraphics .setXORMode( Color .white );
        java.awt .Point imageLoc = getImageLocation( index );
        // bufferGraphics .fillRect( imageLoc .x, imageLoc .y, IMAGE_WIDTH, IMAGE_HEIGHT );
        final int border_size = 4;
        bufferGraphics.fillRect( imageLoc.x + 1, imageLoc.y + 1, border_size - 1, IMAGE_HEIGHT - 1 );
        bufferGraphics.fillRect( imageLoc.x + border_size, imageLoc.y + 1, IMAGE_WIDTH - border_size - 1, border_size - 1);
        bufferGraphics.fillRect( imageLoc.x + border_size, imageLoc.y + IMAGE_HEIGHT - border_size,
                                 IMAGE_WIDTH - border_size - 1, border_size );
        bufferGraphics.fillRect( imageLoc.x + IMAGE_WIDTH - border_size, imageLoc.y + border_size,
                                 border_size - 1, IMAGE_HEIGHT - border_size - border_size);
        bufferGraphics .setPaintMode();
    }

    public void dependencyUpdateDef( Set updatingImages ) {
        if ( bufferImage != null ) {
            Exec.begin_nocancel();
            redraw();
            repaint();
            Exec.end_nocancel();
        }
    }

    public void dependencyUpdateVal(Set updatingImages) {
        dependencyUpdateDef(updatingImages);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    public Dimension getPreferredSize() {
        return new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT * colorings.size() + 2);
    }

}




abstract class EditColorCanvasImage extends Object implements Dependable {
    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    public  DependencyNode   dependencyNode() { return myDependencyNode_; }
    public  void dependencyUpdateDef(Set objs) {/*to override*/};
    public  void dependencyUpdateVal(Set objs) {/*to override*/};

    
    protected  Image image;

    protected  Coloring coloring;

    protected  int imageHeight, imageWidth;

    protected static final  int MARGIN_X = 2, MARGIN_Y = 2;

    protected static final  int COLOR_STRIPE_HEIGHT = 5;

    public static final  Color BGCOLOR = Color .white;
    
    public  EditColorCanvasImage( Coloring coloring ) {
        super();

        this .coloring = coloring;
        DependencyManager.setDependency(this, coloring);
    }

    public void dispose() {
        DependencyManager.remove(this);
    }
    
    public  abstract
    void draw( int imageWidth, int imageHeight, Component imageProducer ) ;

    public  Image image() {
        return image;
    }

    public  Coloring coloring() {
        return coloring;
    }

    protected static
        Color colorStripeColor( double[] color, int pixel ) {
            double[] bgcolor;
            if ( ((double) pixel) / ((double) COLOR_STRIPE_HEIGHT) % 2 == 0 ) {
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


}




class EditColorImageConstant extends EditColorCanvasImage{

    public  EditColorImageConstant( ColoringConstant coloring ) {
        super( coloring );
    }

    public
    void draw( int imageWidth, int imageHeight, Component imageProducer ) {
        if ( this .image == null || this .imageHeight != imageHeight || this .imageWidth != imageWidth ) {
            // we need to create the iamge
            image = imageProducer .createImage( imageWidth, imageHeight );
        }
        if ( image != null ) {
            this .imageHeight = imageHeight;
            this .imageWidth = imageWidth;
            Graphics g = image .getGraphics();
            int textHeight = g .getFontMetrics() .getHeight();
            g .setColor( Color .black );
            g .drawString( "Constant Color", MARGIN_X, MARGIN_Y + textHeight );
            double[] color = ((ColoringConstant) coloring) .color();
            for ( int pixel = 0; pixel < imageWidth - 2 * MARGIN_X - 2; pixel += 2 ) {
                g .setColor( colorStripeColor( color, pixel ) );
                g .fillRect( pixel + MARGIN_X, MARGIN_Y + textHeight * 2 + 2, 2,
                             COLOR_STRIPE_HEIGHT );
            }
        }
    }


}




class EditColorImageExpression extends EditColorCanvasImage{

    public  EditColorImageExpression( ColoringExpression coloring ) {
        super( coloring );
    }

    public
    void draw( int imageWidth, int imageHeight, Component imageProducer ) {
        if ( this .image == null || this .imageHeight != imageHeight || this .imageWidth != imageWidth ) {
            // we need to create the iamge
            image = imageProducer .createImage( imageWidth, imageHeight );
        }
        if ( image != null ) {
            this .imageHeight = imageHeight;
            this .imageWidth = imageWidth;
            Graphics g = image .getGraphics();
            int textHeight = g .getFontMetrics() .getHeight();
            g .setColor( Color .black );
            g .drawString( "Expression:  " + ((ColoringExpression) coloring) .expressionDefinitionString(),
                           MARGIN_X, MARGIN_Y + textHeight );
            // draw the spectrum bar
            for ( int pixel = 0; pixel < imageWidth - 2 * MARGIN_X - 2; pixel += 2 ) {
                g .setColor( Color .getHSBColor( ((float) pixel) / (float) (imageWidth - 2 * MARGIN_X),
                                                 1, 1 ) );
                g .fillRect( pixel + MARGIN_X, MARGIN_Y + textHeight * 2 + 2, 2,
                             COLOR_STRIPE_HEIGHT );
            }
            // put in a few numbers corresponding to color values
            g .setColor( Color .black );
            g .drawString( "0", MARGIN_X, MARGIN_Y + textHeight * 2 );
            g .drawString( "0.3",
                           imageWidth / 3 - g .getFontMetrics() .stringWidth( "0.3" ) / 2,
                           MARGIN_Y + textHeight * 2 );
            g .drawString( "0.6",
                           imageWidth * 2 / 3 - g .getFontMetrics() .stringWidth( "0.6" ) / 2,
                           MARGIN_Y + textHeight * 2 );
            g .drawString( "1",
                           imageWidth - g .getFontMetrics() .stringWidth( "1" ) - MARGIN_X,
                           MARGIN_Y + textHeight * 2 );
        }
    }


}



class EditColorImageGradient extends EditColorCanvasImage implements Dependable {

    public  EditColorImageGradient( ColoringGradient coloring ) {
        super( coloring );
    }

    public
    void draw( int imageWidth, int imageHeight, Component imageProducer ) {
        if ( this .image == null || this .imageHeight != imageHeight || this .imageWidth != imageWidth ) {
            // we need to create the iamge
            image = imageProducer .createImage( imageWidth, imageHeight );
        }
        if ( image != null ) {
            this .imageHeight = imageHeight;
            this .imageWidth = imageWidth;
            Graphics g = image .getGraphics();
            g .setColor( BGCOLOR );
            g .fillRect( 0, 0, imageWidth, imageHeight );
            int textHeight = g .getFontMetrics() .getHeight();
            g .setColor( Color .black );
            g .drawString( "Gradient:  " + ((ColoringGradient) coloring) .expressionDefinitionString(),
                           MARGIN_X, MARGIN_Y + textHeight );
            // draw the gradient
            // calculate the pure color value expressions
            coloring .setCache();
            double[] values = ((ColoringGradient) coloring) .pureColorValues();
            Coloring[] colorings = ((ColoringGradient) coloring) .colorings();
            double[][] colors = new double [ colorings .length ] [];
            for ( int i = 0; i < colorings .length; i++ ) {
                if ( colorings[i] instanceof ColoringConstant ) {
                    colors[i] = ((ColoringConstant) colorings[i]) .color();
                }
                else {
                    colors[i] = new double []{ 0, 0, 0, 1 / 2 };
                }
            }
            double pixelsPerUnit = (imageWidth - 2 * MARGIN_X) / (values[values .length - 1] - values[0]);
            int prevColorValuePixels = 0;
            double firstValue = values[0];
            double[] prevColor = colors[0];
            int nextIndex = 1;
            int nextColorValuePixels = (int) Math .round( (values[nextIndex] - firstValue) * pixelsPerUnit );
            double[] nextColor = colors[nextIndex];
            int pixelIncrement = 2;
            for ( int pixel = 0; pixel < imageWidth - 2 * MARGIN_X - 2; pixel += pixelIncrement ) {
                if ( pixel > nextColorValuePixels && nextIndex + 1 < colors .length ) {
                    prevColorValuePixels = nextColorValuePixels;
                    prevColor = nextColor;
                    ++nextIndex;
                    nextColorValuePixels = (int) Math .round(
                                                             (values[nextIndex] - firstValue) * pixelsPerUnit );
                    nextColor = colors[nextIndex];
                    pixel -= pixelIncrement;
                    continue;
                }
                double coefficientPrev, coefficientNext;
                if ( nextColorValuePixels == prevColorValuePixels ) {
                    coefficientPrev = 0;
                    coefficientNext = 1;
                }
                else {
                    coefficientPrev = ((double) (nextColorValuePixels - pixel)) /
                    (double) (nextColorValuePixels - prevColorValuePixels);
                    coefficientNext = ((double) (pixel - prevColorValuePixels)) /
                        (double) (nextColorValuePixels - prevColorValuePixels);
                }
                Color prevColorOnScreen = colorStripeColor( prevColor, pixel ),
                    nextColorOnScreen = colorStripeColor( nextColor, pixel );
                int red = (int) Math .round( coefficientPrev * prevColorOnScreen .getRed()
                                             + coefficientNext * nextColorOnScreen .getRed() );
                int green = (int) Math .round( coefficientPrev * prevColorOnScreen .getGreen()
                                               + coefficientNext * nextColorOnScreen .getGreen() );
                int blue = (int) Math .round( coefficientPrev * prevColorOnScreen .getBlue()
                                              + coefficientNext * nextColorOnScreen .getBlue() );
                g .setColor( new java.awt .Color( red, green, blue ) );
                g .fillRect( pixel + MARGIN_X, MARGIN_Y + textHeight * 2 + 2,
                             pixelIncrement, COLOR_STRIPE_HEIGHT );
            }
            // put in the values corresponding to pure colors
            String[] definitionStrings = ((ColoringGradient) coloring) .pureColorExpressionDefinitions();
            g .setColor( Color .black );
            g .drawString( definitionStrings[0], MARGIN_X,
                           MARGIN_Y + textHeight * 2 );
            for ( int i = 1; i < definitionStrings .length - 1; i++ ) {
                g .drawString(
                              definitionStrings[i],
                              (int) Math .round( imageWidth *
                                                 ((values[i] - values[0]) / (values[values .length - 1] - values[0]))
                                                 - g .getFontMetrics() .stringWidth( definitionStrings[i] ) / 2 ),
                              MARGIN_Y + textHeight * 2 );
            }
            g .drawString( definitionStrings[definitionStrings .length - 1],
                           imageWidth - MARGIN_X
                           - g .getFontMetrics() .stringWidth( definitionStrings[definitionStrings .length - 1] ),
                           MARGIN_Y + textHeight * 2 );
        }
    }

    public
    void dependencyUpdateVal( Set updatingObjects ) {
        Exec.begin_nocancel();
        draw( this .imageWidth, this .imageHeight, null );
        Exec.end_nocancel();
    }
    
    public
    void dependencyUpdateDef( Set updatingObjects ) {
        dependencyUpdateVal(updatingObjects);
    }

    

}




class EditColorImageChecker extends EditColorCanvasImage implements Dependable{

    public  EditColorImageChecker( ColoringChecker coloring ) {
        super( coloring );
    }

    public
    void draw( int imageWidth, int imageHeight,
               Component imageProducer ) {
        if ( this .image == null || this .imageHeight != imageHeight || this .imageWidth != imageWidth ) {
            // we need to create the iamge
            image = imageProducer .createImage( imageWidth, imageHeight );
        }
        if ( image != null ) {
            this .imageHeight = imageHeight;
            this .imageWidth = imageWidth;
            Graphics g = image .getGraphics();
            int textHeight = g .getFontMetrics() .getHeight();
            // title text
            g .setColor( Color .black );
            String intervalsString = "";
            STEInterval[] intervals = ((ColoringChecker) coloring) .intervals();
            for ( int i = 0; i < intervals .length; i++ ) {
                intervalsString += intervals[i] .name();
                if ( i < intervals .length - 1 ) {
                    intervalsString += ", ";
                }
            }
            g .drawString( "Checkered:  " + intervalsString, MARGIN_X,
                           MARGIN_Y + textHeight );
            // color bar
            Coloring coloring1 = ((ColoringChecker) coloring) .coloring1();
            Coloring coloring2 = ((ColoringChecker) coloring) .coloring2();
            double[] color1, color2;
            if ( coloring1 instanceof ColoringConstant ) {
                color1 = ((ColoringConstant) coloring1) .color();
            }
            else {
                color1 = new double []{ 0, 0, 0, 1 / 2 };
            }
            if ( coloring2 instanceof ColoringConstant ) {
                color2 = ((ColoringConstant) coloring2) .color();
            }
            else {
                color2 = new double []{ 0, 0, 0, 1 / 2 };
            }
            // draw some of the bar for each color
            for ( int pixel = 0; pixel < imageWidth / 2 - MARGIN_X - 1; pixel += 2 ) {
                g .setColor( colorStripeColor( color1, pixel ) );
                g .fillRect( pixel + MARGIN_X, MARGIN_Y + textHeight * 2 + 2, 2,
                             COLOR_STRIPE_HEIGHT );
            }
            for ( int pixel = imageWidth / 2 - MARGIN_X + 2;
                  pixel < imageWidth - 2 * MARGIN_X - 2;
                  pixel += 2 ) {
                g .setColor( colorStripeColor( color2, pixel ) );
                g .fillRect( pixel + MARGIN_X, MARGIN_Y + textHeight * 2 + 2, 2,
                             COLOR_STRIPE_HEIGHT );
            }
        }
    }

    public
    void dependencyUpdateVal( Set updatingObjects ) {
        Exec.begin_nocancel();
        draw( this .imageWidth, this .imageHeight, null );
        Exec.end_nocancel();
    }

    public
    void dependencyUpdateDef( Set updatingObjects ) {
        dependencyUpdateVal(updatingObjects);
    }


}




class EditColorImageUnknown extends EditColorCanvasImage{

    public  EditColorImageUnknown( Coloring coloring ) {
        super( coloring );
    }

    public
    void draw( int imageWidth, int imageHeight,
               Component imageProducer ) {
        image = imageProducer .createImage( imageWidth, imageHeight );
        if ( image != null ) {
            this .imageHeight = imageHeight;
            this .imageWidth = imageWidth;
            Graphics g = image .getGraphics();
            g .setColor( Color .black );
            g .drawString( "Unknown Coloring", MARGIN_X,
                           MARGIN_Y + g .getFontMetrics() .getHeight() );
        }
    }


}



