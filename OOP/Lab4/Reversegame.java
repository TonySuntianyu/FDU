import java.io.IOException;
import java.util.Scanner;

public class Reversegame {
    public static void main(String[] args) {
        // 初始化两个玩家，棋子符号分别为黑色和白色
        Player player1 = new Player("张三", Piece.BLACK);
        Player player2 = new Player("李四", Piece.WHITE);
        
        // 创建 Game 类的实例，实现独立回合管理
        Game game = new Game(player1, player2);
        
        // 开始游戏
        game.start();
    }
    
}