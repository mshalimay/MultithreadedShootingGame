package schmups_demo.model;

import java.awt.*;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import schmups_demo.controller.Game;


public class Asteroid extends Sprite {

	//radius of asteroids
	private static final int SMALL_RADIUS = 20;
    private static final int MAX_RADIUS = 70;
    private static final double MAX_SIZE = (Math.log(MAX_RADIUS) / Math.log(2));

    // @size: asteroid radius = min(MAX_RADIUS, SMALL_RADIUS * 2^size)
	public Asteroid(double size){
		// set team
		setTeam(Team.FOE);

        //set Position, Spin, size, orientation 
        setCenter(new Point(Game.DIM.width-10,Game.R.nextInt(Game.DIM.height))); // asteroid comes from RHS
		setSpin(somePosNegValue(10));
		
         // set velocities (random)
		setDeltaX(somePosNegValue(10));
		setDeltaY(somePosNegValue(10));

		
		// radius = min(MAX_RADIUS, SMALL_RADIUS * 2^size)
        setRadius(Math.min((int) (SMALL_RADIUS * Math.pow(2, size)), MAX_RADIUS));
		setCartesians(genRandomPoints());

        // Specific settings ---------------------------------------------------
        setDAMAGE(-1);
	}

	@Override
	public void move() {

        //The following code block just keeps the sprite inside the bounds of the frame.
        //To ensure this behavior among all sprites in your game, make sure to call super.move() in extending classes
        // where you need to override the move() method.
        Point center = getCenter();

        // if right-bounds reached, bounce left with new speed
        if (center.x + getRadius() > Game.DIM.width) {
            setDeltaX(someNegValue(10));
            double newXPos = center.x + getDeltaX();
            setCenter(new Point((int) newXPos, (int) center.y));
        
        //if left-bounds reached, remove
        } else if (center.x + getRadius() + 10 < 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        
        //bottom-bounds reached, bounce up
        } else if (center.y + getRadius() + 40 >= Game.DIM.height) {
            setDeltaY(someNegValue(10));
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) center.x, (int) newYPos));
        
        //top-bounds reached, bounc down
        } else if (center.y - getRadius() <= 0) {
            setDeltaY(somePosValue(10));
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) center.x, (int) newYPos));
        
        // if in-bounds, just walk with previous velocity
        } else {
            double newXPos = center.x + getDeltaX();
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) newXPos, (int) newYPos));
        }
    }


      // random coordinates to draw asteroids
	  private Point[] genRandomPoints(){
		  //6.283 is the max radians
		  final int MAX_RADIANS_X1000 =6283;

		  Supplier<PolarPoint> polarPointSupplier = () -> {
			  double r = (800 + Game.R.nextInt(200)) / 1000.0; //number between 0.8 and 0.999
			  double theta = Game.R.nextInt(MAX_RADIANS_X1000) / 1000.0; // number between 0 and 6.282
		  	  return new PolarPoint(r,theta);
		  };

		 //random number of vertices between 17 and 23
		 final int vertices = Game.R.nextInt( 7 ) + 17;

		 return polarToCartesian(
				Stream.generate(polarPointSupplier)
				 .limit(vertices)
				 .sorted(new Comparator<PolarPoint>() {
							@Override
							public int compare(PolarPoint pp1, PolarPoint pp2) {
								return  pp1.getTheta().compareTo(pp2.getTheta());
							}
						})
				 .collect(Collectors.toList())
			);
	  }


      @Override
      public void draw(Graphics g) {
          //set custom color
          super.draw(g);
          Graphics2D g2d = (Graphics2D) g;
          g2d.setColor(Color.black);
          g2d.setStroke(new BasicStroke(1));
          render(g2d);
          g2d.setColor(Color.GREEN);
          g2d.fillPolygon(getPolygon());
      }


      public static double getMaxSize() {
          return MAX_SIZE;
      } 
}
