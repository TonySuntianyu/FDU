package editor.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令解析器
 * 解析用户输入的命令字符串
 */
public class CommandParser {
    private static final Pattern QUOTED_TEXT = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern LINE_COL = Pattern.compile("(\\d+):(\\d+)");

    /**
     * 解析命令
     * @param input 用户输入
     * @return 解析后的命令对象
     */
    public static ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        input = input.trim();
        String[] parts = input.split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        ParsedCommand cmd = new ParsedCommand(commandName);

        switch (commandName) {
            case "load":
                cmd.addArg(parseFilePath(args));
                break;
            case "save":
                if (args.equals("all")) {
                    cmd.addArg("all");
                } else if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "init":
                parseInitCommand(cmd, args);
                break;
            case "close":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "edit":
                cmd.addArg(parseFilePath(args));
                break;
            case "append":
                cmd.addArg(parseQuotedText(args));
                break;
            case "insert":
                parseInsertCommand(cmd, args);
                break;
            case "delete":
                parseDeleteCommand(cmd, args);
                break;
            case "replace":
                parseReplaceCommand(cmd, args);
                break;
            case "show":
                if (!args.isEmpty()) {
                    parseShowCommand(cmd, args);
                }
                break;
            case "log-on":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "log-off":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "log-show":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "dir-tree":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "editor-list":
            case "undo":
            case "redo":
            case "exit":
                // 无参数命令
                break;
            default:
                return null;
        }

        return cmd;
    }

    /**
     * 解析文件路径
     */
    private static String parseFilePath(String args) {
        args = args.trim();
        // 如果被引号包裹，去掉引号
        if (args.startsWith("\"") && args.endsWith("\"")) {
            return args.substring(1, args.length() - 1);
        }
        return args;
    }

    /**
     * 解析带引号的文本
     * 处理转义序列：\n (换行), \t (制表符), \\ (反斜杠), \" (引号)
     */
    private static String parseQuotedText(String args) {
        // 手动解析引号内的文本，支持转义引号
        args = args.trim();
        if (!args.startsWith("\"")) {
            return args;
        }
        
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        boolean escaped = false;
        
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            
            if (escaped) {
                // 处理转义字符
                switch (c) {
                    case 'n':
                        result.append('\n');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case '\\':
                        result.append('\\');
                        break;
                    case '"':
                        result.append('"');
                        break;
                    default:
                        result.append('\\').append(c);
                        break;
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                if (inQuotes) {
                    // 结束引号
                    break;
                } else {
                    // 开始引号
                    inQuotes = true;
                }
            } else if (inQuotes) {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    /**
     * 处理转义序列
     * 将 \n 转换为换行符，\t 转换为制表符，\\ 转换为反斜杠
     */
    private static String unescapeString(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i++; // 跳过下一个字符
                        break;
                    case 't':
                        result.append('\t');
                        i++; // 跳过下一个字符
                        break;
                    case '\\':
                        result.append('\\');
                        i++; // 跳过下一个字符
                        break;
                    case '"':
                        result.append('"');
                        i++; // 跳过下一个字符
                        break;
                    default:
                        result.append(c);
                        break;
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 解析行号:列号
     */
    private static int[] parseLineCol(String args) {
        Matcher matcher = LINE_COL.matcher(args);
        if (matcher.find()) {
            int line = Integer.parseInt(matcher.group(1));
            int col = Integer.parseInt(matcher.group(2));
            return new int[]{line, col};
        }
        throw new IllegalArgumentException("无效的行号:列号格式");
    }

    /**
     * 解析init命令
     */
    private static void parseInitCommand(ParsedCommand cmd, String args) {
        args = args.trim();
        if (args.endsWith("with-log")) {
            String filePath = args.substring(0, args.length() - "with-log".length()).trim();
            cmd.addArg(parseFilePath(filePath));
            cmd.addArg("with-log");
        } else {
            cmd.addArg(parseFilePath(args));
        }
    }

    /**
     * 解析insert命令
     */
    private static void parseInsertCommand(ParsedCommand cmd, String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("insert命令参数不足");
        }
        int[] lineCol = parseLineCol(parts[0]);
        cmd.addArg(String.valueOf(lineCol[0]));
        cmd.addArg(String.valueOf(lineCol[1]));
        cmd.addArg(parseQuotedText(parts[1]));
    }

    /**
     * 解析delete命令
     */
    private static void parseDeleteCommand(ParsedCommand cmd, String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("delete命令参数不足");
        }
        int[] lineCol = parseLineCol(parts[0]);
        cmd.addArg(String.valueOf(lineCol[0]));
        cmd.addArg(String.valueOf(lineCol[1]));
        cmd.addArg(parts[1].trim());
    }

    /**
     * 解析replace命令
     */
    private static void parseReplaceCommand(ParsedCommand cmd, String args) {
        // 先提取引号内的文本
        Matcher textMatcher = QUOTED_TEXT.matcher(args);
        String text = "";
        if (textMatcher.find()) {
            text = textMatcher.group(1);
            args = args.substring(0, textMatcher.start()).trim();
        }

        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("replace命令参数不足");
        }
        int[] lineCol = parseLineCol(parts[0]);
        cmd.addArg(String.valueOf(lineCol[0]));
        cmd.addArg(String.valueOf(lineCol[1]));
        cmd.addArg(parts[1].trim());
        cmd.addArg(text);
    }

    /**
     * 解析show命令
     */
    private static void parseShowCommand(ParsedCommand cmd, String args) {
        Matcher matcher = LINE_COL.matcher(args);
        if (matcher.find()) {
            int startLine = Integer.parseInt(matcher.group(1));
            int endLine = Integer.parseInt(matcher.group(2));
            cmd.addArg(String.valueOf(startLine));
            cmd.addArg(String.valueOf(endLine));
        }
    }

    /**
     * 解析后的命令对象
     */
    public static class ParsedCommand {
        private final String commandName;
        private final List<String> args;

        public ParsedCommand(String commandName) {
            this.commandName = commandName;
            this.args = new ArrayList<>();
        }

        public void addArg(String arg) {
            if (arg != null) {
                args.add(arg);
            }
        }

        public String getCommandName() {
            return commandName;
        }

        public List<String> getArgs() {
            return args;
        }

        public String getArg(int index) {
            if (index >= 0 && index < args.size()) {
                return args.get(index);
            }
            return null;
        }

        public int getArgCount() {
            return args.size();
        }
    }
}

