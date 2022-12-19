package schmups_demo.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LaserDrop extends Sprite {
    private static int TICK_GLOW = 5; // glows each 5 frames
    private static Color fillColor;
    
	public LaserDrop(EnemyShip enemyShip) {

        // Generic settings ----------------------------------------------------

        // set team
		setTeam(Team.FLOATER);

		// set time to expiry in frames (if any)
        setExpiry(80);

        //set Position, size, orientation, spin
        setCenter(enemyShip.getCenter());
        setRadius(10);
        setOrientation(enemyShip.getOrientation());
        //setSpin(somePosNegValue(0));
		
        // set velocities
        setDeltaX(0);
		setDeltaY(0);

        // Cartesian points to draw polygon/object
        List<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(0, 1)); //top point
        pntCs.add(new Point(1, 0)); // right point
        pntCs.add(new Point(0, -1)); // botton point
        pntCs.add(new Point(-1, 0)); // left point

        setCartesians(pntCs);

        // Specific settings ---------------------------------------------------
        fillColor = Color.GREEN;
	}

    @Override
    public void draw(Graphics g) {
        // draw polygon/border
        super.draw(g);
        Graphics2D g2d = (Graphics2D) g;
        setColor(Color.black);
        g2d.setStroke(new BasicStroke(1));
        render(g2d);

        // fill object
        // fill w/ alternating colors for glowing effect
        if(getCurNFrame() - getPrevNFrame() > TICK_GLOW){
            fillColor =  fillColor.equals(Color.GREEN) ? Color.white : Color.GREEN;
            setPrevNFrame(getCurNFrame());
        }
        g2d.setColor(fillColor);
        g2d.fillPolygon(getPolygon());
    }
}
