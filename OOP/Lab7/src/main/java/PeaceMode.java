public class PeaceMode implements GameMode {
    @Override
    public boolean isValidMove(Board board, int row, int col, Board.Piece piece) {
        return row >= 0 && row < 8 && col >= 0 && col < 8 && board.getPiece(row, col) == Board.Piece.EMPTY;
    }

    @Override
    public boolean placePiece(Board board, int row, int col, Board.Piece piece) {
        if (!isValidMove(board, row, col, piece)) {
            return false;
        }
        board.setPiece(row, col, piece);
        return true;
    }

    @Override
    public boolean hasValidMoves(Board board, Board.Piece piece) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.getPiece(i, j) == Board.Piece.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }
} 