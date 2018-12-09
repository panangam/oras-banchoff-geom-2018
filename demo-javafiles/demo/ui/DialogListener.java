package demo.ui;

import java.awt.Window;

public interface DialogListener {

    public void dialogCanceled( Window dialog );

    public void dialogOKed( Window dialog );
    
}
