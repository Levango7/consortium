package org.wisdom.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.util.ArrayList;
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
    private Header header;

    @Getter
    @Setter
    private List<Transaction> body;

    public Block(){
        header = new Header();
        body = new ArrayList<>();
    }

    public Block(@NonNull Header header){
        this.header = header;
        body = new ArrayList<>();
    }

    public Block clone() {
        Block b = new Block(header.clone());
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
