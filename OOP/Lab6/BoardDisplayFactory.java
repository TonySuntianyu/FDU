/**
 * 棋盘显示工厂类，根据游戏模式创建合适的显示器
 */
public class BoardDisplayFactory {
    /**
     * 获取对应游戏模式的棋盘显示器
     * @param mode 游戏模式
     * @return 对应的棋盘显示器
     */
    public static BoardDisplay getDisplayFor(GameMode mode) {
        if (mode instanceof GomokuMode) {
            return new GomokuBoardDisplay();
        } else {
            return new ClassicBoardDisplay();
        }
    }
} 