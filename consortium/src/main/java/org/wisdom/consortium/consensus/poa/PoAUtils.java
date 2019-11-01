package org.wisdom.consortium.consensus.poa;

import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import org.wisdom.common.Block;
import org.wisdom.common.HexBytes;
import org.wisdom.common.Transaction;
import org.wisdom.util.BigEndian;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PoAUtils {
    public static byte[] getHash(Transaction transaction) {
        byte[] all = Stream.of(
                BigEndian.encodeInt32(transaction.getVersion()),
                BigEndian.encodeInt32(transaction.getType()),
                BigEndian.encodeInt64(transaction.getCreatedAt()),
                BigEndian.encodeInt64(transaction.getNonce()),
                transaction.getFrom().getBytes(),
                BigEndian.encodeInt64(transaction.getGasPrice()),
                BigEndian.encodeInt64(transaction.getAmount()),
                transaction.getPayload().getBytes(),
                transaction.getTo().getBytes(),
                transaction.getSignature().getBytes()
        ).filter(Objects::nonNull).reduce(new byte[0], Bytes::concat);
        return Hashing.sha256().hashBytes(all).asBytes();
    }

    public static byte[] merkleHash(List<Transaction> transactions) {
        byte[] all = transactions.stream().map(PoAUtils::getHash)
                .reduce(new byte[0], Bytes::concat);
        return Hashing.sha256().hashBytes(all).asBytes();
    }

    public static byte[] getHash(Block block) {
        block.setMerkleRoot(new HexBytes(merkleHash(block.getBody())));
        byte[] all = Stream.of(
                BigEndian.encodeInt32(block.getVersion()),
                block.getHashPrev().getBytes(),
                block.getMerkleRoot().getBytes(),
                BigEndian.encodeInt64(block.getHeight()),
                BigEndian.encodeInt64(block.getCreatedAt()),
                block.getPayload().getBytes()
        ).reduce(new byte[0], Bytes::concat);
        return Hashing.sha256().hashBytes(all).asBytes();
    }
}
