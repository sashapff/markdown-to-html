package md2html;

import java.io.IOException;
import java.util.List;

public abstract class HtmlReceiver {
    protected abstract void write(List<StringBuilder> text) throws IOException;
}
