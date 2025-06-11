import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class ReverseBoard extends Board {
    // 游戏列表

    public ReverseBoard() {
        super();
    }
    
    /**
     * 检查指定位置是否可以落子，同时在符合条件的所有方向上翻转对手棋子。
     * 反转规则：
     * 1. 新落下的棋子与已有同色棋子之间必须夹住连续的对手棋子。
     * 2. 棋子夹的位置可以是横向、纵向或斜向，且中间的所有棋子必须为对手棋子。
     * 3. 一步棋可能在多个方向上夹住对手棋子，所有满足条件的方向都会被翻转。
     *
     * @param row   落子行号
     * @param col   落子列号
     * @param piece 当前玩家棋子颜色
     * @param player 当前玩家对象，用于增加得分
     * @return 如果能落子并成功翻转至少一枚对手棋子则返回 true，否则返回 false
     */
    public boolean canPlaceAndFlip(int row, int col, Piece piece, Player player) {
        if (!isValidPosition(row, col) || grid[row][col] != Piece.EMPTY) {
            return false;
        }

        boolean canFlip = false;
        Piece opponentPiece = (piece == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        
        int flippedCount = 0; // 记录翻转的棋子数量

        for (int[] direction : directions) {
            int dRow = direction[0], dCol = direction[1];
            int currentRow = row + dRow, currentCol = col + dCol;
            boolean hasOpponentPieceBetween = false;
            int directionFlipCount = 0; // 当前方向翻转的棋子数量

            // 沿该方向前进，必须连续遇到对手棋子
            while (isValidPosition(currentRow, currentCol) && grid[currentRow][currentCol] == opponentPiece) {
                hasOpponentPieceBetween = true;
                directionFlipCount++;
                currentRow += dRow;
                currentCol += dCol;
            }

            // 检查该方向上是否存在自己的棋子，从而夹住了对手棋子
            if (hasOpponentPieceBetween && isValidPosition(currentRow, currentCol) && grid[currentRow][currentCol] == piece) {
                canFlip = true;
                flippedCount += directionFlipCount; // 累加翻转的棋子数量
                // 回溯翻转该方向上所有被夹住的对手棋子
                currentRow -= dRow;
                currentCol -= dCol;
                while (currentRow != row || currentCol != col) {
                    grid[currentRow][currentCol] = piece;
                    currentRow -= dRow;
                    currentCol -= dCol;
                }
            }
        }
        
        // 改为直接统计棋盘上该颜色棋子的数量作为得分
        if (canFlip) {
            updatePlayerScore(player);
        }
        
        return canFlip;
    }
    
    /**
     * 更新玩家得分 - 直接统计棋盘上该玩家颜色棋子的数量
     *
     * @param player 需要更新得分的玩家
     */
    private void updatePlayerScore(Player player) {
        Piece piece = player.getPieceType();
        int count = 0;
        
        // 遍历整个棋盘，统计玩家棋子数量
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == piece) {
                    count++;
                }
            }
        }
        
        // 设置玩家分数为棋盘上棋子数量
        player.setScore(count);
    }
    
    /**
     * 重写 placePiece 方法，只有在可以翻转至少一枚对手棋子的情况下才能落子，
     * 并且会自动翻转所有符合条件的对手棋子。
     *
     * 落子条件：
     *  - 必须至少翻转一颗对手棋子才能落子。
     *
     * @param row   落子行号
     * @param col   落子列号
     * @param piece 当前玩家棋子颜色
     * @param player 当前玩家对象
     * @return 如果落子有效则返回 true，否则返回 false
     */
    @Override
    public boolean placePiece(int row, int col, Piece piece) {
        // 获取当前游戏对象和当前玩家
        Player player = (piece == Piece.BLACK) ? game.getPlayer1() : game.getPlayer2();
        
        if (canPlaceAndFlip(row, col, piece, player)) {
            grid[row][col] = piece;
            
            // 在成功落子后，更新双方玩家得分
            updatePlayerScore(game.getPlayer1());
            updatePlayerScore(game.getPlayer2());
            
            return true;
        }
        return false;
    }
    
    /**
     * 判断指定位置是否为合法落子（仅检测，不修改棋盘状态）。
     * 判断规则与 canPlaceAndFlip 相同，但不进行翻转操作。
     *
     * @param row   落子行号
     * @param col   落子列号
     * @param piece 当前玩家棋子颜色
     * @return 如果该位置为合法落子，则返回 true，否则返回 false
     */
    public boolean isValidMove(int row, int col, Piece piece) {
        if (!isValidPosition(row, col) || grid[row][col] != Piece.EMPTY) {
            return false;
        }
        Piece opponentPiece = (piece == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (int[] direction : directions) {
            int dRow = direction[0], dCol = direction[1];
            int currentRow = row + dRow, currentCol = col + dCol;
            boolean hasOpponentPieceBetween = false;
            while (isValidPosition(currentRow, currentCol) && grid[currentRow][currentCol] == opponentPiece) {
                hasOpponentPieceBetween = true;
                currentRow += dRow;
                currentCol += dCol;
            }
            if (hasOpponentPieceBetween && isValidPosition(currentRow, currentCol) && grid[currentRow][currentCol] == piece) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查当前玩家是否存在至少一个合法的落子位置。
     * 根据规则：必须至少翻转一颗对手棋子才能落子；如果有合法落子，则不得弃权，
     * 否则只能弃权由对手继续落子。
     *
     * @param piece 当前玩家棋子颜色
     * @return 如果存在至少一个合法落子位置，则返回 true，否则返回 false
     */
    public boolean hasValidMove(Piece piece) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == Piece.EMPTY && isValidMove(row, col, piece)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 判断是否允许弃权（跳过落子）。
     * 根据强制落子规则：
     *  - 如果存在合法落子，则必须落子，不允许弃权。
     *  - 只有在没有任何合法落子时，才允许弃权。
     *
     * @param piece 当前玩家棋子颜色
     * @return 如果允许弃权返回 true，否则返回 false
     */
    public boolean canPass(Piece piece) {
        return !hasValidMove(piece);
    }
    
    /**
     * 显示当前棋盘状态。
     * 在打印棋盘时，先判断当前位置是否为空且为当前玩家的合法落子位置，
     * 如果是，则该位置打印 "+" 提示，否则打印原有棋子符号。
     *
     * @param player1       玩家1
     * @param player2       玩家2
     * @param currentPlayer 当前轮到的玩家
     * @param boardNumber   当前棋盘编号
     */
    public void display(Player player1, Player player2, Player currentPlayer, int boardNumber) {
        // 在显示棋盘前更新双方玩家得分
        updatePlayerScore(player1);
        updatePlayerScore(player2);
        
        List<String[]> gamesList = getRunningGamesList();
        clearScreen(); // 清屏
        System.out.println("当前棋盘编号：" + boardNumber); // 显示当前棋盘编号
        System.out.print("  ");
        for (char c = 'a'; c < 'a' + SIZE; c++) {
            System.out.print(c + " "); // 显示列标
        }
        System.out.println();
        for (int row = 0; row < SIZE; row++) {
            System.out.print((row + 1) + " "); // 显示行标
            for (int col = 0; col < SIZE; col++) {
                // 如果当前位置为空且为当前玩家的合法落子位置，则打印 "+" 提示
                if (grid[row][col] == Piece.EMPTY && isValidMove(row, col, currentPlayer.getPieceType())) {
                    System.out.print("+ ");
                } else {
                    System.out.print(grid[row][col].getSymbol() + " ");
                }
            }
            // 在前两行显示玩家姓名，并在轮到该玩家时显示其棋子标识
            if (row == 0) {
                System.out.print("  " + player1.getName());
                if (currentPlayer == player1) {
                    System.out.print(" " + player1.getPieceType().getSymbol());
                }
                System.out.print(" \tscore: " + player1.getScore());
            } else if (row == 1) {
                System.out.print("  " + player2.getName());
                if (currentPlayer == player2) {
                    System.out.print(" " + player2.getPieceType().getSymbol());
                }
                System.out.print(" \tscore: " + player2.getScore());
            }
            if (row == 0) {
                System.out.print("\t\tGame List");
            } else if (row > 0 && row <= gamesList.size()) {
                int gameIndex = row - 1;
                String[] gameInfo = gamesList.get(gameIndex);
                
                // 根据索引调整制表位
                if (gameIndex == 0) { // 对于1和2项
                    System.out.print(  "\t\t" + gameInfo[0] + ". " + gameInfo[1]);
                } else if (gameIndex == 1) { // 对于及之后的项
                    System.out.print("\t\t\t\t\t" + gameInfo[0] + ". " + gameInfo[1]);
                } else if (gameIndex == 2) { // 对于3及之后的项
                    System.out.print("\t\t\t\t\t" + gameInfo[0] + ". " + gameInfo[1]);
                }else if (gameIndex >= 3) { // 对于4及之后的项
                    System.out.print("\t\t\t\t\t" + gameInfo[0] + ". " + gameInfo[1]);
                }
            }
            System.out.println();
        }
        
        // 当前玩家没有合法落子位置时，显示提示信息
        if (!hasValidMove(currentPlayer.getPieceType())) {
            System.out.println("注意：" + currentPlayer.getName() + " 没有合法落子位置！请输入 'pass' 跳过本轮。");
        }
        
        System.out.println();
    }

    /**
     * 判断游戏是否结束。
     * 反转棋游戏结束的条件：
     * 1. 棋盘已满
     * 2. 双方均无合法落子位置
     *
     * @return 如果游戏结束返回 true，否则返回 false
     */
    public boolean isGameOver() {
        // 检查棋盘是否已满
        if (isFull()) {
            return true;
        }
        
        // 检查双方是否均无合法落子位置
        return !hasValidMove(Piece.BLACK) && !hasValidMove(Piece.WHITE);
    }
    
    /**
     * 获取游戏结果，返回获胜玩家或平局信息。
     * 
     * @return 游戏结果字符串
     */
    public String getGameResult() {
        int blackCount = 0;
        int whiteCount = 0;
        
        // 统计黑白棋子数量
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == Piece.BLACK) {
                    blackCount++;
                } else if (grid[row][col] == Piece.WHITE) {
                    whiteCount++;
                }
            }
        }
        
        // 确保玩家得分与棋盘上的棋子数量一致
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();
        player1.setScore(blackCount);
        player2.setScore(whiteCount);
        
        // 根据棋子数量确定胜负
        if (blackCount > whiteCount) {
            return player1.getName() + " 获胜！得分：" + blackCount + ":" + whiteCount;
        } else if (whiteCount > blackCount) {
            return player2.getName() + " 获胜！得分：" + whiteCount + ":" + blackCount;
        } else {
            return "游戏平局！得分：" + blackCount + ":" + whiteCount;
        }
    }
    
    /**
     * 重写检查棋盘是否已满并显示游戏结束信息的方法
     * 反转棋采用不同的游戏结束判断逻辑
     */
    @Override
    public void checkGameEnd(Player player1, Player player2) {
        if (isGameOver()) {
            clearScreen();
            display(player1, player2, player1, game.getCurrentBoardIndex()+1);
            System.out.println("反转棋游戏结束！");
            System.out.println(getGameResult());
            
            // 等待用户确认
            System.out.println("按Enter键继续...");
            new Scanner(System.in).nextLine();
        }
    }

    // 添加新游戏
}
