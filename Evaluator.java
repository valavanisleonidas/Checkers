class Evaluator {

    private int maxPlayer;
    private int minPlayer;

    Evaluator(int maxPlayer, int minPlayer) {

        this.maxPlayer = maxPlayer;
        this.minPlayer = minPlayer;
    }

    /**
     * Evaluate the current state
     * Different evaluation is used for EOG and MOG
     */
    int evaluateMove(GameState gameState) {

        if (gameState.isEOG()) {
            return evalueEOG(gameState);
        }

        return evaluateMOG(gameState);
    }

    /**
     * Evaluate the middle of game state based on max and min pieces
     */
    private int evaluateMOG(GameState gameState) {

        int maxPieces = 0;
        int minPieces = 0;

        for (int position = 0; position < GameState.NUMBER_OF_SQUARES; position++) {

            int piece = gameState.get(position);

            if (piece == maxPlayer) {
                maxPieces++;
            }

            if (piece == maxPlayer + 4) {
                maxPieces += 2;
            }

            if (piece == minPlayer) {
                minPieces++;
            }
            if (piece == minPlayer + 4) {
                minPieces += 2;
            }
        }

        return maxPieces - minPieces;
    }

    /**
     * Evaluate the end of game state
     */
    private int evalueEOG(GameState state) {

        if (maxPlayer == Constants.CELL_WHITE && state.isWhiteWin()) {
            return Integer.MAX_VALUE;
        }

        if (maxPlayer == Constants.CELL_RED && state.isRedWin()) {
            return Integer.MAX_VALUE;
        }

        if (minPlayer == Constants.CELL_WHITE && state.isWhiteWin()) {
            return Integer.MIN_VALUE;
        }

        if (minPlayer == Constants.CELL_RED && state.isRedWin()) {
            return Integer.MIN_VALUE;
        }

        //draw
        return 0;
    }
}