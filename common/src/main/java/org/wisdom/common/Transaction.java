package org.wisdom.common;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction implements Cloneable<Transaction>{
    private int version;

    private int type;

    private long nonce;

    private byte[] from;

    private long gasPrice;

    private long amount;

    public byte[] payload;

    private byte[] to;

    private byte[] signature;

    @Override
    public Transaction clone() {
        return builder().version(version)
                .type(type).nonce(nonce)
                .from(from).gasPrice(gasPrice)
                .amount(amount).payload(payload)
                .to(to).signature(signature).build();
    }
}
