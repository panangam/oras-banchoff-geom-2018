package demo.graph;

import java.awt .*;

import demo.Demo;
import demo.ui.FlowVerticalLayout;

public class SelectGroupWindow extends java.awt .Dialog implements java.awt.event.ItemListener {

    private  Demo demo;

    private  Graph myGraph;

    private  GraphCanvas3D myCanvas;

    private  GraphFrame3D myFrame;

    private  GraphGroup myGroup;

    private  Choice groupsChoice = new Choice();

    private 
    Checkbox preserveRotationsCheckbox = new Checkbox( "Preserve Relative Rotations" ),
             linkOnlyRotationCheckbox = new Checkbox( "Link Rotation Only" );

    private 
    java.util .Vector groups = new java.util .Vector();

    private 
    java.awt .Button okButton = new java.awt .Button( "OK" ),
                        cancelButton = new java.awt .Button( "Cancel" );

    public 
    SelectGroupWindow( GraphFrame3D frame, GraphGroup group,
                          Graph graph, GraphCanvas3D canvas, Demo demo ) {
        super( frame, "Select Group" );

            this .demo = demo;
            this .myFrame = frame;
            this .myGroup = group;
            this .myGraph = graph;
            this .myCanvas = canvas;
            makeGroupsChoice();
            this .setLayout( new java.awt .BorderLayout() );
            if ( groupsChoice .countItems() == 0 ) {
                // error: there were no graphs inserted into the choice
                groupsChoice = null;
                add(new java.awt .Label( "You cannot link the rotation of this graph with any more" ),
                    "North");
                add(new java.awt .Label( "graphs, because the rotation of all graphs are linked." ),
                    "Center");
                java.awt .Panel buttonPanel = new java.awt .Panel();
                buttonPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
                buttonPanel .add( okButton );
                add( buttonPanel, "South" );
            }
            else {
                java.awt .Panel choicePanel = new java.awt .Panel();
                choicePanel .add( new java.awt .Label( "Select Group:" ) );
                choicePanel .add( groupsChoice );
                java.awt .Panel buttonPanel = new java.awt .Panel();
                buttonPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
                buttonPanel .add( cancelButton );
                buttonPanel .add( okButton );
                preserveRotationsCheckbox .setState( false );
                Object curselec = this .groups .elementAt( groupsChoice .getSelectedIndex() );
                linkOnlyRotationCheckbox .setState( curselec instanceof GraphGroup ?
                                                  !((GraphGroup) curselec).transformAll() : true );
                groupsChoice.addItemListener(this);
                Panel centerPanel = new Panel();
                centerPanel.setLayout(new FlowVerticalLayout());
                centerPanel.add(preserveRotationsCheckbox);
                centerPanel.add(linkOnlyRotationCheckbox);
                add( choicePanel, "North" );
                add( centerPanel, "Center" );
                add( buttonPanel, "South" );
            }
            pack();
            setVisible(true);
        }

    private  void makeGroupsChoice() {
        // groups will be keyed by group, and store SelectGroupChoiceData
        java.util .Dictionary groups = new java.util .Hashtable();
        java.util .Enumeration frames = this .demo .allGraphFrames();
        while ( frames .hasMoreElements() ) {
            GraphFrame currFrame = (GraphFrame) frames .nextElement();
            if ( currFrame instanceof GraphFrame3D ) {
                GraphGroup currGroup = ((GraphCanvas3D) ((GraphFrame3D) currFrame) .canvas()) .getGroup();
                if ( (myGroup == null || currGroup != myGroup) && currFrame != myFrame ) {
                    if ( currGroup != null && groups .get( currGroup ) != null ) {
                        SelectGroupChoiceData data = (SelectGroupChoiceData) groups .get( currGroup );
                        data .name += ", " + currFrame .getTitle();
                    }
                    else {
                        SelectGroupChoiceData data;
                        if ( currGroup != null ) {
                            data = new SelectGroupChoiceData( currGroup );
                        }
                        else {
                            data = new SelectGroupChoiceData( (GraphFrame3D) currFrame );
                        }
                        data .name = new String( currFrame .getTitle() );
                        groups .put( currGroup != null ? currGroup : new Object(), data );
                    }
                }
            }
        }
        java.util .Enumeration dataEnum = groups .elements();
        while ( dataEnum .hasMoreElements() ) {
            SelectGroupChoiceData currData = (SelectGroupChoiceData) dataEnum .nextElement();
            this .groupsChoice .addItem( currData .name );
            if ( currData .group != null ) {
                this .groups .addElement( currData .group );
            }
            else {
                this .groups .addElement( currData .frame );
            }
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

    public  void itemStateChanged(java.awt.event.ItemEvent e) {
        if (e.getSource() == groupsChoice) {
            Object curselec = this .groups .elementAt( groupsChoice .getSelectedIndex() );
            linkOnlyRotationCheckbox.setState( curselec instanceof GraphGroup ?
                                             !((GraphGroup) curselec).transformAll() : true );
        }
    }

    public 
    boolean action( java.awt .Event event, Object object ) {
        if ( event .target == okButton ) {
            // make the selected graph the master of the graph this window was called from
            if ( groupsChoice != null ) {
                // add this graph to the group
                Object target = this .groups .elementAt( groupsChoice .getSelectedIndex() );
                if ( target instanceof GraphGroup ) {
                    demo .addGraphToGroup( (GraphGroup) target, myGraph, myCanvas,
                                           preserveRotationsCheckbox .getState(),
                                           !linkOnlyRotationCheckbox.getState());
                    // update the menu items of the frame
                    myFrame .setGraphGroupState( true );
                }
                else {
                    if ( target instanceof GraphFrame3D ) {
                        // we need to make a new group
                        java.util .Vector newGroupGraphs = new java.util .Vector( 2 );
                        java.util .Vector newGroupCanvases = new java.util .Vector( 2 );
                        newGroupGraphs .addElement( myGraph );
                        newGroupCanvases .addElement( myCanvas );
                        newGroupGraphs .addElement( ((GraphFrame3D) target) .canvas() .graph() );
                        newGroupCanvases .addElement( ((GraphFrame3D) target) .canvas() );
                        GraphGroup grp = demo.makeGraphGroup(newGroupGraphs.elements(),
                                                             newGroupCanvases.elements(),
                                                             preserveRotationsCheckbox.getState(),
                                                             !linkOnlyRotationCheckbox.getState());
                        myFrame .setGraphGroupState( true );
                        ((GraphFrame3D) target) .setGraphGroupState( true );
                    }
                    else {
                        return true;
                    }
                }
            }
            // close the window -- the user exited
            setVisible(false);
            dispose();
        }
        else {
            if ( event .target == cancelButton ) {
                setVisible(false);
                dispose();
            }
            else {
                if ( event .target != groupsChoice ) {
                    return super .action( event, object );
                }
            }
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




class SelectGroupChoiceData extends Object{

    public  SelectGroupChoiceData( GraphGroup group ) {
        super();

            this .group = group;
        }

    public  SelectGroupChoiceData( GraphFrame3D frame ) {
        super();

            this .frame = frame;
        }

    public  GraphGroup group = null;

    public  GraphFrame3D frame = null;

    public  String name;


}


