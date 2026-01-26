package editor;

import editor.command.CommandExecutor;
import editor.command.CommandParser;
import editor.workspace.Workspace;

import java.util.Scanner;

/**
 * 文本编辑器主程序
 */
public class Main {
    public static void main(String[] args) {
        Workspace workspace = new Workspace();
        CommandExecutor executor = new CommandExecutor(workspace);
        Scanner scanner = new Scanner(System.in);

        System.out.println("欢迎使用文本编辑器！");
        System.out.println("输入 'exit' 退出程序");
        System.out.println();

        // 加载工作区状态（如果需要）
        // workspace.loadState();

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) {
                continue;
            }

            try {
                CommandParser.ParsedCommand cmd = CommandParser.parse(input);
                boolean continueRunning = executor.execute(cmd);
                if (!continueRunning) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("错误: " + e.getMessage());
            }
        }

        scanner.close();
    }
}

