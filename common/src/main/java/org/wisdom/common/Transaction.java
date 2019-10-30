package org.wisdom.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction implements Cloneable<Transaction> {
    private int version;

    private int type;

    private long createdAt;

    private long nonce;

    private HexBytes from;

    private long gasPrice;

    private long amount;

    public HexBytes payload;

    private HexBytes to;

    private HexBytes signature;

    private HexBytes hash;

    @Override
    public Transaction clone() {
        return builder().version(version)
                .type(type).nonce(nonce)
                .createdAt(createdAt).from(from)
                .gasPrice(gasPrice).amount(amount)
                .payload(payload).to(to)
                .signature(signature).build();
    }


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int size() {
        return Constants.INTEGER_SIZE * 2 + Constants.LONG_SIZE * 3 +
                Stream.of(from, payload, to, signature)
                        .map(bytes -> bytes == null ? 0 : bytes.size())
                        .reduce(0, Integer::sum);
    }
}
