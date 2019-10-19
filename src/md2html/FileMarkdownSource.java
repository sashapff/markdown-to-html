package md2html;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class FileMarkdownSource extends MarkdownSource {
    private final Reader reader;

    public FileMarkdownSource(final String fileName) throws MarkdownException {
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (final IOException e) {
            throw error("Error opening input file '%s': %s", fileName, e.getMessage());
        }
    }

    @Override
    protected char readChar() throws IOException {
        final int read = reader.read();
        return read == -1 ? END : (char) read;
    }

}