package at.fhv.sysarch.lab4.game;

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

public class Game {
    private final Renderer renderer;
    private final Physics physics;
    private Point2D mousePressedPoint;

    private Point2D mousePressedPhysicsPoint;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
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

        Vector2 start = new Vector2(mousePressedPhysicsPoint.getX(), mousePressedPhysicsPoint.getY());
        Vector2 end = new Vector2(pX, pY);
        Vector2 direction = end.subtract(start).multiply(-1); //multiply with -1 because it should be in the opposite direction

        Ray ray = null;
        boolean result = false;
        ArrayList<RaycastResult> results = new ArrayList<>();
        try {
            ray = new Ray(start, direction);
            result = this.physics.getWorld().raycast(ray, 1.0, false, false, results);
        } catch (IllegalArgumentException ex) { /*ignore*/ }

        if (result) {
            System.out.println("We hit something");
            Body body = results.get(0).getBody();
            if (body.getUserData() instanceof Ball) {
                Ball b = (Ball) body.getUserData();
                body.applyForce(direction.multiply(500));
                if (!b.isWhite()) {
                    System.out.println("Foul Bruder!");
                }
            }
        }
        this.renderer.setDrawingState(Renderer.CueDrawingState.RELEASED);
    }

    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

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

    private void placeBalls(List<Ball> balls) {
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

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
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

        this.placeBalls(balls);


        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);

        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }
}