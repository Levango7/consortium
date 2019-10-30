package org.wisdom.common;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Header implements Cloneable<Header>{
    private long version;

    private HexBytes hashPrev;

    private HexBytes merkleRoot;

    private long height;

    private long createdAt;

    private HexBytes payload;

    @Override
    public Header clone() {
        return builder().version(version)
                .hashPrev(hashPrev).merkleRoot(merkleRoot)
                .height(height).createdAt(createdAt)
                .payload(payload).build();
    }
}
