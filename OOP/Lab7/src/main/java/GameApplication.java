import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GameApplication extends Application {
    
    // Game components
    private BoardManager boardManager;
    private Player player1;
    private Player player2;
    private ArrayList<Boolean> boardFinished;
    private GameState gameState;
    
    // UI components
    private GridPane boardGrid;
    private ListView<String> gameListView;
    private Label currentGameLabel;
    private Label currentPlayerLabel;
    private Label blackPlayerLabel;
    private Label whitePlayerLabel;
    private Label blackScoreLabel;
    private Label whiteScoreLabel;
    private Label bombCountLabel;
    private Label gameResultLabel;
    private Button passButton;
    private Button bombButton;
    private Button newPeaceButton;
    private Button newReversiButton;
    private Button newGomokuButton;
    private Button playbackButton;
    private Button quitButton;
    private Button hintButton;  // 合法位置提示开关按钮
    
    // Game state
    private boolean bombMode = false;
    private boolean showHints = true;  // 默认显示合法位置提示
    private Timer playbackTimer;
    private Stage primaryStage;
    private boolean updatingGameList = false; // 防止无限递归的标志
    
    // Save file name
    private static final String SAVE_FILE = "pj.game";
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        // Create UI first
        BorderPane root = createMainLayout();
        
        // Setup scene
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("多种棋类游戏");
        stage.setScene(scene);
        
        // Handle window close event for saving state
        stage.setOnCloseRequest(this::handleWindowClose);
        
        // Try to load saved state first, if fails, initialize game
        if (!loadGameState()) {
            // If loading fails, initialize with default state
            initializeGame();
        }
        
        // Update display
        updateDisplay();
        
        stage.show();
    }
    
    private void initializeGame() {
        player1 = new Player("Tom", Board.Piece.BLACK);
        player2 = new Player("Jerry", Board.Piece.WHITE);
        boardManager = new BoardManager();
        boardFinished = new ArrayList<>();
        
        // Initialize with three boards
        boardManager.addBoard(new PeaceMode());
        boardManager.addBoard(new ReversiMode());
        boardManager.addBoard(new GomokuMode());
        
        // Initialize board states
        boardFinished.add(false);
        boardFinished.add(false);
        boardFinished.add(false);
        
        // 直接为每个棋盘设置初始玩家（黑棋先行）
        boardManager.setPlayerAtIndex(0, player1); // 和平模式棋盘
        boardManager.setPlayerAtIndex(1, player1); // 翻转棋模式棋盘
        boardManager.setPlayerAtIndex(2, player1); // 五子棋模式棋盘
        
        // 切换到第一个棋盘作为默认显示
        boardManager.switchBoard(1);
    }
    
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        
        // Left side - chess board (fixed size)
        VBox leftPanel = createBoardPanel();
        leftPanel.setPrefWidth(600);
        leftPanel.setMinWidth(600);
        leftPanel.setMaxWidth(600);
        
        // Right side - information panels (3 columns)
        HBox rightPanel = createInfoPanel();
        
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);
        
        return root;
    }
    
    private VBox createBoardPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.CENTER);
        
        Label title = new Label("棋盘");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(2);
        boardGrid.setVgap(2);
        
        panel.getChildren().addAll(title, boardGrid);
        return panel;
    }
    
    private HBox createInfoPanel() {
        HBox panel = new HBox(10);
        panel.setFillHeight(true);
        
        // Column 1: Player and game info - 给更多空间
        VBox column1 = createPlayerInfoColumn();
        column1.setMinWidth(200);
        column1.setPrefWidth(250);
        
        // Column 2: Game list - 中等空间
        VBox column2 = createGameListColumn();
        column2.setMinWidth(180);
        column2.setPrefWidth(200);
        
        // Column 3: Controls - 固定空间
        VBox column3 = createControlsColumn();
        column3.setMinWidth(140);
        column3.setPrefWidth(150);
        
        // 设置列的增长优先级
        HBox.setHgrow(column1, Priority.SOMETIMES); // 游戏信息列优先扩展
        HBox.setHgrow(column2, Priority.SOMETIMES); // 游戏列表次优先
        HBox.setHgrow(column3, Priority.NEVER);     // 控制列保持固定大小
        
        panel.getChildren().addAll(column1, column2, column3);
        return panel;
    }
    
    private VBox createPlayerInfoColumn() {
        VBox column = new VBox(8); // 减少间距以容纳更多信息
        column.setPadding(new Insets(15));
        
        Label title = new Label("游戏信息");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // 创建标签时设置自动换行和最大宽度
        currentGameLabel = createInfoLabel("当前游戏: 1");
        currentPlayerLabel = createInfoLabel("当前玩家: Tom");
        blackPlayerLabel = createInfoLabel("黑方: Tom");
        whitePlayerLabel = createInfoLabel("白方: Jerry");
        blackScoreLabel = createInfoLabel("黑方分数: 2");
        whiteScoreLabel = createInfoLabel("白方分数: 2");
        bombCountLabel = createInfoLabel("剩余炸弹: 黑方2个, 白方3个");
        gameResultLabel = createInfoLabel("");
        gameResultLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-wrap-text: true;");
        
        column.getChildren().addAll(
            title, currentGameLabel, currentPlayerLabel,
            blackPlayerLabel, whitePlayerLabel,
            blackScoreLabel, whiteScoreLabel,
            bombCountLabel, gameResultLabel
        );
        
        return column;
    }
    
    // 新增方法：创建带自动换行的信息标签
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true); // 启用自动换行
        label.setMaxWidth(Double.MAX_VALUE); // 允许标签使用最大可用宽度
        label.setStyle("-fx-font-size: 12px; -fx-padding: 2px 0;"); // 设置合适的字体大小和内边距
        return label;
    }
    
    private VBox createGameListColumn() {
        VBox column = new VBox(10);
        column.setPadding(new Insets(15)); // 减少内边距
        
        Label title = new Label("游戏列表");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        gameListView = new ListView<>();
        gameListView.setPrefHeight(280); // 稍微减少高度以给游戏信息更多空间
        gameListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                // 防止在更新游戏列表时触发无限递归
                if (!updatingGameList) {
                    switchGame(newValue);
                }
            }
        );
        
        column.getChildren().addAll(title, gameListView);
        return column;
    }
    
    private VBox createControlsColumn() {
        VBox column = new VBox(8); // 减少间距
        column.setPadding(new Insets(15)); // 减少内边距
        
        Label title = new Label("操作");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Game control buttons - 稍微减小按钮宽度以适应窄列
        passButton = new Button("跳过回合");
        passButton.setPrefWidth(110);
        passButton.setOnAction(e -> passMove());
        
        bombButton = new Button("使用炸弹");
        bombButton.setPrefWidth(110);
        bombButton.setOnAction(e -> toggleBombMode());
        
        // New game buttons
        newPeaceButton = new Button("新建和平模式");
        newPeaceButton.setPrefWidth(110);
        newPeaceButton.setOnAction(e -> createNewGame("peace"));
        
        newReversiButton = new Button("新建黑白棋");
        newReversiButton.setPrefWidth(110);
        newReversiButton.setOnAction(e -> createNewGame("reversi"));
        
        newGomokuButton = new Button("新建五子棋");
        newGomokuButton.setPrefWidth(110);
        newGomokuButton.setOnAction(e -> createNewGame("gomoku"));
        
        // Playback button
        playbackButton = new Button("演示模式");
        playbackButton.setPrefWidth(110);
        playbackButton.setOnAction(e -> startPlayback());
        
        // Quit button
        quitButton = new Button("退出游戏");
        quitButton.setPrefWidth(110);
        quitButton.setOnAction(e -> quitGame());
        
        // Hint button
        hintButton = new Button("显示合法位置提示");
        hintButton.setPrefWidth(110);
        hintButton.setOnAction(e -> toggleHints());
        
        column.getChildren().addAll(
            title, passButton, bombButton,
            new Separator(),
            newPeaceButton, newReversiButton, newGomokuButton,
            new Separator(),
            playbackButton, quitButton, hintButton
        );
        
        return column;
    }
    
    private void updateDisplay() {
        updateBoardDisplay();
        updateInfoDisplay();
        updateGameList();
        updateButtonStates();
    }
    
    private void updateBoardDisplay() {
        boardGrid.getChildren().clear();
        
        Board currentBoard = boardManager.getCurrentBoard();
        int size = currentBoard.getSize();
        
        // Add column headers
        for (int col = 0; col < size; col++) {
            char header = (char)('A' + col);
            Label label = new Label(String.valueOf(header));
            label.setAlignment(Pos.CENTER);
            label.setPrefWidth(30);
            boardGrid.add(label, col + 1, 0);
        }
        
        // Add row headers and board cells
        for (int row = 0; row < size; row++) {
            // Row header
            String rowHeader;
            if (size == 15) {
                // 15x15 board uses hex notation
                rowHeader = Integer.toHexString(row + 1).toUpperCase();
            } else {
                // 8x8 board uses decimal notation
                rowHeader = String.valueOf(row + 1);
            }
            
            Label rowLabel = new Label(rowHeader);
            rowLabel.setAlignment(Pos.CENTER);
            rowLabel.setPrefHeight(30);
            boardGrid.add(rowLabel, 0, row + 1);
            
            // Board cells
            for (int col = 0; col < size; col++) {
                Button cell = createBoardCell(row, col);
                boardGrid.add(cell, col + 1, row + 1);
            }
        }
    }
    
    private Button createBoardCell(int row, int col) {
        Button cell = new Button();
        cell.setPrefSize(35, 35);
        cell.setMinSize(35, 35);
        cell.setMaxSize(35, 35);
        
        // 设置基本样式 - 浅绿色背景，黑色边框
        cell.setStyle("-fx-background-color: lightgreen; -fx-border-color: black; -fx-border-width: 1; -fx-padding: 0;");
        
        Board currentBoard = boardManager.getCurrentBoard();
        GameMode currentMode = boardManager.getCurrentMode();
        Board.Piece piece = currentBoard.getPiece(row, col);
        
        // 清空按钮文本
        cell.setText("");
        
        // 根据棋子类型创建相应的图形
        switch (piece) {
            case BLACK:
                // 创建黑色圆形棋子
                Circle blackPiece = new Circle(12);
                blackPiece.setFill(Color.BLACK);
                blackPiece.setStroke(Color.DARKGRAY);
                blackPiece.setStrokeWidth(1);
                cell.setGraphic(blackPiece);
                break;
                
            case WHITE:
                // 创建白色圆形棋子
                Circle whitePiece = new Circle(12);
                whitePiece.setFill(Color.WHITE);
                whitePiece.setStroke(Color.BLACK);
                whitePiece.setStrokeWidth(1);
                cell.setGraphic(whitePiece);
                break;
                
            case BARRIER:
                // 创建棕色方形障碍物
                Rectangle barrier = new Rectangle(20, 20);
                barrier.setFill(Color.SADDLEBROWN);
                barrier.setStroke(Color.BLACK);
                barrier.setStrokeWidth(1);
                cell.setGraphic(barrier);
                break;
                
            case CRATER:
                // 创建弹坑效果
                StackPane craterGraphic = new StackPane();
                
                Circle outerCrater = new Circle(12);
                outerCrater.setFill(Color.TRANSPARENT);
                outerCrater.setStroke(Color.GRAY);
                outerCrater.setStrokeWidth(3);
                
                Circle innerCrater = new Circle(8);
                innerCrater.setFill(Color.LIGHTGRAY);
                innerCrater.setStroke(Color.DARKGRAY);
                innerCrater.setStrokeWidth(1);
                
                craterGraphic.getChildren().addAll(outerCrater, innerCrater);
                cell.setGraphic(craterGraphic);
                break;
                
            default:
                // 空格子 - 检查是否需要显示合法位置提示
                if (showHints && currentMode instanceof ReversiMode && !boardFinished.get(boardManager.getCurrentBoardIndex() - 1)) {
                    Player currentPlayer = boardManager.getCurrentPlayer();
                    if (currentPlayer != null && currentMode.isValidMove(currentBoard, row, col, currentPlayer.getPiece())) {
                        // 创建合法位置提示 - 半透明小圆点
                        Circle hint = new Circle(6);
                        if (currentPlayer.getPiece() == Board.Piece.BLACK) {
                            hint.setFill(Color.BLACK);
                            hint.setOpacity(0.4);
                        } else {
                            hint.setFill(Color.WHITE);
                            hint.setStroke(Color.BLACK);
                            hint.setStrokeWidth(1);
                            hint.setOpacity(0.6);
                        }
                        cell.setGraphic(hint);
                    } else {
                        cell.setGraphic(null);
                    }
                } else {
                    cell.setGraphic(null);
                }
                break;
        }
        
        // 设置点击处理
        cell.setOnAction(e -> handleCellClick(row, col));
        
        return cell;
    }
    
    private void handleCellClick(int row, int col) {
        if (boardFinished.get(boardManager.getCurrentBoardIndex() - 1)) {
            showAlert("游戏已结束", "当前游戏已经结束，请选择其他游戏或创建新游戏。");
            return;
        }
        
        Player currentPlayer = boardManager.getCurrentPlayer();
        if (currentPlayer == null) {
            currentPlayer = player1;
            boardManager.setCurrentPlayer(currentPlayer);
        }
        
        Board currentBoard = boardManager.getCurrentBoard();
        GameMode currentMode = boardManager.getCurrentMode();
        
        if (bombMode && currentMode instanceof GomokuMode) {
            // Bomb mode
            GomokuMode gomokuMode = (GomokuMode) currentMode;
            if (gomokuMode.useBomb(currentBoard, row, col, currentPlayer.getPiece())) {
                bombMode = false;
                switchPlayer();
                updateDisplay();
                checkWinCondition();
            } else {
                showAlert("炸弹使用失败", "炸弹使用失败！请检查炸弹数量或目标位置是否有对方棋子。");
            }
        } else {
            // Normal move
            if (currentMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                switchPlayer();
                updateDisplay();
                checkWinCondition();
            } else {
                // Show error based on game mode
                if (currentMode instanceof GomokuMode) {
                    GomokuMode gomokuMode = (GomokuMode) currentMode;
                    int errorType = gomokuMode.getLastError();
                    switch (errorType) {
                        case GomokuMode.ERROR_BARRIER:
                            showAlert("无效移动", "该位置有障碍物！");
                            break;
                        case GomokuMode.ERROR_OCCUPIED:
                            showAlert("无效移动", "该位置已经有棋子占据！");
                            break;
                        default:
                            showAlert("无效移动", "无效的移动！");
                            break;
                    }
                } else {
                    showAlert("无效移动", "无效的移动！");
                }
            }
        }
    }
    
    private void switchPlayer() {
        Player currentPlayer = boardManager.getCurrentPlayer();
        Player nextPlayer = (currentPlayer == player1) ? player2 : player1;
        boardManager.setCurrentPlayer(nextPlayer);
    }
    
    private void checkWinCondition() {
        int currentBoardIndex = boardManager.getCurrentBoardIndex() - 1;
        Board currentBoard = boardManager.getCurrentBoard();
        GameMode currentMode = boardManager.getCurrentMode();
        
        // Check Gomoku win condition
        if (currentMode instanceof GomokuMode) {
            for (int i = 0; i < currentBoard.getSize(); i++) {
                for (int j = 0; j < currentBoard.getSize(); j++) {
                    Board.Piece piece = currentBoard.getPiece(i, j);
                    if (piece != Board.Piece.EMPTY &&
                        piece != Board.Piece.BARRIER &&
                        piece != Board.Piece.CRATER &&
                        ((GomokuMode)currentMode).checkWin(currentBoard, i, j, piece)) {
                        
                        boardFinished.set(currentBoardIndex, true);
                        String winner = (piece == Board.Piece.BLACK) ? player1.getName() : player2.getName();
                        gameResultLabel.setText("五子连珠！玩家[" + winner + "]胜利！");
                        return;
                    }
                }
            }
        }
        
        // Check end conditions for different game modes
        int[] score = currentBoard.getScore();
        boolean gameEnded = false;
        
        if (currentMode instanceof PeaceMode) {
            // 和平模式：只有棋盘填满时才结束
            gameEnded = currentBoard.isFull();
        } else {
            // 其他模式：棋盘填满或双方都没有合法移动时结束
            gameEnded = currentBoard.isFull() || 
                       (!currentMode.hasValidMoves(currentBoard, Board.Piece.BLACK) &&
                        !currentMode.hasValidMoves(currentBoard, Board.Piece.WHITE));
        }
        
        if (gameEnded) {
            boardFinished.set(currentBoardIndex, true);
            
            String result;
            if (score[0] > score[1]) {
                result = "玩家[" + player1.getName() + "]胜利！";
            } else if (score[1] > score[0]) {
                result = "玩家[" + player2.getName() + "]胜利！";
            } else {
                result = "平局！";
            }
            gameResultLabel.setText("游戏结束！" + result);
        }
    }
    
    private void updateInfoDisplay() {
        currentGameLabel.setText("当前游戏: " + boardManager.getCurrentBoardIndex());
        
        Player currentPlayer = boardManager.getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayerLabel.setText("当前玩家: " + currentPlayer.getName());
        }
        
        blackPlayerLabel.setText("黑方: " + player1.getName());
        whitePlayerLabel.setText("白方: " + player2.getName());
        
        Board currentBoard = boardManager.getCurrentBoard();
        int[] score = currentBoard.getScore();
        blackScoreLabel.setText("黑方分数: " + score[0]);
        whiteScoreLabel.setText("白方分数: " + score[1]);
        
        // Update bomb count for Gomoku mode
        GameMode currentMode = boardManager.getCurrentMode();
        if (currentMode instanceof GomokuMode) {
            GomokuMode gomokuMode = (GomokuMode) currentMode;
            bombCountLabel.setText("剩余炸弹: 黑方" + gomokuMode.getBlackBombs() + "个, 白方" + gomokuMode.getWhiteBombs() + "个");
            bombCountLabel.setVisible(true);
        } else {
            bombCountLabel.setVisible(false);
        }
        
        // Clear result if game is active
        if (!boardFinished.get(boardManager.getCurrentBoardIndex() - 1)) {
            gameResultLabel.setText("");
        }
    }
    
    private void updateGameList() {
        updatingGameList = true; // 设置标志，防止触发选择事件
        
        gameListView.getItems().clear();
        
        for (int i = 1; i <= boardManager.getBoardCount(); i++) {
            GameMode mode = boardManager.getModeAtIndex(i - 1);
            String modeName;
            if (mode instanceof PeaceMode) {
                modeName = "和平模式";
            } else if (mode instanceof ReversiMode) {
                modeName = "黑白棋";
            } else if (mode instanceof GomokuMode) {
                modeName = "五子棋";
            } else {
                modeName = "未知";
            }
            
            String gameItem = "游戏 " + i + " - " + modeName;
            gameListView.getItems().add(gameItem);
        }
        
        // Select current game
        gameListView.getSelectionModel().select(boardManager.getCurrentBoardIndex() - 1);
        
        updatingGameList = false; // 重置标志
    }
    
    private void updateButtonStates() {
        GameMode currentMode = boardManager.getCurrentMode();
        boolean isGameFinished = boardFinished.get(boardManager.getCurrentBoardIndex() - 1);
        
        // Pass button only for Reversi mode
        passButton.setVisible(currentMode instanceof ReversiMode);
        passButton.setDisable(isGameFinished);
        
        // Bomb button only for Gomoku mode
        bombButton.setVisible(currentMode instanceof GomokuMode);
        bombButton.setDisable(isGameFinished);
        
        if (bombMode) {
            bombButton.setText("取消炸弹");
            bombButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        } else {
            bombButton.setText("使用炸弹");
            bombButton.setStyle("");
        }
        
        // Hint button only for Reversi mode
        hintButton.setVisible(currentMode instanceof ReversiMode);
        if (showHints) {
            hintButton.setText("隐藏合法位置提示");
            hintButton.setStyle("-fx-background-color: lightblue;");
        } else {
            hintButton.setText("显示合法位置提示");
            hintButton.setStyle("");
        }
    }
    
    private void switchGame(String gameItem) {
        if (gameItem == null) return;
        
        try {
            String[] parts = gameItem.split(" ");
            int gameIndex = Integer.parseInt(parts[1]);
            
            if (boardManager.switchBoard(gameIndex)) {
                if (boardManager.getCurrentPlayer() == null) {
                    boardManager.setCurrentPlayer(player1);
                }
                bombMode = false; // Reset bomb mode when switching games
                updateDisplay();
            }
        } catch (Exception e) {
            showAlert("错误", "切换游戏失败: " + e.getMessage());
        }
    }
    
    private void passMove() {
        Player currentPlayer = boardManager.getCurrentPlayer();
        if (currentPlayer == null) {
            currentPlayer = player1;
            boardManager.setCurrentPlayer(currentPlayer);
        }
        
        GameMode currentMode = boardManager.getCurrentMode();
        if (currentMode.hasValidMoves(boardManager.getCurrentBoard(), currentPlayer.getPiece())) {
            showAlert("无效操作", "您还有合法移动，不能跳过回合！");
            return;
        }
        
        switchPlayer();
        updateDisplay();
        checkWinCondition();
    }
    
    private void toggleBombMode() {
        bombMode = !bombMode;
        updateButtonStates();
    }
    
    private void createNewGame(String gameType) {
        try {
            GameMode newMode;
            switch (gameType) {
                case "peace":
                    newMode = new PeaceMode();
                    break;
                case "reversi":
                    newMode = new ReversiMode();
                    break;
                case "gomoku":
                    newMode = new GomokuMode();
                    break;
                default:
                    throw new IllegalArgumentException("未知的游戏类型: " + gameType);
            }
            
            int newIndex = boardManager.addBoard(newMode);
            boardFinished.add(false);
            
            // 正确的顺序：先切换到新棋盘，再设置当前玩家
            boardManager.switchBoard(newIndex);
            boardManager.setCurrentPlayer(player1); // 直接设置当前棋盘的当前玩家
            
            updateDisplay();
            showAlert("新游戏", "已创建新的" + gameType + "游戏，编号：" + newIndex);
        } catch (Exception e) {
            showAlert("错误", "创建新游戏失败: " + e.getMessage());
        }
    }
    
    private void startPlayback() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择演示文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("命令文件", "*.cmd")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            playbackFromFile(file.getAbsolutePath());
        }
    }
    
    private void playbackFromFile(String filename) {
        try {
            Scanner fileScanner = new Scanner(new File(filename));
            List<String> commands = new ArrayList<>();
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim().toUpperCase();
                if (!line.isEmpty()) {
                    commands.add(line);
                }
            }
            fileScanner.close();
            
            if (commands.isEmpty()) {
                showAlert("错误", "文件为空或无有效命令");
                return;
            }
            
            // Start playback
            playbackCommands(commands, 0);
            
        } catch (FileNotFoundException e) {
            showAlert("错误", "文件未找到: " + filename);
        } catch (Exception e) {
            showAlert("错误", "读取文件失败: " + e.getMessage());
        }
    }
    
    private void playbackCommands(List<String> commands, int index) {
        if (index >= commands.size()) {
            showAlert("演示完成", "演示模式播放完成");
            return;
        }
        
        String command = commands.get(index);
        
        // Execute command
        executePlaybackCommand(command);
        
        // Schedule next command
        playbackTimer = new Timer();
        playbackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    playbackCommands(commands, index + 1);
                });
            }
        }, 1000); // 1 second interval
    }
    
    private void executePlaybackCommand(String command) {
        Board currentBoard = boardManager.getCurrentBoard();
        GameMode currentMode = boardManager.getCurrentMode();
        Player currentPlayer = boardManager.getCurrentPlayer();
        
        if (currentPlayer == null) {
            currentPlayer = player1;
            boardManager.setCurrentPlayer(currentPlayer);
        }
        
        try {
            if (command.equals("PASS")) {
                switchPlayer();
                updateDisplay();
                return;
            }
            
            // Handle Gomoku commands
            if (currentMode instanceof GomokuMode && currentBoard.getSize() == 15) {
                if (command.startsWith("@") && command.length() == 3) {
                    // Bomb command
                    String position = command.substring(1);
                    int row = Character.digit(position.charAt(0), 16) - 1;
                    int col = position.charAt(1) - 'A';
                    
                    GomokuMode gomokuMode = (GomokuMode) currentMode;
                    if (gomokuMode.useBomb(currentBoard, row, col, currentPlayer.getPiece())) {
                        switchPlayer();
                        updateDisplay();
                        checkWinCondition();
                    }
                    return;
                } else if (command.length() == 2) {
                    // Normal move
                    int row = Character.digit(command.charAt(0), 16) - 1;
                    int col = command.charAt(1) - 'A';
                    
                    if (currentMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                        switchPlayer();
                        updateDisplay();
                        checkWinCondition();
                    }
                    return;
                }
            }
            
            // Handle regular 8x8 board commands
            if (command.matches("[1-8][A-H]")) {
                int row = Integer.parseInt(command.substring(0, 1)) - 1;
                int col = command.charAt(1) - 'A';
                
                if (currentMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                    switchPlayer();
                    updateDisplay();
                    checkWinCondition();
                }
            }
            
        } catch (Exception e) {
            // Ignore invalid commands in playback
        }
    }
    
    private void quitGame() {
        saveGameState();
        Platform.exit();
    }
    
    private void handleWindowClose(WindowEvent event) {
        saveGameState();
    }
    
    private void saveGameState() {
        try {
            gameState = new GameState();
            gameState.saveState(boardManager, boardFinished, player1, player2, showHints);
            gameState.saveToFile(SAVE_FILE);
        } catch (Exception e) {
            System.err.println("保存游戏状态失败: " + e.getMessage());
        }
    }
    
    private boolean loadGameState() {
        try {
            File saveFile = new File(SAVE_FILE);
            if (saveFile.exists()) {
                gameState = new GameState();
                gameState.loadFromFile(SAVE_FILE);
                
                // 尝试恢复状态
                gameState.restoreState(this);
                
                // 验证恢复后的状态
                if (boardManager == null || boardManager.getBoardCount() == 0) {
                    throw new IllegalStateException("恢复的棋盘管理器无效");
                }
                
                // 确保boardFinished列表大小匹配
                while (boardFinished.size() < boardManager.getBoardCount()) {
                    boardFinished.add(false);
                }
                while (boardFinished.size() > boardManager.getBoardCount()) {
                    boardFinished.remove(boardFinished.size() - 1);
                }
                
                System.out.println("游戏状态加载成功");
                return true;
            }
        } catch (Exception e) {
            System.err.println("加载游戏状态失败: " + e.getMessage());
            System.out.println("将使用默认初始化状态");
        }
        return false;
    }
    
    // Methods for GameState to access private fields
    public void setBoardManager(BoardManager boardManager) {
        this.boardManager = boardManager;
    }
    
    public void setBoardFinished(ArrayList<Boolean> boardFinished) {
        this.boardFinished = boardFinished;
    }
    
    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }
    
    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
    
    public void setShowHints(boolean showHints) {
        this.showHints = showHints;
    }
    
    public boolean getShowHints() {
        return showHints;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void toggleHints() {
        showHints = !showHints;
        updateDisplay();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 