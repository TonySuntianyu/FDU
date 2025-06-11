import java.io.IOException;

public class Board {
    private final int SIZE = 8;
    private final Piece[][] grid;
    public Board() {
        grid = new Piece[SIZE][SIZE];
        initializeBoard();
    }
    private void initializeBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = Piece.EMPTY;
            }
        }
    }
    public boolean placePiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col) && grid[row][col] == Piece.EMPTY) {
            grid[row][col] = piece;
            return true;
        }
        return false;
    }
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }
    public boolean isFull() {
        for (Piece[] row : grid) {
            for (Piece cell : row) {
                if (cell == Piece.EMPTY) return false;
            }
        }
        return true;
    }
    // 修改：display方法增加当前棋盘编号参数，用于提示
    public void display(Player player1, Player player2, Player currentPlayer, int boardNumber) {
        clearScreen();
        System.out.println("当前棋盘编号：" + boardNumber);  // 显示当前棋盘编号
        System.out.print("  ");
        for (char c = 'a'; c < 'a' + SIZE; c++) {
            System.out.print(c + " ");
        }
        System.out.println();
        for (int row = 0; row < SIZE; row++) {
            System.out.print((row + 1) + " ");
            for (int col = 0; col < SIZE; col++) {
                System.out.print(grid[row][col].getSymbol() + " ");
            }
            // 在前两行显示玩家姓名，并在轮到该玩家时显示其棋子标识
            if (row == 0) {
                System.out.print("  " + player1.getName());
                if (currentPlayer == player1) {
                    System.out.print(" " + player1.getPieceType().getSymbol());
                }
            } else if (row == 1) {
                System.out.print("  " + player2.getName());
                if (currentPlayer == player2) {
                    System.out.print(" " + player2.getPieceType().getSymbol());
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private void clearScreen() {
        try {
            // 清屏方法，仅在 Windows 下有效
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}

