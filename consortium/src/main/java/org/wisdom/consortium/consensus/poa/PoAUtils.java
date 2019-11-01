package org.wisdom.consortium.consensus.poa;

import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import org.wisdom.common.Block;
import org.wisdom.common.HexBytes;
import org.wisdom.common.Transaction;
import org.wisdom.util.CommonUtil;

import java.util.List;

public class PoAUtils {
    public static byte[] getHash(Transaction transaction) {
        return Hashing.sha256().hashBytes(CommonUtil.getRaw(transaction)).asBytes();
    }

    public static byte[] merkleHash(List<Transaction> transactions) {
        byte[] all = transactions.stream().map(PoAUtils::getHash)
                .reduce(new byte[0], Bytes::concat);
        return Hashing.sha256().hashBytes(all).asBytes();
    }

    public static byte[] getHash(Block block) {
        block.setMerkleRoot(new HexBytes(merkleHash(block.getBody())));
        return Hashing.sha256().hashBytes(CommonUtil.getRaw(block.getHeader())).asBytes();
    }
}
