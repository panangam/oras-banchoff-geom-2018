package demo.ui;
//
//  EditVisibleWindowCallback.java
//  Demo
//
//  Created by David Eigen on Tue Jul 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

public interface EditVisibleWindowCallback {

    public boolean isObjectVisible(Object obj);

    public void setObjectVisible(Object obj, boolean vis);

    public String getTitle(Object obj);

    public void editVisibleWindowClosed(EditVisibleWindow win);
    
}
