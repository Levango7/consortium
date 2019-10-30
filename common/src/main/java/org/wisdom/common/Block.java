package org.wisdom.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.stream.Collectors;

public class Block implements Cloneable<Block>, Chained{
    private static abstract class ExcludedMethods{
        public abstract Block clone();
        public abstract int size();
    }

    // extend from header
    @JsonIgnore
    @Delegate(excludes = ExcludedMethods.class)
    @Setter
    private Header header;

    @Getter
    @Setter
    private List<Transaction> body;

    public Block(){
        header = new Header();
    }

    public Block clone() {
        Block b = new Block();
        b.header = header.clone();
        b.setBody(body.stream().map(Transaction::clone).collect(Collectors.toList()));
        return b;
    }

    // serialization only
    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "size")
    public int size(){
        return header.size() + bodySize();
    }

    private int bodySize(){
        return body == null ? 0 : body.stream()
                .map(Transaction::size)
                .reduce(0, Integer::sum);
    }
}
