package md2html;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FileHtmlReceiver extends HtmlReceiver {
    private final PrintWriter out;

    public FileHtmlReceiver(final String fileName) throws Exception {
        try {
            out = new PrintWriter(fileName);
        } catch (IOException e) {
            throw new Exception(String.format("Error opening output file '%s': %s", fileName, e.getMessage()));
        }
    }


    @Override
    public void write(List<StringBuilder> text) {
        for (StringBuilder s : text) {
            out.print(s.toString());
        }
        out.close();
    }
}
