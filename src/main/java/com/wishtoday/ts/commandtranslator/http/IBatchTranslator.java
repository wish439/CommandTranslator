package com.wishtoday.ts.commandtranslator.http;

import java.util.List;

public interface IBatchTranslator {
    List<String> translate(List<String> strings);
}
