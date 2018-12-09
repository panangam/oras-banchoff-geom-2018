package demo.graph;

import java.awt .*;

import demo.util.Set;
import demo.ui.DemoFrame;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * GraphFrame is the superclass for all frames that draw graphs in them.
 *
 * @author deigen
 */
public class GraphFrame extends DemoFrame implements Dependable {

    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    
    public  GraphFrame( String title ) {
        super( title );
    }

    public  GraphFrame( ) {
    }

    public void dependencyUpdateDef(Set updatingObjects) {
        // to be overridden by subclasses
    }
    
    public void dependencyUpdateVal(Set updatingObjects) {
        // to be overridden by subclasses
    }
    
    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }


}


