import java.util.ArrayList;

public class BoardManager {
    private ArrayList<Board> boards = new ArrayList<>();
    private ArrayList<GameMode> modes = new ArrayList<>();
    // 存储每个棋盘的当前玩家
    private ArrayList<Player> currentPlayers = new ArrayList<>();
    private int currentBoardIndex = -1;

    public int addBoard(GameMode mode) {
        Board newBoard = new Board();
        if (mode instanceof PeaceMode) {
            newBoard.initializePeaceBoard();
        } else if (mode instanceof ReversiMode) {
            newBoard.initializeReversiBoard();
        } else if (mode instanceof GomokuMode) {
            newBoard.initializeGomokuBoard();
        }
        boards.add(newBoard);
        modes.add(mode);
        // 为新棋盘设置初始玩家（黑棋先行）
        currentPlayers.add(null);  // 将在Game类中设置初始玩家
        if (currentBoardIndex == -1) {
            currentBoardIndex = 0;
        }
        return boards.size();  // 返回从1开始的棋盘编号
    }

    public boolean switchBoard(int index) {
        // 将用户输入的1-based索引转换为0-based
        int actualIndex = index - 1;
        if (actualIndex >= 0 && actualIndex < boards.size()) {
            currentBoardIndex = actualIndex;
            return true;
        }
        return false;
    }

    public Board getCurrentBoard() {
        return boards.get(currentBoardIndex);
    }

    public GameMode getCurrentMode() {
        return modes.get(currentBoardIndex);
    }

    public int getCurrentBoardIndex() {
        return currentBoardIndex + 1;  // 返回从1开始的棋盘编号
    }

    public int getBoardCount() {
        return boards.size();
    }

    public GameMode getModeAtIndex(int index) {
        if (index >= 0 && index < modes.size()) {
            return modes.get(index);
        }
        return null;
    }

    // 获取当前棋盘的当前玩家
    public Player getCurrentPlayer() {
        return currentPlayers.get(currentBoardIndex);
    }

    // 设置当前棋盘的当前玩家
    public void setCurrentPlayer(Player player) {
        currentPlayers.set(currentBoardIndex, player);
    }

    // 设置指定棋盘的当前玩家
    public void setPlayerAtIndex(int index, Player player) {
        if (index >= 0 && index < currentPlayers.size()) {
            currentPlayers.set(index, player);
        }
    }
} 