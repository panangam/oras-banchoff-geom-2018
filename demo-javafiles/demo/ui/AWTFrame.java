//
//  AWTFrame.java
//  Demo
//
//  Created by David Eigen on Fri Jul 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.ui;

import java.awt.*;
import java.awt.event.*;

import demo.Demo;

public class AWTFrame extends Frame {

    public static boolean USE_REAL_MENUS = ! Demo.runningOnMac();

    private static boolean ADD_TO_SETSIZE = Demo.runningOnMac();

    private Panel framePanel_ = new Panel();
    private Panel internalPanel_ = new Panel();
    private AWTMenuBar menubar_;
    private AWTMenuBarCanvas menuBarCanvas_;
    private boolean animateSetSize_ = false;

    private boolean doingLayout_ = false;
    
    public AWTFrame() {
        awtFrameInit();
    }

    public AWTFrame(String title) {
        super(title);
        awtFrameInit();
    }

    public void awtFrameInit() {
        super.setLayout(new BorderLayout());
        super.add(framePanel_, BorderLayout.CENTER);
        framePanel_.setLayout(new BorderLayout());
        framePanel_.add(internalPanel_, BorderLayout.CENTER);
        internalPanel_.setLayout(new FlowLayout(FlowLayout.CENTER));
    }

    public void setMenuBar(AWTMenuBar mb) {
        if (USE_REAL_MENUS) {
            this.setMenuBar(mb.menubar());
        }
        else {
            mb.setFrame(this);
            if (menuBarCanvas_ != null)
                framePanel_.remove(menuBarCanvas_);
            menuBarCanvas_ = new AWTMenuBarCanvas(mb, this);
            framePanel_.add(menuBarCanvas_, BorderLayout.NORTH);
        }
    }

    public void menuAdded(AWTMenu m) {
        if (!USE_REAL_MENUS && menuBarCanvas_ != null)
            menuBarCanvas_.menuAdded(m);
    }
    public void menuRemoved(AWTMenu m) {
        if (!USE_REAL_MENUS && menuBarCanvas_ != null)
            menuBarCanvas_.menuRemoved(m);
    }

    public synchronized Insets getInsets() {
        Insets insets = super.getInsets();
        if ((!USE_REAL_MENUS) && (menuBarCanvas_ != null) && (!doingLayout_))
            return new Insets(insets.top + menuBarCanvas_.getSize().height,
                              insets.left, insets.bottom, insets.right);
        return insets;
    }

    public void setLayout(LayoutManager layout) {
        if (internalPanel_ != null)
            internalPanel_.setLayout(layout);
    }
    
    public Component add(Component comp) {
        return internalPanel_.add(comp);
    }
    public Component add(Component comp, int index) {
        return internalPanel_.add(comp, index);
    }
    public void add(Component comp, Object constraints) {
        internalPanel_.add(comp, constraints);
    }
    public void add(Component comp, Object constraints, int index) {
        internalPanel_.add(comp, constraints, index);
    }
    public Component add(String name, Component comp) {
        return internalPanel_.add(name, comp);
    }
    

    public void remove(Component comp) {
        internalPanel_.remove(comp);
    }
    public void remove(int index) {
        internalPanel_.remove(index);
    }
    public void removeAll() {
        internalPanel_.removeAll();
    }

    public Component getComponent(int i) {
        return internalPanel_.getComponent(i);
    }
    public Component getComponentAt(int x, int y) {
        return internalPanel_.getComponentAt(x,y);
    }
    public Component getComponentAt(java.awt.Point p) {
        return internalPanel_.getComponentAt(p);
    }
    public int getComponentCount() {
        return internalPanel_.getComponentCount();
    }
    public Component[] getComponents() {
        return internalPanel_.getComponents();
    }
    
    public LayoutManager getLayout() {
        return internalPanel_.getLayout();
    }

    public void addContainerListener(ContainerListener l) {
        internalPanel_.addContainerListener(l);
    }
    public void removeContainerListener(ContainerListener l) {
        internalPanel_.removeContainerListener(l);
    }

    public synchronized void doLayout() {
        doingLayout_ = true;
        super.doLayout();
        doingLayout_ = false;
    }

    /**
     * Whether to animate the window when the size is changed.
     */
    public void setAnimateSetSize(boolean b) {
        animateSetSize_ = b;
    }

    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }
    
    public void setSize(int w, int h) {
        if (ADD_TO_SETSIZE) { w += 2; h += 2; }
        if (this.isVisible() && animateSetSize_) {
            SetSizeThread thread = new SetSizeThread(w,h);
            thread.start();
        }
        else {
            super.setSize(w,h);
        }
    }

    protected void superSetSize(int w, int h) {
        super.setSize(w,h);
    }
    
    private class SetSizeThread extends Thread {
        int w, h;
        public SetSizeThread(int w, int h) {
            this.w = w;
            this.h = h;
        }
        public void run() {
            final int STEPS = 5, DELAY = 15;
            int oldw = getSize().width, oldh = getSize().height;
            int diffw = w - oldw, diffh = h - oldh;
            double currw = oldw, currh = oldh;
            double incrw = diffw/(double)STEPS, incrh = diffh/(double)STEPS;
            try {
                for (int steps = 0; steps < STEPS; ++steps) {
                    currw += incrw;
                    currh += incrh;
                    superSetSize((int) currw, (int) currh);
                    Thread.sleep(DELAY);
                }
            }
            catch (InterruptedException ex) {}
            superSetSize(w,h);
        }
    }

    /*
    public void setVisible(boolean b) {
        Dimension d = getSize();
        super.setVisible(b);
        Dimension d2 = getSize();
        if (d.width != d2.width || d.height != d2.height) {
            boolean anim = animateSetSize_;
            animateSetSize_ = false;
            setSize(d);
            animateSetSize_ = anim;
        }
    }
    */
    
}





class AWTMenuBarCanvas extends Canvas implements MouseListener, MouseMotionListener {
    
    private static final int
    MARGIN_TOP = 2, MARGIN_BOTTOM = 6,
    MARGIN_LEFT = 10, MENU_SPACING = 25,
    MENU_TRIANGLE_SPACING = 2,
    TRIANGLE_WIDTH = 7, TRIANGLE_HEIGHT = 4;

    AWTMenuBar bar_;
    AWTFrame frame_;

    Image bufferImage_ = null;
    int bufferImageWidth_, bufferImageHeight_;

    public AWTMenuBarCanvas(AWTMenuBar bar, AWTFrame frame) {
        super();
        bar_ = bar;
        frame_ = frame;
        this.addMouseListener(this);
        for (int i = 0; i < bar_.getMenuCount(); ++i)
            this.add(((AWTMenu) bar_.getMenu(i)).getPopupMenu());
        setSize(getPreferredSize());
    }

    public void menuAdded(AWTMenu m) {
        this.add(m.getPopupMenu());
    }
    public void menuRemoved(AWTMenu m) {
        this.remove(m.getPopupMenu());
    }

    public void paint(Graphics g) {
        if (bufferImage_ == null ||
            getSize().width != bufferImageWidth_ ||
            getSize().height != bufferImageHeight_) {
            bufferImageWidth_ = getSize().width;
            bufferImageHeight_ = getSize().height;
            bufferImage_ = createImage(bufferImageWidth_, bufferImageHeight_);
        }
        if (bufferImage_ == null) return;
        Graphics bufg = bufferImage_.getGraphics();
        if ( Demo.javaVersionIsGreaterThanOrEqualTo(new int[]{1,2}) ) {
            Graphics2DSettings.setTextAntialiasing(bufg, true);
            Graphics2DSettings.setAntialiasing(bufg, true);
        }
        bufg.setColor(Color.white);
        bufg.fillRect(0, 0, bufferImageWidth_, bufferImageHeight_);
        bufg.setFont(bar_.getFont());
        FontMetrics metrics = bufg.getFontMetrics();
        int fontHeight = metrics.getHeight();
        int triangleTop = (fontHeight+3)/2 + MARGIN_TOP + 1;
        int x = MARGIN_LEFT;
        bufg.setColor(Color.black);
        for (int i = 0; i < bar_.getMenuCount(); ++i) {
            String name = bar_.getMenu(i).getLabel();
            // menu name
            bufg.drawString(name, x, MARGIN_TOP + fontHeight);
            // triangle
            int tx = x + metrics.stringWidth(name) + MENU_TRIANGLE_SPACING;
            bufg.fillPolygon(new int[]{tx,tx+TRIANGLE_WIDTH-1,tx+(TRIANGLE_WIDTH-1)/2},
                             new int[]{triangleTop, triangleTop, triangleTop + TRIANGLE_HEIGHT-1},
                             3);
            x += metrics.stringWidth(name) + MENU_SPACING;
        }
        bufg.drawLine(0, getSize().height - 1, getSize().width, getSize().height - 1);
        g.drawImage(bufferImage_, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public Dimension getPreferredSize() {
        FontMetrics metrics;
        Graphics g = this.getGraphics();
        if (g == null) {
            Image img = Demo.createImage(1,1);
            g = img.getGraphics();
            img.flush();
            img = null;
        }
        g.setFont(bar_.getFont());
        metrics = g.getFontMetrics();
        String str = "";
        for (int i = 0; i < bar_.getMenuCount(); ++i)
            str += bar_.getMenu(i).getLabel();
        return new Dimension(metrics.stringWidth(str) + bar_.getMenuCount()*MENU_SPACING + MARGIN_LEFT,
                             metrics.getHeight() + MARGIN_TOP + MARGIN_BOTTOM);
    }

    public void selectMenu(int x) {
        x = x - MARGIN_LEFT;
        Graphics g = this.getGraphics();
        if (g == null) return;
        g.setFont(bar_.getFont());
        FontMetrics metrics = g.getFontMetrics();
        int selectedMenu = -1;
        int popupMenuX = 0;
        int prevMenuX = -MENU_SPACING/2;
        for (int i = 0; i < bar_.getMenuCount(); ++i) {
            int menuX = prevMenuX + metrics.stringWidth(bar_.getMenu(i).getLabel()) + MENU_SPACING;
            if (x >= prevMenuX && x <= menuX) {
                selectedMenu = i;
                popupMenuX = prevMenuX + MENU_SPACING/2;
                break;
            }
            prevMenuX = menuX;
        }
        if (selectedMenu != -1) {
            AWTMenu menu = (AWTMenu) bar_.getMenu(selectedMenu);
            menu.getPopupMenu().show(this, popupMenuX + MARGIN_LEFT, getSize().height);
        }        
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
        selectMenu(e.getX());
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
   //     selectMenu(e.getX());
    }
    public void mouseMoved(MouseEvent e) {
    }

    
}



