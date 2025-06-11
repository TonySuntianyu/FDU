public class ReversiMode implements GameMode {
    @Override
    public boolean isValidMove(Board board, int row, int col, Board.Piece piece) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8 || board.getPiece(row, col) != Board.Piece.EMPTY) {
            return false;
        }

        Board.Piece opposite = (piece == Board.Piece.BLACK) ? Board.Piece.WHITE : Board.Piece.BLACK;
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            boolean hasOpposite = false;

            while (r >= 0 && r < 8 && c >= 0 && c < 8 && board.getPiece(r, c) == opposite) {
                r += dir[0];
                c += dir[1];
                hasOpposite = true;
            }

            if (hasOpposite && r >= 0 && r < 8 && c >= 0 && c < 8 && board.getPiece(r, c) == piece) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean placePiece(Board board, int row, int col, Board.Piece piece) {
        if (!isValidMove(board, row, col, piece)) {
            return false;
        }

        board.setPiece(row, col, piece);
        Board.Piece opposite = (piece == Board.Piece.BLACK) ? Board.Piece.WHITE : Board.Piece.BLACK;
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            boolean hasOpposite = false;

            while (r >= 0 && r < 8 && c >= 0 && c < 8 && board.getPiece(r, c) == opposite) {
                r += dir[0];
                c += dir[1];
                hasOpposite = true;
            }

            if (hasOpposite && r >= 0 && r < 8 && c >= 0 && c < 8 && board.getPiece(r, c) == piece) {
                int flipR = row + dir[0];
                int flipC = col + dir[1];
                while (flipR != r || flipC != c) {
                    board.setPiece(flipR, flipC, piece);
                    flipR += dir[0];
                    flipC += dir[1];
                }
            }
        }
        return true;
    }

    @Override
    public boolean hasValidMoves(Board board, Board.Piece piece) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(board, i, j, piece)) {
                    return true;
                }
            }
        }
        return false;
    }
} 