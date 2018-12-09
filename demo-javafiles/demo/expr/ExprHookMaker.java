//
//  ExprHookMaker.java
//  Demo
//
//  Created by David Eigen on Wed Apr 02 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.expr;

import mathbuild.Operator;
import mathbuild.Executor;
import mathbuild.type.*;
import mathbuild.value.*;
import mathbuild.MB;

import java.lang.reflect.*;

import demo.gfx.Point;


public class ExprHookMaker {

    
    private Class
        MB_VALUE_CLASS, MB_VALUE_SCALAR_CLASS,
        MB_VALUE_VECTOR_CLASS, MB_VALUE_MATRIX_CLASS,
        MB_VALUE_LIST_CLASS, POINT_CLASS;


    private static final ExprHookMaker instance_ = new ExprHookMaker();
    private ExprHookMaker() {
        try {
            MB_VALUE_CLASS        = Class.forName("mathbuild.value.Value");
            MB_VALUE_SCALAR_CLASS = Class.forName("mathbuild.value.ValueScalar");
            MB_VALUE_VECTOR_CLASS = Class.forName("mathbuild.value.ValueVector");
            MB_VALUE_MATRIX_CLASS = Class.forName("mathbuild.value.ValueMatrix");
            MB_VALUE_LIST_CLASS   = Class.forName("mathbuild.value.ValueList");
            POINT_CLASS           = Class.forName("demo.gfx.Point");
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException("Class not found.");
        }
    }
    
    

    /**
     * Makes a hook Executor that calls the given method on the given object.
     * @param obj the object to call the method on
     * @param methodDecl declaration of the (public) method, in the form
     *        <return_type> <name>(<arg_types>)
     *        for example, "Point getVertex(int)"
     *        As of now, the following types can be used (n or m should be a numeric integer):
     *        double, int, boolean, int[n], double[n], double[n][m], Point, Point<n>,
     *        ValueScalar, ValueVector<n>, ValueMatrix<n,m>,
     *        index, index[n]  Note: index, index[n] are same as int, int[n], only
     *                               0 in Demo is converted to 1 in Mathbuild, and vice versa.
	 *        Note: Square brackets (as in double[n]) mean array, and angled brackets 
	 *              (as in Point<n>) mean dimension.
     */
    public static Executor makeHook(Object obj, String methodDecl) {
        return instance_.makeHookImpl(obj, methodDecl);
    }

    /**
     * Makes a hook executor that uses the given operator on its arguments.
     * If does not expect any parameters, makeHook(Operator) may be used,
     * or types can be have length zero or be null.
     * @param types the expected types of the arguments to the operator
     * @param op the operator that should be used
     */
    public static Executor makeHook(Type[] types, Operator op) {
        if (types == null) types = new Type[0];
        return MB.makeHook(types, op);
    }

    /**
     * Makes a hook executor that uses the given operator.
     * The operator should not be expecting any arguments.
     * @param op the operator that should be used
     */
    public static Executor makeHook(Operator op) {
        return makeHook(null, op);
    }



    private Executor makeHookImpl(Object obj, String methodDecl) {
        int space = methodDecl.indexOf(' ');
        int oparen = methodDecl.indexOf('(');
        int cparen = methodDecl.indexOf(')');
        if (oparen == -1 || cparen != methodDecl.length() - 1)
            throw new RuntimeException("Malformed method declaration.");
        String returnTypeStr = methodDecl.substring(0,space).trim();
        String methodName = methodDecl.substring(space+1, oparen).trim();
        String methodArgsStr = methodDecl.substring(oparen+1, cparen).trim();
        java.util.Vector methodArgsStrs = new java.util.Vector();
        int comma;
        while ((comma = methodArgsStr.indexOf(",")) != -1) {
            methodArgsStrs.addElement(methodArgsStr.substring(0, comma).trim());
            methodArgsStr = methodArgsStr.substring(comma+1).trim();
        }
        if (methodArgsStr.length() > 0)
            methodArgsStrs.addElement(methodArgsStr.trim());
        int n = methodArgsStrs.size();
        Class[] argClasses = new Class[n];
        Type[] argTypes = new Type[n];
        Converter[] argConvertersToDemo = new Converter[n];
        Converter[] argConvertersToMB = new Converter[n];
        for (int i = 0; i < n; ++i) {
            Object[] stuff = recognizeType((String) methodArgsStrs.elementAt(i));
            argClasses[i] =          stuff[0] == null ? null : (Class) stuff[0];
            argTypes[i] =            stuff[1] == null ? null : (Type) stuff[1];
            argConvertersToDemo[i] = stuff[2] == null ? null : (Converter) stuff[2];
            argConvertersToMB[i] =   stuff[3] == null ? null : (Converter) stuff[3];
        }
        if (argClasses.length == 0) argClasses = null;
        Method method;
        try {
            method = obj.getClass().getMethod(methodName, argClasses);
        }
        catch (NoSuchMethodException ex) {
            throw new RuntimeException("Method not found: " + methodDecl);
        }
        Object[] retstuff = recognizeType(returnTypeStr);
        Class retClass = (Class) retstuff[0];
        Type retType = (Type) retstuff[1];
        Converter retConverterToDemo = (Converter) retstuff[2];
        Converter retConverterToMB = (Converter) retstuff[3];
        if ( !( retClass.isAssignableFrom(method.getReturnType())
                || method.getReturnType().isAssignableFrom(retClass) ) )
            throw new RuntimeException("return type not compatible with declaration return type");
        Operator op = new OpMethodInvoke(method, obj, retType,
                                         retConverterToMB, argConvertersToDemo);
        return makeHook(argTypes, op);
    }        

    
    // returns [Class, Type, Converter (to Demo), Converter (to MB)]
    private Object[] recognizeType(String typeStr) {
        int arrays = 0;
        Class c = null;
        Type type = null;
        Converter converterMB = null, converterD = null; // MB goes to MB, D goes to Demo
        boolean convertIndices = false; // whether to add 1 in the converter, for index conversion
        java.util.Vector arraySizes = new java.util.Vector();
        typeStr = typeStr.trim();
        while (typeStr.indexOf('[') != -1) {
            ++arrays;
            int open = typeStr.lastIndexOf('[');
            int close = typeStr.lastIndexOf(']');
            if (open == close - 1)
                arraySizes.addElement(new Integer(-1));
            else
                arraySizes.addElement(Integer.valueOf(typeStr.substring(open+1,close).trim()));
            typeStr = typeStr.substring(0, open).trim();
        }
        while (typeStr.indexOf('<') != -1) {
            int open = typeStr.lastIndexOf('<');
            int close = typeStr.lastIndexOf('>');
            while (typeStr.substring(0,open).lastIndexOf('<')
                   > typeStr.substring(0,open).lastIndexOf('>'))
                open = typeStr.substring(0,open).lastIndexOf('<');
            if (open == close - 1)
                arraySizes.addElement(new Integer(-1));
            else {
                String numstr = typeStr.substring(open+1, close).trim();
                do {
                    int i = numstr.indexOf(',');
                    if (i == -1) i = numstr.length();
                    try {
                        arraySizes.addElement(Integer.valueOf(numstr.substring(0, i).trim()));
                    }
                    catch (NumberFormatException ex) {
                        arraySizes.addElement(numstr.substring(0,i).trim());
                    }
                    if (i == numstr.length())
                        numstr = "";
                    else
                        numstr = numstr.substring(i+1).trim();
                } while (numstr.length() > 0);
            }
            typeStr = typeStr.substring(0, open).trim();
        }
        typeStr = typeStr.trim();
        if (arrays > 0) {
            if(typeStr.equals("boolean")) typeStr = "Z";
            else if(typeStr.equals("byte")) typeStr = "B";
            else if(typeStr.equals("char")) typeStr = "C";
            else if(typeStr.equals("short")) typeStr = "S";
            else if(typeStr.equals("int")) typeStr = "I";
            else if(typeStr.equals("long")) typeStr = "J";
            else if(typeStr.equals("float")) typeStr = "F";
            else if(typeStr.equals("double")) typeStr = "D";
            else if(typeStr.equals("index")) {typeStr = "I"; convertIndices = true;}
            else typeStr = "L" + typeStr + ";";
            for (int i = 0; i < arrays; ++i)
                typeStr = "[" + typeStr;
        }
        if(typeStr.equals("boolean")) c = Boolean.TYPE;
        else if(typeStr.equals("byte")) c = Byte.TYPE;
        else if(typeStr.equals("char")) c = Character.TYPE;
        else if(typeStr.equals("short")) c = Short.TYPE;
        else if(typeStr.equals("int")) c = Integer.TYPE;
        else if(typeStr.equals("long")) c = Long.TYPE;
        else if(typeStr.equals("float")) c = Float.TYPE;
        else if(typeStr.equals("double")) c = Double.TYPE;
        else if(typeStr.equals("index")) {c = Integer.TYPE; convertIndices = true;}
        else {
            final String[] prefixes = new String[]{"", "demo.", "demo.gfx.", "mathbuild.value.", "java.lang."};
            boolean classFound = false;
            int prefixIx = 0;
            while (!classFound && prefixIx < prefixes.length) {
                try {
                    int bracket = typeStr.lastIndexOf('[');
                    if (bracket == -1) bracket = -2;
                    String typeStr2 =
                        typeStr.substring(0,bracket+2)
                        + prefixes[prefixIx]
                        + typeStr.substring(bracket+2);
                    c = Class.forName(typeStr2);
                    classFound = true;
                }
                catch (ClassNotFoundException ex) {
                    ++prefixIx;
                }
            }
            if (!classFound)
                throw new RuntimeException("Class not found: " + typeStr);
        }
        boolean validType = false;
        // we now have the class c, and the array sizes (if any)


        // ******* TO ADD NEW TYPES THAT CAN BE RECOGNIZED, EDIT ******* //
        // ******* THE CODE AFTER THIS -- YOU WILL PROBABLY JUST ******* //  
        // ******* HAVE TO ADD AN IF AT THE END, SIMILAR TO THE  ******* //
        // ******* OTHER IF STATEMENTS.                          ******* //

        // The following code checks if the class c is one of the classes we are
        // dealing with, and sets up the appropriate converter objects.

        // Value
        if (MB_VALUE_CLASS.isAssignableFrom(c)) {
            // no conversion necessary
            validType = true;
            converterMB = converterD = new Converter(){
                public Object convert(Object obj){return obj;}
            };
            if (MB_VALUE_SCALAR_CLASS.isAssignableFrom(c)) {
                type = MB.TYPE_SCALAR;
            }
            else if (MB_VALUE_VECTOR_CLASS.isAssignableFrom(c)) {
                int size = -1;
                if (arraySizes.size() == 1)
                    size = ((Integer) arraySizes.elementAt(0)).intValue();
                if (size != -1)
                    type = new TypeVector(size);
            }
            else if (MB_VALUE_MATRIX_CLASS.isAssignableFrom(c)) {
                int sizei = -1, sizej = -1;
                if (arraySizes.size() == 2) {
                    sizei = ((Integer) arraySizes.elementAt(0)).intValue();
                    sizej = ((Integer) arraySizes.elementAt(1)).intValue();
                }
                if (sizei != -1 && sizej != -1)
                    type = new TypeMatrix(sizei,sizej);
            }
            else if (MB_VALUE_LIST_CLASS.isAssignableFrom(c)) {
                Object[] comptypeStuff = recognizeType((String) arraySizes.elementAt(0));
                final Class compc = (Class) comptypeStuff[0];
                final Type compt = (Type) comptypeStuff[1];
                final Converter compToD = (Converter) comptypeStuff[2];
                final Converter compToMB = (Converter) comptypeStuff[3];
                validType = true;
                type = new TypeList(compt);
                /* these converters not needed for vals. Use them if we make arrays in general
                converterD = new Converter(){
                    public Object convert(Object obj){
                        Value[] vals = ((ValueList) obj).values();
                        Object[] objs = new Object[vals.length];
                        for (int i = 0; i < vals.length; ++i)
                            objs[i] = compToD.convert(vals[i]);
                        return objs;
                    }
                };
                converterMB = new Converter(){
                    public Object convert(Object obj){
                        Object[] objs = (Object[]) obj;
                        Value[] vals = new Value[objs.length];
                        for (int i = 0; i < objs.length; ++i)
                            vals[i] = (Value) compToMB.convert(objs[i]);
                        return new ValueList(vals, compt);
                    }
                };
                 */
            }
            else {
                throw new RuntimeException("Only scalar, vector, matrix supported so far.");
            }
        }
        // Primitive type: double, int (either index or not index), or boolean
        if (c.isPrimitive()) {
            // primitive: so, return type is a scalar
            validType = true;
            type = MB.TYPE_SCALAR;
            if (c == Integer.TYPE) {
                if (convertIndices) {
                    converterMB = new Converter(){
                        public Object convert(Object obj){
                            return new ValueScalar(((Integer) obj).doubleValue() + 1);
                        }
                    };
                    converterD = new Converter(){
                        public Object convert(Object obj){
                            return new Integer((int) ((ValueScalar) obj).number() - 1);
                        }
                    };
                }
                else {
                    converterMB = new Converter(){
                        public Object convert(Object obj){
                            return new ValueScalar(((Integer) obj).doubleValue());
                        }
                    };
                    converterD = new Converter(){
                        public Object convert(Object obj){
                            return new Integer((int) ((ValueScalar) obj).number());
                        }
                    };
                }
            }
            else if (c == Double.TYPE) {
                converterMB = new Converter(){
                    public Object convert(Object obj){
                        return new ValueScalar(((Double) obj).doubleValue());
                    }
                };
                converterD = new Converter(){
                    public Object convert(Object obj){
                        return new Double(((ValueScalar) obj).number());
                    }
                };
            }
            else if (c == Boolean.TYPE) {
                converterMB = new Converter(){
                    public Object convert(Object obj){
                        return new ValueScalar(((Boolean) obj).booleanValue() ? 1 : 0);
                    }
                };
                converterD = new Converter(){
                    public Object convert(Object obj){
                        return new Boolean(((ValueScalar) obj).number() > 0);
                    }
                };
            }
            else validType = false;
        }
        // int[], index[], double[], or double[][]
        if (c.isArray()) {
            Class cc = c.getComponentType();
            if (cc.isArray()) {
                Class ccc = cc.getComponentType();
                if (!ccc.isPrimitive())
                    throw new RuntimeException("only double[] and double[][] supported.");
                validType = true;
                int sizei = -1, sizej = -1;
                if (arraySizes.size() == 2) {
                    sizei = ((Integer) arraySizes.elementAt(0)).intValue();
                    sizej = ((Integer) arraySizes.elementAt(1)).intValue();
                }
                if (sizei != -1 && sizej != -1)
                    type = new TypeMatrix(sizei,sizej);
                if (ccc == Double.TYPE) {
                    converterMB = new Converter(){
                        public Object convert(Object obj) {
                            return new ValueMatrix((double[][]) obj);
                        }
                    };
                    converterD = new Converter(){
                        public Object convert(Object obj) {
                            return ((ValueMatrix) obj).doubleVals();
                        }
                    };
                }
                else {
                    throw new RuntimeException("double[][] is the only recognized 2D array type");
                }
            }
            else {
                if (!cc.isPrimitive())
                    throw new RuntimeException("unsupported type.");
                validType = true;
                int size = -1;
                if (arraySizes.size() == 1)
                    size = ((Integer) arraySizes.elementAt(0)).intValue();
                if (size != -1)
                    type = new TypeVector(size);
                if (cc == Double.TYPE) {
                    converterMB = new Converter(){
                        public Object convert(Object obj) {
                            return new ValueVector((double[]) obj);
                        }
                    };
                    converterD = new Converter(){
                        public Object convert(Object obj) {
                            return ((ValueVector) obj).doubleVals();
                        }
                    };
                }
                else if (cc == Integer.TYPE) {
                    if (convertIndices) {
                        converterMB = new Converter(){
                            public Object convert(Object obj) {
                                int[] is = (int[]) obj;
                                double[] ds = new double[is.length];
                                for (int i = 0; i < is.length; ++i)
                                    ds[i] = is[i] + 1;
                                return new ValueVector(ds);
                            }
                        };
                        converterD = new Converter(){
                            public Object convert(Object obj) {
                                double[] ds = ((ValueVector) obj).doubleVals();
                                int[] is = new int[ds.length];
                                for (int i = 0; i < is.length; ++i)
                                    is[i] = (int) ds[i] - 1;
                                return is;
                            }
                        };
                    }
                    else {
                        converterMB = new Converter(){
                            public Object convert(Object obj) {
                                int[] is = (int[]) obj;
                                double[] ds = new double[is.length];
                                for (int i = 0; i < is.length; ++i)
                                    ds[i] = is[i];
                                return new ValueVector(ds);
                            }
                        };
                        converterD = new Converter(){
                            public Object convert(Object obj) {
                                double[] ds = ((ValueVector) obj).doubleVals();
                                int[] is = new int[ds.length];
                                for (int i = 0; i < is.length; ++i)
                                    is[i] = (int) ds[i];
                                return is;
                            }
                        };
                    }
                }
                else {
                    throw new RuntimeException("int[], index[], and double[] are the only recognized 1D array types");
                }
            }
        }
        // Point
        if (POINT_CLASS.isAssignableFrom(c)) {
            if (arraySizes.size() == 0) {
                validType = true;
                type = new TypeVector(3);
                converterMB = new Converter(){
                    public Object convert(Object obj) {
                        return new ValueVector(((Point) obj).untransformedCoords);
                    }
                };
                converterD = new Converter(){
                    public Object convert(Object obj) {
                        return new Point(((ValueVector) obj).doubleVals(), 0);
                    }
                };
            }
            else if (arraySizes.size() == 1) {
                validType = true;
                final int dim = ((Integer) arraySizes.elementAt(0)).intValue();
                type = new TypeVector(dim);
                converterMB = new Converter(){
                    public Object convert(Object obj) {
                        double[] cs = ((Point) obj).untransformedCoords;
                        double[] v = new double[dim];
                        if (dim <= cs.length) {
                            for (int i = 0; i < dim; ++i)
                                v[i] = cs[i];
                        }
                        else {
                            int i;
                            for (i = 0; i < cs.length; ++i)
                                v[i] = cs[i];
                            for (; i < dim; ++i)
                                v[i] = 0;
                        }
                        return new ValueVector(v);
                    }
                };
                converterD = new Converter(){
                    public Object convert(Object obj) {
                        return new Point(((ValueVector) obj).doubleVals(), 0);
                    }
                };
            }
        }
        if (!validType)
            throw new RuntimeException("Invalid type.");
        return new Object[]{c, type, converterD, converterMB};
    }


}





interface Converter {
	public Object convert(Object obj);
}




class OpMethodInvoke implements Operator {
	
	private Object object_;
	private Method method_;
	private Type returnType_;
	private Converter toMB_;
	private Converter[] toDemo_;
	
	public OpMethodInvoke(Method m, Object obj, Type retType,
						  Converter toMB, Converter[] toDemo) {
		method_ = m;
		object_ = obj;
		returnType_ = retType;
		toMB_ = toMB;
		toDemo_ = toDemo;
	}
	
	public Type type() {
		return returnType_;
	}
	
	public Value operate(Value[] vals) {
		Object[] objs = null;
		if (vals != null) {
			objs = new Object[vals.length];
			for (int i = 0; i < objs.length; ++i)
				objs[i] = toDemo_[i].convert(vals[i]);
		}
		try {
			return (Value) toMB_.convert(method_.invoke(object_, objs));
		}
		catch (IllegalAccessException ex) {throw new RuntimeException(ex.toString());}
		catch (IllegalArgumentException ex) {throw new RuntimeException(ex.toString());}
		catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (targetEx instanceof RuntimeException)
				throw (RuntimeException) targetEx;
			throw new RuntimeException(targetEx.toString());
		}
	}
	
}


