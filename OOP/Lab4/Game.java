import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    // 使用动态列表存储所有棋盘
    private final List<Board> boards;
    // 每个棋盘的独立回合管理列表，0表示由player1落子，1表示由player2落子
    private final List<Integer> boardTurns;
    // 游戏类型列表
    private final List<String> gameTypes;
    // 当前使用的棋盘索引，初始为0
    private int currentBoardIndex = 0;

    private final Player player1;
    private final Player player2;
    private final Scanner scanner;

    public Game(Player player1, Player player2) {
        boards = new ArrayList<>();
        boardTurns = new ArrayList<>();
        gameTypes = new ArrayList<>();
        // 初始化两个棋盘：
        // 默认和平游戏（普通棋盘），输入 "peace" 时代表此类型游戏
        boards.add(new Board());
        boardTurns.add(0);
        gameTypes.add("peace");
        // 反转棋盘，输入 "reversi" 时代表此类型游戏
        boards.add(new ReverseBoard());
        boardTurns.add(0);
        gameTypes.add("reversi");
        this.player1 = player1;
        this.player2 = player2;
        this.scanner = new Scanner(System.in);
        
        // 设置每个棋盘的游戏列表引用
        updateBoardsGameList();
    }
    
    // 更新所有棋盘的游戏列表信息
    private void updateBoardsGameList() {
        for (Board board : boards) {
            board.setGameList(this);
        }
    }
    
    // 获取游戏类型列表
    public List<String[]> getGameTypeList() {
        List<String[]> result = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            result.add(new String[]{String.valueOf(i + 1), gameTypes.get(i)});
        }
        return result;
    }

    public void start() {
        // 游戏一直运行直到用户输入quit退出
        boolean running = true;
        boolean allGameEndedNotified = false;
        
        while (running) {
            Board currentBoard = boards.get(currentBoardIndex);
            // 根据当前棋盘的回合值确定当前玩家
            Player currentPlayer = (boardTurns.get(currentBoardIndex) == 0) ? player1 : player2;
            currentBoard.display(player1, player2, currentPlayer, currentBoardIndex + 1);
            
            // 检查所有棋盘是否已满或游戏结束，且未显示提示
            boolean allEnded = allBoardsFull() || allBoardsGameOver();
            if (allEnded && !allGameEndedNotified) {
                System.out.println("所有游戏都已结束！您可以添加新游戏继续，或输入quit退出。");
                allGameEndedNotified = true;
            } else if (!allEnded) {
                // 如果状态变化，重置通知标志
                allGameEndedNotified = false;
            }
            
            // 检查当前棋盘是否为反转棋游戏结束状态
            if (currentBoard instanceof ReverseBoard && ((ReverseBoard) currentBoard).isGameOver()) {
                //System.out.println("反转棋游戏已结束！" + ((ReverseBoard) currentBoard).getGameResult());
                
                // 等待用户确认后继续
                System.out.println("按Enter键继续...");
                scanner.nextLine();
                
                // 如果还有其他未结束的游戏，切换到下一个可用棋盘
                boolean foundValidBoard = false;
                for (int i = 0; i < boards.size(); i++) {
                    int nextBoardIndex = (currentBoardIndex + i + 1) % boards.size();
                    Board nextBoard = boards.get(nextBoardIndex);
                    if (!(nextBoard instanceof ReverseBoard) || !((ReverseBoard) nextBoard).isGameOver()) {
                        currentBoardIndex = nextBoardIndex;
                        foundValidBoard = true;
                        break;
                    }
                }
                
                if (!foundValidBoard) {
                    System.out.println("没有可用的棋盘！您可以添加新游戏继续。");
                }
                continue;
            }
            
            System.out.print("请玩家[" + currentPlayer.getName() + "]输入落子位置、棋盘编号，或添加新游戏(peace/reversi)，或输入 quit 退出");
            // 如果当前是反转棋并且当前玩家没有合法落子位置，显示pass提示
            if (currentBoard instanceof ReverseBoard && !((ReverseBoard) currentBoard).hasValidMove(currentPlayer.getPieceType())) {
                System.out.print("，或输入 pass 跳过本轮");
            }
            System.out.print("：");
            
            String input = scanner.nextLine().trim();

            // 判断是否退出游戏
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("退出游戏！");
                running = false;
                break;
            }
            
            // 处理 pass 命令（仅在反转棋游戏中有效）
            if (input.equalsIgnoreCase("pass")) {
                if (currentBoard instanceof ReverseBoard && ((ReverseBoard) currentBoard).canPass(currentPlayer.getPieceType())) {
                    System.out.println(currentPlayer.getName() + " 跳过本轮。");
                    // 更新当前棋盘的回合
                    boardTurns.set(currentBoardIndex, (boardTurns.get(currentBoardIndex) + 1) % 2);
                    scanner.nextLine();
                    continue;
                } else {
                    System.out.println("当前您有合法落子位置，不能跳过！");
                    scanner.nextLine();
                    continue;
                }
            }

            // 新增：判断是否添加新游戏
            if (input.equalsIgnoreCase("peace")) {
                // 添加和平游戏：普通棋盘
                boards.add(new Board());
                boardTurns.add(0);
                gameTypes.add("peace");
                System.out.println("成功添加新游戏peace，游戏编号为：" + boards.size());
                updateBoardsGameList();
                allGameEndedNotified = false; // 新游戏添加后重置状态
                scanner.nextLine();
                continue;
            } else if (input.equalsIgnoreCase("reversi")) {
                // 添加反转棋盘
                boards.add(new ReverseBoard());
                boardTurns.add(0);
                gameTypes.add("reversi");
                System.out.println("成功添加新游戏reversi，游戏编号为：" + boards.size());
                updateBoardsGameList();
                allGameEndedNotified = false; // 新游戏添加后重置状态
                scanner.nextLine();
                continue;
            } else if (input.length() == 1) {
                // 单字符输入，视为棋盘编号
                char ch = input.charAt(0);
                if (Character.isDigit(ch)) {
                    int boardNumber = Character.getNumericValue(ch);
                    if (boardNumber < 1 || boardNumber > boards.size()) {
                        System.out.println("棋盘编号必须在1到" + boards.size() + "之间！");
                        scanner.nextLine();
                        continue;
                    } else {
                        currentBoardIndex = boardNumber - 1;
                        System.out.println("成功切换到棋盘" + boardNumber + "，请继续输入落子位置。");
                        scanner.nextLine();
                    }
                } else {
                    System.out.println("输入格式错误，请输入棋盘编号（1~" + boards.size() + "）或落子位置（例如：1a）！或新游戏类型（peace/reversi）");
                    scanner.nextLine();
                }
                continue;
            } else if (input.length() == 2) {
                // 视为落子位置，例如 "1a"
                boolean validMove = false;
                try {
                    int row = Character.getNumericValue(input.charAt(0)) - 1;
                    char colChar = Character.toLowerCase(input.charAt(1));
                    int col = colChar - 'a';
                    validMove = currentBoard.placePiece(row, col, currentPlayer.getPieceType());
                    if (!validMove) {
                        System.out.println("落子位置有误或该位置已被占用，请重新输入！");
                        scanner.nextLine();
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("输入格式错误，请输入棋盘编号（1~" + boards.size() + "）或落子位置（例如：1a）！或新游戏类型（peace/reversi）");
                    scanner.nextLine();
                    continue;
                }
                
                // 检查棋盘是否已满并显示游戏结束信息
                currentBoard.checkGameEnd(player1, player2);
                
                // 更新当前棋盘的回合
                boardTurns.set(currentBoardIndex, (boardTurns.get(currentBoardIndex) + 1) % 2);
            } else {
                System.out.println("输入格式错误，请输入棋盘编号（1~" + boards.size() + "）、落子位置（例如：1a），或新游戏命令（peace/reversi）！");
                scanner.nextLine();
                continue;
            }
        }
        // 游戏结束消息移至quit命令处理
    }

    // 判断所有棋盘是否均已满
    private boolean allBoardsFull() {
        for (Board board : boards) {
            if (!board.isFull()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断所有棋盘游戏是否都已结束
     * 包括：棋盘已满，或者反转棋双方均无合法落子位置
     *
     * @return 如果所有棋盘游戏都已结束返回 true，否则返回 false
     */
    private boolean allBoardsGameOver() {
        for (Board board : boards) {
            if (board instanceof ReverseBoard) {
                // 对于反转棋，判断是否游戏已结束
                if (!((ReverseBoard) board).isGameOver()) {
                    return false;
                }
            } else {
                // 对于普通棋盘，判断是否已满
                if (!board.isFull()) {
                    return false;
                }
            }
        }
        return true;
    }

    // 获取玩家1
    public Player getPlayer1() {
        return player1;
    }

    // 获取玩家2
    public Player getPlayer2() {
        return player2;
    }

    // 获取当前棋盘索引
    public int getCurrentBoardIndex() {
        return currentBoardIndex;
    }
}
