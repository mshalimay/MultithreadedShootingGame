package schmups_demo.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bullet extends Sprite {
    private static final double FIRE_POWER = 40.0;
    private double specialMutliplier;

    public Bullet(Sprite sprite) {
        setTeam(Team.FRIEND);

        //a bullet expires after 20 frames. set to one more than frame expiration
        setExpiry(50);

         // set Position, Spin, size
        if(CommandCenter.getInstance().getSpecialFalcon()){
            setRadius(30);
            setCenter(new Point(sprite.getCenter().x, sprite.getCenter().y)); //everything is relative to the falcon ship that fired the bullet
            specialMutliplier = 2.5;
        }
        else{
            setRadius(12);
            setCenter(new Point(sprite.getCenter().x, sprite.getCenter().y+15)); //everything is relative to the falcon ship that fired the bullet
            specialMutliplier = 1;
        }

        //set the bullet orientation to the falcon (ship) orientation
        setOrientation(sprite.getOrientation());
        setDeltaX(sprite.getDeltaX() +
                Math.cos(Math.toRadians(sprite.getOrientation())) * FIRE_POWER * specialMutliplier);
        setDeltaY(sprite.getDeltaY() +
                Math.sin(Math.toRadians(sprite.getOrientation())) * FIRE_POWER * specialMutliplier);
      
        
        // damage
        setDAMAGE(-1);

        // Drawing
        List<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(0, 3)); //top point
        pntCs.add(new Point(1, -1));
        pntCs.add(new Point(0, -2));
        pntCs.add(new Point(-1, -1));

        setCartesians(pntCs);
    }

    @Override
      public void draw(Graphics g) {
          //set custom color
          super.draw(g);
          Graphics2D g2d = (Graphics2D) g;
          
          g2d.setColor(Color.black);
          
          g2d.setStroke(new BasicStroke(1));
          render(g2d);

          if(CommandCenter.getInstance().getSpecialFalcon()){
            g2d.setColor(Color.GREEN);
          }else{
            g2d.setColor(Color.WHITE);
          }
          g2d.fillPolygon(getPolygon());
      }
}
