package org.atpfivt;

final class StripMarkedSections {
    private StripMarkedSections() {

    }
    static String stripMarkedSections(String src) {
        StringBuilder out = new StringBuilder(src.length());
        int cursor = 0;

        while (cursor < src.length()) {
            int open = src.indexOf("//[[", cursor);
            if (open == -1) {                 // no more markers
                out.append(src, cursor, src.length());
                break;
            }
            out.append(src, cursor, open);    // keep text before "[["
            int close = src.indexOf("//]]", open + 4);
            if (close == -1) {                // unmatched start → keep the rest
                out.append(src, open, src.length());
                break;
            }
            cursor = close + 4;               // jump past "]]" and continue
        }
        return out.toString();
    }
}
