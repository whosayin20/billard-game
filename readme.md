# Billiard-game
Hüseyin Arziman & Yusuf Cetinkaya

## General
In the last Lab exercise, the goal was to implement a simplified 2D billiard game. The rendering was done with JavaFX. The physics engine dyn4j was used for all the physics. 
The intention behind this exercise was to develop a physics and rendering system into an interactive realtime game using object-oriented principles. In addition, the game logic
and rules had to be implemented. Depending on whether a foul is committed or a point is scored, the player either gets 1 point deducted, 1 point added or 0 points. Two players 
always take turns playing against each other. Basically, a game goes on indefinitely, as the balls reappear as soon as only 2 are left. The aim is therefore to win on points.


## Game

The `Game` class implements three interfaces:
- BallsCollisionListener
- BallPocketedListener
- ObjectsRestListener

The `BallsCollisionListener` gets triggered if a contact point between two bodies were made and both of them were from the `Ball` type. 

The `BallPocketedListener` gets triggered if a ball has been pocketed. This is detected by taking the difference from the ball position and the pocket, 
which as a result return a vector2. From that, the magnitude is calculated and lastly checked, if it is smaller or equal to the balls radius. If that is the case, the ball
is recognized as pocketed.

The `ObjectsRestListener` has two methods: onStartAllObjectsRest, onEndAllObjectsRest. During the game, the velocity of every single ball is tracked. If anyone has a speed which
is higher than zero the onStartAllObjectsRest method is called. Otherwise, the onEndAllObjectsRest method. 


## Cue

In order to check whether the cue hit any ball rays were used. Basically, a ray is sent into the direction which the cue pointed to and checked if it has hit any ball.
If it has, a force is applied to the ball in the opposite direction of the cue and depending on the length of the cue the force will be more powerful.

## Physics Engine:

We used the dyn4j library in order to simulate the physics in our game. The library for instance calculates the rebound force of the pool 
balls in the event of collision between two balls.
