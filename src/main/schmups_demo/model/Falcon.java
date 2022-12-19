package schmups_demo.model;

import java.awt.*;
import java.awt.image.BufferedImage;

import schmups_demo.controller.Game;


public class Falcon extends Sprite {

	private static final int DEGREE_STEP = 15;
    private final int RADIUS = 35;

    private static final int FRAMES_BETWEEN_LASERS = Game.getFPS() * 2; // 2 seconds between lasers
    private long framesSinceLastLaser;

	private boolean movingRight = false;
	private boolean movingLeft = false;
    private boolean goingUp = false;
	private boolean goingDown = false;
    private int velocity;

    private final BufferedImage playerShipImg;

	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================

	public Falcon(BufferedImage playerShipImg) {
		setTeam(Team.FRIEND);
		//this is the size (radius) of the falcon
		setRadius(RADIUS);
		setOrientation(0);

        if(CommandCenter.getInstance().getSpecialFalcon()){
            velocity = (int) (DEGREE_STEP * 1.5);
        }
        else{
            velocity = DEGREE_STEP;
        }
        

        // specific settings
        framesSinceLastLaser = -FRAMES_BETWEEN_LASERS;
        this.playerShipImg = playerShipImg;
	}

	@Override
	public boolean isProtected() {
		return getFade() < 255;
	}


	// ==============================================================
	// METHODS
	// ==============================================================
	@Override
	public void move() {

	    // Move left (if not outside bounds)
		if (movingLeft && getCenter().x - RADIUS > 0) {
			getCenter().move(getCenter().x - velocity , getCenter().y);
		}
		// Move right (if not outside bounds)
		if (movingRight && getCenter().x + RADIUS < Game.DIM.width-10) {
			getCenter().move(getCenter().x + velocity, getCenter().y);
		}

        // Move up (if not outside bounds)
		if (goingUp && getCenter().y - RADIUS > 0) {
			getCenter().move(getCenter().x, getCenter().y - velocity);
		}
		//move down (if not outside bounds)
		if (goingDown && getCenter().y + RADIUS < Game.DIM.height - 40) {
			getCenter().move(getCenter().x, getCenter().y +  velocity);
		}

	} //end move



	//methods for moving the falcon
	public void rotateLeft() {
		movingLeft = true;
	}

	public void rotateRight() {
		movingRight = true;
	}

    public void goUp() {
		goingUp = true;
	}

	public void goDown() {
		goingDown = true;
	}


    	//methods for moving the falcon
	public void stopLeft() {
		movingLeft = false;
	}

	public void stopRight() {
		movingRight = false;
	}

    public void stopUp() {
		goingUp = false;
	}

	public void stopDown() {
		goingDown = false;
	}


	@Override
	public void draw(Graphics g) {
        drawImage(playerShipImg, g);
    }


    public static int getFRAMES_BETWEEN_LASERS() {
        return FRAMES_BETWEEN_LASERS;
    }
    public long getFramesSinceLastLaser() {
        return framesSinceLastLaser;
    }
    public void setFramesSinceLastLaser(long framesSinceLastLaser) {
        this.framesSinceLastLaser = framesSinceLastLaser;
    }


} //end class
