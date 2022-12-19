package schmups_demo.model;

import java.awt.*;
import java.awt.image.BufferedImage;

import schmups_demo.controller.Game;

public class EnemyMissile extends Sprite {

    public EnemyMissile(Sprite sprite){
        // set team
        setTeam(Team.FOE);
               
        //set Position, size, orientation
        setCenter(sprite.getCenter()); //everything is relative to the ship that fired the bullet
        setRadius(CommandCenter.getInstance().getMissileImg().getWidth()/2);
        setOrientation(sprite.getOrientation()); //set the bullet orientation to the falcon (ship) orientation        

        // Specific settings ---------------------------------------------------
        setDAMAGE(-5);
    }

    public EnemyMissile(EnemyShip enemyShip){
        // set team
        setTeam(Team.FOE);
               
        //set Position, size, orientation
        setCenter(enemyShip.getCenter()); //everything is relative to the ship that fired the bullet
        setRadius(CommandCenter.getInstance().getMissileImg().getWidth()/2);
        setOrientation(enemyShip.getOrientation()); //set the bullet orientation to the falcon (ship) orientation        

        // Specific settings ---------------------------------------------------
        setDAMAGE(-5);
    }

    public EnemyMissile(Sprite boss, Point center){
        // set team
        setTeam(Team.FOE);
        //set Position, size, orientation
        setCenter(center); //everything is relative to the ship that fired the bullet
        setRadius(CommandCenter.getInstance().getMissileImg().getWidth()/2);
        setOrientation(boss.getOrientation()); //set the bullet orientation to the falcon (ship) orientation        

        // Specific settings ---------------------------------------------------
        setDAMAGE(-5);
    }

    
    @Override
    public void move(){
        // calculate coordinates relative to Falcon for tracking 
        int deltaX = CommandCenter.getInstance().getFalcon().getCenter().x - this.getCenter().x;
        int deltaY = CommandCenter.getInstance().getFalcon().getCenter().y - this.getCenter().y;
        double radians = Math.atan2(deltaY, deltaX);
        
        // missile velocity
        setDeltaX(getDeltaX() + Math.cos(radians) * 4/3);
        setDeltaY(getDeltaY() + Math.sin(radians) * 4/3);
        
        setCenter(new Point((int) (getCenter().x + getDeltaX()), (int) (getCenter().y + getDeltaY())));

        //left-bounds reached -> REMOVE
        // TODO: change condition to if passed falcon by X distance? Include up and low bounds?

        if (getCenter().x + getRadius() + 10 < 0 || getCenter().y + getRadius() >= Game.DIM.height || getCenter().y + getRadius() <= 0) 
        {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage img = CommandCenter.getInstance().getMissileImg();
        // TODO add the radius manually and change image width here?
        drawImage(img, g);
    }
}
    

