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
            case "insert-before":
                parseInsertBeforeCommand(cmd, args);
                break;
            case "append-child":
                parseAppendChildCommand(cmd, args);
                break;
            case "edit-id":
                parseEditIdCommand(cmd, args);
                break;
            case "edit-text":
                parseEditTextCommand(cmd, args);
                break;
            case "delete-element":
                cmd.addArg(args.trim());
                break;
            case "xml-tree":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
                break;
            case "spell-check":
                if (!args.isEmpty()) {
                    cmd.addArg(parseFilePath(args));
                }
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
     * 支持两种格式（兼容 Lab1 & Lab2）：
     * Lab1: init <file> [with-log]
     * Lab2: init <text|xml> <file> [with-log]
     */
    private static void parseInitCommand(ParsedCommand cmd, String args) {
        args = args.trim();
        if (args.isEmpty()) {
            throw new IllegalArgumentException("init命令参数不足");
        }

        boolean withLog = false;
        if (args.endsWith("with-log")) {
            withLog = true;
            args = args.substring(0, args.length() - "with-log".length()).trim();
        }

        // 先拆分出第一个单词，判断是否为 text/xml（Lab2 新格式）
        String[] parts = args.split("\\s+", 2);
        String first = parts[0];

        if ("text".equalsIgnoreCase(first) || "xml".equalsIgnoreCase(first)) {
            // Lab2 新格式: init <text|xml> <file> [with-log]
            if (parts.length < 2) {
                throw new IllegalArgumentException("init命令参数不足");
            }
            cmd.addArg(first.toLowerCase());                 // fileType
            cmd.addArg(parseFilePath(parts[1]));             // filePath
            if (withLog) {
                cmd.addArg("with-log");
            }
        } else {
            // Lab1 旧格式: init <file> [with-log]，默认为 text 类型
            cmd.addArg(parseFilePath(args));                 // filePath
            if (withLog) {
                cmd.addArg("with-log");
            }
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
     * 解析insert-before命令
     * 格式: insert-before <tagName> <newId> <targetId> ["text"]
     */
    private static void parseInsertBeforeCommand(ParsedCommand cmd, String args) {
        String[] parts = args.split("\\s+", 4);
        if (parts.length < 3) {
            throw new IllegalArgumentException("insert-before命令参数不足");
        }
        cmd.addArg(parts[0]); // tagName
        cmd.addArg(parts[1]); // newId
        cmd.addArg(parts[2]); // targetId
        if (parts.length > 3) {
            cmd.addArg(parseQuotedText(parts[3])); // text (可选)
        }
    }

    /**
     * 解析append-child命令
     * 格式: append-child <tagName> <newId> <parentId> ["text"]
     */
    private static void parseAppendChildCommand(ParsedCommand cmd, String args) {
        String[] parts = args.split("\\s+", 4);
        if (parts.length < 3) {
            throw new IllegalArgumentException("append-child命令参数不足");
        }
        cmd.addArg(parts[0]); // tagName
        cmd.addArg(parts[1]); // newId
        cmd.addArg(parts[2]); // parentId
        if (parts.length > 3) {
            cmd.addArg(parseQuotedText(parts[3])); // text (可选)
        }
    }

    /**
     * 解析edit-id命令
     * 格式: edit-id <oldId> <newId>
     */
    private static void parseEditIdCommand(ParsedCommand cmd, String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("edit-id命令参数不足");
        }
        cmd.addArg(parts[0]); // oldId
        cmd.addArg(parts[1]); // newId
    }

    /**
     * 解析edit-text命令
     * 格式: edit-text <elementId> "text"
     */
    private static void parseEditTextCommand(ParsedCommand cmd, String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("edit-text命令参数不足");
        }
        cmd.addArg(parts[0]); // elementId
        cmd.addArg(parseQuotedText(parts[1])); // text
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

