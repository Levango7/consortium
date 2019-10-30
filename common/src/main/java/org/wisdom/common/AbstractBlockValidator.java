package org.wisdom.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractBlockValidator {
    private static final ValidateResult SUCCESS = new ValidateResult(true, "");

    protected static ValidateResult success(){return SUCCESS;}

    protected static ValidateResult fault(String reason){return new ValidateResult(false, reason);}

    @Getter
    @AllArgsConstructor
    private static class ValidateResult{
        private boolean success;
        @NonNull
        private String reason;
    }

    public abstract ValidateResult validateBlock(Block block, Block dependency);
}
