package com.google.gson.stream;

import java.util.Stack;

public enum JsonParserState {
    START,
    IN_OBJECT,
    IN_ARRAY,
    AFTER_NAME,
    AFTER_VALUE;
}

