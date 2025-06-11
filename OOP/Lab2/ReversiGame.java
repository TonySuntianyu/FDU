import java.util.Scanner;

public class ReversiGame {

    public static class Board {
        public enum Piece { EMPTY, BLACK, WHITE }
        private Piece[][] board = new Piece[8][8];

        public Board() {
            for (int i = 0; i < 8; i++) 
                for (int j = 0; j < 8; j++) 
                    board[i][j] = Piece.EMPTY;
        }

        public void printBoard(Player currentPlayer, Player player1, Player player2) {
            System.out.println("  A B C D E F G H");
            for (int i = 0; i < 8; i++) {
                System.out.print((i+1) + " ");
                for (int j = 0; j < 8; j++) {
                    String symbol = switch(board[i][j]) {
                        case BLACK -> "●";
                        case WHITE -> "○";
                        case EMPTY -> "·";
                    };
                    System.out.print(symbol + " ");
                }
                
                System.out.print("   ");
                if (i == 3) {
                    String mark = (currentPlayer == player1) ? "●" : " ";
                    System.out.printf("玩家[%s] %s", player1.getName(), mark);
                } else if (i == 4) {
                    String mark = (currentPlayer == player2) ? "○" : " ";
                    System.out.printf("玩家[%s] %s", player2.getName(), mark);
                }
                System.out.println();
            }
            System.out.println("\n");
        }

        public boolean placePiece(int row, int col, Piece piece) {
            if (row < 0 || row >=8 || col <0 || col >=8) return false;
            if (board[row][col] != Piece.EMPTY) return false;
            board[row][col] = piece;
            return true;
        }
        
        public boolean isFull() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == Piece.EMPTY) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static class Player {
        private final String name;
        private final Board.Piece piece;
        public Player(String name, Board.Piece piece) {
            this.name = name;
            this.piece = piece;
        }
        public String getName() { return name; }
        public Board.Piece getPiece() { return piece; }
    }

    public static class Game {
        private final Board board = new Board();
        private final Player player1;
        private final Player player2;
        private Player currentPlayer;

        public Game(String p1, String p2) {
            player1 = new Player(p1, Board.Piece.BLACK);
            player2 = new Player(p2, Board.Piece.WHITE);
            currentPlayer = player1;
        }

        public void start() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                clearScreen();
                board.printBoard(currentPlayer, player1, player2);
                
                if (board.isFull()) {
                    System.out.println("棋盘已满！");
                    System.out.println("游戏结束！");
                    break;
                }
                
                while (true) {
                    
                    System.out.print("请玩家[" + currentPlayer.getName() + "]输入落子位置（例如1a）：");
                    String input = scanner.nextLine().trim();
        
                    if (input.equalsIgnoreCase("exit")) {
                        scanner.close();
                        return;
                    }
        
                    if (!input.matches("[1-8][a-hA-H]")) {
                        System.out.println("输入格式错误，请输入1a到8h之间的坐标！");
                        continue;
                    }
        
                    int row = Integer.parseInt(input.substring(0, 1)) - 1;
                    int col = input.toLowerCase().charAt(1) - 'a';
        
                    if (!board.placePiece(row, col, currentPlayer.getPiece())) {
                        System.out.println("该位置已被占用！");
                        continue;
                    }
        
                    currentPlayer = (currentPlayer == player1) ? player2 : player1;
                    break;
                }
            }
            scanner.close();
        }
    }

    public static void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }

    public static void main(String[] args) {
        new Game("张三", "李四").start();
    }
}