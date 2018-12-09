//
//  RemoveButton.java
//  Demo
//
//  Created by David Eigen on Sat Aug 17 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.Set;

/**
 * A LittleButton is a small "button" (actually a graphic) that the user can click on. It can
 * dispatch events to ActionListeners just like a java.awt.Button. The label string should be
 * very small, often just one character.
 */
public class LittleButton extends Component implements MouseListener {

    
    private static final java.awt.Color INACTIVE_FG_COLOR = new java.awt.Color(50,50,50),
        INACTIVE_BG_COLOR = new java.awt.Color(255,255,255),
        ACTIVE_FG_COLOR   = new java.awt.Color(0,0,0),
        ACTIVE_BG_COLOR   = new java.awt.Color(205,205,205);

    private boolean mouseDown_ = false;
    private boolean active_ = false;
    protected int width_, height_;
    private String label_;
    protected Font font_;
    
    private demo.util.Set listeners_ = new demo.util.Set();

    public LittleButton() {
        this("");
    }

    public LittleButton(String label) {
        label_ = label;
        font_ = new Font("Ariel", Font.PLAIN, 10);
        width_ = height_ = 14;
        this.addMouseListener(this);
    }

    public void addActionListener(ActionListener l) {
        listeners_.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners_.remove(l);
    }

    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent)
            processActionEvent((ActionEvent) e);
        super.processEvent(e);
    }

    protected void processActionEvent(ActionEvent e) {
        for (java.util.Enumeration ls = listeners_.elements();
             ls.hasMoreElements();)
            ((ActionListener) ls.nextElement()).actionPerformed(e);
    }

    public void paint(Graphics g) {
        if ( demo.Demo.javaVersionIsGreaterThanOrEqualTo(new int[]{1,2}) )
            Graphics2DSettings.setAntialiasing(g, true);
        setBGColor(g);
        g.fillOval(1,1,width_,height_);
        setFGColor(g);
        g.drawOval(1, 1, width_, height_);
        paintGraphic(g);
    }

    protected void setFGColor(Graphics g) {
        g.setColor(active_ ? ACTIVE_FG_COLOR : INACTIVE_FG_COLOR);
    }

    protected void setBGColor(Graphics g) {
        g.setColor(active_ ? ACTIVE_BG_COLOR : INACTIVE_BG_COLOR);
    }

    protected void paintGraphic(Graphics g) {
        setFGColor(g);
        g.setFont(font_);        
        int w = g.getFontMetrics().stringWidth(label_);
        int h = g.getFontMetrics().getHeight();
        g.drawString(label_, width_/2 - w/2 + 1, height_/2 + h/3 + 1);
    }

    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
        if (mouseDown_) {
            active_ = true;
            repaint();
        }
    }
    public void mouseExited(MouseEvent e) {
        if (mouseDown_) {
            active_ = false;
            repaint();
        }
    }
    public void mousePressed(MouseEvent e) {
        mouseDown_ = true;
        active_ = true;
        repaint();
    }
    public void mouseReleased(MouseEvent e) {
        mouseDown_ = false;
        repaint();
        if (active_) {
            processEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                         "RemoveButton action performed"));
        }
        active_ = false;
    }

    public Dimension getPreferredSize() {
        return new Dimension(width_+2, height_+2);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
