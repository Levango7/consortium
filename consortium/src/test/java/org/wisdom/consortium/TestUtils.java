package org.wisdom.consortium;

import org.wisdom.common.Block;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.util.BigEndian;

import java.util.Arrays;

public class TestUtils {
    static final byte[] BYTES = new byte[32];

    static Block getBlock(long height) {
        org.wisdom.consortium.entity.Block b = new org.wisdom.consortium.entity.Block(
                BigEndian.encodeInt64(height), 1, height == 0 ? BYTES : BigEndian.encodeInt64(height - 1),
                BYTES, height, System.currentTimeMillis() / 1000, BYTES
        );
        org.wisdom.consortium.entity.Transaction.TransactionBuilder builder = org.wisdom.consortium.entity
                .Transaction.builder().blockHash(BigEndian.encodeInt64(height))
                .height(height)
                .from(BYTES).payload(BYTES).to(BYTES)
                .signature(BYTES);
        b.setBody(Arrays.asList(
                builder.position(0).hash((height + "" + 0).getBytes()).build(),
                builder.position(1).hash((height + "" + 1).getBytes()).build(),
                builder.position(2).hash((height + "" + 2).getBytes()).build()
        ));
        return Mapping.getFromBlockEntity(b);
    }
}
