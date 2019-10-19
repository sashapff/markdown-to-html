package md2html;

import java.util.ArrayList;
import java.util.List;

class MarkdownParser {
    MarkdownParser(MarkdownSource source) {
        this.source = source;
    }

    private final MarkdownSource source;
    private StringBuilder notParsed;
    private StringBuilder parsed;

    List<StringBuilder> parse() throws MarkdownException {
        char last = '\1';
        char prelast;
        List<StringBuilder> stringBuilders = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        while (true) {
            prelast = last;
            last = source.getChar();
            char next = source.nextChar();
            if (next != '\n' && last == '\n' && prelast == '\n') {
                stringBuilders.add(current);
                current = new StringBuilder();
            }
            if (!(next == '\n' && (last == '\n' || last == '\1')) && next != MarkdownSource.END) {
                current.append(next);
            }
            if (next == MarkdownSource.END) {
                if (!current.toString().equals("")) {
                    stringBuilders.add(current);
                }
                break;
            }
        }
        List<StringBuilder> parsedList = new ArrayList<>();
        for (StringBuilder i : stringBuilders) {
            notParsed = i;
            parsed = new StringBuilder();
            if (i.length() != 0) {
                parseHeadLine(0, i.length());
                parsedList.add(parsed);
            }
        }
        return parsedList;
    }

    private void parseHeadLine(int l, int r) {
        int position = l;
        while (position < r && notParsed.charAt(position) == ' ' && position < 3) {
            position++;
        }
        int cnt = 0;
        while (position < r && notParsed.charAt(position) == lattice) {
            cnt++;
            position++;
        }
        if (position < r && notParsed.charAt(position) == ' ' && cnt >= 1 && cnt <= 6) {
            parsed
                    .append("<h")
                    .append(cnt)
                    .append(">");
            parseParagraph(position + 1, r - 1, false);
            parsed
                    .append("</h")
                    .append(cnt)
                    .append(">\n");
        } else {
            parsed.append("<p>");
            parseParagraph(l, r - 1, false);
            parsed.append("</p>\n");
        }
    }

    private void parseParagraph(int l, int r, boolean flag) {
        int i = l;
        while (i < r) {
            char c = notParsed.charAt(i);
            if (notParsed.charAt(i) == less) {
                parsed.append("&lt;");
                i++;
            } else if (notParsed.charAt(i) == lager) {
                parsed.append("&gt;");
                i++;
            } else if (notParsed.charAt(i) == ampersand) {
                parsed.append("&amp;");
                i++;
            } else if (c == '\\' && i + 1 < r && (notParsed.charAt(i + 1) == star
                    || notParsed.charAt(i + 1) == underscore)) {
                i++;
            } else if (c == singleQuote && !flag) {
                i = parseSingleQuote(i, r);
            } else if (c == star && !flag) {
                i = parseStarAndUnderscore(i, r, star);
            } else if (c == underscore && !flag) {
                i = parseStarAndUnderscore(i, r, underscore);
            } else if (c == minus && (i + 1 < r && notParsed.charAt(i + 1) == minus) && !flag) {
                i = parseMinus(i, r);
            } else if (c == exclamationMark && !flag) {
                i = parseExclamationMark(i, r);
            } else {
                parsed.append(c);
                i++;
            }
        }
    }

    int parseSingleQuote(int l, int r) {
        int j = l + 1;
        while (j < r && notParsed.charAt(j) != singleQuote) {
            j++;
        }
        if (notParsed.charAt(j) != singleQuote) {
            parsed.append(source.getChar());
            return l + 1;
        } else if (notParsed.charAt(l + 1) == less) {
            parsed.append("<code>&lt;</code>");
            return j + 1;
        } else if (notParsed.charAt(l + 1) == lager) {
            parsed.append("<code>&gt;</code>");
            return j + 1;
        } else if (notParsed.charAt(l + 1) == ampersand) {
            parsed.append("<code>&amp;</code>");
            return j + 1;
        } else {
            parsed.append("<code>");
            parseParagraph(l + 1, j, false);
            parsed.append("</code>");
            return j + 1;
        }
    }

    int parseStarAndUnderscore(int l, int r, char starOrUnderscore) {
        if (l + 1 < r && notParsed.charAt(l + 1) == starOrUnderscore) {
            int j = l + 2;
            while (j + 1 < r && !(notParsed.charAt(j) == starOrUnderscore && notParsed.charAt(j + 1) == starOrUnderscore && notParsed.charAt(j - 1) != '\\')) {
                j++;
            }
            if (j + 1 >= r || (j + 1 < r && notParsed.charAt(j) != starOrUnderscore || notParsed.charAt(j + 1) != starOrUnderscore)) {
                parsed.append(source.getChar());
                return l + 1;
            } else {
                parsed.append("<strong>");
                parseParagraph(l + 2, j, false);
                parsed.append("</strong>");
                return j + 2;
            }
        } else {
            int j = l + 1;
            while (j < r && !(notParsed.charAt(j) == starOrUnderscore && notParsed.charAt(j - 1) != '\\')) {
                j++;
            }
            if (j >= r || (j < r && notParsed.charAt(j) != starOrUnderscore)) {
                parsed.append(starOrUnderscore);
                return l + 1;
            } else {
                parsed.append("<em>");
                parseParagraph(l + 1, j, false);
                parsed.append("</em>");
                return j + 1;
            }
        }
    }

    int parseMinus(int l, int r) {
        int j = l + 2;
        while (j + 1 < r && !(notParsed.charAt(j) == minus && notParsed.charAt(j + 1) == minus)) {
            j++;
        }
        if (j + 1 >= r || (j + 1 < r && notParsed.charAt(j) != minus || notParsed.charAt(j + 1) != minus)) {
            parsed.append(source.getChar());
            return l + 1;
        } else {
            parsed.append("<s>");
            parseParagraph(l + 2, j, false);
            parsed.append("</s>");
            return j + 2;
        }
    }

    int parseExclamationMark(int l, int r) {
        int j = l + 1;
        int begText = 0, endText = 0, begLink = 0, endLink = 0;
        while (j < r && notParsed.charAt(j) != '[') {
            j++;
        }
        if (j >= r || (j < r && notParsed.charAt(j) != '[')) {
            parsed.append(source.getChar());
            return l + 1;
        } else {
            begText = j;
            while (j < r && notParsed.charAt(j) != ']') {
                j++;
            }
            endText = j;
            while (j < r && notParsed.charAt(j) != '(') {
                j++;
            }
            begLink = j;
            while (j < r && notParsed.charAt(j) != ')') {
                j++;
            }
            endLink = j;
        }
        parsed
                .append("<img alt='");
        parseParagraph(begText + 1, endText, true);
        parsed
                .append("' src='")
                .append(notParsed.substring(begLink + 1, endLink))
                .append("'>");
        return j + 1;
    }

    private static final char star = '*';
    private static final char singleQuote = '`';
    private static final char underscore = '_';
    private static final char minus = '-';
    private static final char exclamationMark = '!';
    private static final char less = '<';
    private static final char lager = '>';
    private static final char ampersand = '&';
    private static final char lattice = '#';

}