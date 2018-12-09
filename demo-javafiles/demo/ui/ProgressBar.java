package demo.ui;

import java.awt.*;

public class ProgressBar extends Canvas implements Runnable {

    private boolean indeterminate = false;
    private boolean runningIndeterminate = false;
    private int indeterminateProgress = 0;
    
    private Thread indeterminateThread = null;
    
    private int max = 100;
    private int progress = 0;
    
    private int MARGIN_WIDTH = 5;
    private int HEIGHT = 15;
    private int INDETERMINATE_STRIP_WIDTH = 8;


    public ProgressBar() {
    }
    
    public int getBarHeight() {
        return HEIGHT;
    }

    public void paint(Graphics g) {
        if (g == null || this.size().width <= 0 || this.size().height <= 0)
            return;
        Image bufferImg = createImage(size().width, size().height);
        Graphics bufferG = bufferImg.getGraphics();
        bufferG.clearRect(0, 0, size().width, size().height);
        bufferG.setColor(java.awt.Color.black);
        bufferG.drawRect( MARGIN_WIDTH, size().height / 2 - HEIGHT / 2,
                          size().width - MARGIN_WIDTH * 2, HEIGHT );
        bufferG.setClip(MARGIN_WIDTH, size().height / 2 - HEIGHT / 2,
                          size().width - MARGIN_WIDTH * 2, HEIGHT );
        if (this.indeterminate) {
            for (int x = -(HEIGHT+INDETERMINATE_STRIP_WIDTH)*2 + indeterminateProgress;
                 x < size().width;
                 x += INDETERMINATE_STRIP_WIDTH*2)
                bufferG.fillPolygon(new int[]{
                                        x,
                                        x+INDETERMINATE_STRIP_WIDTH,
                                        x+INDETERMINATE_STRIP_WIDTH+HEIGHT,
                                        x+HEIGHT },
                                    new int[]{
                                        size().height / 2 + HEIGHT / 2 + (HEIGHT%2),
                                        size().height / 2 + HEIGHT / 2 + (HEIGHT%2),
                                        size().height / 2 - HEIGHT / 2,
                                        size().height / 2 - HEIGHT / 2 },
                                    4);
            indeterminateProgress = (indeterminateProgress + 1) % (INDETERMINATE_STRIP_WIDTH*2);
        }
        else {
            if (max == 0)
                bufferG.fillRect(MARGIN_WIDTH, size().height / 2 - HEIGHT / 2,
                                 size().width - MARGIN_WIDTH * 2, HEIGHT);
            else
                bufferG.fillRect(MARGIN_WIDTH, size().height / 2 - HEIGHT / 2,
                                 (size().width - MARGIN_WIDTH * 2) * progress / max, HEIGHT);
        }
        g.drawImage(bufferImg, 0, 0, this);
    }
    
    public void update(Graphics g) {
        paint(g);
    }

    public void setIndeterminate(boolean b) {
        if (b == this.indeterminate) return;
        this.indeterminate = runningIndeterminate = b;
        if (this.indeterminate) {
            indeterminateProgress = 0;
            indeterminateThread = new Thread(this);
            indeterminateThread.start();
        }
        else {
            indeterminateThread.stop();
            indeterminateThread = null;
        }
    }
    
    public void reset(int max) {
        this.max = max;
        this.progress = 0;
        paint(this.getGraphics());
    }
    
    public void increment() {
        if (progress < max) {
            ++progress;
            paint(this.getGraphics());
        }
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
        paint(this.getGraphics());
    }

    public void run() {
        while (runningIndeterminate) {
            paint(this.getGraphics());
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }

}