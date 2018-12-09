package demo.graph;

public class MenuItemGroup extends Object{

    public  MenuItemGroup() {
        super();
    }

    public  void addItem( java.awt .CheckboxMenuItem item ) {
        if ( items .size() == 0 ) {
            selected = item;
            item .setState( true );
        }
        else {
            item .setState( false );
        }
        items .put( item, item );
    }

    public  void selectItem( java.awt .CheckboxMenuItem item ) {
        if ( items .get( item ) == null ) {
            throw new RuntimeException( "INTERNAL ERROR: Menu item to be selected is not part of the group." );
        }
        selected .setState( false );
        item .setState( true );
        selected = item;
    }

    public  java.awt.CheckboxMenuItem selectedItem() {
        return selected;
    }

    private 
    java.util .Dictionary items = new java.util .Hashtable( 7 );

    private  java.awt.CheckboxMenuItem selected = null;


}


