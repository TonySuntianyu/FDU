public class Board {
    public enum Piece { EMPTY, BLACK, WHITE, BARRIER, CRATER }
    private Piece[][] board;
    private int size = 15;  // 默认五子棋模式为15x15棋盘

    public Board() {
        initializeReversiBoard();
    }

    // 初始化传统黑白棋模式的棋盘
    public void initializeReversiBoard() {
        board = new Piece[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                board[i][j] = Piece.EMPTY;

        board[3][3] = Piece.WHITE;
        board[3][4] = Piece.BLACK;
        board[4][3] = Piece.BLACK;
        board[4][4] = Piece.WHITE;
    }

    // 初始化和平模式的空棋盘
    public void initializeEmptyBoard() {
        board = new Piece[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                board[i][j] = Piece.EMPTY;
    }
    
    // 初始化和平模式的棋盘（带初始棋子）
    public void initializePeaceBoard() {
        board = new Piece[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                board[i][j] = Piece.EMPTY;
                
        // 添加与黑白棋模式相同的初始棋子
        board[3][3] = Piece.WHITE;
        board[3][4] = Piece.BLACK;
        board[4][3] = Piece.BLACK;
        board[4][4] = Piece.WHITE;
    }

    // 初始化五子棋模式的15x15棋盘（带障碍物）
    public void initializeGomokuBoard() {
        board = new Piece[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                board[i][j] = Piece.EMPTY;
        
        // 添加固定障碍物
        setBarrier(2, 5);   // 3F (3-1, F-A = 5)
        setBarrier(7, 6);   // 8G (8-1, G-A = 6)
        setBarrier(8, 5);   // 9F (9-1, F-A = 5)
        setBarrier(11, 10); // CK (C=12-1, K-A = 10)
    }
    
    // 设置障碍物
    public void setBarrier(int row, int col) {
        if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
            board[row][col] = Piece.BARRIER;
        }
    }
    
    // 设置弹坑
    public void setCrater(int row, int col) {
        if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
            board[row][col] = Piece.CRATER;
        }
    }

    public Piece getPiece(int row, int col) {
        if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
            return board[row][col];
        }
        return Piece.EMPTY; // 越界返回EMPTY
    }

    public void setPiece(int row, int col, Piece piece) {
        if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
            board[row][col] = piece;
        }
    }
    
    // 获取棋盘大小
    public int getSize() {
        return board.length;
    }

    public boolean isFull() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == Piece.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    // 计算得分
    public int[] getScore() {
        int[] score = new int[2]; // [0]黑棋分数，[1]白棋分数
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == Piece.BLACK) score[0]++;
                else if (board[i][j] == Piece.WHITE) score[1]++;
            }
        }
        return score;
    }
} 