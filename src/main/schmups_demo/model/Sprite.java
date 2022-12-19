package schmups_demo.model;


import lombok.Data;
import lombok.experimental.Tolerate;
import schmups_demo.controller.Game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

//the lombok @Data gives us automatic getters and setters on all members
@Data
public abstract class Sprite implements Movable {
    //the center-point of this sprite
    private Point center;
    //this causes movement; change-in-x and change-in-y
    private double deltaX, deltaY;

    //every sprite has a team: friend, foe, floater, or debris.
    private Team team;
    //the radius of circumscribing circle
    private int radius;

    //orientation from 0-359
    private int orientation;
    //natural mortality (short-lived sprites only)
    private int expiry;
    //the color of this sprite
    private Color color;

    //some sprites spin, such as floaters and asteroids
    private int spin;

    //use for fade-in/fade-out
    private int fade;

    private int DAMAGE;

    private Polygon polygon;

    private long curNFrame;
    private long prevNFrame;
    //these are Cartesian points used to draw the polygon.
    //once set, their values do not change. It's the job of the render() method to adjust for orientation and location.
    private Point[] cartesians;

  
    //constructor
    public Sprite() {
        setCurNFrame(Long.MIN_VALUE);
        setPrevNFrame(Long.MIN_VALUE);
        setDAMAGE(0);
        //default sprite color
        setColor(Color.black);
        //place the sprite at some random location in the frame at instantiation
        setCenter(new Point(Game.R.nextInt(Game.DIM.width), Game.DIM.height));
    }


    @Override
    public void move() {
        Point center = getCenter();

        //right-bounds reached => remove sprite
        if (center.x + getRadius() > Game.DIM.width) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
    
        //left-bounds reached => remove sprite
        } else if (center.x + getRadius() < 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);

        //bottom-bounds reached => bounce up 
        } else if (center.y + getRadius() >= Game.DIM.height) {
            // setDeltaY(someNegValue(10)); change velocity after bounce
            setDeltaY(-getDeltaY());
            setDeltaX(0);
        
        // top-bounds reached => bounce up w/ possibly different velocity
        } else if (center.y + getRadius() <= 0) {
            // setDeltaY(somePosValue(10)); change velocity after bounce
            setDeltaY(-getDeltaY());
            setDeltaX(0);
        
        //in-bounds => continue walking
        } else {}

        // walks in the direction defined above
        setCenter(new Point((int) (center.x + getDeltaX()), (int) (center.y + getDeltaY())));

        //expire (decrement expiry) on short-lived objects only; default value of expiry is zero, so apply only to expiring sprites
        if (getExpiry() > 0) expire();
    }

    public void expire() {
        //if expiring sprite has expiry of one, commits suicide by enqueuing itself (this) onto the ops list
        if (getExpiry() == 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
        //and then decrements in all cases
        setExpiry(getExpiry() - 1);
    }

    protected double hypotFunction(double dX, double dY) {
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }

    // Random generators -------------------------------------------------------
    protected int somePosNegValue(int seed) {
        int randomNumber = Game.R.nextInt(seed);
        if (randomNumber % 2 == 0)
            randomNumber = -randomNumber;
        return randomNumber;
    }

    protected int somePosValue(int seed) {
        return Game.R.nextInt(seed);

    }
    protected int someNegValue(int seed) {
        return -Game.R.nextInt(seed);

    }

    @Override
    public boolean isProtected() {
        //by default, sprites are not protected
        return false;
    }

    //certain Sprites, such as Asteroid use this
    protected Point[] polarToCartesian(List<PolarPoint> polPolars) {
        //when casting from double to int, we truncate and lose precision, so best to be generous with multiplier
        final int PRECISION_MULTIPLIER = 1000;
        Function<PolarPoint, Point> polarToCartTransform = pp -> new Point(
                (int) (getCenter().x + pp.getR() * getRadius() * PRECISION_MULTIPLIER
                        * Math.sin(Math.toRadians(getOrientation())
                        + pp.getTheta())),
                (int) (getCenter().y - pp.getR() * getRadius() * PRECISION_MULTIPLIER
                        * Math.cos(Math.toRadians(getOrientation())
                        + pp.getTheta())));

        return polPolars.stream()
                .map(polarToCartTransform)
                .toArray(Point[]::new);
    }

    protected List<PolarPoint> cartesianToPolar(List<Point> pntCartesians) {
        BiFunction<Point, Double, PolarPoint> cartToPolarTransform = (pnt, hyp) -> new PolarPoint(
                //this is r from PolarPoint(r,theta).
                hypotFunction(pnt.x, pnt.y) / hyp, //r is relative to the largestHypotenuse
                //this is theta from PolarPoint(r,theta)
                Math.toDegrees(Math.atan2(pnt.y, pnt.x)) * Math.PI / 180
        );

        //determine the largest hypotenuse
        double largestHypotenuse = 0;
        for (Point pnt : pntCartesians)
            if (hypotFunction(pnt.x, pnt.y) > largestHypotenuse)
                largestHypotenuse = hypotFunction(pnt.x, pnt.y);


        //we must make hypotenuse final to pass into a stream.
        final double hyp = largestHypotenuse;

        return pntCartesians.stream()
                .map(pnt -> cartToPolarTransform.apply(pnt, hyp))
                .collect(Collectors.toList());

    }

    @Override
    public void draw(Graphics g) {
        setCurNFrame(curNFrame+1);
        //set the native color of the sprite
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(getColor());
        render(g2d);
    }

    public void draw(Graphics g, Color color) {
        //set custom color
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(1));
        render(g2d);
    }


    public void render(Graphics2D g2d) {
        // to render this Sprite, we need to, 1: convert raw cartesians to raw polars, 2: adjust polars
        // for orientation of sprite. Convert back to cartesians 3: adjust for center-point (location).
        // and 4: pass the cartesian-x and cartesian-y coords as arrays, along with length, to drawPolygon().

        //convert raw cartesians to raw polars
        List<PolarPoint> polars = cartesianToPolar(Arrays.asList(getCartesians()));

        //rotate raw polars given the orientation of the sprite. Then convert back to cartesians.
        Function<PolarPoint, Point> adjustForOrientation =
                pp -> new Point(
                        (int)  (pp.getR() * getRadius()
                                * Math.sin(Math.toRadians(getOrientation())
                                + pp.getTheta())),

                        (int)  (pp.getR() * getRadius()
                                * Math.cos(Math.toRadians(getOrientation())
                                + pp.getTheta())));

        // adjust for the location (center-point) of the sprite.
        // the reason we subtract the y-value has to do with how Java plots the vertical axis for
        // graphics (from top to bottom)
        Function<Point, Point> adjustForLocation =
                p -> new Point(
                        getCenter().x + p.x,
                        getCenter().y - p.y);

        // @changed polygon definition to use the object for painting
        // create and draw a polygon to the screen
        polygon = new Polygon(polars.stream()
                        .map(adjustForOrientation)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.x)
                        .mapToInt(Integer::intValue)
                        .toArray(),
                        polars.stream()
                        .map(adjustForOrientation)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.y)
                        .mapToInt(Integer::intValue)
                        .toArray(),
                        getCartesians().length);
        g2d.drawPolygon(polygon);

        // this marks ORANGE the center of the polygon (for debugging)
        // g2d.setColor(Color.ORANGE);
        // g2d.fillOval(getCenter().x - 1, getCenter().y - 1, 2, 2);
    }

    public void drawImage(BufferedImage img, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.toRadians(getOrientation()), img.getWidth()/2, img.getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		img = op.filter(img, null);
		g2d.drawImage(img, getCenter().x - getRadius(), getCenter().y - getRadius(), null);
	}

    //in order to overload a lombok'ed method, we need to use the @Tolerate annotation
    //this overloaded method allows us to pass-in either a List<Point> or Point[] (lombok'ed method) to setCartesians()
    @Tolerate
    public void setCartesians(List<Point> pntPs) {
        setCartesians(pntPs.stream()
                .toArray(Point[]::new));

    }
}
