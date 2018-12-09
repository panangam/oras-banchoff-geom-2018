package demo.graph;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.io.*;
import demo.util.*;
import demo.depend.*;
import demo.exec.*;
import demo.Demo;
import demo.plot.Plot;
import demo.gfx.Ray;
import demo.gfx.RayIntersectable;
import demo.gfx.RayIntersection;
import demo.expr.Expression;
import demo.expr.ste.STEConstant;


/**
 * A point on the screen that the user can click and drag around.
 * The GraphCanvas handles the click and dragging of hotspots, and 
 * converting on-screen pixel values to abstract graph-space coordinates
 * The Hotspot class stores and can set the location of the point
 * in abstract graph-space coordinates.
 * In the future, it is possible that hotspots could have restrictions, so
 * the could only move one one particular line, for example.
 *
 * @author deigen
 */
public class Hotspot implements RayIntersectable, FileObject, Dependable {

    // points are in hotspot-space
    private STEConstant point_; // the point
    private STEConstant unconstrainedPoint_; // before applying constraints
    private ValueVector worldspacePoint_; // point_, in world-space

    // the worldspace origin all values in this hotspot have as an origin.
    // that is, subtracting origin_ brings worldspace to hotspot space
    // adding origin_ brings point_ to worldspace
    private ValueVector origin_;
    private Expression originExpr_;

    // constraint is in hotspot-space
    private Expression constraint_;
    // set of objects to constrain to
    private Set constraintObjects_ = new Set();
    // addition definitions for env the constraint should be interpreted in
    private Environment constraintExprDefs_ = new Environment();

    /** constants for constraint mode */
    public static final int
        NO_CONSTRAINTS = 0,
        CONSTRAIN_WITH_EXPR = 1,
        CONSTRAIN_TO_OBJS = 2;
    // how we are constraining
    private int constraintMode_ = NO_CONSTRAINTS;
    
    private int pointDimension_;
    
    /**
     * Creates a hotspot whose origin is at 0 and without constraints (that is, a constraint
     * where the constrained point is the same as the unconstrained point).
     * @param the dimension of the point for this hotspot
     * @param origin the expression for the origin. Should return a ValueVector of
     *        dimension pointDimension
     */
    public Hotspot(int pointDimension, Expression origin) {
        pointDimension_ = pointDimension;
        point_ = new STEConstant("", new ValueVector(pointDimension == 2 ? new double[]{0,0}
                                                                         : new double[]{0,0,0}));
        unconstrainedPoint_ = new STEConstant("Point",
                                    new ValueVector(pointDimension == 2 ? new double[]{0,0}
                                                                        : new double[]{0,0,0}));
        worldspacePoint_ = new ValueVector(pointDimension == 2 ? new double[]{0,0}
                                                                   : new double[]{0,0,0});
        constraintExprDefs_.put(unconstrainedPoint_.name(), unconstrainedPoint_);
        originExpr_ = origin;
        origin_ = (ValueVector) originExpr_.evaluate();
        constraint_ = Demo.recognizeExpression("Point", constraintExprDefs_);
        unconstrainedPoint_.setUserEditable(false);
        point_.setUserEditable(false);
        DependencyManager.setDependency(this, originExpr_);
        DependencyManager.setDependency(this, constraint_);
        DependencyManager.setDependency(point_, this);
    }

    /**
     * Sets the STEConstant for the hotspot-space constrained point of this hotspot
     */
    public void setPoint(STEConstant point) {
        DependencyManager.setDependency(point, this);
        DependencyManager.removeDependency(point_, this);
        point_ = point;
    }

    /**
     * Sets the constraint mode of this hotspot.
     * Updates to meet the constraint (if possible).
     */
    public void setConstraintMode(int mode) {
        constraintMode_ = mode;
        if (constraintMode_ != NO_CONSTRAINTS)
            setValue((ValueVector) point_.value(), null);
    }

    /**
     * Sets the expression for the origin of this hotspot's space.
     * @throws CircularException if there is a circular dependency. In this case,
     *         the origin of this hotspot is left unchanged.
     */
    public void setOriginExpr(Expression origin) {
        DependencyManager.setDependency(this, origin);
        originExpr_.dispose();
        originExpr_ = origin;
    }
    
    /**
     * Sets the expression for the constraint of this hotspot.
     * Applies the constraint to the current point and sets the current point.
     * @throws CircularException if there is a circular dependency. In this case,
     *         the constraint of this hotspot is left unchanged.
     */
    public void setConstraint(Expression constraint) {
        DependencyManager.setDependency(this, constraint);
        constraint_.dispose();
        constraint_ = constraint;
        if (constraintMode_ == CONSTRAIN_WITH_EXPR)
            setValue((ValueVector) point_.value(), null);
    }

    /**
     * Sets the set of objects for the constraint of this hotspot.
     * Applies the constraint (if in constrain by objects mode) to the
     * current point and sets the current point.
     * @throws CircularException if there is a circular dependency. In this case,
     *         the constraint of this hotspot is left unchanged.
     */
    public void setConstraintObjects(Set constraintObjs) {
        DependencyManager.removeDependencies(this, constraintObjects_.elements());
        DependencyManager.addDependencies(this, constraintObjs.elements());
        constraintObjects_ = constraintObjs;
        if (constraintMode_ == CONSTRAIN_TO_OBJS)
            setValue((ValueVector) point_.value(), null);
    }

    /**
     * @return whether this hotspot is inside the given box in world-space.
     * 	       The box is given by the min and max x, y, and z values (planes).
     */
    public boolean containedInBox(double minx, double miny, double minz,
                                  double maxx, double maxy, double maxz) {
        double[] value = worldspacePoint_.doubleVals();
        if (value.length == 2)
            return (minx <= value[0] && value[0] <= maxx) &&
                   (miny <= value[1] && value[1] <= maxy);
        else 
            return (minx <= value[0] && value[0] <= maxx) &&
                   (miny <= value[1] && value[1] <= maxy) &&
                   (minz <= value[2] && value[2] <= maxz);
    }
    
    /**
     * Sets the location of the hotspot to the given location (given in world-space).
     * If any constraints are on the hotspot, the location of the hotspot is set to
     * the constraint applied to the given point.
     * @param location the location to set the point to (with constraints applied) in world-space
     */
    public void setLocation(double x, double y, double z) {
        setLocation(new ValueVector(new double[]{x,y,z}));
    }
    
    /**
     * Sets the location of the hotspot to the given location (given in world-space).
     * If any non-object constraints are on the hotspot, the location of the hotspot is set to
     * the constraint applied to the given point.
     * @param location the location to set the point to (with constraints applied) in world-space
     */
    public void setLocation(ValueVector value) {
        setLocation(value, null);
    }

    /**
     * Same as setLocation(ValueVector)
     */
    public void setLocation(Value value) {
        if (value instanceof ValueVector)
            setLocation( (ValueVector) value );
        else
            throw new RuntimeException("Don't know how to set location to non-vector");
    }

    /**
     * Sets the location of the hotspot to the given location (given in world-space).
     * If any constraints are on the hotspot, the location of the hotspot is set to
     * the constraint applied to the given point.
     * @param location the location to set the point to (with constraints applied) in world-space
     * @param ray the ray from the eye point (used for constraints)
     */
    public void setLocation(ValueVector value, Ray ray) {
        // make value have the right dimensions
        double[] vals = value.doubleVals();
        if (value.dimension() == pointDimension_)
            value = value;
        else if (value.dimension() == 3 && pointDimension_ == 2)
            value = new ValueVector(new double[]{ vals[0], vals[1] });
        else if (value.dimension() == 2 && pointDimension_ == 3)
            value = new ValueVector(new double[]{ vals[0], vals[1], 0 });
        else
            throw new RuntimeException("Dimensions of point and given value not compatible.");
        // bring worldspace point to hotspot-space
        if (constraintMode_ == CONSTRAIN_WITH_EXPR)
            value = M.sub(value, origin_);
        // apply constraint and set the hotspot value
        setValue(value, ray);
    }
        
    /**
    * Sets the location of the hotspot to the given location (given in hotspot-space)
    * If any constraints are on the hotspot, the location of the hotspot is set to
    * the constraint applied to the given point.
    * @param location the location to set the point to (after constrains have been applied)
    *        in hotspot-space
    */
    public void setLocationHotspotSpace(Value value) {
        if (!(value instanceof ValueVector))
            throw new RuntimeException("Value for hotspot point must be a vector.");
        ValueVector vec = (ValueVector) value;
        // make value have the right dimensions
        if (vec.dimension() != pointDimension_)
            throw new RuntimeException("Dimensions of point and given value not compatible.");
        // apply constraint and set the hotspot value
        setValue(vec, null);
    }

    /**
     * @return the expression for the origin of this hotspot's space in CONSTRAIN_WITH_EXPR mode
     */
    public Expression originExpr() {
        return originExpr_;
    }

    /**
     * @return the expression for the constraint for CONSTRAIN_WITH_EXPR mode
     */
    public Expression constraint() {
        return constraint_;
    }

    /**
     * @return the set of objects this hotspot is constrained to in CONSTRAIN_TO_OBJS mode
     */
    public Set constraintObjects() {
        return constraintObjects_;
    }

    /**
     * @return the constraint mode of this hotspot
     */
    public int constraintMode() {
        return constraintMode_;
    }

    /**
     * @return the SymbolTableEntry for the constrained point
     */
    public STEConstant pointTableEntry() {
        return point_;
    }

    /**
     * @return the name of this hotspot
     */
    public String name() {
        return point_.name();
    }

    /**
     * @return the Environment containing the local definitions that the constraint
     *         expression should be interpreted in
     */
    public Environment constraintExprDefs() {
        return constraintExprDefs_;
    }

    /**
     * @return the location of this hotspot in hotspot-space
     */
    public Value location() {
        return point_.value();
    }

    /**
     * Disposes of this hotspot; disposes all expressions this hotspot uses.
     * Does not remove the table entry for this hotspot from any environment.
     */
    public void dispose() {
        if (originExpr_ != null) originExpr_.dispose();
        if (constraint_ != null) constraint_.dispose();
        DependencyManager.remove(this);
    }


    /**
     * Intersects this hotspot with a ray.
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        double[] p = M.point(worldspacePoint_.doubleVals());
        double t = M.length(M.sub(p, ray.p));
        double[] q1 = M.add(ray.p, M.mult(t, ray.v));
        double[] q2 = M.add(ray.p, M.mult(-t, ray.v));
        if (M.length(M.sub(p, q1)) < ray.tol) {
            intersection.set(ray, this, t, q1);
            return true;
        }
        if (M.length(M.sub(p, q2)) < ray.tol) {
            intersection.set(ray, this, -t, q2);
            return true;
        }
        return false;
    }


    private boolean intersectWithConstraintObjs(Ray ray, RayIntersection intersection) {
        RayIntersection i = new RayIntersection();
        boolean intersected = false;
        RayIntersectable obj;
        for (java.util.Enumeration objs = constraintObjects_.elements();
             objs.hasMoreElements();) {
            obj = (RayIntersectable) objs.nextElement();
            if (obj instanceof Plot)
                ((Plot) obj).ensureCalculated();
            if (obj.intersect(ray, i) && i.t < intersection.t) {
                intersection.set(i);
                intersected = true;
            }
        }
        return intersected;
    }
    
    private void setValue(ValueVector value, Ray ray) {
        unconstrainedPoint_.setValue(value);
        if (constraintMode_ == CONSTRAIN_WITH_EXPR) {
            Value ptVal = constraint_.evaluate();
            if ( !(ptVal instanceof ValueVector) )
                throw new RuntimeException("Constrained value is not a vector.");
            double[] ptValv = ((ValueVector) ptVal).doubleVals();
            if (ptValv.length == pointDimension_)
                ptVal = ptVal;
            else if (ptValv.length == 3 && pointDimension_ == 2)
                ptVal = new ValueVector(new double[]{
                    ptValv[0],
                    ptValv[1] });
            else if (ptValv.length == 2 && pointDimension_ == 3)
                ptVal = new ValueVector(new double[]{
                    ptValv[0],
                    ptValv[1],
                    0 });
            else
                throw new RuntimeException(
                            "Dimensions of point and constrained value not compatible.");
            Exec.begin_nocancel();
            point_.setValue(ptVal);
            worldspacePoint_ = M.add((ValueVector) ptVal, origin_);
            Exec.end_nocancel();
        }
        else if (constraintMode_ == CONSTRAIN_TO_OBJS) {
            RayIntersection intersection = new RayIntersection();
            if (ray == null) {
                // don't have a user ray: try a bunch of times and hopefully we'll intersect
                final int MAX_INTERSECT_TRIES = 15;
                double[] currPt = M.point(value.doubleVals());
                // first try principal directions (these stand a good chance)
                boolean intersected =
                    (pointDimension_ == 3 &&
                       intersectWithConstraintObjs(new Ray(currPt, new double[]{0,0,-1,0}),
                                                   intersection))
                    || intersectWithConstraintObjs(new Ray(currPt, new double[]{-1,0,0,0}),
                                                   intersection)
                    || intersectWithConstraintObjs(new Ray(currPt, new double[]{0,-1,0,0}),
                                                   intersection);
                if (!intersected) {
                    // didn't get principal direction: try some random dirs
                    for (int tries = 0; tries < MAX_INTERSECT_TRIES; ++tries) {
                        double[] v = new double[]{Math.random()-0.5, Math.random()-0.5,
                                                  pointDimension_ == 2 ? 0 : Math.random()-0.5, 0};
                        if (intersectWithConstraintObjs(new Ray(currPt, v), intersection))
                            break;
                    }
                }
            }
            else {
                intersectWithConstraintObjs(ray, intersection);
            }
            if (intersection.valid) {
                ValueVector p;
                if (pointDimension_ == 2)
                    p = new ValueVector(new double[]{ intersection.p[0],
                                                      intersection.p[1] });
                else if (pointDimension_ == 3)
                    p = new ValueVector(new double[]{ intersection.p[0],
                                                      intersection.p[1],
                                                      intersection.p[2] });
                else throw new RuntimeException("INTERNAL ERROR: dimension must be 2 or 3");
                Exec.begin_nocancel();
                point_.setValue(p);
                worldspacePoint_ = p;
                Exec.end_nocancel();
            }
        }
        else {
            Exec.begin_nocancel();
            point_.setValue(value);
            worldspacePoint_ = new ValueVector(value.doubleVals());
            Exec.end_nocancel();
        }
    }


    public void dependencyUpdateVal(Set updatingObjects) {
        // origin expression or constraint updated
        if (updatingObjects.contains(originExpr_) ) {
            ValueVector o = (ValueVector) originExpr_.evaluate();
            ValueVector w = M.add((ValueVector) point_.value(), o);
            Exec.begin_nocancel();
            origin_ = o;
            worldspacePoint_ = w;
            Exec.end_nocancel();
        }
        if ( (constraintMode_ == CONSTRAIN_WITH_EXPR && updatingObjects.contains(constraint_)) ||
             (constraintMode_ == CONSTRAIN_TO_OBJS && updatingObjects.containsAny(constraintObjects_)) ) {
            setValue((ValueVector) point_.value(), null);
        }
    }

    public void dependencyUpdateDef(Set updatingObjects) {
        dependencyUpdateVal(updatingObjects);
    }
    
        

    // ****************************** FILE I/O ****************************** //

    String[] constraintObjs__;
    String point__, originExpr__, constraint__;
    
    public Hotspot(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        point__ = parser.parseWord(props.get("entry"));
        if (props.contains("cmode") && props.contains("cobjs")) {
            // new version
            constraintMode_ = (int) parser.parseNumber(props.get("cmode"));
            constraintObjs__ = parser.parseObjectList(props.get("cobjs"));
        }
        else {
            // for slightly older version
            constraintMode_ = CONSTRAIN_WITH_EXPR;
            constraintObjs__ = null;
        }
        if (props.contains("constraint") && props.contains("origin")) {
            // both slightly older and new version
            originExpr__ = parser.parseExpression(props.get("origin"));
            constraint__ = parser.parseExpression(props.get("constraint"));
        }
    }

    public void loadFileBind(FileParser parser) {
        constraintObjects_ = new Set();
        if (constraintObjs__ != null)
            for (int i = 0; i < constraintObjs__.length; ++i)
                constraintObjects_.add(parser.getObject(constraintObjs__[i]));
        DependencyManager.addDependencies(this, constraintObjects_.elements());
    }

    public void loadFileExprs(FileParser parser) {
        point_ = (STEConstant) parser.currEnvLookup(point__);
        point_.setUserEditable(false);
        pointDimension_ = ((ValueVector) point_.value()).dimension();
        unconstrainedPoint_ = new STEConstant("Point",
                                              point_.value());
        unconstrainedPoint_.setUserEditable(false);
        constraintExprDefs_.put(unconstrainedPoint_.name(), unconstrainedPoint_);
        if (originExpr__ != null && constraint__ != null) {
            originExpr_ = parser.recognizeExpression(originExpr__);
            parser.pushEnvironment(parser.currEnvironment().append(constraintExprDefs_));
            constraint_ = parser.recognizeExpression(constraint__);
            parser.popEnvironment();
        }
        else {
            // really old version
            String zeroStr = "";
            for (int i = 0; i < pointDimension_; ++i)
                zeroStr += "0" + (i < pointDimension_ - 1 ? "," : "");
            originExpr_ = Demo.recognizeExpression(zeroStr, new Environment());
            constraint_ = Demo.recognizeExpression("Point", constraintExprDefs_);
        }
        DependencyManager.setDependency(this, originExpr_);
        DependencyManager.setDependency(this, constraint_);
        DependencyManager.setDependency(point_, this);
    }

    public void loadFileFinish(FileParser parser) {
        origin_ = (ValueVector) originExpr_.evaluate();
        worldspacePoint_ = M.add((ValueVector) point_.value(), origin_);
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("entry", generator.generateWord(point_.name()));
        // the following are added in the newer version of Hotspot
        props.add("origin", generator.generateExpression(originExpr_));
        props.add("constraint", generator.generateExpression(constraint_));
        props.add("cobjs", generator.generateObjectIDList(constraintObjects_.elements()));
        props.add("cmode", generator.generateNumber(constraintMode_));
        return generator.generateProperties(props);
    }

    

    // *** IMPLEMENTATION FOR DEPENDABLE *** //
    private DependencyNode __myDependencyNode__ = new DependencyNode(this);
    public DependencyNode dependencyNode() { return __myDependencyNode__; }

    
    
}
