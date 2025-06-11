import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Game state data
    private SerializableBoardManager boardManager;
    private ArrayList<Boolean> boardFinished;
    private SerializablePlayer player1;
    private SerializablePlayer player2;
    private boolean showHints = true;  // 合法位置提示状态
    
    public void saveState(BoardManager boardManager, ArrayList<Boolean> boardFinished, 
                         Player player1, Player player2, boolean showHints) {
        this.boardManager = new SerializableBoardManager(boardManager);
        this.boardFinished = new ArrayList<>(boardFinished);
        this.player1 = new SerializablePlayer(player1);
        this.player2 = new SerializablePlayer(player2);
        this.showHints = showHints;
    }
    
    public void restoreState(GameApplication app) {
        try {
            Player restoredPlayer1 = player1.toPlayer();
            Player restoredPlayer2 = player2.toPlayer();
            
            // 先设置GameApplication中的玩家，以便BoardManager能使用正确的引用
            app.setPlayer1(restoredPlayer1);
            app.setPlayer2(restoredPlayer2);
            
            BoardManager restoredBoardManager = boardManager.toBoardManager(restoredPlayer1, restoredPlayer2);
            ArrayList<Boolean> restoredBoardFinished = new ArrayList<>(boardFinished);
            
            // 验证恢复的数据完整性
            if (restoredBoardManager.getBoardCount() == 0) {
                throw new IllegalStateException("恢复的棋盘管理器为空");
            }
            
            if (restoredBoardFinished.size() != restoredBoardManager.getBoardCount()) {
                // 修复boardFinished大小不匹配的问题
                restoredBoardFinished = new ArrayList<>();
                for (int i = 0; i < restoredBoardManager.getBoardCount(); i++) {
                    restoredBoardFinished.add(false);
                }
            }
            
            app.setBoardManager(restoredBoardManager);
            app.setBoardFinished(restoredBoardFinished);
            app.setShowHints(showHints);
            
            // 验证当前棋盘索引的有效性
            int currentIndex = restoredBoardManager.getCurrentBoardIndex();
            if (currentIndex < 1 || currentIndex > restoredBoardManager.getBoardCount()) {
                restoredBoardManager.switchBoard(1); // 切换到第一个棋盘
            }
            
        } catch (Exception e) {
            throw new RuntimeException("恢复游戏状态失败: " + e.getMessage(), e);
        }
    }
    
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }
    
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            GameState loaded = (GameState) in.readObject();
            this.boardManager = loaded.boardManager;
            this.boardFinished = loaded.boardFinished;
            this.player1 = loaded.player1;
            this.player2 = loaded.player2;
            this.showHints = loaded.showHints;
        }
    }
    
    // Serializable wrapper for Player
    private static class SerializablePlayer implements Serializable {
        private String name;
        private Board.Piece piece;
        
        public SerializablePlayer(Player player) {
            this.name = player.getName();
            this.piece = player.getPiece();
        }
        
        public Player toPlayer() {
            return new Player(name, piece);
        }
    }
    
    // Serializable wrapper for BoardManager
    private static class SerializableBoardManager implements Serializable {
        private ArrayList<SerializableBoard> boards;
        private ArrayList<SerializableGameMode> modes;
        private ArrayList<SerializablePlayer> currentPlayers;
        private int currentBoardIndex;
        
        public SerializableBoardManager(BoardManager boardManager) {
            this.boards = new ArrayList<>();
            this.modes = new ArrayList<>();
            this.currentPlayers = new ArrayList<>();
            this.currentBoardIndex = boardManager.getCurrentBoardIndex() - 1; // Convert to 0-based
            
            for (int i = 0; i < boardManager.getBoardCount(); i++) {
                // Get board by switching to it temporarily
                int originalIndex = boardManager.getCurrentBoardIndex();
                boardManager.switchBoard(i + 1);
                
                Board board = boardManager.getCurrentBoard();
                GameMode mode = boardManager.getCurrentMode();
                Player player = boardManager.getCurrentPlayer();
                
                boards.add(new SerializableBoard(board, mode.getClass().getSimpleName()));
                modes.add(new SerializableGameMode(mode));
                currentPlayers.add(player != null ? new SerializablePlayer(player) : null);
                
                // Restore original index
                boardManager.switchBoard(originalIndex);
            }
        }
        
        public BoardManager toBoardManager(Player player1, Player player2) {
            BoardManager boardManager = new BoardManager();
            
            // Add all boards and restore their states individually
            for (int i = 0; i < boards.size(); i++) {
                SerializableBoard serBoard = boards.get(i);
                SerializableGameMode serMode = modes.get(i);
                
                GameMode mode = serMode.toGameMode();
                int newIndex = boardManager.addBoard(mode);
                
                // Temporarily switch to the newly created board to restore its state
                boardManager.switchBoard(newIndex);
                Board board = boardManager.getCurrentBoard();
                serBoard.restoreBoard(board, mode.getClass().getSimpleName());
                
                // Restore the current player for this specific board
                SerializablePlayer serPlayer = currentPlayers.get(i);
                if (serPlayer != null) {
                    if (serPlayer.piece == Board.Piece.BLACK) {
                        boardManager.setCurrentPlayer(player1);
                    } else {
                        boardManager.setCurrentPlayer(player2);
                    }
                } else {
                    // Default to player1 if no player info is saved
                    boardManager.setCurrentPlayer(player1);
                }
            }
            
            // Finally, restore the actual current board index from the saved state
            boardManager.switchBoard(currentBoardIndex + 1); // Convert back to 1-based
            
            return boardManager;
        }
    }
    
    // Serializable wrapper for Board
    private static class SerializableBoard implements Serializable {
        private Board.Piece[][] board;
        private int size;
        private String associatedModeType; // 添加关联的模式类型
        
        public SerializableBoard(Board board, String modeType) {
            this.size = board.getSize();
            this.associatedModeType = modeType;
            this.board = new Board.Piece[size][size];
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    this.board[i][j] = board.getPiece(i, j);
                }
            }
        }
        
        public void restoreBoard(Board board, String currentModeType) {
            // 向后兼容性：如果associatedModeType为null，根据尺寸推断模式类型
            if (associatedModeType == null) {
                if (size == 8) {
                    associatedModeType = "ReversiMode"; // 默认8x8为黑白棋模式
                } else if (size == 15) {
                    associatedModeType = "GomokuMode"; // 默认15x15为五子棋模式
                } else {
                    System.err.println("警告：未知的棋盘尺寸: " + size);
                    return;
                }
            }
            
            // 检查尺寸和模式类型是否匹配
            if (board.getSize() != size) {
                System.err.println("警告：棋盘尺寸不匹配。保存的尺寸: " + size + ", 当前尺寸: " + board.getSize());
                return;
            }
            
            // 模式兼容性检查：8x8模式间可以互相兼容（和平模式和黑白棋模式）
            boolean modeCompatible = false;
            if (size == 8 && (associatedModeType.equals("PeaceMode") || associatedModeType.equals("ReversiMode"))) {
                if (currentModeType.equals("PeaceMode") || currentModeType.equals("ReversiMode")) {
                    modeCompatible = true;
                }
            } else if (associatedModeType.equals(currentModeType)) {
                modeCompatible = true;
            }
            
            if (!modeCompatible) {
                System.err.println("警告：模式类型不兼容。保存的模式: " + associatedModeType + ", 当前模式: " + currentModeType);
                return;
            }
            
            // 只在尺寸和模式都匹配时才恢复棋盘状态
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    board.setPiece(i, j, this.board[i][j]);
                }
            }
        }
    }
    
    // Serializable wrapper for GameMode
    private static class SerializableGameMode implements Serializable {
        private String modeType;
        private int blackBombs = -1;
        private int whiteBombs = -1;
        private int moveCount = -1;
        
        public SerializableGameMode(GameMode mode) {
            if (mode instanceof PeaceMode) {
                modeType = "peace";
            } else if (mode instanceof ReversiMode) {
                modeType = "reversi";
            } else if (mode instanceof GomokuMode) {
                modeType = "gomoku";
                GomokuMode gomokuMode = (GomokuMode) mode;
                blackBombs = gomokuMode.getBlackBombs();
                whiteBombs = gomokuMode.getWhiteBombs();
                moveCount = gomokuMode.getMoveCount();
            }
        }
        
        public GameMode toGameMode() {
            switch (modeType) {
                case "peace":
                    return new PeaceMode();
                case "reversi":
                    return new ReversiMode();
                case "gomoku":
                    GomokuMode gomokuMode = new GomokuMode();
                    if (blackBombs >= 0) {
                        gomokuMode.setBlackBombs(blackBombs);
                    }
                    if (whiteBombs >= 0) {
                        gomokuMode.setWhiteBombs(whiteBombs);
                    }
                    if (moveCount >= 0) {
                        gomokuMode.setMoveCount(moveCount);
                    }
                    return gomokuMode;
                default:
                    throw new IllegalArgumentException("Unknown mode type: " + modeType);
            }
        }
    }
} 