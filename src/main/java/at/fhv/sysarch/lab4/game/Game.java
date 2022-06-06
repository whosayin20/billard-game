package at.fhv.sysarch.lab4.game;

import at.fhv.sysarch.lab4.physics.BallPocketedListener;
import at.fhv.sysarch.lab4.physics.BallsCollisionListener;
import at.fhv.sysarch.lab4.physics.ObjectsRestListener;
import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.fhv.sysarch.lab4.game.Player.PlayerName.PLAYER1;
import static at.fhv.sysarch.lab4.game.Player.PlayerName.PLAYER2;

public class Game implements BallsCollisionListener, BallPocketedListener, ObjectsRestListener {
    private final Renderer renderer;
    private final Physics physics;
    private Point2D mousePressedPoint;
    private Point2D mousePressedPhysicsPoint;

    private Player player1;

    private Player player2;

    private Player currPlayer;

    private List<Ball> pocketedBalls;

    private boolean ballsMoving;

    private boolean ballsPocketedInRound;

    private boolean whiteBallTouchedOtherBalls;

    private boolean whiteBallTouched;

    private boolean whiteBallPocketed;

    private boolean foul;

    private boolean roundRunning;

    private Vector2 preFoulPos;

    private String[] interjections = {"Nice!", "Wow!", "Very good!", "Excellent!", "Pro!", "Amazing!"};

    private int index = 0;

    private Table table;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.physics.setBallsCollisionListener(this);
        this.physics.setBallPocketedListener(this);
        this.physics.setObjectsRestListener(this);
        this.player1 = new Player(PLAYER1);
        this.player2 = new Player(PLAYER2);
        this.currPlayer = player1;
        this.pocketedBalls = new ArrayList<>();
        this.initWorld();
    }

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);

        mousePressedPoint = new Point2D(x, y);
        mousePressedPhysicsPoint = new Point2D(pX, pY);

        this.renderer.setCueStartCoordinates(x, y);
        this.renderer.setCueEndCoordinates(x, y);
        this.renderer.setDrawingState(Renderer.CueDrawingState.PRESSED);
    }

    public void onMouseReleased(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);

        if (!ballsMoving) {
            Vector2 start = new Vector2(mousePressedPhysicsPoint.getX(), mousePressedPhysicsPoint.getY());
            Vector2 end = new Vector2(pX, pY);
            Vector2 direction = end.subtract(start).multiply(-1); //multiply with -1 because it should be in the opposite direction

            Ray ray = null;
            boolean result = false;
            ArrayList<RaycastResult> results = new ArrayList<>();
            try {
                ray = new Ray(start, direction);
                result = this.physics.getWorld().raycast(ray, 0.1, false, false, results);
            } catch (IllegalArgumentException ex) { /*ignore*/ }

            if (result) {
                Body body = results.get(0).getBody();
                if (body.getUserData() instanceof Ball) {
                    Ball b = (Ball) body.getUserData();
                    body.applyForce(direction.multiply(500));
                    if (b.isWhite()) {
                        whiteBallTouched = true;
                    } else {
                        //hit another ball
                        foul = true;
                    }
                }
            }
        }
        this.renderer.setDrawingState(Renderer.CueDrawingState.RELEASED);
    }

    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        Point2D mouseDraggedPoint = new Point2D(x, y);
        Point2D cueLength = calculateMaxCueLength(mousePressedPoint, mouseDraggedPoint);
        Point2D newEnd = mousePressedPoint.add(cueLength);

        this.renderer.setCueEndCoordinates(newEnd.getX(), newEnd.getY());
        this.renderer.setDrawingState(Renderer.CueDrawingState.DRAGGED);
    }

    private Point2D calculateMaxCueLength(Point2D start, Point2D end) {
        Point2D cueDistanceVec = end.subtract(start);
        double cueLength = cueDistanceVec.magnitude();
        if (cueLength > 250) cueLength = 250;

        cueDistanceVec = cueDistanceVec.normalize();
        //Return a new point with where the normalized vector is multiplied with the cue length, which can not be longer than 200
        return new Point2D(cueDistanceVec.getX() * cueLength, cueDistanceVec.getY() * cueLength);
    }

    private void placeBalls(List<Ball> balls, boolean ignoreTopSpot) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2 * Ball.Constants.RADIUS * 2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);
            physics.addBody(b.getBody());

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }

            if (ignoreTopSpot && 1 == colSize) {
                return;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();

        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;
            balls.add(b);
        }
        this.placeBalls(balls, false);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);

        table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);

        renderer.setStrikeMessage("Next Strike: " + currPlayer.getName());
    }

    @Override
    public void onBallsCollide(Ball b1, Ball b2) {
        if (b1.isWhite() && !b2.isWhite() || !b1.isWhite() && b2.isWhite()) {
            whiteBallTouchedOtherBalls = true;
        }
    }

    @Override
    public boolean onBallPocketed(Ball b) {
        //stopp ball movement
        b.getBody().setLinearVelocity(0, 0);

        if (b.isWhite()) {
            whiteBallPocketed = true;
            foul = true;
        } else if (whiteBallTouchedOtherBalls) {
            currPlayer.updateScore(1);
            ballsPocketedInRound = true;
            updateRenderedScore();
            removeBall(b);
            interjectionMessage();
        } else {
            //ball without hitting white ball pocketed
            foul = true;
            removeBall(b);
        }
        return false;
    }

    private void removeBall(Ball b) {
        pocketedBalls.add(b);
        renderer.removeBall(b);
        physics.getWorld().removeBody(b.getBody());
    }

    private void interjectionMessage() {
        index++;
        String interjection = interjections[index % interjections.length];
        renderer.setActionMessage(interjection);
    }

    private void updateRenderedScore() {
        switch (currPlayer.getName()) {
            case PLAYER1:
                renderer.setPlayer1Score(currPlayer.getScore());
                break;
            case PLAYER2:
                renderer.setPlayer2Score(currPlayer.getScore());
                break;
        }
    }

    private void prepareNextRound() {
        whiteBallTouchedOtherBalls = false;
        ballsPocketedInRound = false;
        whiteBallPocketed = false;
        whiteBallTouched = false;
        foul = false;
        roundRunning = false;
    }

    private void clearMessage() {
        renderer.setActionMessage("");
        renderer.setFoulMessage("");
    }

    @Override
    public void onEndAllObjectsRest() {
        //System.out.println("movement ended");
        ballsMoving = false;
        roundRunning = true;

        if (whiteBallPocketed && foul) {
            renderer.setFoulMessage("Foul! White ball pocketed");
            Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        } else if (!whiteBallTouched && foul) {
            renderer.setFoulMessage("Foul! Another ball instead of white ball was not stroken");
        } else if (whiteBallTouched && !whiteBallTouchedOtherBalls) {
            renderer.setFoulMessage("Foul! White ball did not strike any other balls");
            Ball.WHITE.setPosition(preFoulPos.x, preFoulPos.y); //reset to pre foul position
            foul = true;
        } else if (whiteBallTouchedOtherBalls && !ballsPocketedInRound) {
            switchPlayer();
            updateRenderedScore();
            prepareNextRound();
        }

        if (foul) {
            currPlayer.minusPoint();
            updateRenderedScore();
            switchPlayer();
            prepareNextRound();
        } else {
            preFoulPos = Ball.WHITE.getBody().getTransform().getTranslation();
        }

        resetBalls();
    }

    private void resetBalls() {
        if (pocketedBalls.size() >= 14) {
            List<Ball> balls = new ArrayList<>();

            for(Ball b : Ball.values()) {
                if(b.isWhite() || !pocketedBalls.contains(b)) { //continue if the ball is white or the ball is not pocketed
                    continue;
                }
                balls.add(b);
            }

            Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
            placeBalls(balls, true);
            table = new Table();
            physics.getWorld().removeBody(table.getBody());
            physics.getWorld().addBody(table.getBody());
            renderer.setTable(table);
            pocketedBalls.clear();
        }
    }

    private void switchPlayer() {
        renderer.setActionMessage("Instruction: Change Player!");
        if (currPlayer.getName().equals(PLAYER2)) {
            currPlayer = player1;
            renderer.setStrikeMessage("Strike " + currPlayer.getName());
        } else {
            currPlayer = player2;
            renderer.setStrikeMessage("Strike " + currPlayer.getName());
        }
    }

    @Override
    public void onStartAllObjectsRest() {
        //System.out.println("moving currently");
        this.ballsMoving = true;

        if (!roundRunning) {
            return;
        }
        clearMessage();
        roundRunning = false;
        ballsPocketedInRound = false;
    }
}