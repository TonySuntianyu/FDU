import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game {
    private final BoardManager boardManager = new BoardManager();
    private final Player player1;
    private final Player player2;
    private final Scanner scanner = new Scanner(System.in);
    private ArrayList<Boolean> boardFinished = new ArrayList<>();

    public Game(String p1, String p2) {
        player1 = new Player(p1, Board.Piece.BLACK);
        player2 = new Player(p2, Board.Piece.WHITE);

        // 初始化时创建三个不同模式的棋盘
        boardManager.addBoard(new PeaceMode());    // 1号和平模式
        boardManager.addBoard(new ReversiMode());  // 2号黑白棋模式
        boardManager.addBoard(new GomokuMode());   // 3号五子棋模式

        // 为每个棋盘设置初始玩家
        boardManager.setPlayerAtIndex(0, player1);
        boardManager.setPlayerAtIndex(1, player1);
        boardManager.setPlayerAtIndex(2, player1);

        // 初始化每个棋盘的状态为未结束
        boardFinished.add(false);
        boardFinished.add(false);
        boardFinished.add(false);
    }

    public void start() {
        while (true) {
            ReversiGame.clearScreen();
            int currentBoardIndex = boardManager.getCurrentBoardIndex() - 1;  // 转换为0-based索引
            Board currentBoard = boardManager.getCurrentBoard();
            GameMode currentMode = boardManager.getCurrentMode();
            Player currentPlayer = boardManager.getCurrentPlayer();
            
            // 如果当前玩家为null，设置为player1
            if (currentPlayer == null) {
                currentPlayer = player1;
                boardManager.setCurrentPlayer(currentPlayer);
            }

            currentBoard.printBoard(currentPlayer, player1, player2,
                currentMode, boardManager.getCurrentBoardIndex(), boardManager);

            System.out.println();

            // 检查五子棋胜利条件
            if (currentMode instanceof GomokuMode) {
                for (int i = 0; i < currentBoard.getSize(); i++) {
                    for (int j = 0; j < currentBoard.getSize(); j++) {
                        Board.Piece piece = currentBoard.getPiece(i, j);
                        if (piece != Board.Piece.EMPTY &&
                            piece != Board.Piece.BARRIER &&
                            piece != Board.Piece.CRATER &&
                            ((GomokuMode)currentMode).checkWin(currentBoard, i, j, piece)) {
                            boardFinished.set(currentBoardIndex, true);
                            System.out.println("五子连珠！游戏结束！");
                            System.out.printf("玩家[%s]胜利！\n",
                                (piece == Board.Piece.BLACK) ? player1.getName() : player2.getName());
                            break;
                        }
                    }
                    if (boardFinished.get(currentBoardIndex)) break;
                }
            }

            int[] score = currentBoard.getScore();

            // 检查当前棋盘是否结束
            if (!boardFinished.get(currentBoardIndex) &&
                (currentBoard.isFull() || (!currentMode.hasValidMoves(currentBoard, Board.Piece.BLACK) &&
                !currentMode.hasValidMoves(currentBoard, Board.Piece.WHITE)))) {

                boardFinished.set(currentBoardIndex, true);  // 标记当前棋盘已结束
                System.out.println("当前棋盘游戏结束！");
                System.out.printf("最终比分 - %s: %d  %s: %d\n",
                    player1.getName(), score[0],
                    player2.getName(), score[1]);
                if (score[0] > score[1]) {
                    System.out.printf("玩家[%s]胜利！\n", player1.getName());
                } else if (score[1] > score[0]) {
                    System.out.printf("玩家[%s]胜利！\n", player2.getName());
                } else {
                    System.out.println("平局！");
                }
                System.out.println("\n您可以：");
                System.out.println("1. 输入棋盘编号切换到其他棋盘");
                System.out.println("2. 输入peace创建新的和平模式棋盘");
                System.out.println("3. 输入reversi创建新的黑白棋模式棋盘");
                System.out.println("4. 输入gomoku创建新的五子棋模式棋盘");
                System.out.println("5. 输入exit退出游戏");
                System.out.print("\n请输入命令：");
            } else if (boardFinished.get(currentBoardIndex)) {
                // 如果当前棋盘已经结束，显示结束状态
                System.out.println("当前棋盘已结束！");
                System.out.printf("最终比分 - %s: %d  %s: %d\n",
                    player1.getName(), score[0],
                    player2.getName(), score[1]);
                if (score[0] > score[1]) {
                    System.out.printf("玩家[%s]胜利！\n", player1.getName());
                } else if (score[1] > score[0]) {
                    System.out.printf("玩家[%s]胜利！\n", player2.getName());
                } else {
                    System.out.println("平局！");
                }
                System.out.println("\n您可以：");
                System.out.println("1. 输入棋盘编号切换到其他棋盘");
                System.out.println("2. 输入peace创建新的和平模式棋盘");
                System.out.println("3. 输入reversi创建新的黑白棋模式棋盘");
                System.out.println("4. 输入gomoku创建新的五子棋模式棋盘");
                System.out.println("5. 输入exit退出游戏");
                System.out.print("\n请输入命令：");
            } else {
                if (!currentMode.hasValidMoves(currentBoard, currentPlayer.getPiece())) {
                    System.out.println("玩家 " + currentPlayer.getName() + " 没有合法移动，跳过回合");
                    currentPlayer = (currentPlayer == player1) ? player2 : player1;
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}
                    continue;
                }

                System.out.print("请玩家[" + currentPlayer.getName() + "]输入命令：");
            }

            String input = scanner.nextLine().trim().toUpperCase(); // 改为大写以匹配15x15棋盘的列号

            // 处理命令输入
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                return;
            } else if (input.startsWith("PLAYBACK ")) {
                // 提取文件名
                String filename = input.substring(9).trim();
                playbackFromFile(filename);
                continue;
            } else if (input.equalsIgnoreCase("peace")) {
                int newIndex = boardManager.addBoard(new PeaceMode());
                boardFinished.add(false);  // 添加新棋盘状态
                // 为新棋盘设置初始玩家
                boardManager.setPlayerAtIndex(newIndex - 1, player1);
                boardManager.switchBoard(newIndex); // 切换到新创建的棋盘
                System.out.println("已创建新的peace棋盘，编号：" + newIndex);
                continue;
            } else if (input.equalsIgnoreCase("reversi")) {
                int newIndex = boardManager.addBoard(new ReversiMode());
                boardFinished.add(false);  // 添加新棋盘状态
                // 为新棋盘设置初始玩家
                boardManager.setPlayerAtIndex(newIndex - 1, player1);
                boardManager.switchBoard(newIndex); // 切换到新创建的棋盘
                System.out.println("已创建新的reversi棋盘，编号：" + newIndex);
                continue;
            } else if (input.equalsIgnoreCase("gomoku")) {  // 添加五子棋模式选项
                int newIndex = boardManager.addBoard(new GomokuMode());
                boardFinished.add(false);  // 添加新棋盘状态
                // 为新棋盘设置初始玩家
                boardManager.setPlayerAtIndex(newIndex - 1, player1);
                boardManager.switchBoard(newIndex); // 切换到新创建的棋盘
                System.out.println("已创建新的gomoku棋盘，编号：" + newIndex);
                continue;
            } else if (input.matches("\\d+")) {
                int boardIndex = Integer.parseInt(input);
                if (boardManager.switchBoard(boardIndex)) {
                    // 确保当前棋盘的玩家不为null
                    if (boardManager.getCurrentPlayer() == null) {
                        boardManager.setCurrentPlayer(player1);
                    }
                    System.out.println("已切换到棋盘 " + boardIndex);
                    continue;
                } else {
                    System.out.println("无效的棋盘编号！");
                    System.out.println("按任意键继续...");
                    scanner.nextLine();
                    continue;
                }
            }

            // 如果当前棋盘已结束，跳过移动逻辑
            if (boardFinished.get(currentBoardIndex)) {
                continue;
            }

            // 处理五子棋模式的特殊输入格式
            if (currentMode instanceof GomokuMode && currentBoard.getSize() == 15) {
                // 处理炸弹命令 @位置
                if (input.startsWith("@") && input.length() == 3) {
                    // 格式: @FA
                    String position = input.substring(1); // 获取位置部分
                    if (position.length() == 2 && 
                        position.charAt(0) >= '1' && position.charAt(0) <= 'F' && 
                        position.charAt(1) >= 'A' && position.charAt(1) <= 'O') {
                        
                        // 解析16进制行号和列号
                        int row = Character.digit(position.charAt(0), 16) - 1; // 转换16进制到0-based索引
                        int col = position.charAt(1) - 'A'; // 字母转为0-based索引
                        
                        GomokuMode gomokuMode = (GomokuMode)currentMode;
                        if (gomokuMode.useBomb(currentBoard, row, col, currentPlayer.getPiece())) {
                            System.out.println("炸弹已使用，消除了对方棋子！");
                            
                            // 更新当前棋盘的当前玩家
                            Player nextPlayer = (currentPlayer == player1) ? player2 : player1;
                            boardManager.setCurrentPlayer(nextPlayer);
                            
                            try { Thread.sleep(1500); } catch (InterruptedException e) {}
                            continue;
                        } else {
                            System.out.println("炸弹使用失败！请检查炸弹数量或目标位置是否有对方棋子。");
                            try { Thread.sleep(1500); } catch (InterruptedException e) {}
                            continue;
                        }
                    }
                }
                
                // 处理普通落子命令 (1-F)(A-O)
                if (input.length() == 2 && 
                    ((input.charAt(0) >= '1' && input.charAt(0) <= '9') || 
                    (input.charAt(0) >= 'A' && input.charAt(0) <= 'F')) && 
                    input.charAt(1) >= 'A' && input.charAt(1) <= 'O') {
                    
                    // 解析16进制行号和列号
                    int row = Character.digit(input.charAt(0), 16) - 1; // 转换16进制到0-based索引
                    int col = input.charAt(1) - 'A'; // 字母转为0-based索引
                    
                    GomokuMode gomokuMode = (GomokuMode)currentMode;
                    gomokuMode.isValidMove(currentBoard, row, col, currentPlayer.getPiece());
                    
                    if (!gomokuMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                        // 根据错误类型显示不同的提示信息
                        int errorType = gomokuMode.getLastError();
                        
                        if (errorType == GomokuMode.ERROR_BARRIER) {
                            System.out.println("该位置有障碍物！");
                        } else if (errorType == GomokuMode.ERROR_OCCUPIED) {
                            System.out.println("该位置已经有棋子占据！");
                        } else {
                            System.out.println("无效的移动！");
                        }
                        try { Thread.sleep(1500); } catch (InterruptedException e) {}
                        continue;
                    }
                    
                    // 更新当前棋盘的当前玩家
                    Player nextPlayer = (currentPlayer == player1) ? player2 : player1;
                    boardManager.setCurrentPlayer(nextPlayer);
                    continue;
                }
                
                System.out.println("输入格式错误！请输入1A到FO之间的坐标或@位置来使用炸弹！");
                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                continue;
            }
            
            // 处理传统8x8棋盘的输入
            if (!input.matches("[1-8][A-Ha-h]")) {
                System.out.println("输入格式错误，请输入1a到8h之间的坐标！");
                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                continue;
            }

            int row = Integer.parseInt(input.substring(0, 1)) - 1;
            int col = Character.toUpperCase(input.charAt(1)) - 'A';

            if (!currentMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                System.out.println("无效的移动！");
                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                continue;
            }

            // 更新当前棋盘的当前玩家
            Player nextPlayer = (currentPlayer == player1) ? player2 : player1;
            boardManager.setCurrentPlayer(nextPlayer);
        }
    }

    /**
     * 从文件中读取棋谱并自动播放
     * @param filename 棋谱文件名
     */
    private void playbackFromFile(String filename) {
        try {
            File file = new File(filename);
            Scanner fileScanner = new Scanner(file);
            
            int currentBoardIndex = boardManager.getCurrentBoardIndex() - 1;
            Board currentBoard = boardManager.getCurrentBoard();
            GameMode currentMode = boardManager.getCurrentMode();
            Player currentPlayer = player1; // 开始时默认为黑方（player1）
            
            System.out.println("开始播放棋谱: " + filename);
            System.out.println("每步间隔1秒，回放过程中，按任意键可以停止回放");
            
            // 创建一个标志来控制回放是否继续
            AtomicBoolean stopPlayback = new AtomicBoolean(false);
            
            // 记录游戏是否因棋盘已满或其他条件而自动结束
            AtomicBoolean autoFinished = new AtomicBoolean(false);
            
            // 创建一个线程来监听用户输入
            Thread inputThread = new Thread(() -> {
                try {
                    System.in.read();
                    stopPlayback.set(true);
                } catch (Exception e) {
                    // 忽略异常
                }
            });
            inputThread.setDaemon(true); // 设置为守护线程，这样主程序结束时它会自动结束
            inputThread.start();
            
            int moveCount = 0;
            
            while (fileScanner.hasNextLine() && !boardFinished.get(currentBoardIndex) && !stopPlayback.get()) {
                String move = fileScanner.nextLine().trim().toUpperCase();
                
                if (move.isEmpty()) continue;
                
                // 处理pass指令
                if (move.equalsIgnoreCase("PASS")) {
                    System.out.println("回放: 玩家 " + currentPlayer.getName() + " 跳过回合");
                    currentPlayer = (currentPlayer == player1) ? player2 : player1;
                    boardManager.setCurrentPlayer(currentPlayer);
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                    continue;
                }
                
                // 五子棋特殊处理
                if (currentMode instanceof GomokuMode && currentBoard.getSize() == 15) {
                    if (move.startsWith("@") && move.length() == 3) {
                        // 炸弹命令处理逻辑
                        String position = move.substring(1);
                        if (position.length() == 2 && 
                            position.charAt(0) >= '1' && position.charAt(0) <= 'F' && 
                            position.charAt(1) >= 'A' && position.charAt(1) <= 'O') {
                            
                            int row = Character.digit(position.charAt(0), 16) - 1;
                            int col = position.charAt(1) - 'A';
                            
                            GomokuMode gomokuMode = (GomokuMode)currentMode;
                            if (gomokuMode.useBomb(currentBoard, row, col, currentPlayer.getPiece())) {
                                System.out.println("回放: 玩家[" + currentPlayer.getName() + 
                                                   "] 在 " + move + " 使用炸弹");
                                // 更新玩家
                                currentPlayer = (currentPlayer == player1) ? player2 : player1;
                                boardManager.setCurrentPlayer(currentPlayer);
                                
                                // 更新棋盘显示
                                ReversiGame.clearScreen();
                                currentBoard.printBoard(currentPlayer, player1, player2,
                                    currentMode, boardManager.getCurrentBoardIndex(), boardManager);
                                System.out.println("自动下棋中...按任意键停止");
                                
                                moveCount++;
                                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                                continue;
                            }
                        }
                    } else if (move.length() == 2 && 
                        ((move.charAt(0) >= '1' && move.charAt(0) <= '9') || 
                        (move.charAt(0) >= 'A' && move.charAt(0) <= 'F')) && 
                        move.charAt(1) >= 'A' && move.charAt(1) <= 'O') {
                        
                        int row = Character.digit(move.charAt(0), 16) - 1;
                        int col = move.charAt(1) - 'A';
                        
                        if (currentMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                            System.out.println("回放: 玩家[" + currentPlayer.getName() + 
                                               "] 在 " + move + " 落子");
                            
                            // 检查五子棋胜利条件
                            if (((GomokuMode)currentMode).checkWin(currentBoard, row, col, currentPlayer.getPiece())) {
                                System.out.println("五子连珠！游戏结束！");
                                System.out.printf("玩家[%s]胜利！\n", currentPlayer.getName());
                                boardFinished.set(currentBoardIndex, true);
                                
                                // 更新棋盘显示
                                ReversiGame.clearScreen();
                                currentBoard.printBoard(currentPlayer, player1, player2,
                                    currentMode, boardManager.getCurrentBoardIndex(), boardManager);
                                
                                // 游戏结束，显示通知后自动退出回放
                                System.out.println("游戏结束，自动退出回放...");
                                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                                return; // 直接返回，不要求用户输入
                            }
                            
                            // 更新玩家
                            currentPlayer = (currentPlayer == player1) ? player2 : player1;
                            boardManager.setCurrentPlayer(currentPlayer);
                            
                            // 更新棋盘显示
                            ReversiGame.clearScreen();
                            currentBoard.printBoard(currentPlayer, player1, player2,
                                currentMode, boardManager.getCurrentBoardIndex(), boardManager);
                            System.out.println("自动下棋中...按任意键停止");
                            
                            moveCount++;
                            try { Thread.sleep(1000); } catch (InterruptedException e) {}
                            continue;
                        }
                    }
                } else {
                    // 处理普通落子（传统8x8棋盘）
                    if (move.matches("[1-8][A-Ha-h]")) {
                        int row = Integer.parseInt(move.substring(0, 1)) - 1;
                        int col = Character.toUpperCase(move.charAt(1)) - 'A';
                        
                        if (currentMode.placePiece(currentBoard, row, col, currentPlayer.getPiece())) {
                            System.out.println("回放: 玩家[" + currentPlayer.getName() + 
                                               "] 在 " + move + " 落子");
                            
                            // 更新玩家
                            currentPlayer = (currentPlayer == player1) ? player2 : player1;
                            boardManager.setCurrentPlayer(currentPlayer);
                            
                            // 更新棋盘显示
                            ReversiGame.clearScreen();
                            currentBoard.printBoard(currentPlayer, player1, player2,
                                currentMode, boardManager.getCurrentBoardIndex(), boardManager);
                            
                            // 检查棋盘是否已满或者没有可行移动
                            if (currentBoard.isFull() || 
                                (!currentMode.hasValidMoves(currentBoard, Board.Piece.BLACK) &&
                                !currentMode.hasValidMoves(currentBoard, Board.Piece.WHITE))) {
                                
                                int[] score = currentBoard.getScore();
                                boardFinished.set(currentBoardIndex, true);
                                autoFinished.set(true); // 标记为自动结束
                                
                                System.out.println("棋盘已满或无可行移动，游戏结束！");
                                System.out.printf("最终比分 - %s: %d  %s: %d\n",
                                    player1.getName(), score[0],
                                    player2.getName(), score[1]);
                                
                                // 游戏结束，显示通知后自动退出回放
                                System.out.println("游戏结束，自动退出回放...");
                                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                                return; // 直接返回，不要求用户输入
                            }
                            
                            System.out.println("自动下棋中...按任意键停止");
                            moveCount++;
                            try { Thread.sleep(1000); } catch (InterruptedException e) {}
                            continue;
                        }
                    }
                }
                
                // 如果到这里，说明指令无效
                System.out.println("棋谱中包含无效的移动: " + move);
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            
            fileScanner.close();
            
            // 尝试中断输入线程
            inputThread.interrupt();
            
            // 清空输入缓冲区
            try {
                while (System.in.available() > 0) {
                    System.in.read();
                }
            } catch (Exception e) {
                // 忽略异常
            }
            
            // 检查游戏是否自动结束（通过棋盘满或五子连珠等条件）
            if (autoFinished.get() || (boardFinished.get(currentBoardIndex) && !stopPlayback.get())) {
                // 游戏自动结束的情况，直接返回，无需用户确认
                System.out.println("游戏自动结束，回放结束，共计" + moveCount + "步");
                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                return; // 直接返回，不要求用户输入
            }
            
            // 用户主动中止情况下要求确认
            if (stopPlayback.get()) {
                System.out.println("用户停止了回放，已播放" + moveCount + "步");
                System.out.println("按ENTER键继续游戏...");
                scanner.nextLine();
            } else if (!fileScanner.hasNextLine()) {
                // 棋谱已放完但游戏未结束的情况
                System.out.println("棋谱播放完成，共计" + moveCount + "步");
                
                // 检查是否达到游戏结束条件
                if (currentBoard.isFull() || 
                    (!currentMode.hasValidMoves(currentBoard, Board.Piece.BLACK) &&
                    !currentMode.hasValidMoves(currentBoard, Board.Piece.WHITE))) {
                    
                    boardFinished.set(currentBoardIndex, true);
                    int[] score = currentBoard.getScore();
                    
                    System.out.println("游戏结束！");
                    System.out.printf("最终比分 - %s: %d  %s: %d\n",
                        player1.getName(), score[0],
                        player2.getName(), score[1]);
                    
                    if (score[0] > score[1]) {
                        System.out.printf("玩家[%s]胜利！\n", player1.getName());
                    } else if (score[1] > score[0]) {
                        System.out.printf("玩家[%s]胜利！\n", player2.getName());
                    } else {
                        System.out.println("平局！");
                    }
                    
                    // 游戏结束，直接返回
                    System.out.println("游戏结束，自动退出回放...");
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}
                    return; // 直接返回，不要求用户输入
                } else {
                    // 棋谱播放完毕但游戏未结束，询问用户是否继续
                    System.out.println("按ENTER键继续游戏...");
                    scanner.nextLine();
                }
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("找不到棋谱文件: " + filename);
            System.out.println("按ENTER键继续...");
            scanner.nextLine();
        }
    }
} 