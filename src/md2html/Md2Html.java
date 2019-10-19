package md2html;

public class Md2Html {
    public static void main(String[] args) throws MarkdownException, Exception {
        new FileHtmlReceiver(args[1]).write(new MarkdownParser(new FileMarkdownSource(args[0])).parse());
    }
}
