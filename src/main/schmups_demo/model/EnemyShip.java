package schmups_demo.model;

import java.awt.*;
import java.awt.image.BufferedImage;

import schmups_demo.controller.Game;

public class EnemyShip extends Sprite {

    // velocity
    private static int DEGREE_STEP = 5;

    // firing control -- @DIFFICULTY_LEVEL #REVIEW
    private static double PROB_FIRE_MISSILE = 20; // at each second, probability of PROB_FIRE_MISSILE of shooting a missile
    private static double PROB_FIRE_BULLET = 50; // // at each second, probability of PROB_FIRE_BULLET of shooting a bullet
    private static int FRAMES_BETWEEN_MISSILES = 10; // minimum number of frames before shooting a new missile
    private static int FRAMES_BETWEEN_BULLETS = 20; // minimum number of frames before shooting a new bullet
    
    // aux variables for EnemyShip control
    private int framesSinceLastBullet;
    private int framesSinceLastMissile;
    private boolean firingMissile;
    private boolean firingBullet;

    // object drawing 
    private final BufferedImage enemyImg;

    public EnemyShip(Falcon falcon){
        setTeam(Team.FOE);
        
        // image to draw
        enemyImg = CommandCenter.getInstance().getEnemyShipImg();
		
        // set size, center, orientation
        setRadius(enemyImg.getWidth()/2);
        setCenter(new Point(Game.DIM.width, Game.R.nextInt(Game.DIM.height))); // enemy spawn from the rhs screen	
        setOrientation(180); // pointing to the left

        // specific settings
        firingMissile = false;
        firingBullet = false;
        setFramesSinceLastMissile(0);
        setFramesSinceLastBullet(0);
    }

    @Override
    public void move(){
        
        Point center = getCenter();

        isShooting();
   
        //right-bounds reached => bounce left
        if (center.x + getRadius() > Game.DIM.width) {
            setDeltaX(-DEGREE_STEP);
            setDeltaY(0);
            
        //left-bounds reached => bounce right
        } else if (center.x + getRadius() < 0) {
            setDeltaX(DEGREE_STEP);
            setDeltaY(0);
         
        //bottom-bounds reached => bounce up 
        } else if (center.y + getRadius() >= Game.DIM.height) {
            // setDeltaY(someNegValue(10)); change velocity after bounce
            setDeltaX(0);
            setDeltaY(-DEGREE_STEP);
        
        // top-bounds reached => bounce down 
        } else if (center.y + getRadius() <= 0) {
            // setDeltaY(somePosValue(10)); change velocity after bounce
            setDeltaY(DEGREE_STEP);
            setDeltaX(0);
        
        //in-bounds => follow falcon 
        } else {
            //int deltaX = CommandCenter.getInstance().getFalcon().getCenter().x - this.getCenter().x;
            int deltaX = this.getCenter().x - CommandCenter.getInstance().getFalcon().getCenter().x;
            int deltaY = CommandCenter.getInstance().getFalcon().getCenter().y - this.getCenter().y;
            
            double radians = Math.atan2(deltaY, deltaX);
            
            // if too distant, get closer to falcon
            if (deltaX > 5 * getRadius()) {
                setDeltaX(-DEGREE_STEP);
            }
            // if too close, moves away from falcon
            else if(deltaX < 4.99 * getRadius()){
                setDeltaX(DEGREE_STEP);
            }
            else{
                setDeltaX(0);
            }         
            // TODO change this hard coding
            setDeltaY(Math.sin(radians) * 50);
        }
        setCenter(new Point((int) (getCenter().x + getDeltaX()), (int) (getCenter().y + getDeltaY())));
    }

    // set ship to shooting missile/bullet or not
    public void isShooting(){
        // check if one second has transcurred; if not, set to false
        if((Game.getnFrame() % (Game.getFPS()) == 0)){
            firingMissile = (Game.R.nextInt(100) < PROB_FIRE_MISSILE);
            firingBullet = (Game.R.nextInt(100) < PROB_FIRE_BULLET);
        }
        else{
            firingMissile = false;
            firingBullet = false;
        }
    }   

    // =========================================================================
    // Getters & Setters
    //==========================================================================

    public boolean isFiringMissile(){
        return firingMissile;
    }

    public boolean isFiringBullet(){
        return firingBullet;
    }

    public boolean getFiringMissile() {
        return this.firingMissile;
    }

    public void setFiringMissile(boolean firingMissile) {
        this.firingMissile = firingMissile;
    }

    public boolean getFiringBullet() {
        return this.firingBullet;
    }

    public void setFiringBullet(boolean firingBullet) {
        this.firingBullet = firingBullet;
    }

    // tracks frames since shooting last projectile
    public int getFramesSinceLastBullet() {
        return this.framesSinceLastBullet;
    }

    public void setFramesSinceLastBullet(int framesSinceLastBullet) {
        this.framesSinceLastBullet = framesSinceLastBullet;
    }

    public int getFramesSinceLastMissile() {
        return this.framesSinceLastMissile;
    }

    public void setFramesSinceLastMissile(int framesSinceLastMissile) {
        this.framesSinceLastMissile = framesSinceLastMissile;
    }

    public void updateFramesSinceLastMissile(int frames){
        this.framesSinceLastMissile += frames;
    }

    public void updateFramesSinceLastBullet(int frames){
        this.framesSinceLastBullet += frames;
    }


    // minimum ammount of frames transcurred to shot another missile?
    public boolean missileDelayTranscurred(){
        return framesSinceLastMissile >= FRAMES_BETWEEN_MISSILES;
    }

    // minimum ammount of frames transcurred to shot another missile?
    public boolean bulletDelayTranscurred(){
        return framesSinceLastBullet >= FRAMES_BETWEEN_BULLETS;
    }

    public int getFramesBetweenBullets() {
        return FRAMES_BETWEEN_BULLETS;
    }

    public int getFramesBetweenMissiles() {
        return FRAMES_BETWEEN_MISSILES;
    }
   
    //=======================================================================
    // Draw
    //========================================================================
    @Override
    public void draw(Graphics g) {
        drawImage(enemyImg, g);
    }
}