package editor.spellcheck;

/**
 * 拼写错误信息
 */
public class SpellError {
    protected final String word;
    protected final String suggestion;
    protected final int position; // 在文本中的位置

    public SpellError(String word, String suggestion, int position) {
        this.word = word;
        this.suggestion = suggestion;
        this.position = position;
    }

    public String getWord() {
        return word;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getPosition() {
        return position;
    }
}

