
public class ReversiGame {
    public static void main(String[] args) {
        // 使用固定的玩家名称
        String player1Name = "张三";
        String player2Name = "李四";
        
        System.out.println("欢迎来到棋盘游戏！");
        System.out.println("玩家1（黑棋）: " + player1Name);
        System.out.println("玩家2（白棋）: " + player2Name);
        
        Game game = new Game(player1Name, player2Name);
        game.start();
    }
    
    // 清屏方法
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // 如果清屏失败，打印多行空行代替
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}
