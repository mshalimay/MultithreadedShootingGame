package schmups_demo.model;

import java.awt.*;
import java.awt.image.BufferedImage;

import schmups_demo.controller.Game;


// this implements a Boss. A boss:
// (i) stays fixed in the rightmost position
// (ii) may fire multiple missiles and bullets at the same time
// (iii) has lifepoints, so dont die with one single shot

public class Boss extends Sprite {
    // firing control -- @DIFFICULTY_LEVEL #REVIEW
    private static double PROB_FIRE_MISSILE = 20; // probability of shooting a missile AT EACH SECOND
    private static double PROB_FIRE_BULLET = 70;  // probability of shooting a bullet AT EACH SECOND
    private static int FRAMES_BETWEEN_MISSILES = Game.getFPS() * 5; // minimum number of frames before shooting new missiles
    private static int FRAMES_BETWEEN_BULLETS =  Game.getFPS() * 2; // minimum of 5 seconds before shooting new bullets
    private static int MAX_BULLETS_FIRED = 10; // maximum number of bullets boss can fire in one sweep
    private static int MAX_MISSILES_FIRED = 5; // maximum number of bullets boss can fire in one sweep
    private static int MAX_HEALTH = 60;
    
    // aux variables for Boss control
    private int framesSinceLastBullet;
    private int framesSinceLastMissile;
    private boolean firingMissile;
    private boolean firingBullet;
    private int numBulletsFired;
    private int numMissilesFired;
    private int health;
    private final BufferedImage bossImg;
    
    // holds the Y position for where the sprite starts and ends. 
    int minBound; 
    int maxBound; 
    // The boss will shoot from anywhere between minBound and maxBound

    public Boss(){
        setTeam(Team.FOE);
        
        // image to draw
        bossImg = CommandCenter.getInstance().getBossImg();
		
        // set size, center, orientation
        setRadius(bossImg.getWidth()/2);
        setCenter(new Point(Game.DIM.width - getRadius(), Game.DIM.height/2)); // enemy spawn from the rhs screen	
        setOrientation(180); // pointing to the left

        // specific settings
        this.health = MAX_HEALTH;
        firingMissile = false;
        firingBullet = false;
        setFramesSinceLastMissile(0);
        setFramesSinceLastBullet(0);

        minBound = getCenter().y - getHeight()/2;
        maxBound = getCenter().y + getHeight()/2;
    }

    @Override
    public void move(){   
        // is shooting?
        isShooting();
    }

// ================================================================================
// Shooting behavior 
// ================================================================================
    
    // set ship to shooting missile/bullet or not
    public void isShooting(){
        // check if one second has transcurred; if not, dont shoot
        if(Game.getnFrame() % (Game.getFPS())== 0){
            firingMissile = (Game.R.nextInt(100) < PROB_FIRE_MISSILE);
            firingBullet = (Game.R.nextInt(100) < PROB_FIRE_BULLET);
        }
        else{
            firingMissile = false;
            firingBullet = false;
        }
        // shoot how many bullets/missiles?
        numBulletsFired = firingBullet ? 1 + Game.R.nextInt(MAX_BULLETS_FIRED) : 0;
        numMissilesFired = firingMissile ? 1 + Game.R.nextInt(MAX_MISSILES_FIRED) : 0;
    } 
    
    // creates an array of bullets centered around Boss
    public EnemyBullet[] fireBullets(){
        EnemyBullet[] bullets = new EnemyBullet[this.numBulletsFired];

        for (int i = 0; i < numBulletsFired; i++) {
            // create a new bullet centered around a random position in the sprite height (the boss shots from all its height)
            bullets[i] = new EnemyBullet(this, new Point(getCenter().x - getRadius(), Game.generateRandomInt(minBound, maxBound)));
        }
        return bullets;
    }

    // creates an array of missiles centered around Boss
    public EnemyMissile[] fireMissiles(){
        EnemyMissile[] missiles = new EnemyMissile[this.numMissilesFired];
        for (int i = 0; i < numMissilesFired; i++) {
            // create a new bullet centered around a random position in the boss height (the boss shots from all its height) 
            missiles[i] = new EnemyMissile(this, new Point(getCenter().x - getRadius(), Game.generateRandomInt(minBound, maxBound)));
        }
        return missiles;
    }

// ====================================================================================
// Getters and setters 
// ====================================================================================
    
    public boolean isFiringMissile(){
        return firingMissile;
    }

    public boolean isFiringBullet(){
        return firingBullet;
    }

    // =========================================================================
    // Getters & Setters
    //==========================================================================

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
        // drawImage(bossImg, g);
        Graphics2D g2d = (Graphics2D) g;
        int width = Game.DIM.width/4;
        int height = Game.DIM.height/2;
        g2d.drawImage(bossImg, getCenter().x - (int) (0.6 * width), getCenter().y - height/2, 
                    width, height, null);
    }

    public void updateHealth(int damage){
        this.health += damage;
    }

    public int getHealth(){
        return health;
    }

    public int getHeight(){
        return bossImg.getHeight();
    }

    public int getWidth(){
        return bossImg.getWidth();
    }
}