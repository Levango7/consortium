package org.wisdom.consortium.dao;

import org.wisdom.common.HexBytes;
import org.wisdom.consortium.entity.Block;
import org.wisdom.consortium.entity.HeaderAdapter;
import org.wisdom.consortium.entity.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Mapping {
    public static org.wisdom.common.Block getFromBlockEntity(Block block){
        org.wisdom.common.Header header = getFromHeaderEntity(block);
        org.wisdom.common.Block res = new org.wisdom.common.Block(header);
        res.setBody(getFromTransactionsEntity(block.getBody()));
        return res;
    }

    public static List<org.wisdom.common.Block> getFromBlocksEntity(Collection<Block> blocks){
        return blocks.stream().map(Mapping::getFromBlockEntity).collect(Collectors.toList());
    }

    public static org.wisdom.common.Header getFromHeaderEntity(HeaderAdapter header){
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

    public static List<org.wisdom.common.Header> getFromHeadersEntity(Collection<? extends HeaderAdapter> headers){
        return headers.stream().map(Mapping::getFromHeaderEntity).collect(Collectors.toList());
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

    public static List<org.wisdom.common.Transaction> getFromTransactionsEntity(Collection<Transaction> transactions){
        return transactions.stream()
                .sorted((x, y) -> x.getPosition() - y.getPosition())
                .map(Mapping::getFromTransactionEntity)
                .collect(Collectors.toList());
    }
}
