package schmups_demo.model;

import lombok.Data;
import schmups_demo.controller.Game;
import schmups_demo.controller.Sound;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {


    // Game control - counters -------------------------------------------
    private int nSpecialWeapons;
    private int nAsteroids;
    private int nEnemyShips;
    private int numEnemyDestroyed;
    private int prevNumEnemyDestroyed;
    private int numPlayers;
    private int numBoss;
    private int enemDestrSinceLastBoss;

     // Game control - max values for counters -------------------------------------------------------------------------
     private int MAX_LIFE_POINTS = 100;
     private int MIN_LIFE_POINTS = 0;

     private int MAX_NUM_LASER = 5;
     private int MIN_NUM_LASER = 0;

    // Game control - other --------------------------------------------------------------------------------------------
	private int life;
    private int level = 1;
	private long score;
	private int numLaser;
    private boolean paused;
	private boolean muted;
    private boolean isFirstGame = true; // used to track if it is the first of all games 
    public boolean isPlaying = false;
    private boolean specialFalcon = false; // special falcon; activated if special sequnce entered in start screen
    private boolean resetSpecialFalcon = false; // used to reset special falcon value just one time in GameOver-Update loop
    public ArrayList<Integer> keySequency = new ArrayList<>(); // used to check if user entered special sequence

    // Initialization (when player enter 'S') --------------------------------------------------------------------------
    private int INIT_ASTEROIDS = 20; // number of asteroids immeadiately spawn at start of each game
    private int INIT_ENEMY_SHIPS = 5; // number of enemy ships immeadiately spawn at start of each game

    // image containers ------------------------------------------------------------------------------------------------
	BufferedImage[] enemyShipImgs =  new BufferedImage[4];
    BufferedImage[][] meteorImgs = new BufferedImage[4][4];
    private BufferedImage[] bossImgs = new BufferedImage[3];
    private BufferedImage missileImg;
	private BufferedImage bgImg;  
	
    
    // TODO for two player game
    // BufferedImage[] playerShipImgs =  new BufferedImage[2];

	//the falcon is located in the movFriends list, but since we use this reference a lot, we keep track of it in a //separate reference. Use final to ensure that the falcon ref always points to the single falcon object on heap
	//Lombok will not provide setter methods on final members
        // TODO the image reference is ugly, results from the original code structure;  // ideally, change all images to static, include them in the constructor  // and change the calls to CommandCenter in other parts of the code
	private Falcon falcon; //= new Falcon(loadGraphic("playerShip1_red.png"));

	// Lists of MOVABLE objects
	private final List<Movable> movDebris = new LinkedList<>();
	private final List<Movable> movFriends = new LinkedList<>();
	private final List<Movable> movFoes = new LinkedList<>();
	private final List<Movable> movFloaters = new LinkedList<>();
    private final List<Background> backgrounds = new LinkedList<>(); // List of Background (control rolling background))
    private Laser laserP1; // Laser object from player 1 (helps add/removing Laser faster)

    // Queue of game operations to perform
	private final GameOpsQueue opsQueue = new GameOpsQueue();

	//singleton
	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {}

	//this class maintains game state - make this a singleton.
	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}

// =====================================================================================================================
// Initialize game
// =====================================================================================================================
    // ran when user enter 's'
    public void initGame(){
        isPlaying = true;
        Sound.playSound("startGame.wav");
        loadGraphics();
        clearAll();
        setLevel(1);
        setScore(0);
        if(specialFalcon){
            setNumLaser(MAX_NUM_LASER);
            setLife(MAX_LIFE_POINTS*2);
        }
        else{
            setNumLaser(1);
            setLife(MAX_LIFE_POINTS);
        }
        setPaused(false);
        setNumPlayers(1);
        setNumBoss(0);
        
        // add a rolling background
        backgrounds.add(new Background(0, 0));
        backgrounds.add(new Background(Game.DIM.getWidth(), 0));

        // initialize objects
        initFalcon();
        initAsteroids();
        initEnemyShips();
        this.isFirstGame = false; // false forever after game started at least one time
        resetSpecialFalcon = true;
        keySequency.clear();
    }
    
       
    // initialize falcon ship
	public void initFalcon(){

        if(specialFalcon){
            falcon = new Falcon(loadGraphic("brazilianFalcon.png"));
        } 
        else{
            falcon = new Falcon(loadGraphic("playerShip1_red.png"));
        }
        
        if (isGameOver()) return;		
        //put falcon in the middle left of the screen, oriented to the right
		falcon.setCenter(new Point(getFalcon().getRadius(),  Game.DIM.height/2));
		falcon.setOrientation(360);
		falcon.setDeltaX(0);
		falcon.setDeltaY(0);
        opsQueue.enqueue(falcon, GameOp.Action.ADD);
	}

    // initialize some asteroids
    public void initAsteroids(){
        for (int i = 0; i < INIT_ASTEROIDS; i++) {
            opsQueue.enqueue(new Asteroid(0), GameOp.Action.ADD);
        }
    }

    // initialize some enemy ships
    public void initEnemyShips(){
        for (int i = 0; i < INIT_ENEMY_SHIPS; i++) {
            opsQueue.enqueue(new EnemyShip(getFalcon()), GameOp.Action.ADD);
        }
    }

	private void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
        backgrounds.clear();
	}

	public boolean isGameOver() {		// if lifepoints over, then game over
        if(getLife() <= MIN_LIFE_POINTS){
            isPlaying = false;
            if(resetSpecialFalcon){
                specialFalcon = false;
                resetSpecialFalcon = false;
                keySequency.clear();
                Sound.playSound("gameOver.wav");
            }
            return true;
        }
        return false;
	}

// =====================================================================================================================
// Image control
// =====================================================================================================================
    
    // loads all images used in the game
    private void loadGraphics() {
        loadEnemyShipImgs();
        loadMeteorImgs();
        loadBossImgs();
        
        missileImg = loadGraphic("missileFoe.png");
        bgImg = loadGraphic("bgImg.png");
    
    }

    // loads all enemy ship images
    private void loadEnemyShipImgs(){
        enemyShipImgs[0] = loadGraphic("enemyBlack1.png");
        //enemyShipImgs[0] = loadGraphic("boss.gif");
        enemyShipImgs[1] = loadGraphic("enemyBlue2.png");
        enemyShipImgs[2] = loadGraphic("enemyGreen3.png");
        enemyShipImgs[3] = loadGraphic("enemyRed4.png");
    }

    // loads all boss images
    private void loadBossImgs(){
        bossImgs[0] = loadGraphic("boss1.png");
        bossImgs[1] = loadGraphic("boss2.png");
        bossImgs[2] = loadGraphic("boss3.png");
    }

    // loads all meteor images
    private void loadMeteorImgs(){
        meteorImgs[3][0] = loadGraphic("meteorBrown_big1.png");
        meteorImgs[3][1] = loadGraphic("meteorBrown_big2.png");
        meteorImgs[3][2] = loadGraphic("meteorGrey_big3.png");
        meteorImgs[3][3] = loadGraphic("meteorGrey_big4.png");

        meteorImgs[2][0] = loadGraphic("meteorBrown_med1.png");
        meteorImgs[2][1] = loadGraphic("meteorBrown_med3.png");
        meteorImgs[2][2] = loadGraphic("meteorGrey_med1.png");
        meteorImgs[2][3] = loadGraphic("meteorGrey_med2.png");

        meteorImgs[1][0] = loadGraphic("meteorBrown_small1.png");
        meteorImgs[1][1] = loadGraphic("meteorBrown_small2.png");
        meteorImgs[1][2] = loadGraphic("meteorGrey_small1.png");
        meteorImgs[1][3] = loadGraphic("meteorGrey_small2.png");
        
        meteorImgs[0][0] = loadGraphic("meteorBrown_tiny1.png");
        meteorImgs[0][1] = loadGraphic("meteorBrown_tiny2.png");
        meteorImgs[0][2] = loadGraphic("meteorGrey_tiny1.png");
        meteorImgs[0][3] = loadGraphic("meteorGrey_tiny2.png");
    }

    // auxiliary method to load an image into Java
    private BufferedImage loadGraphic(String imgName) {
		BufferedImage img;
		try {
			img = ImageIO.read(CommandCenter.class.getResourceAsStream("/resources/img/" + imgName));
		}
		catch (IOException e) {
			e.printStackTrace();
			img = null;
		}
		return img;
	}

// =====================================================================================================================
// Special getters/setters/updaters
// =====================================================================================================================

    public void updateLife(int lifePoints){
        if(getLife() + lifePoints > MAX_LIFE_POINTS){
            setLife(MAX_LIFE_POINTS);
        }
        else if(getLife() + lifePoints < MIN_LIFE_POINTS){
            setLife(MIN_LIFE_POINTS);
        }
        else{
            setLife(getLife() + lifePoints);
        }
    }

    public void updateNumLaser(int laserPoints){
        if(getNumLaser() + laserPoints > MAX_NUM_LASER){
            setNumLaser(laserPoints);
        }
        else if(getNumLaser() + laserPoints < MIN_NUM_LASER){
            setNumLaser(MIN_NUM_LASER);
        }
        else{
            setNumLaser(getNumLaser() + laserPoints);
        }
    }

    public void updateNumEnemyDestroyed(int add){
        setNumEnemyDestroyed(getNumEnemyDestroyed() + add);
    }

    public void updateScore(int add){
        setScore(getScore() + add);
    }

    public void updateNumBoss(int add){
        setNumBoss(getNumBoss() + add);
    }

    public void updateNEnemyShips(int add){
        setNEnemyShips(nEnemyShips + add);
    }

    // Get images --------------------------------------------------------------
    public BufferedImage getEnemyShipImg(){
        return enemyShipImgs[Game.R.nextInt(enemyShipImgs.length)];
    }

    public BufferedImage getBossImg(){
        return bossImgs[Game.R.nextInt(bossImgs.length)];
    }

    public BufferedImage getMeteorImg(int size){
        return meteorImgs[size][Game.R.nextInt(meteorImgs[size].length)];
    }
   
    public boolean getIsFirstGame(){
        return isFirstGame;
    }

    public List<Movable> getMovables() {
		List<Movable> movList = new ArrayList<>();
		movList.addAll(movDebris);
		movList.addAll(movFriends);
		movList.addAll(movFoes);
		movList.addAll(movFloaters);
		return movList;
	}

    public boolean isPlaying(){
        return isPlaying;
    }

    public boolean getSpecialFalcon(){
        return specialFalcon;
    }

     // TODO for two player game
    // public BufferedImage getPlayerImg(){
    //     if(numPlayers==1){
    //         return playerShipImgs[0];
    //     }
    //     else{
    //         return enemyShipImgs[1];
    //     }
    // }
    
}
