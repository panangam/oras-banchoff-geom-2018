## Built-in functions

- cos
- sin
- tan
- pi
- true
- false
- random
- ln
- abs
- sign
- acos
- asin
- atan
- cosh
- sinh
- tanh
- sqrt
- cross
- dot
- map
- transpose
- not
- fpart
- ipart
- mod
- grad
- length

                    ########## GENERAL FUNCTIONS #####

###### scale : Range --> R
###### a Range is anything with a start, end, and resolution (like an interval or variable)
###### scale linearly maps a range to [0,1] where 0 corresponds to the start of the range,
###### and 1 corresponds to the end
scale(x) = (x - x_min) / (x_max - x_min)

###### clamp : (R,R) --> (R -> R)
###### clamp(a,b) is a function that clamps to the range [a,b], where a < b.
###### That is, for a real number x, clamp(a,b)(x) clamps x to [a,b].
clamp(a,b) = func(x){ if (x < a) then a else {if (x > b) then b else x} }

###### arg : (R,R) --> R
###### arg(x,y) is the counter-clockwise angle the vector (x,y) makes from (1,0), 
###### between 0 and 2*pi
arg(x,y) = if ((x,y) = (0,0)) then {0} else { if (x < 0) then { atan(y/x) + pi } else { if (y < 0) then {atan(y/x)+2*pi} else {atan(y/x)} } }

###### I : any --> any
###### Identity function.
I(x) = x

###### Basis2 : R^n, R^n --> (R^n -> R^2)
###### Given two linearly independent vectors a and b, Basis2 creates the frame (E1, E2), 
###### where E1 is the unit vector in the direction of a, and E2 is the unit vector orthogonal
###### to E1 in the direction that b points. In other words, Basis2 does a Gram-Schmidt 
###### orthonormalization of (a,b). Basis2 then returns a function that maps R^n to R^2 such
###### a given point x in R^n is mapped to a point in R^2 whose first coordinate is dot(x,E1)
###### and whose second coordinate is dot(x,E2). In other words, the function maps a point
###### x to its projection in the E1,E2 plane, and returns this projection expressed as a point
###### in the E1,E2 coordinate system.
Basis2(a,b) = let [E1 = unit(a)] [E2 = unit(b-dot(b,E1)*E1)] { func(x) {dot(x,E1), dot(x,E2)} }

###### Basis3 : R^n, R^n, R^n --> (R^n -> R^3)
###### Same as Basis2, but makes a basis of 3-space. Can be used to map to 3-space or for
###### redefining the basis. Given three vectors a, b and c, Basis3 returns a function
###### that maps a point x to its projection in the a, b, c space. It expresses this
###### projection in the orthonormal coordinate system obtained from a, b and c.
Basis3(a,b,c) = let [E1 = unit(a)] [E2 = unit(b-dot(b,E1)*E1)] [E3 = unit(c - dot(c,E2)*E2 - dot(c,E1)*E1)] { func(x) {dot(x,E1), dot(x,E2), dot(x,E3)} }

###### PlaneToSpace : R^2 --> R^3
###### PlaneToSpace : (any -> R^2) --> (any -> R^3)
###### Maps a point in R^2 to a point in the x-y plane of R^3. If given a function that
###### returns a point in R^2, PlaneToSpace returns the same function but returns its
###### point in R^3.
PlaneToSpace(x) = (x_1, x_2, 0)




                ########## DIFFERENTIAL GEOMETRY FUNCTIONS #####

    ########    CURVES    ###

###### T : (R -> R^n) --> (R -> R^n)
###### Unit Tangent Vector: maps parameterized curve to tangent vector function
###### Other names defined as T: TangentVector
T(X) = unit(X')
TangentVector = T

###### Speed : (R -> R^n) --> (R -> R)
###### Speed(X)(t) is s'(t), the speed function of the curve. It is the derivative of arclength.
Speed(X) = length(X')

###### P : (R -> R^n) --> (R -> R^n)
###### Unit Principle Normal Vector: maps parameterized curve to princible normal vector function
###### Other names defined as P: PrincipalNormalVector
P(X) = unit(T(X)')
PrincipalNormalVector = P

###### Curvature : (R -> R^n) --> (R -> R^n)
###### Returns the curvature function of a given parameterized curve. Works for n = 2 or 3.
Curvature(X) = length(T(X)'/Speed(X))

###### Evolute : (R -> R^n) --> (R -> R^n)
###### Maps a parameterized curve to the evolute curve. Works for n = 2 or 3; don't know about other n.
###### Other names defined as Evolute: EvoluteCurve
Evolute(X) = X + P(X)/Curvature(X)
EvoluteCurve = Evolute

###### OscCircle : (R -> R^n) , R --> (R -> R^n)
###### Maps a parameterized plane or space curve at a given point to its osculating circle at that point.
###### The point is specified as a value for the parameter of the curve.
###### The osculating circle is given as a circle function: it takes a parameter between 0 and 2*pi.
OscCircle(X,t) = func(angle){(X(t) + (P(X)(t) + cos(angle)*T(X)(t) + sin(angle)*P(X)(t))/Curvature(X)(t))}


    ######## PLANE CURVES ###

###### U : (R -> R^2) --> (R -> R^2)
###### Normal vector to curve: maps parameterized curve to unit normal vector function
###### U is T rotated pi/2 counter-clockwise
###### Names defined as U: PlaneCurveNormal
U(X) = (-T_2(X), T_1(X))
PlaneCurveNormal = U

    ######## SPACE CURVES ###

###### B : (R -> R^3) --> (R -> R^3)
###### Unit Binormal Vector: maps parameterized curve in R^3 to binormal vector function
###### Other names defined as B: BinormalVector
B(X) = unit(cross(X', X''))
BinormalVector = B

###### Torsion : (R -> R^3) --> (R -> R)
###### Maps a parameterized space curve to its (parameterized) torsion.
Torsion(X) = dot(unit(cross(X', X'')), X''')


    ########   SURFACES   ###

###### N : (R,R -> R^3) --> (R,R -> R^3)
###### Unit Normal Vector: maps parameterized surface in R^3 to unit normal funciton
###### Other names defined as N: NormalVector
N(X) = unit(cross(X__1, X__2))
NormalVector = N

###### g11, g12, g21, g22:
###### g_uv : (R,R -> R^3) --> (R,R -> R)
###### Metric coefficients: Given a parameterized surface, g11, g12, g21, and g22 return
###### the metric coefficient for the surface as a function from the domain to R.
###### Other names defined as g_uv are (substitute "12" for desired vars): g12 , MetricCoef12
g11(X) = dot(X__1,X__1)
g12(X) = dot(X__1,X__2)
g21(X) = dot(X__2,X__1)
g22(X) = dot(X__2,X__2)
MetricCoef11 = g11
MetricCoef12 = g12
MetricCoef21 = g21
MetricCoef22 = g22

###### Matrix of metric coefficients: called gmatrix, MetricCoefMatrix
gmatrix = [g11, g12 ; g21, g22]
MetricCoefMatrix = gmatrix

###### L11, L12, L21, L22:
###### L_uv : (R,R -> R^3) --> (R,R -> R)
###### the L (lower) coefficients.
L11(X) = N(X)*X__1__1
L12(X) = N(X)*X__1__2
L21(X) = N(X)*X__2__1
L22(X) = N(X)*X__2__2

###### TotalCurvature : (R,R -> R^3) --> (R,R -> R)
###### Total Curvature / Gaussian Curvature: maps a parameterized surface to its total curvature function
###### Assumes equality of mixed partials.
###### Other names for TotalCurvature: GaussianCurvature
TotalCurvature(X) = dot(cross(N(X)__1,N(X)__2),cross(X__1,X__2))/length(cross(X__1,X__2))^2
GaussianCurvature = TotalCurvature
K = TotalCurvature

###### MeanCurvature : (R,R -> R^3) --> (R,R -> R)
###### MeanCurvature. Maps a parameterized surface to its Mean Curvature
###### Assumes equality of mixed partials.
MeanCurvature(X) = 0.5*((L11(X)*g22(X)-2*L12(X)*g12(X)+L22(X)*g11(X))/(g11(X)*g22(X)-g12(X)^2))
H = MeanCurvature



                ########## COMMON SHAPES #####

###### Circle : R --> (R -> R^2)
###### Given a radius R, returns a circle of radius R in the plane centered at the origin.
###### The circle is a function of an angle parameter in the range [0, 2*pi]
Circle(R) = func(angle){R*(cos(angle), sin(angle))}

###### Disc : R --> (R,R -> R^2)
###### Given a radius R, returns a disc of radius R in the plane centered at the origin.
###### The disc is a function of two parameters (radius, angle). To fill the disc,
###### angle should be in [0, 2*pi], and radius should be in [0,1].
Disc(R) = func(radius, angle){R*radius*(cos(angle), sin(angle))}

###### Sphere : R --> (R,R -> R^3)
###### Given a radius R, returns a sphere of radius R in 3-space centered at the origin.
###### The sphere is a function of two parameters (theta, phi). To cover the sphere exactly
###### once, theta should be in [0,2*pi] and phi should be in [0,pi].
Sphere(R) = func(theta, phi){R*(cos(theta)*cos(phi), cos(theta)*sin(phi), sin(theta))}

###### Torus : R,R --> (R,R -> R^3)
###### Given a radius to the center of the tube and a radius of the tube, produces the torus
###### around the z-axis.
Torus(R, r) = func(theta, phi){(R+r*cos(phi))*cos(theta), (R+r*cos(phi))*sin(theta), r*sin(phi)}

###### PlaneNormal : R^3 --> (R,R -> R^3)
###### Given a vector n, returns the plane normal to n through the origin.
###### The plane is given as a function of two parameters (u,v). These parameters are the
###### variables for an orthonormal coordinate system in the plane.
PlaneNormal(n) = let [a = if (unit(n + (1,0,0)*length(n)/2) = unit(n)) then (n + (0,1,0)) else (n + (1,0,0)*length(n)/2)] [E1 = unit(cross(n, a))] [E2 = unit(cross(E1, n))] { func(u,v){ u*E1 + v*E2 } }

###### Tube : (R -> R^3), R --> (R,R -> R^3)
###### Given a parameterized space curve and a radius, returns the tube around the curve.
###### The tube is given as a function of two parameters (t, theta). To cover the tube exactly
###### once, theta should be in [0,2*pi]. t is the parameter for the curve.
Tube(X, R) = func(t, theta){ X(t) + R*cos(theta)*P(X)(t) + R*sin(theta)*B(X)(t) }

###### OscTube : (R -> R^3) --> (R,R -> R^3)
###### Given a parameterized space curve, returns the osculating tube of the curve.
###### The tube is given as a function of two parameters (t, theta). To cover the tube exactly
###### once, theta should be in [0,2*pi]. t is the parameter for the curve.
OscTube(X) = func(t, theta){ X(t) + (cos(theta)*T(X)(t) + (1+sin(theta))*P(X)(t))/Curvature(X)(t) }

###### Cylinder : (R -> R^2), R^2 --> (R,R -> R^3)
###### Given a plane curve and a vector, returns the cylinder created by pushing the curve
###### (lying in the x-y plane) along the given vector. The cylinder is given as a function
###### of two parameters (t,v), where v is the distance along the vector and t is the 
###### parameter for the curve.
Cylinder(X, A) = func(t,v){ (X_1, X_2, 0)(t) + v*A }

###### TangStrip : (R -> R^3) --> (R,R -> R^3)
###### Given a space curve, returns the tangential strip of the curve.
###### The tangential strip is given as a function of two parameters, (t,v).
###### t is the parameter for the curve, and v is the distance along the tangent vector to go.
TangStrip(X) = func(t,v){ X(t) + v*T(X)(t) }

###### RevSurface : (R -> R^2) -> (R,R -> R^3)
###### Given a plane curve, returns the surface of revolution of the curve. The curve is
###### treated to be in the x-y plane, and is revolved about the y-axis. The surface is
###### given as a function of two parameters (t, theta). t is the parameter for the curve,
###### and theta is the angle of the revolution. To cover the surface exactly once, theta
###### should be in [0,2*pi].
RevSurface(X) = func(t, theta){ cos(theta)*X_1(t), X_2(t), sin(theta)*X_1(t) }




## Other functionalities

- Vector Component [_]: (1,2,3)_1 (== 1)
- Function Derivative [_]: 
    - f(x) = x^2; f_x(x) (== 2*x)
    - f__1(x) (== 2*x)
- use | for list 
- use @ to access list component
- use [1,2;3,4] for matrices
- (), {}, and [] are all the same