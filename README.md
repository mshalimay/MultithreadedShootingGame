# Shmups Demo
To run the game, open the `Game` class and run the `main` method

----
# Game Description

"Schmups" (abbreviation of "Shoot 'em ups") is a term used to refer to classic space-shooter games from the 80's and 90's. Examples include 'Space Invaders', 'Gradius', 'Asteroids', among many. 

This is a demo for a SChmups game developed for MPCS 51036 Fall 2022.

In this game, you are the controller of a ship traveling the space, having to dodge asteroids, meteors and to face enemies trying to destroy the ship. 

Enemies shoot bullets and guided missiles. To protect yourself, you may shoot bullets or the cannon laser. To shoot bullets, press the ``SPACE`` key. To shoot *Cannon Lasers*, press and *hold* the ``F`` key. 
Warning: cannon lasers are strong and useful when many enemies are on the screen, but their quantity is limited. 

Your ship is hurt whenever it is hit by a bullet, missile, asteroid or meteor. If you ran out of life points (shown in the bar in the top of the screen), game over.

By destroying radioactive (green) asteroids, you can get lifepoints back and by destroying enemy ships, you may get additional laser cannons.

As you get deeper into the galaxy (i.e., destroy more enemies), the game level increases and with it, also increases the appearance of asteroids, meteors and enemies, and the size of meteors and asteroids. 

If many enemies are destroyed, bigger enemy ships might be sent to destroy your ship - beware.

Since we are in the world cup, a special treat is reserved for those who played super star soccer in the 90's: by entering a special sequence of keys in the startup screen, you might get a stronger (Brazilian) ship to drive through the galaxy. 
He is smaller (easier to dodge projectiles!), faster, has larger bullets and can shoot them faster, starts with more lasers, and has the double of life points.


-----

# References
This games builds heavily on Adam Gerber's architechture and utilities for stream control and collision detection. 

sounds: https://mixkit.co/free-sound-effects/
sounds and images: https://opengameart.org/

to edit images: https://microsoft-paint-3d.en.softonic.com/
to edit audios: https://www.audacityteam.org/

# For developers
Obs: many variables serves the purpose of altering the *difficulty* of the game. To find the ones that may be tweaked for so, do a CTRL+F for `@DIFFICULTY_LEVEL` in the whole code. 

Terminology:
- *Sprite*s are the visual objects populating the game. E.g.:  ships,  asteroids, items are all sprites
- *Raster* sprites in this game are sprites based on *png* images
- *Vector* sprites in this game are sprites directly draw on the screen using Java's graphic libraries

#### Screen
- Objects appear in the RHS side from the screen and move to the LHS
- Off screen behavior
	- Objects If asteroids, meteors or space ships try to get off screen, they will bounce back to the screen. 
	- Asteroids/Meteors may bounce back with different velocities
	- If missiles or bullets get off screen, they are removed from the game
	- Falcon can move anywere in the screen, but if it is close to off bounds, game will stop processing arrow keys up/down/left/right depending on the case
- Score, Life points, Level, Cannon Lasers
	- The state of the game is draw in the top of the screen
	- LifePoints are drawn as a bar which is a function of the MAX_LIFE_POINTS for the spaceship. The bar is emptyied if the falcon collides with objects giving `DAMAGE` 

#### Background
- There is a rolling background to give the effect the ship is travelling in the space
- This is implemented by using two movables whose sprite is the image saw in the screen (the sky with stars). 
- Initially, the first sprite is drawn occupying the whole screen and the second is drawn off screen to the right by the exact *width* of the screen. 
- Both "move left" at each frame, and when the first is off bounds to the left by the exact screen's *width* , it is repositioned at the right. This repeats during the whole game

#### Sounds
- There is a specific sound for each relevant behavior in the game, such as: 
	- `Falcon`: 1x for firing bullets, 1x for picking items in the screen, 1x when the laser cannon is shot
	- `EnemyShip` and `Boss`: 1x when the ship is destroyed, 1x when a missile is fired 
	- `Meteor` and `Asteroid`: 1x when destroyed
	- `Missile`: one when it is fired, one when collides with Falcon
	- Level up: a sound notification is played
	- Special Falcon unlocked: a sound notification is played in the start screen
	- Background music

#### Gameplay
- **Game Over**:
	- Game is Over if lifepoints run to zero
- Score
    - Score increases if player destroy `EnemyShip` (1 point) or `Boss` (5 points)
- **Game Level**:
	- Game level increases at each `CHANGE_LEVEL` = 5 `EnemmyShip` destroyed
	- A sound notification play when game level changes
	- When the game level increases:
		- the *spawn rate* and *size* of asteroids and meteors that appear on the screen increases
		- the larger size is implemented by drawing from a uniform distribution that becomes more "tilted to the right" as the level increases; 
		- a cap on the maximum size of asteroids/meteors and their spawining rate is imposed
		- The *spawn rate* of enemy ships increase
			- this is implemented by increasing the spawning a specified ammount of seconds in each level up; a cap on the maximum spawning rate is imposed

- **Boss**:
	- Each time `SPAWN_BOSS` = 30 enemies are destroyed, a boss appears on the center-right of the screen
- **Special Falcon**
	- At the start screen, if enter "UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT, A, B", the game will start with the special falcon
	- Many parts of the code have clauses to give "special powers" to the special falcon. To search for it, look for `getSpecialFalcon()`
	- Bullets, lifepoint bar and laser colors are changed (to green because it is a Brazilian spaceship)
- **Collisions**
	- Collisions are implemented in the same way as in the original game: measuring the distance of the circles surrounding the sprites 
		- This is not perfect for the raster sprites, who are squares/rectangles, but work well enough
	- But many specific behaviors were added to deal with each pair objects that may be colliding. Some cases:
		- `Falcon` is not removed from the game when collide with objects giving `DAMAGE`; instead, lifepoints are reduced. Similar to `Boss` objects
		- `EnemyShip` "bounce" when colliding with each other, to prevent them accumulating in the same square
		- `LaserDrop` and `LifePointDrop` collides only with `Falcon`
		- `Bullet` (incl. `EnemyBullet`) collides only with `SpaceShip`, `Falcon` and `Boss`. This prevents, among other things, bullets cancelling each other when many are shot by both FOEs and Friends
		- `Missile` colliding with `Falcon` bullets are destroyed
		- Etc

#### Keys
- If in the start screen, any key pressed is put into a list to check if the user entered the special combination to unlock the special Falcon
	- Once the game start, this checking is halted to minimize impact on game performance
- *UP, RIGHT, LEFT, DOWN* keys: 
    - Move the `Falcon` in the respective direction. If `Falcon` off screen, these keys are deactived accordingly (eg: if in the top screen, *UP* key will not work)
- *SPACE* key: 
    - shoots `Bullet`
- *F* key: 
    - shoots the laser cannon if the number of lasers is positive and a minimum ammount of frames since last laser was shot
	- Some specifics to implement the "press and hold", number of lasers, and "time between frames" behavior
		- Time between frames is controlled by variables `FRAMES_BETWEEN_LASERS` and `FramesSinceLastLaser` of the `Falcon` object. These are tested every time the player enters "F"
		- For number of lasers: `CommandCenter` `numLaser` variable is updated whenever the player shots a laser; if this variable == 0, the laser is not shot. Tested every time player enters "F"
		- Press and Hold is controlled by creating a variable in the `CommandCenter` that stores a (unique) reference to the laser object (can have only one in the screen for each player); if F key was pressed, but is not hold, the "key release" behavior will check if the reference exists and remove it from the game

#### Falcon
- Falcon is a raster sprite, always oriented to the right
- Falcon may move up, down, left and right, as long it is not off-bounds in the screen
- Specific settings if SPECIAL FALCON:
	- move faster, image is different

#### Asteroid
- Asteroid is a vector sprite similar to the original game. 
- Differences in appearance: color and border. To color the asteroid, original code was modified to return the actual polygon drawn to be filled with Graphics2D
- Differences in functionality: `Asteroids` do not spawn new asteroids; instead, they have a *probability* of spawning `LifePointDrop` objects when destroyed
- Asteroid size: is a function of the game level (roughly, 2^game_level, see code more details), with a maximum size specified in the code. 
- Asteroid gives a damage of 1 when collides with the Falcon, independent of the size.

#### Meteor
- `Meteor` is a raster sprite. They come in different sizes, formats and colors. For each of these, there is a different image associated
- To select the asteroid color/format, given a size (random, drawn during gameplay), a random value is drawn from a uniform distribution
- Meteors gives damage of `SIZE` when collides with Falcon; tiny asteroids give no damage

#### Enemy Ship
- `EnemyShip` is a raster sprite. They come in different colors/formats.
	- To select color/format a random value is drawn from a uniform distribution
- EnemyShips gives no damage if collides with `Falcon`
- Move: 
	- enemy ships move slowly towards of falcon to shoot bullets at it. Move faster in the Y-direction and slowly in the X-direction. If they get too close to `Falcon` in *X* direction, they move back (to the right) of the screen
- Enemy ships have a *probability* of firing bullets and missiles *at each frame*. The number of missiles/bullets they can fire at each interval is limited (see code)
- Enemy ships have a life of one; they are destroyed by one bullet of Falcon
- `Missile` are fired from the center of `EnemyShip` and accelerates towards the Falcon
- `EnemyBullets` are fired from the center of `EnemyShip` and move slowly towards the Falcon (like if they were aiming at it)
- When an enemy is destroyed, it has a *probability* of spawning `LaserDrop` objects
- Killing an EnemyShip gives 1 score points

#### Boss
- Boss are raster sprites. They come in different colors/formats.They are big sprites
	- To select color/format a random value is drawn from a uniform distribution
- Boss is fixed in the middle of the screen in the rightmost part of the screen
- A boss has more lifepoints than a typical ship. It takes more bullets to destroy it
- A boss may shoot multiple bullets and multiple missiles at the same time. At each second, there is a probability of shooting a missile, shooting a bullet and the number of missiles and bullets shot is random 
- Bullets and missiles will come not only from the center, but from the whole height of the boss
- Missiles fired follows the Falcon and acccelerate on each sprite
- Bullets fired follows the Falcon slowly
- Killing a Boss gives 5 score points
#### Bullet
- Raster sprite similar to the original game. 
- Main difference: has coloring and a DAMAGE of 1. 
- Coloring is implemented filling the polygon constructed with the x,y points
- A bullet has an expiry that is sufficient for it to walk the whole screen
- If a bullet gets off screen, it is removed from the game

#### EnemyBullet
- Similar to Bullet.
- Follows the falcon position slowly

#### EnemyMissile
- `EnemyMissile` is a raster sprite.
- Missiles start from the center of `EnemyShip` or height of `Boss` and go in the direction of the `Falcon` with accelerating velocity; they may follow the falcon even if it dodges the missile a first time (turning "back" and following the Falcon again)
- if `Missile` hits the falcon, gives DAMAGE of 5 and explode/remove from the game
- If Missile goes off screen, it is removed from the game

#### Laser
- `Laser` objects are vector sprites
	- specifically, a rectangle that start from the center of the falcon and span the whole horizontal space of the screen
- *Glowing* behavior: color alternate between white and cyan/green at each `FRAME_BETWEEN_GLOW` frames.
	- Laser glow is cyan for traditional falcon, green for special falcon
- To keep an instance of laser firing, the player must keep pressing letter F
- Laser objects expire after `FRAMES_EXPIRE_LASER` in the screen *or* if the player stops pressing *F*
- If `Laser` hits an enemy (or group of), it keep giving damage of 1 while they are in contact with FOEs

#### LifePointDrop
- Vector sprites; specifically, diamonds.
- *Glowing* behavior: color alternate between white and pink at each `FRAME_BETWEEN_GLOW` frames.
- `LifePointDrop` has `DAMAGE` of +1; i.e., if collides with Falcon, add one to life points

#### LaserDrop
- Vector sprites; specifically, diamonds
- *Glowing* behavior: color alternate between white green pink at each `FRAME_BETWEEN_GLOW` frames.
- If LaserDrop collides with the falcon, add one to the number of laser cannons (up to a maximum of 5)

#### Sprite
- This is a super class for all sprites in the game
	- additional attributes storing the number of frames (to control things like glowing between frames, shooting missiles/bullets between frames, etc)
	- Standard behavior for the MOVE() method, to bounce back to the screen instead of appearing on the other side
	- Render operation for images added
	- Create a `Polygon` object with x-y coordinates to be filled with color


#### CommandCenter
- CommandCenter has a lot of modifications to implement features of the new game. They are commented in the code
	- One of the big modifications is the addition of methods to load images for the raster sprites and to get images in a random fashion
	- Other modifications include: counters for the number of enemies, number of lasers; lifepoint control; initialization of FOEs; checkers for special Falcon, etc
