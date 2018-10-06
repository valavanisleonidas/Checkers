import java.util.Comparator;

public class GameStateComparator implements Comparator<GameState> {

    private int maxPlayer;
    private int minPlayer;

    GameStateComparator(int maxPlayer, int minPlayer){
        this.maxPlayer = maxPlayer;
        this.minPlayer = minPlayer;
    }

    @Override
    public int compare(GameState gameState1, GameState gameState2) {

        Evaluator evaluator = new Evaluator(maxPlayer, minPlayer);

        int gameState1evaluation = evaluator.evaluateMove(gameState1);
        int gameState2evaluation = evaluator.evaluateMove(gameState2);

        return  Integer.compare(gameState1evaluation, gameState2evaluation);
    }
}