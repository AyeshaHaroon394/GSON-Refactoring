package com.google.gson.stream;

import java.util.Stack;

public class JsonStateMachine {
    private JsonParserState currentState;
    private final Stack<JsonParserState> stateStack;

    public JsonStateMachine(Stack<JsonParserState> stateStack) {
        this.stateStack = stateStack;
    }

    private void validateTransition(JsonParserState currentState, JsonParserState newState) {
        if (currentState == null && newState != JsonParserState.START) {
            throw new IllegalStateException("Parser must start in START state");
        }
        // Add validation logic for state transitions
    }

    public void transition(JsonParserState newState) {
        validateTransition(currentState, newState);
        currentState = newState;
    }

    public void pushState() {
        stateStack.push(currentState);
    }

    public JsonParserState popState() {
        return stateStack.pop();
    }
}
