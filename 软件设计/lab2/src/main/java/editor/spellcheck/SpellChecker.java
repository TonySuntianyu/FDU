package editor.spellcheck;

import java.util.List;

/**
 * 拼写检查器接口
 * 使用适配器模式，隔离第三方库依赖
 */
public interface SpellChecker {
    /**
     * 检查文本中的拼写错误
     * @param text 要检查的文本
     * @return 拼写错误列表
     */
    List<SpellError> check(String text);
}

