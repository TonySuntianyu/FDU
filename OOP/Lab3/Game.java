import java.util.Scanner;

public class Game {
    // 修改：维护三个棋盘
    private final Board[] boards = new Board[3];
    // 当前使用的棋盘索引，默认 0 表示棋盘1
    private int currentBoardIndex = 0;

    private final Player player1;
    private final Player player2;
    // 修改：每个棋盘都有独立的回合管理，使用数组存储每个棋盘的当前玩家索引（0 表示 player1，1 表示 player2）
    private final int[] boardTurns = new int[3];
    private final Scanner scanner;

    public Game(Player player1, Player player2) {
        // 初始化三个棋盘
        for (int i = 0; i < boards.length; i++) {
            boards[i] = new Board();
            boardTurns[i] = 0; // 每个棋盘初始时均由 player1 落子
        }
        this.player1 = player1;
        this.player2 = player2;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        // 游戏结束条件：所有棋盘均已满
        while (!allBoardsFull()) {
            Board currentBoard = boards[currentBoardIndex];
            // 根据当前棋盘的回合索引确定当前玩家
            Player currentPlayer = (boardTurns[currentBoardIndex] == 0) ? player1 : player2;
            currentBoard.display(player1, player2, currentPlayer, currentBoardIndex + 1);
            System.out.print("请玩家[" + currentPlayer.getName() + "]输入落子位置或者棋盘编号：");
            String input = scanner.nextLine().trim();

            if (input.length() == 1) {
                // 单个字符输入：视为棋盘编号
                char ch = input.charAt(0);
                if (Character.isDigit(ch)) {
                    int boardNumber = Character.getNumericValue(ch);
                    if (boardNumber < 1 || boardNumber > 3) {
                        System.out.println("棋盘编号必须在1到3之间！");
                        scanner.nextLine();
                    } else {
                        currentBoardIndex = boardNumber - 1;
                        System.out.println("成功切换到棋盘" + boardNumber + "，请继续输入落子位置。");
                        scanner.nextLine();
                    }
                } else {
                    System.out.println("输入格式错误，请输入棋盘编号（1~3）或落子位置（例如：1a）！");
                    scanner.nextLine();
                }
                // 切换棋盘不消耗回合，直接继续循环
                continue;
            } else if (input.length() == 2) {
                // 输入长度为2时，视为落子位置，例如 "1a"
                boolean validMove = false;
                try {
                    int row = Character.getNumericValue(input.charAt(0)) - 1;
                    char colChar = Character.toLowerCase(input.charAt(1));
                    int col = colChar - 'a';
                    validMove = boards[currentBoardIndex].placePiece(row, col, currentPlayer.getPieceType());
                    if (!validMove) {
                        System.out.println("落子位置有误或该位置已被占用，请重新输入！");
                        scanner.nextLine();
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("输入格式有误，请使用如 '1a' 的格式！");
                    scanner.nextLine();
                    continue;
                }
                // 修改：仅更新当前棋盘的回合，不影响其他棋盘
                boardTurns[currentBoardIndex] = (boardTurns[currentBoardIndex] + 1) % 2;
            } else {
                System.out.println("输入格式错误，请输入棋盘编号（1~3）或落子位置（例如：1a）！");
                scanner.nextLine();
                continue;
            }
        }
        // 游戏结束时显示当前棋盘状态
        Player lastPlayer = (boardTurns[currentBoardIndex] == 0) ? player1 : player2;
        boards[currentBoardIndex].display(player1, player2, lastPlayer, currentBoardIndex + 1);
        System.out.println("游戏结束，所有棋盘均已满！");
        scanner.close();
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
}