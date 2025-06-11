public class GomokuMode implements GameMode {
    // 使用moveCount来计算轮数
    private int moveCount = 0;
    // 炸弹数量
    private int blackBombs = 2; // 黑方有2个炸弹
    private int whiteBombs = 3; // 白方有3个炸弹
    
    // 错误类型常量
    public static final int ERROR_NONE = 0;       // 没有错误
    public static final int ERROR_BARRIER = 1;    // 障碍物错误
    public static final int ERROR_OCCUPIED = 2;   // 位置已被占用错误
    public static final int ERROR_OUTOFBOUNDS = 3; // 超出边界

    // 用于存储最后一次验证的错误类型
    private int lastError = ERROR_NONE;

    @Override
    public boolean isValidMove(Board board, int row, int col, Board.Piece piece) {
        // 重置错误类型
        lastError = ERROR_NONE;
        
        // 检查是否在棋盘范围内
        if (row < 0 || row >= board.getSize() || col < 0 || col >= board.getSize()) {
            lastError = ERROR_OUTOFBOUNDS;
            return false;
        }
        
        // 检查是否是障碍物
        if (board.getPiece(row, col) == Board.Piece.BARRIER) {
            lastError = ERROR_BARRIER;
            return false;
        }
        
        // 检查是否有其他棋子或弹坑
        if (board.getPiece(row, col) != Board.Piece.EMPTY) {
            lastError = ERROR_OCCUPIED;
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取最后一次验证的错误类型
     * @return 错误类型常量
     */
    public int getLastError() {
        return lastError;
    }

    @Override
    public boolean placePiece(Board board, int row, int col, Board.Piece piece) {
        if (!isValidMove(board, row, col, piece)) {
            return false;
        }
        board.setPiece(row, col, piece);
        // 增加移动次数计数
        moveCount++;
        return true;
    }

    @Override
    public boolean hasValidMoves(Board board, Board.Piece piece) {
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getPiece(i, j) == Board.Piece.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    // 获取当前轮数（每两步为一轮，从1开始计数）
    public int getRoundCount() {
        return (moveCount / 2) + 1;
    }
    
    // 获取剩余炸弹数量
    public int getBlackBombs() {
        return blackBombs;
    }
    
    public int getWhiteBombs() {
        return whiteBombs;
    }
    
    // 使用炸弹
    public boolean useBomb(Board board, int row, int col, Board.Piece currentPiece) {
        Board.Piece oppositeColor = (currentPiece == Board.Piece.BLACK) ? Board.Piece.WHITE : Board.Piece.BLACK;
        
        // 检查炸弹数量
        if (currentPiece == Board.Piece.BLACK) {
            if (blackBombs <= 0) return false;
        } else {
            if (whiteBombs <= 0) return false;
        }
        
        // 检查目标位置是否有对方棋子
        if (board.getPiece(row, col) != oppositeColor) {
            return false;
        }
        
        // 使用炸弹，减少炸弹数量
        if (currentPiece == Board.Piece.BLACK) {
            blackBombs--;
        } else {
            whiteBombs--;
        }
        
        // 清除对方棋子，设置弹坑
        board.setCrater(row, col);
        moveCount++; // 使用炸弹也算一步
        
        return true;
    }

    // 检查是否有五子连珠
    public boolean checkWin(Board board, int row, int col, Board.Piece piece) {
        int[][] directions = {
            {1, 0},   // 水平
            {0, 1},   // 垂直
            {1, 1},   // 右下斜
            {1, -1}   // 左下斜
        };

        for (int[] dir : directions) {
            int count = 1;  // 从1开始，因为包含当前位置

            // 正向检查
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 0 && r < board.getSize() && c >= 0 && c < board.getSize() && board.getPiece(r, c) == piece) {
                count++;
                r += dir[0];
                c += dir[1];
            }

            // 反向检查
            r = row - dir[0];
            c = col - dir[1];
            while (r >= 0 && r < board.getSize() && c >= 0 && c < board.getSize() && board.getPiece(r, c) == piece) {
                count++;
                r -= dir[0];
                c -= dir[1];
            }

            if (count >= 5) {
                return true;
            }
        }
        return false;
    }
} 