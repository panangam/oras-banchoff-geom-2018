package demo.ui;

public class Graphics2DSettings {

    public static void setAntialiasing( java.awt.Graphics g, boolean b ) {
        ((java.awt.Graphics2D) g).setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, 
                b ? java.awt.RenderingHints.VALUE_ANTIALIAS_ON 
                  : java.awt.RenderingHints.VALUE_ANTIALIAS_OFF );
    }
    
    public static void setTextAntialiasing( java.awt.Graphics g, boolean b ) {
        ((java.awt.Graphics2D) g).setRenderingHint( java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, 
                b ? java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON 
                  : java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
    }
    
    public static void setLineThickness( java.awt.Graphics g, float thickness ) {
        ((java.awt.Graphics2D) g).setStroke( new java.awt.BasicStroke( thickness ) );
    }
    
    public static void setRenderingToSpeed( java.awt.Graphics g ) {
        ((java.awt.Graphics2D) g).setRenderingHint(
                    java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_SPEED);
    }

}
