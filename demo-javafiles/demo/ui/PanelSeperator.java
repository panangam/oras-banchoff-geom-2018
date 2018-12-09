//
//  PanelSeperator.java
//  Demo
//
//  Created by David Eigen on Tue Jun 11 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.ui;

import java.awt.*;

public class PanelSeperator extends Panel{

    public  PanelSeperator() {
        this( HORIZONTAL );
    }

    public  PanelSeperator( int type ) {
        super();

        this .type = type;
    }

    // constants for how to draw the line
    public static final  int HORIZONTAL = 1, VERTICAL = 2;

    private  int type;

    public  void paint( Graphics g ) {
        g .setColor( Color .black );
        if ( type == HORIZONTAL ) {
            // horizontal line
            g .drawLine( 5, this .size() .height / 2,
                         this .size() .width - 5, this .size() .height / 2 );
        }
        else {
            // vertical line
            g .drawLine( this .size() .width / 2, 0,
                         this .size() .width / 2, this .size() .height );
        }
    }


}
