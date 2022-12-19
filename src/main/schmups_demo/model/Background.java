package schmups_demo.model;
import java.awt.image.BufferedImage;

import schmups_demo.controller.Game;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

import java.awt.*;

public class Background extends Sprite{
    private static final int SPEED = 20;

    public Background(double Xpos, double yPos){
        setTeam(Team.DEBRIS);
        // asteroids come from the RHS
        setCenter(new Point((int) Xpos, (int) yPos));

    }

    @Override
    public void move() {
        // Rolling backgrounds
        // backgrounds will always move left
        double newXPos = getCenter().x - SPEED;
        setCenter(new Point((int) newXPos, (int) getCenter().y));
        
        // but when one of the backgrounds is completely off screen to the left, put it at the start
        // of the screen to the right
        if (getCenter().x <= - Game.DIM.width)
            setCenter(new Point((int) Game.DIM.width, (int) getCenter().y));
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        BufferedImage img = CommandCenter.getInstance().getBgImg();
		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.toRadians(getOrientation()), img.getWidth()/2, img.getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		img = op.filter(img, null);

        // if-else to draw the second background a little larger; this seem to be necessary bcs of the "circle" style of sprites; 
        // even after placing one background coming exactly after the other, a gap still appeared between the two
        // making both greater is no good: the game becomes slower
     
        g2d.drawImage(CommandCenter.getInstance().getBgImg(),getCenter().x - getRadius(), getCenter().y - getRadius(), 
             Game.DIM.width, Game.DIM.height, null);
        
    }
    
}
