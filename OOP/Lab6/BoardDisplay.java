/**
 * 棋盘显示接口，用于定义不同棋盘类型的显示逻辑
 */
public interface BoardDisplay {
    /**
     * 显示棋盘
     * @param board 要显示的棋盘
     * @param currentPlayer 当前玩家
     * @param player1 玩家1
     * @param player2 玩家2
     * @param mode 游戏模式
     * @param boardIndex 棋盘索引
     * @param boardManager 棋盘管理器
     */
    void displayBoard(Board board, Player currentPlayer, Player player1, Player player2,
                    GameMode mode, int boardIndex, BoardManager boardManager);
} 