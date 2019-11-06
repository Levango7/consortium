package org.wisdom.common;

public interface BlockValidator {
    ValidateResult validate(Block block, Block dependency);
}
