//
//  TypeList.java
//  Demo
//
//  Created by David Eigen on Sun Apr 20 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

public class TypeList extends Type {

    private Type componentType_;
    
    /**
     * Creats a List type, with all components having the given type.
     * @param type the type of the components of this list
     */
    public TypeList(Type type) {
        super(LIST);
        componentType_ = type;
    }

    /**
     * @return the type of the components in this list
     */
    public Type componentType() {
        return componentType_;
    }



    public boolean isType(int type) {
        return type == LIST;
    }

    public boolean isType(Type type) {
        if (!(type instanceof TypeList))
            return false;
        return componentType_.isType(((TypeList) type).componentType());
    }

    public boolean compatibleType(Type type) {
        return isType(type);
    }

    public boolean containsType(int type) {
        if (type == LIST)
            return true;
        return componentType_.containsType(type);
    }

    public boolean containsType(Type type) {
        if (isType(type))
            return true;
        return componentType_.containsType(type);
    }

    public String toString() {
        return "list<" + componentType_.toString() + ">";
    }

}
