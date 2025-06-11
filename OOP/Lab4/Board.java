import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Board {
    protected final int SIZE = 8; // 棋盘的大小
    protected final Piece[][] grid; // 棋盘的二维数组
    protected Game game; // 游戏引用

    public Board() {
        grid = new Piece[SIZE][SIZE]; // 初始化棋盘数组
        initializeBoard(); // 初始化棋盘
    }

    // 设置游戏引用
    public void setGameList(Game game) {
        this.game = game;
    }

    // 初始化棋盘，将所有位置设置为空
    protected void initializeBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = Piece.EMPTY; // 设置每个格子为空
            }
        }
        // 设置初始棋子位置
        grid[3][4] = Piece.BLACK; // 4E
        grid[4][3] = Piece.BLACK; // 5D
        grid[3][3] = Piece.WHITE; // 4D
    
        grid[4][4] = Piece.WHITE; // 5E
    }

    // 在指定位置放置棋子
    public boolean placePiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col) && grid[row][col] == Piece.EMPTY) {
            grid[row][col] = piece; // 放置棋子
            return true;
        }
        return false; // 位置无效或已有棋子
    }

    // 检查位置是否有效
    protected boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE; // 检查行列是否在范围内
    }

    // 检查棋盘是否已满
    public boolean isFull() {
        for (Piece[] row : grid) {
            for (Piece cell : row) {
                if (cell == Piece.EMPTY) return false; // 发现空格子则返回 false
            }
        }
        return true; // 没有空格子则返回 true
    }

    // 检查棋盘是否已满并显示游戏结束信息
    public void checkGameEnd(Player player1, Player player2) {
        if (isFull()) {
            System.out.println("棋盘已满，游戏结束！");
            
            // 计算棋子数量
            int blackCount = 0;
            int whiteCount = 0;
            
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] == Piece.BLACK) {
                        blackCount++;
                    } else if (grid[row][col] == Piece.WHITE) {
                        whiteCount++;
                    }
                }
            }
            clearScreen();
            display(player1, player2, player1, game.getCurrentBoardIndex()+1);
        System.out.println("棋盘已满，游戏结束！");
            // 等待用户确认
            System.out.println("按Enter键继续...");
            new Scanner(System.in).nextLine();
        }
    }

    // 显示棋盘
    public void display(Player player1, Player player2, Player currentPlayer, int boardNumber) {
        clearScreen(); // 清屏
        System.out.println("当前棋盘编号：" + boardNumber); // 显示当前棋盘编号
        System.out.print("  ");
        for (char c = 'a'; c < 'a' + SIZE; c++) {
            System.out.print(c + " "); // 显示列标
        }
        System.out.println();
        
        List<String[]> gamesList = getRunningGamesList();
        
        for (int row = 0; row < SIZE; row++) {
            System.out.print((row + 1) + " "); // 显示行标
            for (int col = 0; col < SIZE; col++) {
                System.out.print(grid[row][col].getSymbol() + " "); // 显示棋子
            }
            
            // 在左侧显示玩家信息和当前游戏
            if (row == 0) {
                System.out.print("  ");
                if (boardNumber > 0) {
                    System.out.print("Game " + boardNumber);
                }
            } else if (row == 1) {
                System.out.print("  Player1 [" + player1.getName() + "]");
                if (currentPlayer == player1) {
                    System.out.print(" " + player1.getPieceType().getSymbol());
                }
            } else if (row == 2) {
                System.out.print("  Player2 [" + player2.getName() + "]");
                if (currentPlayer == player2) {
                    System.out.print(" " + player2.getPieceType().getSymbol());
                }
            }
            
            // 在右侧显示游戏列表
            if (row == 0) {
                System.out.print("\t\tGame List");
            } else if (row > 0 && row <= gamesList.size()) {
                int gameIndex = row - 1;
                String[] gameInfo = gamesList.get(gameIndex);
                
                // 根据索引调整制表位
                if (gameIndex < 2) { // 对于1和2项
                    System.out.print("\t" + gameInfo[0] + ". " + gameInfo[1]);
                } else { // 对于3及之后的项
                    System.out.print("\t\t\t" + gameInfo[0] + ". " + gameInfo[1]);
             
              
              
                }
            }
            
            System.out.println();
        }
        System.out.println();
    }

    // 获取正在运行的游戏列表
    protected List<String[]> getRunningGamesList() {
        if (game != null) {
            return game.getGameTypeList();
        }
        // 默认情况下，至少返回当前游戏
        List<String[]> gamesList = new ArrayList<>();
        gamesList.add(new String[]{"1", "peace"});
        return gamesList;
    }

    // 清屏方法，仅在 Windows 下有效
    protected void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}