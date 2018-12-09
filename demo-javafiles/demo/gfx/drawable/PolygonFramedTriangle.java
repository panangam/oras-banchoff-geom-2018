package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

/**
 * A triangle in 3D that can be drawn.
 * The frame (the edges) of triangle are drawn.
 * Triangles can be filled when drawing with a Z buffer.
 *
 * @author deigen
 */
 public class PolygonFramedTriangle extends PolygonTriangle implements Drawable3D {


    /**
     * @param pt1 a vertex of the triangle
     * @param pt2 a vertex of the triangle
     * @param pt3 a vertex of the triabgle
     * @param color1 the color at vertex 1 of the triangle
     * @param color2 the color at vertex 2 of the triangle
     * @param color3 the color at vertex 3 of the triangle
     * @param vec1 the lighting vector at vertex 1 of the triangle (e.g. normal vector)
     * @param vec2 the lighting vector at vertex 2 of the triangle (e.g. normal vector)
     * @param vec3 the lighting vector at vertex 3 of the triangle (e.g. normal vector)
     * @param frameColor the color of the frame of the triangle, when drawing filled and framed
     */
    public PolygonFramedTriangle(Point pt1, Point pt2, Point pt3,
                                 DemoColor color1, DemoColor color2, DemoColor color3,
                                 LightingVector vec1, LightingVector vec2, LightingVector vec3,
                                 DemoColor frameColor) {
        super(pt1, pt2, pt3,
              color1, color2, color3,
              vec1, vec2, vec3,
              true, true, true,
              frameColor);
    }


}
