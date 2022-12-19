package schmups_demo.model;

import java.awt.*;
import java.awt.image.BufferedImage;

import schmups_demo.controller.Game;


public class Meteor extends Sprite {

    private static final int MAX_SIZE = CommandCenter.getInstance().getMeteorImgs().length-1;
    private int size;

    private BufferedImage meteorImg;
   
	public Meteor(int size){
		// set team
		setTeam(Team.FOE);

         // image to draw
         meteorImg = CommandCenter.getInstance().getMeteorImg(size);
		
         // set size, center, orientation
         setRadius((int) (meteorImg.getWidth()/2));

        //set Position, Spin, size, orientation 
        setCenter(new Point(Game.DIM.width-10,Game.R.nextInt(Game.DIM.height))); // asteroid comes from RHS
		setSpin(somePosNegValue(10));
		
         // set velocities (random)
		setDeltaX(somePosNegValue(10));
		setDeltaY(somePosNegValue(10));

        // Specific settings ---------------------------------------------------
        setDAMAGE(-1);
        this.size = size;
	}

	@Override
	public void move() {
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
        } else if (center.y + getRadius() >= Game.DIM.height) {
            setDeltaY(someNegValue(10));
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) center.x, (int) newYPos));
        
        //top-bounds reached, bounc down
        } else if (center.y - getRadius() <= 0) {
            setDeltaY(somePosValue(10));
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) center.x, (int) newYPos));
        
        // if in-bounds, walk with previous velocity
        } else {
            double newXPos = center.x + getDeltaX();
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) newXPos, (int) newYPos));
        }
    }

    @Override
    public int getDAMAGE() {
        // bigger meteor gives more damage (the tiny one gives no damage)
        return this.size;
    }

      @Override
      public void draw(Graphics g) {
          drawImage(meteorImg, g);
      }

      public static int getMaxSize() {
          return MAX_SIZE;
      } 
}
