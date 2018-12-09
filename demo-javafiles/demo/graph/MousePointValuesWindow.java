package demo.graph;

import java.awt.*;

import demo.Demo;
import demo.expr.ste.STEValue;

public class MousePointValuesWindow extends Dialog {

    private  Demo demo;

    private  GraphCanvas3D myCanvas;
    
    
    private java.awt.TextField value0Fld, value1Fld, value2Fld;


    private 
    java.awt .Button okButton = new java.awt .Button( "OK" ),
                        cancelButton = new java.awt .Button( "Cancel" );

    public 
    MousePointValuesWindow( GraphFrame frame, GraphCanvas3D canvas, Demo demo, boolean showValue2 ) {
        super( frame, "Select Variables" );

            this .demo = demo;
            this .myCanvas = canvas;
            
            STEValue[] currValues = canvas.getMousePointValues();
            value0Fld = new TextField(currValues[0] == null ? "" : currValues[0].name());
            value1Fld = new TextField(currValues[1] == null ? "" : currValues[1].name());
            value2Fld = new TextField(currValues[2] == null ? "" : currValues[2].name());
            
            if (showValue2)
                setLayout(new GridLayout(4,2));
            else
                setLayout(new GridLayout(3,2));
            add(new Label("Horizontal: "));
            add(value0Fld);
            add(new Label("Vertical: "));
            add(value1Fld);
            if (showValue2) {
                add(new Label("Out of screen:"));
                add(value2Fld);
            }
            
            add(new Label(""));
            Panel btnsPanel = new Panel();
            btnsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            btnsPanel.add(cancelButton);
            btnsPanel.add(okButton);
            add(btnsPanel);
            
            pack();
            setVisible(true);
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

    public 
    boolean action( java.awt .Event event, Object object ) {
        if ( event .target == okButton ) {
            boolean success = demo .setGraphCanvasMousePlaceValues( myCanvas,
                                     value0Fld.getText(), value1Fld.getText(), value2Fld.getText() );
            // close the window if there were no errors -- the user exited
            if (success) {
                setVisible(false);
                dispose();
            }
        }
        else if ( event .target == cancelButton ) {
            setVisible(false);
            dispose();
        }
        else {
            return super .action( event, object );
        }
        return true;
    }

    public  boolean keyDown( java.awt .Event e, int key ) {
        if ( key == 10 || key == 3 ) {
            // enter or return key pressed: send OK event
            return action( new Event( okButton, Event .ACTION_EVENT, null ), null );
        }
        return super .keyDown( e, key );
    }

}
