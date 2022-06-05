package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Vector2;

public class Physics implements ContactListener, StepListener {

    private World world;

    private BallsCollisionListener ballsCollisionListener;

    private BallPocketedListener ballPocketedListener;

    private ObjectsRestListener objectsRestListener;

    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    public World getWorld() {
        return world;
    }

    public void addBody(Body body) {
        this.world.addBody(body);
    }

    @Override
    public void begin(Step step, World world) {
    }

    @Override
    public void updatePerformed(Step step, World world) {
    }

    @Override
    public void postSolve(Step step, World world) {
    }

    @Override
    public void end(Step step, World world) {
        int movingBalls = 0;

        for (Ball ball : Ball.values()) {
            if (!ball.getBody().getLinearVelocity().isZero()) {
                movingBalls++;
            }
        }
        if (movingBalls == 0) {
            objectsRestListener.onEndAllObjectsRest();
        } else {
            objectsRestListener.onStartAllObjectsRest();
        }
    }

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public boolean begin(ContactPoint point) {
        Body body1 = point.getBody1();
        Body body2 = point.getBody2();

        if (body1.getUserData() instanceof Ball && body2.getUserData() instanceof Ball) {
            Ball ball1 = (Ball) body1.getUserData();
            Ball ball2 = (Ball) body2.getUserData();
            this.ballsCollisionListener.onBallsCollide(ball1, ball2);
        }

        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        //Sensor for Pockets
        if (point.isSensor()) {
            Body ball = point.getBody1();
            Body pocket = point.getBody2();
            if (isBallPocketed(ball, pocket, point)) ballPocketedListener.onBallPocketed((Ball) ball.getUserData());
        }
        return true;
    }

    private boolean isBallPocketed(Body ball, Body pocket, PersistedContactPoint point) {
        //world coordinates of ball
        Vector2 ballPosition = ball.getTransform().getTranslation();
        //relative table pocket position
        Vector2 pocketPosition = pocket.getTransform().getTranslation();
        Vector2 pocketCenter = point.getFixture2().getShape().getCenter();
        //world coordinates of pocket
        Vector2 pocketInWorld = pocketPosition.add(pocketCenter);
        Vector2 difference = ballPosition.difference(pocketInWorld);
        double magnitudeDifference = difference.getMagnitude();
        return magnitudeDifference <= Ball.Constants.RADIUS;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }

    public void setBallsCollisionListener(BallsCollisionListener ballsCollisionListener) {
        this.ballsCollisionListener = ballsCollisionListener;
    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }

    public void setObjectsRestListener(ObjectsRestListener objectsRestListener) {
        this.objectsRestListener = objectsRestListener;
    }
}
