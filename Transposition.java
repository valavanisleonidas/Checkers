import java.util.Random;

class Transposition {

    enum Type {
        LOWERBOUND, UPPERBOUND, EXACT
    }

    private static int[][] zorbistHashing = randomizeZorbist();

    int depth;
    int value;
    int player;

    Type type;

    private Transposition(int value, int depth, int player, Type type) {

        this.value = value;
        this.depth = depth;
        this.type = type;
        this.player = player;
    }

    /**
     * Create a new Transpostion based on the alpha an beta values
     */
    static Transposition newTransposition(int alpha, int beta, int value, int depth, int player) {

        Type type = Type.EXACT;

        if (value <= alpha) {
            type = Type.LOWERBOUND;
        } else if (value >= beta) {
            type = Type.UPPERBOUND;
        }

        return new Transposition(value, depth, player, type);
    }

    /**
     * Randomly initialize zorbist hashing array
     */
    private static int[][] randomizeZorbist() {

        Random random = new Random();

        int[][] zorbist = new int[GameState.NUMBER_OF_SQUARES][4];

        for (int i = 0; i < zorbist.length; i++) {

            for (int j = 0; j < zorbist[i].length; j++) {
                zorbist[i][j] = random.nextInt(Integer.MAX_VALUE);
            }
        }

        return zorbist;
    }

    /**
     * Generate hash key based on zorbist hash function
     * @return the hash key
     */
    static long gameStateHash(GameState gameState) {

        final int whiteKing = Constants.CELL_WHITE + 4;
        final int redKing = Constants.CELL_RED + 4;

        int hash = 0;

        for (int i = 0; i < zorbistHashing.length; i++) {

            int piece = gameState.get(i);
            int pieceIndex = -1;

            if (piece == Constants.CELL_EMPTY || piece == Constants.CELL_INVALID) {
                continue;
            }

            switch (piece) {
                case Constants.CELL_WHITE:
                    pieceIndex = 0;
                    break;
                case Constants.CELL_RED:
                    pieceIndex = 1;
                    break;
                case whiteKing:
                    pieceIndex = 2;
                    break;
                case redKing:
                    pieceIndex = 3;
                    break;
            }

            hash ^= zorbistHashing[i][pieceIndex];
        }

        return hash;
    }
}

