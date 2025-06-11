public interface GameMode {
    boolean isValidMove(Board board, int row, int col, Board.Piece piece);
    boolean placePiece(Board board, int row, int col, Board.Piece piece);
    boolean hasValidMoves(Board board, Board.Piece piece);
} 