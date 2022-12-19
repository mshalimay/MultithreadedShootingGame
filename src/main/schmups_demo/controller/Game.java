package schmups_demo.controller;

import javax.sound.sampled.Clip;

import lombok.Data;
import schmups_demo.model.*;
import schmups_demo.view.GamePanel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

@Data
public class Game implements Runnable, KeyListener {

    // Window control -----------------------------------------------------------
	public static final Dimension DIM = new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width,
                                        Toolkit.getDefaultToolkit().getScreenSize().height - 20); //the dimension of the game.
	private final GamePanel gmpPanel;
	
    // Keys --------------------------------------------------------------------
	private final int PAUSE = 80, // p key
			QUIT = 81, // q key
			LEFT = 37, // rotate left; left arrow
			RIGHT = 39, // rotate right; right arrow
			UP = 38, // thrust; up arrow
			DOWN = 40, // break, down arrow
			START = 83, // s key
			FIRE = 32, // space key
			MUTE = 77, // m-key mute
            LASERCANNON = 70, // F key; fire laser cannon
            A_KEY = 65,
            B_KEY = 66; // F key; fire laser cannon

	// for possible future use
	// HYPER = 68, 					// D key
	// SHIELD = 65, 				// A key
	
	//private final Clip clpThrust ---------------------------------------------
	private final Clip clpMusicBackground;

    // Animation control -------------------------------------------------------
    public final static int ANI_DELAY = 40; // milliseconds between screen
    private final Thread animationThread;
    private static long nFrame; // frame counter used for some timed operations, like glowing of the sprites and spawn rates


	// Spawn of objects ------------------------------------------------------------------------------------------------
    
    // easier to think frequencies in terms of time; this is a derived variable just to make easier to translate from frames to time throghout the code
    private static final int FPS = 1000/ANI_DELAY; 
    
    // -- @DIFFICULTY_LEVEL #REVIEW
    private int SPAWN_ENEMY_SHIP =  5 * FPS; // new object every 10 seconds (or every 250 frames if ANY_DELAY = 40)
    private int SPAWN_ASTEROID = 5 * FPS; // new object every 5 seconds
    private int SPAWN_METEOR = 5 * FPS; // new object every 5 seconds
    private static final int SPAWN_BOSS = 30; // how many enemies destroyed to spawn boss?
    private static final int MAX_ENEMIES = 15;

    // Spawn of droppings -- @DIFFICULTY_LEVEL #REVIEW
    private double PROB_DROP_SPECIAL = 30; // probability of enemies dropping special weapon
    private double PROB_DROP_LIFE_POINT = 30; // probability of asteroids dropping life points
    
    // Dynamics for level changes --------------------------------------------------------------------------------------
    // -- @DIFFICULTY_LEVEL #REVIEW
    private static final int CHANGE_LEVEL = 5; // how many enemies destroyed to change levels?
    
    private static final int INCREASE_ASTEROID_SPAWN = (int) (2 * FPS) ; // increase the frequency by INCREASE_ASTEROID_SPAWN seconds
    private static final int INCREASE_METEOR_SPAWN = (int) (2 * FPS) ; // increase the frequency by INCREASE_ASTEROID_SPAWN seconds
    private static final int INCREASE_ENEMY_SHIP_SPAWN = 2 * FPS ;
    private static final int PRIZE_LP_NEW_LEVEL = 5;     // increase LifePoint drops prob by this value on each level

    private static final int MAX_ASTEROID_SPAWN_RATE = (int) (0.5 * FPS); // new object every MAX_ASTEROID_SPAWN_RATE seconds
    private static final int MAX_METEOR_SPAWN_RATE = (int) (1 * FPS); // new object every MAX_ASTEROID_SPAWN_RATE seconds
    private static final int MAX_ENEMY_SHIP_SPAWN_RATE = (int) (1 * FPS); // new object every MAX_ASTEROID_SPAWN_RATE seconds
    private final double MAX_PROB_DROP_LIFE_POINT = 30; // TODO implements this if change lifepoints at each level


    // Other game control ----------------------------------------------------------------------------------------------
    private static int BOSS_SCORE_POINTS = 5; // How many score points a boss is worth?
    private int BOUNCE_BY = 5; 

    private int[] SPECIAL_SEQUENCE = {UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT, A_KEY, B_KEY};
    
    // Common Random generator (used by other classes w/ same seed)
	public static final Random R = new Random();

	// =========================================================================
	// Constructor & Main
	// =========================================================================


    // Constructor, creates panel, calls the thread run
	public Game() {
		gmpPanel = new GamePanel(DIM);
		gmpPanel.addKeyListener(this); //Game object implements KeyListener
		//clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
		clpMusicBackground = Sound.clipForLoopFactory("music-background.wav");

		//fire up the animation thread
		animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
		animationThread.start();
	}

	public static void main(String args[]) {
		//typical Swing application start; we pass EventQueue a Runnable object.
		EventQueue.invokeLater(Game::new);
	}

	@Override
	public void run() {

        // =============/*  */=====================================================
        // SECTION main game loop
        //===================================================================
		// lower animation thread's priority, thereby yielding to the "main" aka 'Event Dispatch'
		// thread which listens to keystrokes
		animationThread.setPriority(Thread.MIN_PRIORITY);

		// and get the current time
		long lStartTime = System.currentTimeMillis();

		// this thread animates the scene
		while (Thread.currentThread() == animationThread) {
			gmpPanel.update(gmpPanel.getGraphics()); // draws on the screen		
           nFrame++; // updates the frame counter (used by other methods/classes)
           checkCollisions(); 
           checkNewLevel();
           spawnAsteroid();
           spawnMeteor();
		   spawnEnemyShip();
           foeFiring();
           spawnBoss();

			// delay time between animations
			try {
				// The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update)
				// between frames takes longer than ANI_DELAY, then the difference between lStartTime -
				// System.currentTimeMillis() will be negative, then zero will be the sleep time
				lStartTime += ANI_DELAY;

				Thread.sleep(Math.max(0,
						lStartTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				// do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
			}
		} // end while
	} // end run // \SECTION

    
    //===============================================================================================================
    // //SECTION  Collisions 
    //===============================================================================================================
    // #REVIEW generalize projectile? Maybe create a enum 'type' in Sprite class
    // 
    // SECTION  1) Detect the collisions ------------------------------------------------------------------------------
	private void checkCollisions() {
		for (Movable mov1 : CommandCenter.getInstance().getMovables()) {
			for (Movable mov2 : CommandCenter.getInstance().getMovables()) {
				if (mov1 instanceof Sprite && mov2 instanceof Sprite) {
                    if (mov1.getTeam() != Movable.Team.DEBRIS && mov2.getTeam() != Movable.Team.DEBRIS){
                        Sprite sprite1 = (Sprite) mov1;
                        Sprite sprite2 = (Sprite) mov2;                        
                        if (sprite1.getCenter().distance(sprite2.getCenter()) < (mov1.getRadius() + mov2.getRadius())) 
                        {
                            collBounce(sprite1, sprite2);
                            CollSpriteProjectile(sprite1, sprite2);
                            CollFalconAsteroid(sprite1, sprite2);
                            CollFalconMeteor(sprite1, sprite2);
                            CollFalconFloaters(sprite1, sprite2);
                        }
                    }
				}
			} // end inner for
		} // end outer for
        // process the add/removal resulting from collisions
        processGameOpsQueue();
    } // \SECTION
    
    // SECTION 2) Specific behavior after each collision ---------------------------------------------------------------

    // Specific behavior for collision of each object and types of projectiles
    private void CollSpriteProjectile(Sprite sprite, Sprite projectile)
    {
        // Falcon collision with projectiles
        if(sprite instanceof Falcon){
            // projectile is an EnemyBullet or an EnemyMissile
            if(projectile instanceof EnemyBullet || projectile instanceof EnemyMissile){
                CommandCenter.getInstance().updateLife(projectile.getDAMAGE());
                CommandCenter.getInstance().getOpsQueue().enqueue(projectile, GameOp.Action.REMOVE);
                if(projectile instanceof EnemyMissile){Sound.playSound("missileDestroyed.wav");}
            }
        } // FOEs ex-Boss collision with (falcon) projectiles
        else if(sprite instanceof EnemyShip || sprite instanceof Asteroid || sprite instanceof EnemyMissile || sprite instanceof Meteor){
            // if projectile is a (falcon) Bullet or Laser, destroy the sprite
            if(projectile instanceof Bullet || projectile instanceof Laser){
                CommandCenter.getInstance().getOpsQueue().enqueue(sprite, GameOp.Action.REMOVE); // remove the sprite
                
                // And remove the bullet (but dont remove the Laser!)
                if(projectile instanceof Bullet){
                    CommandCenter.getInstance().getOpsQueue().enqueue(projectile, GameOp.Action.REMOVE);
                }
                // specific behaviors:
                // if EnemyShip destroyed, (possibly) spawn a LaserPoint; update by 1 numEnemyDestroyed/score
                if(sprite instanceof EnemyShip){
                    Sound.playSound("foeDestroyed.wav");
                    CommandCenter.getInstance().updateNumEnemyDestroyed(1); // REVIEW score diff enemy destroyed for possibly diff behavior 
                    CommandCenter.getInstance().updateScore(1);
                    spawnSpecialWeapon((EnemyShip) sprite);
                }
                // if Asteroid destroyed, (possibly) spawn a LifePoint
                else if(sprite instanceof Asteroid){
                    Sound.playSound("explosion.wav");
                    spawnLifePoint((Asteroid) sprite);
                }
                else if(sprite instanceof Meteor){
                    Sound.playSound("explosion.wav");
                }
            }
        } // Boss collision with (falcon) projectiles
        else if (sprite instanceof Boss){
            if(projectile instanceof Bullet || projectile instanceof Laser){
                Boss boss = (Boss) sprite;
                boss.updateHealth(projectile.getDAMAGE());
                if(boss.getHealth() <= 0){
                    CommandCenter.getInstance().getOpsQueue().enqueue(boss, GameOp.Action.REMOVE);
                    CommandCenter.getInstance().updateNumBoss(-1);
                    Sound.playSound("foeDestroyed.wav");
                    
                    // TODO include big blast sound, perhaps blast image
                    CommandCenter.getInstance().updateScore(BOSS_SCORE_POINTS);
                    // TODO normalize spawn of enemies
                }
                // remove the bullet (but dont remove the Laser!)
                if(projectile instanceof Bullet){
                    CommandCenter.getInstance().getOpsQueue().enqueue(projectile, GameOp.Action.REMOVE);
                }
            }
        }
    }


    private void collBounce(Sprite sprite1, Sprite sprite2) {
		if (sprite1 instanceof EnemyShip && sprite2 instanceof EnemyShip) {
				adjustBump(sprite1, sprite2);
		}
	}

	private void adjustBump(Sprite movOne, Sprite movTwo){
		Point center1 = movOne.getCenter();
		Point center2 = movTwo.getCenter();

		if (center1.getX() < center2.getX()) {
			center1.setLocation(center1.getX() - BOUNCE_BY, center1.getY());
			center2.setLocation(center2.getX() + BOUNCE_BY, center2.getY());
		}
		else {
			center1.setLocation(center1.getX() + BOUNCE_BY, center1.getY());
			center2.setLocation(center2.getX() - BOUNCE_BY, center2.getY());
		}

		if (center1.getY() < center2.getY()) {
			center1.setLocation(center1.getX(), center1.getY() - BOUNCE_BY);
			center2.setLocation(center2.getX(), center2.getY() + BOUNCE_BY);
		}
		else {
			center1.setLocation(center1.getX(), center1.getY() + BOUNCE_BY);
			center2.setLocation(center2.getX(), center2.getY() - BOUNCE_BY);
		}
	}


    private void CollFalconAsteroid(Sprite falcon, Sprite asteroid){
        if(falcon instanceof Falcon && asteroid instanceof Asteroid){
            CommandCenter.getInstance().updateLife(asteroid.getDAMAGE());
            CommandCenter.getInstance().getOpsQueue().enqueue(asteroid, GameOp.Action.REMOVE);
            Sound.playSound("explosion.wav");
        }
    }
    
    private void CollFalconMeteor(Sprite falcon, Sprite meteor){
        if(falcon instanceof Falcon && meteor instanceof Meteor){
            CommandCenter.getInstance().updateLife(meteor.getDAMAGE());
            CommandCenter.getInstance().getOpsQueue().enqueue(meteor, GameOp.Action.REMOVE);
            Sound.playSound("explosion.wav");
        }
    }

    private void CollFalconFloaters(Sprite falcon, Sprite floater){
        if(falcon instanceof Falcon && floater.getTeam() == Movable.Team.FLOATER ){
            CommandCenter.getInstance().getOpsQueue().enqueue(floater, GameOp.Action.REMOVE);
            Sound.playSound("pickUpItems.wav");
            if(floater instanceof LifePointDrop){
                CommandCenter.getInstance().updateLife(floater.getDAMAGE());;
            }
            else if(floater instanceof LaserDrop){
                CommandCenter.getInstance().updateNumLaser(1);
            }
        }
    }

    // \SECTION
    
    // SECTION 3) Process the deferred mutation (i.e., run all operations queued in previous steps) ----------------------------
	
    private void processGameOpsQueue() {
		//deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
		// mutating the movable linkedlists while iterating them above
        while(!CommandCenter.getInstance().getOpsQueue().isEmpty()){
            GameOp gameOp =  CommandCenter.getInstance().getOpsQueue().dequeue();
            Movable mov = gameOp.getMovable();
            GameOp.Action action = gameOp.getAction();

            switch (mov.getTeam()){
                
                case FOE:
                    if (action == GameOp.Action.ADD){
                        CommandCenter.getInstance().getMovFoes().add(mov);
                        if(mov instanceof EnemyShip){
                            CommandCenter.getInstance().updateNEnemyShips(1);
                        }
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovFoes().remove(mov);
                        if(mov instanceof EnemyShip){
                            CommandCenter.getInstance().updateNEnemyShips(-1);
                        }
                    }
                    break;
                
                case FRIEND:
                    // add friends
                    if (action == GameOp.Action.ADD)
                    {
                        CommandCenter.getInstance().getMovFriends().add(mov);          
                    
                        // special behavior if LASER
                        if(mov instanceof Laser)
                        {
                            CommandCenter.getInstance().setLaserP1((Laser) mov); // laser object is unique; this makes easier to remove afterwards (without loops)
                        }
                    }
                    
                    // remove friends 
                    else 
                    { 
                        CommandCenter.getInstance().getMovFriends().remove(mov);
                        
                        // special behavior if falcon
                        if (mov instanceof Falcon) 
                        {
                        }
                        // special behavior if Laser
                        else if (mov instanceof Laser)
                        {
                            CommandCenter.getInstance().setLaserP1(null);
                        }
                        else 
                        {
                            // possible customized behavior for other objects
                        }
                    }
                    break;
                
                case FLOATER:
                    if (action == GameOp.Action.ADD){
                        CommandCenter.getInstance().getMovFloaters().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovFloaters().remove(mov);
                    }
                    break;

                case DEBRIS:
                    if (action == GameOp.Action.ADD){
                        CommandCenter.getInstance().getMovDebris().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovDebris().remove(mov);
                    }
                    break;
            } // end switch
        } // end while 
    } // \SECTION
    // \SECTION   
    
    //==================================================================================================================
    // SECTION Spawn objects
    //==================================================================================================================
    
    // Spawn Enemy Projectiles -----------------------------------------------------------------------------------------
    private void foeFiring() 
    {
		for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
			
            // enemy ship is firing?
            if (movFoe instanceof EnemyShip) 
            {
                EnemyShip enemyShip = (EnemyShip) movFoe;
				
                // enemy ship firing Missiles?
                if (enemyShip.isFiringMissile() && enemyShip.missileDelayTranscurred()) 
				{
					CommandCenter.getInstance().getOpsQueue().enqueue(new EnemyMissile(enemyShip), GameOp.Action.ADD);
					enemyShip.setFramesSinceLastMissile(0);
				}
				else {
					enemyShip.updateFramesSinceLastMissile(1);
				}

                // enemy ship firing Bullets?
                if (enemyShip.isFiringBullet() && enemyShip.bulletDelayTranscurred())
				{
					CommandCenter.getInstance().getOpsQueue().enqueue(new EnemyBullet(enemyShip), GameOp.Action.ADD);
                    enemyShip.setFramesSinceLastBullet(0);
				}
				else {
					enemyShip.updateFramesSinceLastBullet(1);
				}
			}
            
            // boss is firing?
            else if(movFoe instanceof Boss)
            {
                Boss boss = (Boss) movFoe;
                if(boss.isFiringBullet()){
                    for (EnemyBullet bullet : boss.fireBullets()){
                        CommandCenter.getInstance().getOpsQueue().enqueue(bullet, GameOp.Action.ADD);
                    }
                    boss.setFramesSinceLastBullet(0);
                }
                else{
                    boss.updateFramesSinceLastBullet(1);
                }

                if(boss.isFiringMissile()){
                    for (EnemyMissile missile : boss.fireMissiles()){
                        CommandCenter.getInstance().getOpsQueue().enqueue(missile, GameOp.Action.ADD);
                    }
                    boss.setFramesSinceLastMissile(0);
                }
                else{
                    boss.updateFramesSinceLastMissile(1);
                }                  
            } 
        } // end for movFoe
	}
  
    
    // Spawn ships, asteroids, etc -------------------------------------------------------------------------------------

    // spawn EnemyShips
    private void spawnEnemyShip() {
		if(CommandCenter.getInstance().getNEnemyShips()>=MAX_ENEMIES){
            return;
        }
        //appears more often as your level increases; second condition guarantees dont spawn before loading images
		if (nFrame % (SPAWN_ENEMY_SHIP) == 0 && !CommandCenter.getInstance().getIsFirstGame()) {
			CommandCenter.getInstance().getOpsQueue().enqueue(new EnemyShip(CommandCenter.getInstance().getFalcon()), GameOp.Action.ADD);
		}
	}

	// spawn new asteroids 
	private void spawnAsteroid() {
		//asteroid size and spawning frequency increases as level increases; second condition guarantees dont spawn before loading images
		if (nFrame % (SPAWN_ASTEROID) == 0 && !CommandCenter.getInstance().getIsFirstGame()) {
			// asteroiSize = random between 0 and min(current level, multiplier * maxAsteroidSize) 
                // the multiplier makes the distribution of asteroid sizes tilted to the right making the game more difficult
                // -- @DIFFICULTY_LEVEL #REVIEW
            double asteroidSize = (double) R.nextInt(Math.min(CommandCenter.getInstance().getLevel(), (int) (1.5 * Asteroid.getMaxSize())));
            CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(asteroidSize), GameOp.Action.ADD);
		}
	}

    // spawn new meteors
	private void spawnMeteor() {
		//asteroid size and spawning frequency increases as level increases; second condition guarantees dont spawn before loading images
		if (nFrame % (SPAWN_ASTEROID) == 0 && !CommandCenter.getInstance().getIsFirstGame()) {
			// meteorSize = random between 0 and meteorImgs.length() 
                // -- @DIFFICULTY_LEVEL #REVIEW
                // the multiplier 2 * Meteor.getMaxSize() makes the distribution sizes tilted to the right making the game more difficult
            int meteorSize = Math.min(Meteor.getMaxSize(), R.nextInt(Math.min(CommandCenter.getInstance().getLevel(), 2 * Meteor.getMaxSize())));
            CommandCenter.getInstance().getOpsQueue().enqueue(new Meteor(meteorSize), GameOp.Action.ADD);
		}
	}

    // spawn new Boss
    private void spawnBoss() {
        // spawn when threshold achieved; just one boss in the screen per time
		if (CommandCenter.getInstance().getNumEnemyDestroyed() - CommandCenter.getInstance().getEnemDestrSinceLastBoss() > SPAWN_BOSS
        && !CommandCenter.getInstance().getIsFirstGame()
        && CommandCenter.getInstance().getNumBoss()<1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Boss(), GameOp.Action.ADD);
            CommandCenter.getInstance().updateNumBoss(1);
            CommandCenter.getInstance().setEnemDestrSinceLastBoss(CommandCenter.getInstance().getNumEnemyDestroyed());
		}
        //TODO change the spawning behavior of other enemies while boss is in the screen
	}

	private void spawnLifePoint(Asteroid asteroid) {
        if(Game.R.nextInt(101) < PROB_DROP_LIFE_POINT)
        {
            CommandCenter.getInstance().getOpsQueue().enqueue(new LifePointDrop(asteroid), GameOp.Action.ADD);
        }
	}

    private void spawnSpecialWeapon(EnemyShip enemyShip) {
        if(Game.R.nextInt(101) < PROB_DROP_SPECIAL)
        {
            CommandCenter.getInstance().getOpsQueue().enqueue(new LaserDrop(enemyShip), GameOp.Action.ADD);
        }
	} // \SECTION
    

    // SECTION Other game control --------------------------------------------------------------------------------------
	private boolean isLevelClear(){
        // check if number enemies destroyed since last level is greater than the threshold to change levels
        if(CommandCenter.getInstance().getNumEnemyDestroyed() - CommandCenter.getInstance().getPrevNumEnemyDestroyed() > CHANGE_LEVEL){
            // update previous number of enemy destroyed to the current number
            CommandCenter.getInstance().setPrevNumEnemyDestroyed(CommandCenter.getInstance().getNumEnemyDestroyed());
            Sound.playSound("levelUp.wav");
            return true;
        }
        return false;
	}

	private void checkNewLevel(){
		if (isLevelClear()) {
			// increases the difficulty level; this will increase the spawn rate of enemies in the screen
			CommandCenter.getInstance().setLevel(CommandCenter.getInstance().getLevel() + 1);
            if(PROB_DROP_LIFE_POINT + PRIZE_LP_NEW_LEVEL < MAX_PROB_DROP_LIFE_POINT){
                PROB_DROP_LIFE_POINT += PRIZE_LP_NEW_LEVEL;
             }

             // changes spawining after level is clear -- @DIFFICULTY_LEVEL #REVIEW
            SPAWN_ASTEROID = Math.max(SPAWN_ASTEROID - INCREASE_ASTEROID_SPAWN, MAX_ASTEROID_SPAWN_RATE);
            SPAWN_METEOR = Math.max(SPAWN_METEOR - INCREASE_METEOR_SPAWN, MAX_METEOR_SPAWN_RATE);
            SPAWN_ENEMY_SHIP = Math.max(SPAWN_ENEMY_SHIP - INCREASE_ENEMY_SHIP_SPAWN, MAX_ENEMY_SHIP_SPAWN_RATE);
            
            // TODO add some premium for changing levels? 
		}
	}

    public void tick() {
		if (nFrame == Long.MAX_VALUE)
			nFrame = Long.MIN_VALUE;
		else
			nFrame++;
	}

	public long getFrame() {
		return nFrame;
	}
    // \SECTION


    // =================================================================================================================
    // SECTION I/O & media control -----------------------------------------------------------------------------------
	// =================================================================================================================
    
    // Varargs for stopping looping-music-clips
	private static void stopLoopingSounds(Clip... clpClips) {
		for (Clip clp : clpClips) {
			clp.stop();
		}
	}

	// Kyelistener methods ---------------------------------------------------------------------------------------------

    private void checkSpecialFalcon(){
        if(CommandCenter.getInstance().getSpecialFalcon()){
            return;
        }
        if(CommandCenter.getInstance().getKeySequency().size() < SPECIAL_SEQUENCE.length){
            return;
        }

        int j = 0;
        for (int i = CommandCenter.getInstance().getKeySequency().size()-SPECIAL_SEQUENCE.length; i < CommandCenter.getInstance().getKeySequency().size(); i++) {
            if(CommandCenter.getInstance().getKeySequency().get(i) != SPECIAL_SEQUENCE[j]){
                return;
            }
            j += 1;
        }
        CommandCenter.getInstance().setSpecialFalcon(true);
        Sound.playSound("specialUnlocked.wav");
        CommandCenter.getInstance().getKeySequency().clear();
    }


	@Override
	public void keyPressed(KeyEvent e) {
		Falcon fal = CommandCenter.getInstance().getFalcon();
		int nKey = e.getKeyCode();
        if(CommandCenter.getInstance().isGameOver()){
            CommandCenter.getInstance().getKeySequency().add(nKey);
            checkSpecialFalcon();
        };

		if (nKey == START && CommandCenter.getInstance().isGameOver())
			CommandCenter.getInstance().initGame();

		if (fal != null) {
			switch (nKey) {
				case PAUSE:
					CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
					if (CommandCenter.getInstance().isPaused())
						//stopLoopingSounds(clpMusicBackground, clpThrust);
					break;
				case QUIT:
					System.exit(0);
					break;
				case UP:
					fal.goUp();
					break;
				case DOWN:
					fal.goDown();
					break;
				case LEFT:
					fal.rotateLeft();
					break;
				case RIGHT:
					fal.rotateRight();
					break;
                
                case LASERCANNON:
                    // fire laser cannon if: player has any; some frames has passed since last fire
                    if(CommandCenter.getInstance().getNumLaser() > 0 
                    && (nFrame - fal.getFramesSinceLastLaser()) > Falcon.getFRAMES_BETWEEN_LASERS())
                    {
                        CommandCenter.getInstance().getOpsQueue().enqueue(new Laser(fal), GameOp.Action.ADD);
                        CommandCenter.getInstance().updateNumLaser(-1);
                        fal.setFramesSinceLastLaser(nFrame);
                        Sound.playSound("laserCannon.wav");
                    }
                    break;
				default:
					break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Falcon fal = CommandCenter.getInstance().getFalcon();
		int nKey = e.getKeyCode();
		//show the key-code in the console
		System.out.println(nKey);

		if (fal != null) {
			switch (nKey) {

                case FIRE:
					CommandCenter.getInstance().getOpsQueue().enqueue(new Bullet(fal), GameOp.Action.ADD);
					Sound.playSound("bullet.wav");
					break;
                    
                case LASERCANNON:
                    if(!(CommandCenter.getInstance().getLaserP1() == null)){
                        CommandCenter.getInstance().getOpsQueue().enqueue(CommandCenter.getInstance().getLaserP1(), GameOp.Action.REMOVE);
                    }
                    break;
				
                case LEFT:
					fal.stopLeft();
					break;
				
                case RIGHT:
					fal.stopRight();
					break;
				
                 case UP:
					fal.stopUp();
					break;
				case DOWN:
					fal.stopDown();;
					break;

				case MUTE:
					CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());

					if (!CommandCenter.getInstance().isMuted()){
						stopLoopingSounds(clpMusicBackground);
					}
					else {
						clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
					}
					break;

				default:
					break;
			}
		}
	}

	@Override
	// does nothing, but we need it b/c of KeyListener contract
	public void keyTyped(KeyEvent e) {
	}

    public static long getnFrame() {
        return nFrame;
    }

    public static int getAnyDelay(){
        return ANI_DELAY;
    }

    public static int getFPS(){
        return FPS;
    }

    /* Auxiliar method to generate bounded integers (inclusive) 
    @param lBound: integer generated >= lBound
    @param uBound: integer generated <= uBound
    @return: a random integer
    */
   public static int generateRandomInt(int lBound, int uBound){
        return Game.R.nextInt(uBound + 1 - lBound) + lBound;
   }
}


