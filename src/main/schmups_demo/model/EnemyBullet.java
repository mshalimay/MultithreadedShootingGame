package schmups_demo.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EnemyBullet extends Sprite {
    private static final double FIRE_POWER = 40.0;

    public EnemyBullet(Sprite sprite) {
        // set team 
        setTeam(Team.FOE);

        // set time to expiry in frames (if any)
        setExpiry(51);
        
        // set Position, Spin, size
        setRadius(12);
            // FIXME mudar o hard coding
        setCenter(new Point(sprite.getCenter().x, sprite.getCenter().y - 25)); //everything is relative to the ship that fired the bullet
        setOrientation(sprite.getOrientation());
                
        // set velocities
        setDeltaX(-FIRE_POWER);
        int deltaX = CommandCenter.getInstance().getFalcon().getCenter().x - sprite.getCenter().x;
        int deltaY = CommandCenter.getInstance().getFalcon().getCenter().y - sprite.getCenter().y;
        double radians = Math.atan2(deltaY, deltaX);
        setDeltaY(getDeltaY() + Math.sin(radians) * 3);

        // Cartesian points to draw polygon/object
        List<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(0, 3)); //top point
        pntCs.add(new Point(1, -1));
        pntCs.add(new Point(0, -2));
        pntCs.add(new Point(-1, -1));
        setCartesians(pntCs);

        // Specific settings ---------------------------------------------------
        setDAMAGE(-1);
    }

    public EnemyBullet(Sprite boss, Point center) {
        // set team 
        setTeam(Team.FOE);

        // set time to expiry in frames (if any)
        setExpiry(51);
        
        // set Position, Spin, size
        setRadius(10);
            // FIXME mudar o hard coding
        setCenter(center); //everything is relative to the ship that fired the bullet
        setOrientation(180);
                
         // set velocities
        setDeltaX(-FIRE_POWER);
        
        int deltaX = CommandCenter.getInstance().getFalcon().getCenter().x - center.x;
        int deltaY = CommandCenter.getInstance().getFalcon().getCenter().y - center.y;
        double radians = Math.atan2(deltaY, deltaX);
        setDeltaY(getDeltaY() + Math.sin(radians) * 3);
        
        // Cartesian points to draw polygon/object
        List<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(0, 3)); //top point
        pntCs.add(new Point(1, -1));
        pntCs.add(new Point(0, -2));
        pntCs.add(new Point(-1, -1));
        setCartesians(pntCs);

        // Specific settings ---------------------------------------------------
        setDAMAGE(-1);
    }

    @Override
      public void draw(Graphics g) {
          //set custom color
          super.draw(g);
          Graphics2D g2d = (Graphics2D) g;
          g2d.setColor(Color.black);
          g2d.setStroke(new BasicStroke(1));
          render(g2d);
          g2d.setColor(Color.RED);
          g2d.fillPolygon(getPolygon());
      }
}
