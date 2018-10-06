import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

class Minimax {

    private static final double TIME_LIMIT = 5e7;

    private static Map<Long, Transposition> transpositionTable = new HashMap<>();

    private boolean timeout;

    private int maxIndex;
    private int maxValue;
    private int maxPlayer;
    private int minPlayer;

    private Deadline deadline;
    private Evaluator evaluator;

    Minimax(int player, Deadline pDue) {

        this.maxPlayer = player;
        this.minPlayer = player % 2 + 1;
        this.maxIndex = -1;
        this.maxValue = Integer.MIN_VALUE;
        this.deadline = pDue;
        this.evaluator = new Evaluator(maxPlayer, minPlayer);
    }

    /**
     * Run the minimax algorithm with nextStates and depth
     */
    void run(int depth, Vector<GameState> nextStates) {

        timeout = false;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int value = Integer.MIN_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int maxIndex = -1;

        for (int i = 0; i < nextStates.size(); i++) {

            GameState nextState = nextStates.get(i);

            value = Math.max(value, alphabeta(depth, alpha, beta, nextState.getNextPlayer(), nextState));

            //if timeout occurs break the loop and don't update the maxValue
            if (deadline.timeUntil() < TIME_LIMIT) {
                timeout = true;
                break;
            }

            if (value > maxValue) {
                maxValue = value;
                maxIndex = i;
            }

            alpha = Math.max(alpha, value);
        }

        //if timeout occurs return without updating the maxValue
        if (timeout) {
            return;
        }

        if (maxValue > this.maxValue) {
            this.maxIndex = maxIndex;
            this.maxValue = maxValue;
        }
    }

    /**
     * Return the max value's index
     */
    int getMaxIndex(){

        //if index is -1 return the first state
        if (maxIndex == -1){
            return 0;
        }

        return maxIndex;
    }

    /**
     * Implement alphabeta for both max and min players
     *
     * @return the score for this game state
     */
    private int alphabeta(int depth, int alpha, int beta, int player, GameState gameState) {

        if (isEOG(depth, gameState)) {
            return evaluateEOG(alpha, beta, player, depth, gameState);
        }

        //generate hash key for state
        long gameStateHash = Transposition.gameStateHash(gameState);

        //search for existing transposition
        Transposition transposition = transpositionTable.getOrDefault(gameStateHash, null);

        //if the transposition exists and has higher depth update the values
        if (isValidTransposition(depth, transposition)) {

            if (transposition.type == Transposition.Type.EXACT) {
                return transposition.value;
            }

            alpha = updateAlpha(alpha, transposition);
            beta = updateBeta(beta, transposition);
        }

        int score = player == maxPlayer ? max(depth, alpha, beta, gameState) : min(depth, alpha, beta, gameState);

        //update transposition table with the new values
        updateTranspositionTable(alpha, beta, player, depth, score, gameStateHash);

        return score;
    }

    /**
     * Update the alpha value if the transposition type is LOWERBOUND
     */
    private int updateAlpha(int alpha, Transposition transposition) {

        if (transposition.type == Transposition.Type.LOWERBOUND && transposition.value > alpha) {
            return transposition.value;
        }

        return alpha;
    }

    /**
     * Update the beta value if the transposition type is UPPERBOUND
     */
    private int updateBeta(int beta, Transposition transposition) {

        if (transposition.type == Transposition.Type.UPPERBOUND && transposition.value < beta) {
            beta = transposition.value;
        }

        return beta;
    }

    /**
     * Implement max function using the alphabeta
     */
    private int max(int depth, int alpha, int beta, GameState gameState) {

        Vector<GameState> nextStates = new Vector<>();
        gameState.findPossibleMoves(nextStates);
        sortStates(nextStates, true);

        int value = Integer.MIN_VALUE;

        for (GameState nextState : nextStates) {

            int alphaBetaValue = alphabeta(depth - 1, alpha, beta, nextState.getNextPlayer(), nextState);

            if (deadline.timeUntil() < TIME_LIMIT) {
                timeout = true;
                break;
            }

            value = Math.max(value, alphaBetaValue);

            if (isValidMinPrune(value, beta)) {
                break;
            }

            //update alpha value
            alpha = Math.max(alpha, value);
        }

        return value;
    }

    /**
     * Implement min function using the alphabeta
     */
    private int min(int depth, int alpha, int beta, GameState gameState) {

        Vector<GameState> nextStates = new Vector<>();
        gameState.findPossibleMoves(nextStates);
        sortStates(nextStates, false);

        int value = Integer.MAX_VALUE;

        for (GameState nextState : nextStates) {

            int alphaBetaValue = alphabeta(depth - 1, alpha, beta, nextState.getNextPlayer(), nextState);

            if (deadline.timeUntil() < TIME_LIMIT) {
                timeout = true;
                break;
            }

            value = Math.min(value, alphaBetaValue);

            if (isValidMaxPrune(value, alpha)) {
                break;
            }

            //update beta value
            beta = Math.min(beta, value);
        }

        return value;
    }

    /**
     * Checks if the max node can prune
     */
    private boolean isValidMaxPrune(int value, int alpha){

        return value <= alpha;
    }

    /**
     * Checks if the min node can prune
     */
    private boolean isValidMinPrune(int value, int beta){

        return value >= beta;
    }

    /**
     * Evaluate the terminal states and update the transposition table
     *
     * @return the score based on the evaluation method
     */
    private int evaluateEOG(int alpha, int beta, int player, int depth, GameState gameState) {

        int score = evaluator.evaluateMove(gameState);
        long gameStateHash = Transposition.gameStateHash(gameState);

        Transposition transposition = transpositionTable.getOrDefault(gameStateHash, null);

        //if the transposition doesn't exist or exists and we should update it
        if (transposition == null || transposition.depth < depth){
            updateTranspositionTable(alpha, beta, player, depth, score, gameStateHash);
        }

        return score;
    }

    /**
     * Update the transposition table based on current values of alpha, beta and score
     */
    private void updateTranspositionTable(int alpha, int beta, int player, int depth, int score, long gameStateHash) {

        transpositionTable.put(gameStateHash, Transposition.newTransposition(alpha, beta, score, depth, player));
    }

    /**
     * Check if this is a terminal state
     */
    private boolean isEOG(int depth, GameState gameState) {

        return depth == 0 || gameState.isEOG();
    }

    /**
     * Check if the transposition exist and its depth is higher from the current depth
     */
    private boolean isValidTransposition(int depth, Transposition transposition) {

        return transposition != null && transposition.depth > depth;
    }

    /**
     * Sorting states using the GameStateComparator based on their evaluation
     *
     * @param isDescending true for max nodes and false for min nodes
     */
    private void sortStates(Vector<GameState> states, boolean isDescending) {

        GameStateComparator gameStateComparator = new GameStateComparator(maxPlayer, minPlayer);

        if (isDescending) {
            states.sort(gameStateComparator.reversed());
        } else {
            states.sort(gameStateComparator);
        }
    }
}