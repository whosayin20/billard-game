package at.fhv.sysarch.lab4.game;

public class Player {
    public enum PlayerName {
        PLAYER1,
        PLAYER2
    }

    private PlayerName name;

    private int score;
    public Player(PlayerName name) {
        this.name = name;
        this.score = 0;
    }

    public void updateScore(int point) {
        this.score += point;
    }

    public void minusPoint() {
        this.score--;
    }

    public PlayerName getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
