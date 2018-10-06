import java.util.*;

class Player {

    private static final double TIME_LIMIT = 5e7;

    GameState play(final GameState pState, final Deadline pDue) {

        if (pState.isEOG()) {
            return new GameState(pState, new Move());
        }

        int depth = 8;
        int maxDepth = 15;

        Vector<GameState> nextStates = new Vector<>();
        pState.findPossibleMoves(nextStates);

        Minimax minimax = new Minimax(pState.getNextPlayer(), pDue);

        //do iterative deepening
        iterativeDeepening(depth, maxDepth, pDue, minimax, nextStates);

        return nextStates.get(minimax.getMaxIndex());
    }

    /**
     *
     * Iterative deepening until the maxDepth or a timeout occurs
     */
    private static void iterativeDeepening(int depth, int maxDepth, final Deadline pDue, Minimax minimax, Vector<GameState> nextStates){

        while (depth <= maxDepth && pDue.timeUntil() > TIME_LIMIT) {
            minimax.run(depth++, nextStates);
        }
    }
}
