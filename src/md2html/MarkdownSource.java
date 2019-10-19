package md2html;

import java.io.IOException;

public abstract class MarkdownSource {
    public static char BEGIN = '\1';
    public static char END = '\0';
    protected int pos;
    protected int line = 1;
    protected int posInLine;
    private char c = BEGIN;
    public MarkdownSource() {
        this.pos = 0;
        this.line = 0;
        this.posInLine = 0;
    }

    protected abstract char readChar() throws IOException;

    public char nextChar() throws MarkdownException {
        try {
            if (c == '\n') {
                line++;
                posInLine = 0;
            }
            c = readChar();
            pos++;
            posInLine++;
            return c;
        } catch (final IOException e) {
            throw error("Source read error", e.getMessage());
        }
    }

    public char getChar() {
        return c;
    }

    public MarkdownException error(final String format, final Object... args) {
        return new MarkdownException(line, posInLine, String.format("%d:%d: %s", line, posInLine, String.format(format, args)));
    }
}
