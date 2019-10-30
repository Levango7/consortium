package org.wisdom.consortium.dao;

import org.wisdom.common.HexBytes;
import org.wisdom.consortium.entity.AbstractHeader;
import org.wisdom.consortium.entity.Block;
import org.wisdom.consortium.entity.Header;
import org.wisdom.consortium.entity.Transaction;

public class Mapping {
    static org.wisdom.common.Block getFromBlockEntity(Block block){
        org.wisdom.common.Header header = getFromHeaderEntity(block);

    }

    static org.wisdom.common.Header getFromHeaderEntity(AbstractHeader header){
        return org.wisdom.common.Header.builder()
                .hash(new HexBytes(header.getHash()))
                .version(header.getVersion())
                .hashPrev(new HexBytes(header.getHashPrev()))
                .merkleRoot(new HexBytes(header.getMerkleRoot()))
                .height(header.getHeight())
                .createdAt(header.getCreatedAt())
                .payload(new HexBytes(header.getPayload()))
                .build();
    }

    static org.wisdom.common.Transaction getFromTransactionEntity(Transaction transaction){
        return null;
    }
}
