package com.wishtoday.ts.commandtranslator.Reader;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class SelectorCommandParser implements CommandParser<SelectorCommandParser.ParsedResult> {
    private final String string;
    private int cursor;
    private static final Set<Character> selectors = Set.of('a', 's', 'r', 'p', 'e');

    public SelectorCommandParser(String string) {
        this.string = string;
        this.cursor = 0;
    }

    public static CommandParser<SelectorCommandParser.ParsedResult> of(String string) {
        return new SelectorCommandParser(string);
    }

    @Override
    public String getCommand() {
        return string;
    }

    public ParsedResult parse() {
        List<ReadElement> readElements = new ArrayList<>();
        while (cursor < string.length()) {
            readElements.add(readNext());
        }
        return new ParsedResult(readElements);
    }

    private ReadElement readNext() {
        if (isSelectorStart()) {
            return readSelector();
        } else {
            return readText();
        }
    }

    //@a[...]...
    private ReadElement readSelector() {
        int start = cursor;

        cursor += 2;

        if (cursor >= string.length() || string.charAt(cursor) != '[') {
            return new ReadElement(string.substring(start, cursor), true);
        }

        int depth = 0;
        while (cursor < string.length()) {
            char c = string.charAt(cursor);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    cursor++;
                    break;
                }
            }
            cursor++;
        }

        return new ReadElement(string.substring(start, cursor), true);
    }


    private ReadElement readText() {
        int start = cursor;
        while (cursor < string.length() && !isSelectorStart()) {
            cursor++;
        }
        int end = cursor;
        return new ReadElement(this.string.substring(start, end), false);
    }

    private boolean isSelectorStart() {
        if (cursor + 1 >= string.length()) return false;
        if (string.charAt(cursor) != '@') return false;
        return selectors.contains(string.charAt(cursor + 1));
    }

    public static class ParsedResult {
        private final StringBuilder sb;
        @Getter
        private final List<ReadElement> readElements;
        private String string;
        public ParsedResult(List<ReadElement> readElements) {
            this.sb = new StringBuilder();
            this.readElements = readElements;
        }
        public void forEach(Consumer<ReadElement> consumer) {
            this.readElements.forEach(consumer);
        }
        public Stream<ReadElement> stream() {
            return readElements.stream();
        }

        public ParsedResult changeAllText(Function<String, String> consumer) {
            List<ReadElement> newList = new ArrayList<>();
            for (ReadElement element : readElements) {
                if (element.isSelector()) {
                    newList.add(element);
                    continue;
                }
                String apply = consumer.apply(element.content);
                ReadElement readElement = new ReadElement(apply, false);
                newList.add(readElement);
            }
            return new ParsedResult(newList);
        }
        @Override
        public @NotNull String toString() {
            if (string != null && !string.isEmpty()) return string;
            readElements.stream().map(ReadElement::content).forEach(sb::append);
            String s = sb.toString();
            this.string = s;
            return s;
        }
    }

    public record ReadElement(String content, boolean isSelector) {
        public static final ReadElement EMPTY = new ReadElement("", false);
    }
}
