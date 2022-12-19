package schmups_demo.view;


import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import schmups_demo.controller.Game;
import schmups_demo.model.Background;
import schmups_demo.model.CommandCenter;
import schmups_demo.model.Movable;



public class GamePanel extends Panel {

	// ==============================================================
	// FIELDS
	// ==============================================================

	// The following "off" vars are used for the off-screen double-buffered image.
	private Image imgOff;
	private Graphics grpOff;

	private GameFrame gmf;
	private Font fnt = new Font("SansSerif", Font.BOLD, 15);
	private Font fntBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
	private FontMetrics fmt;
	private int fontWidth;
	private int fontHeight;
	private String strDisplay = "";


    private Color LIFE_BAR_FILL;
    private Color LIFE_BAR_BORDER = new Color(255, 204, 204);
    private static int LIFE_BAR_SIZE = 4;

	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================

	public GamePanel(Dimension dim) {
		gmf = new GameFrame();
		gmf.getContentPane().add(this);
		gmf.pack();
		initView();

		gmf.setSize(dim);
		gmf.setTitle("UChmups demo");
		gmf.setResizable(false);
		gmf.setVisible(true);
		setFocusable(true);
	}


	// ==============================================================
	// methods to draw in the screen
	// ==============================================================

	private void drawScore(Graphics g) {
		g.setColor(Color.white);
		g.setFont(fnt);
		if (CommandCenter.getInstance().getScore() != 0) {
			g.drawString("SCORE :  " + CommandCenter.getInstance().getScore(), fontWidth, fontHeight);
		} else {
			g.drawString("NO SCORE", fontWidth, fontHeight);
		}
	}

    public void drawLifePoints(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // lifebar position
        int xPos = (int) (Game.DIM.getWidth()/2);
        int yPos = 10;

        // draw the life bar rectangle
        g2d.setColor(LIFE_BAR_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(xPos, yPos, LIFE_BAR_SIZE * (CommandCenter.getInstance().getMAX_LIFE_POINTS()), 20);
		
        // fill the rectangle with current life points
        if(CommandCenter.getInstance().getSpecialFalcon()){
            LIFE_BAR_FILL = new Color(0, 204, 0);
        }
        else{
            LIFE_BAR_FILL = new Color(255, 204, 204);
        }
        g2d.setColor(LIFE_BAR_FILL);
		g.fillRect(xPos, yPos, LIFE_BAR_SIZE*(CommandCenter.getInstance().getLife()), 20);
	}

    private void drawCurLevel(Graphics g) {
		g.setColor(Color.white);
		g.setFont(fnt);
		g.drawString("Level :  " + CommandCenter.getInstance().getLevel(), Game.DIM.width/8, fontHeight);
	}
    private void drawNumLasers(Graphics g) {
		g.setColor(Color.white);
		g.setFont(fnt);
		g.drawString("Laser Cannons remaning :  " + CommandCenter.getInstance().getNumLaser(), Game.DIM.width/4, fontHeight);
	}

    // ==============================================================
	// Update Panel - animate the game scene
	// ==============================================================

	@SuppressWarnings("unchecked")
	public void update(Graphics g) {
		//create an image off-screen
		imgOff = createImage(Game.DIM.width, Game.DIM.height);
		//get its graphics context
		grpOff = imgOff.getGraphics();

		//Fill the off-screen image background with black.
		grpOff.setColor(Color.BLACK);
		grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
		drawScore(grpOff);

        // if game over, draw Game Over screen
		if (CommandCenter.getInstance().isGameOver()) {
			String title = !CommandCenter.getInstance().getIsFirstGame() ? "GAME OVER":"UChmups Demo";
            displayTextOnScreen(grpOff,
                    title,
					"Use the arrow keys to move the ship",
					"Use the space bar to fire",
                    "Use the F key to shoot the Cannon Laser",
					"'S' to Start",
					"'P' to Pause",
					"'Q' to Quit",
					"Destroy enemy ships to drop Cannon Lasers",
					"Destroy radioactive (green) asteroids to drop life points"
			);
        // if game paused, draw Game paused screen
		} else if (CommandCenter.getInstance().isPaused()) {
			strDisplay = "Game Paused";
			grpOff.drawString(strDisplay,
					(Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4);
		}

		// if playing, draw  movables in the screen (also lifepoints, level, num lasers)
		else {
            drawLifePoints(grpOff);
            drawCurLevel(grpOff);
            drawNumLasers(grpOff);
            for (Background background : CommandCenter.getInstance().getBackgrounds()){
                background.move();
                background.draw(grpOff);
            }
			iterateMovables(grpOff,
					CommandCenter.getInstance().getMovDebris(),
					CommandCenter.getInstance().getMovFloaters(),
					CommandCenter.getInstance().getMovFoes(),
					CommandCenter.getInstance().getMovFriends());
		}

		//after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
		// of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
		// without the use of a double-buffered off-screen image, you will see flickering.
		g.drawImage(imgOff, 0, 0, this);
	}

    // iterate over all movables: (i) moving them according to the game state and (ii) drawing in the screen the result
	@SafeVarargs
	private final void iterateMovables(final Graphics g, List<Movable>... arrayOfListMovables) {

        // define a function with two arguments to pass to the stream
		BiConsumer<Graphics, Movable> moveDraw = (grp, mov) -> {
			mov.move();
			mov.draw(grp);
		};

		//we use flatMap to flatten the List<Movable>[] passed-in above into a single stream of Movables
		Arrays.stream(arrayOfListMovables) //Stream<List<Movable>>
				.flatMap(Collection::stream) //Stream<Movable>
				.forEach(m -> moveDraw.accept(g, m));

	}

	private void initView() {
		Graphics g = getGraphics();            // get the graphics context for the panel
		g.setFont(fnt);                        // take care of some simple font stuff
		fmt = g.getFontMetrics();
		fontWidth = fmt.getMaxAdvance();
		fontHeight = fmt.getHeight();
		g.setFont(fntBig);                    // set font info
	}


	// This method draws some text to the middle of the screen before/after a game
	private void displayTextOnScreen(final Graphics graphics, String... lines) {

		AtomicInteger spacer = new AtomicInteger(0);
		Arrays.stream(lines)
				.forEach(s -> {
							graphics.drawString(s, (Game.DIM.width - fmt.stringWidth(s)) / 2,
									Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40));
						}
				);
	}
}
