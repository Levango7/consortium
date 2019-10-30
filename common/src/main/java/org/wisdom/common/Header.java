package org.wisdom.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Header implements Cloneable<Header>, Chained {
    private int version;

    private HexBytes hashPrev;

    private HexBytes merkleRoot;

    private long height;

    private long createdAt;

    private HexBytes payload;

    private HexBytes hash;

    @Override
    public Header clone() {
        return builder().version(version)
                .hashPrev(hashPrev).merkleRoot(merkleRoot)
                .height(height).createdAt(createdAt)
                .payload(payload).build();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int size() {
        return Constants.INTEGER_SIZE + Constants.LONG_SIZE * 2 +
                Stream.of(hashPrev, merkleRoot, payload, hash)
                .map(bytes -> bytes == null ? 0 : bytes.size())
                .reduce(0, Integer::sum);
    }
}
