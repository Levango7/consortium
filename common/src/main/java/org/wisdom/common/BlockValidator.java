package org.wisdom.common;

public interface BlockValidator {
    ValidateResult validateBlock(Block block, Block dependency);
}
