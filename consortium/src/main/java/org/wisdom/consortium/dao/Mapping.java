package org.wisdom.consortium.dao;

import org.wisdom.common.HexBytes;
import org.wisdom.consortium.entity.AbstractHeader;
import org.wisdom.consortium.entity.Block;
import org.wisdom.consortium.entity.Transaction;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Mapping {
    public static org.wisdom.common.Block getFromBlockEntity(Block block){
        org.wisdom.common.Header header = getFromHeaderEntity(block);
        org.wisdom.common.Block res = new org.wisdom.common.Block();
        res.setHeader(header);
        if(block.getBody() == null){
            block.setBody(new ArrayList<>());
            return res;
        }
        res.setBody(block.getBody().stream()
                .map(Mapping::getFromTransactionEntity)
                .collect(Collectors.toList())
        );
        return res;
    }

    public static org.wisdom.common.Header getFromHeaderEntity(AbstractHeader header){
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

    public static org.wisdom.common.Transaction getFromTransactionEntity(Transaction transaction){
        return org.wisdom.common.Transaction.builder().version(transaction.getVersion())
                .type(transaction.getType()).createdAt(transaction.getCreatedAt())
                .nonce(transaction.getNonce()).from(new HexBytes(transaction.getFrom()))
                .gasPrice(transaction.getGasPrice()).amount(transaction.getAmount())
                .payload(new HexBytes(transaction.getPayload())).to(new HexBytes(transaction.getTo()))
                .signature(new HexBytes(transaction.getSignature())).hash(new HexBytes(transaction.getHash()))
                .build();
    }
}
