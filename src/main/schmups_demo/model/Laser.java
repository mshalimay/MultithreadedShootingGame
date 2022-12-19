package schmups_demo.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import schmups_demo.controller.Game;

public class Laser extends Sprite {
    private static int FRAME_BETWEEN_GLOW = 25 / Game.getAnyDelay(); // glows each 25 miliseconds
    private static int FRAMES_EXPIRE_LASER = (int) (2.5 * Game.getFPS()); // expires after 3 SECONDS

    private Sprite sprite; // ship firing the laser
    private Color fillColor = Color.CYAN;

    public Laser(Sprite sprite) {
        
        setTeam(Team.FRIEND);
        this.sprite = sprite;

        // laser expire after ...
        setExpiry(FRAMES_EXPIRE_LASER);
        setRadius((int) (Game.DIM.getWidth()/2));
        
        //everything is relative to the falcon ship that fired the bullet

        //set orientation to the falcon (ship) orientation
        setOrientation(sprite.getOrientation());

        // damage:
        setDAMAGE(-1);

        // Draw polygon -------------------------------------------
        int y = getRadius()/4; // laser bean WIDTH
        int x = 4; // laser bean HEIGHT
		
        List<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(x, y)); // upper right point
        pntCs.add(new Point(x, -y)); // lower right point
        pntCs.add(new Point(-x, -y)); // lower left point
        pntCs.add(new Point(-x, y)); // upper left point
        setCartesians(pntCs);

        // Specific settings  -------------------------------------------
        this.fillColor = Color.WHITE;
    }

    @Override
    public void move() {
        if(CommandCenter.getInstance().getSpecialFalcon())
        {
            setCenter(new Point(sprite.getCenter().x - 50 + (int) (1.12* getRadius()), sprite.getCenter().y));
        }
        else{
            setCenter(new Point(sprite.getCenter().x -50+ (int) (1.12* getRadius()), sprite.getCenter().y+12));
        }

            //expire (decrement expiry) on short-lived objects only; default value of expiry is zero, so apply only to expiring sprites
        if (getExpiry() > 0) expire();
    }

    @Override
      public void draw(Graphics g) {
        //set custom color
        super.draw(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(1));
        g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        render(g2d);
          
        // fill object
        // fill w/ alternating colors for glowing effect
        if(getCurNFrame() - getPrevNFrame() > FRAME_BETWEEN_GLOW){
            if(CommandCenter.getInstance().getSpecialFalcon()){
                fillColor =  fillColor.equals(Color.GREEN) ? Color.white : Color.GREEN;
            }
            else
            {
                fillColor =  fillColor.equals(Color.CYAN) ? Color.white : Color.CYAN;
            }
            setPrevNFrame(getCurNFrame());
        }
        g2d.setColor(fillColor);
        g2d.fillPolygon(getPolygon());
      }
}
