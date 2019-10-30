package org.wisdom.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Block extends Header {
    @Getter
    @Setter
    private List<String> body;
}
