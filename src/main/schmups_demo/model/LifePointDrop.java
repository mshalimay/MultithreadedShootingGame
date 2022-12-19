package schmups_demo.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LifePointDrop extends Sprite {
    private static final int TICK_GLOW = 5;
    private static Color fillColor;

	public LifePointDrop(Asteroid asteroid) {

        // Generic settings ----------------------------------------------------
        // set team
		setTeam(Team.FLOATER);

		// set time to expiry in frames (if any)
        setExpiry(80);

        //set Position, Spin, size, orientation
        setRadius(10);
        setCenter(asteroid.getCenter());
        setOrientation(asteroid.getOrientation());
        //setSpin(somePosNegValue(0));
		
        // set velocities
        setDeltaX(0);
		setDeltaY(0);

        // Cartesian points drawing polygon (if any)
		List<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(0, 1)); //top point
        pntCs.add(new Point(1, 0)); // right point
        pntCs.add(new Point(0, -1)); // botton point
        pntCs.add(new Point(-1, 0)); // left point

        setCartesians(pntCs);

        // Border color (if any)
        setColor(Color.BLACK);

        // Specific settings ---------------------------------------------------
        fillColor = Color.PINK;
        setDAMAGE(1);
	}

    @Override
    public void draw(Graphics g) {
        // draw object and border
        super.draw(g);
        Graphics2D g2d = (Graphics2D) g;
        setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        render(g2d);

        // fill object
        // fill w/ alternating colors for glowing effect
        if(getCurNFrame() - getPrevNFrame() > TICK_GLOW){
            fillColor =  fillColor.equals(Color.PINK) ? Color.white : Color.PINK;
            setPrevNFrame(getCurNFrame());
        }
        g2d.setColor(fillColor);
        g2d.fillPolygon(getPolygon());
    }
}
