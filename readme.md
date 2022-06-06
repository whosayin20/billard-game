# Billiard-game
Hüseyin Arziman & Yusuf Cetinkaya

## General


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
