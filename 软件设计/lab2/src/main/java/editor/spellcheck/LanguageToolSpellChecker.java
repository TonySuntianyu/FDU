package editor.spellcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * LanguageTool 拼写检查器适配器
 * 使用简单的基于规则的拼写检查（模拟）
 * 
 * 注意：实际项目中应该使用真实的 LanguageTool API
 * 这里为了简化实现，使用一个简单的拼写检查器
 */
public class LanguageToolSpellChecker implements SpellChecker {
    // 简单的常见拼写错误字典
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-zA-Z]+\\b");
    
    // 常见拼写错误映射
    private static final java.util.Map<String, String> COMMON_ERRORS = new java.util.HashMap<>();
    
    static {
        COMMON_ERRORS.put("recieve", "receive");
        COMMON_ERRORS.put("occured", "occurred");
        COMMON_ERRORS.put("seperate", "separate");
        COMMON_ERRORS.put("definately", "definitely");
        COMMON_ERRORS.put("accomodate", "accommodate");
        COMMON_ERRORS.put("Itallian", "Italian");
        COMMON_ERRORS.put("Rowlling", "Rowling");
    }

    @Override
    public List<SpellError> check(String text) {
        List<SpellError> errors = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return errors;
        }

        // 提取所有单词
        java.util.regex.Matcher matcher = WORD_PATTERN.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            String lowerWord = word.toLowerCase();
            
            // 检查是否是常见拼写错误
            if (COMMON_ERRORS.containsKey(lowerWord)) {
                String suggestion = COMMON_ERRORS.get(lowerWord);
                // 保持原单词的大小写
                if (Character.isUpperCase(word.charAt(0))) {
                    suggestion = Character.toUpperCase(suggestion.charAt(0)) + suggestion.substring(1);
                }
                errors.add(new SpellError(word, suggestion, matcher.start()));
            }
        }

        return errors;
    }
}

