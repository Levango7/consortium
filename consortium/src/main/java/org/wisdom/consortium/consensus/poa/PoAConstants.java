package org.wisdom.consortium.consensus.poa;

import org.wisdom.common.HexBytes;
import org.wisdom.util.BigEndian;

public class PoAConstants {
    public static final HexBytes ZERO_BYTES = new HexBytes(new byte[32]);
    public static final int BLOCK_VERSION = BigEndian.decodeInt32(new byte[]{0, 'p', 'o', 'a'});
    public static final int TRANSACTION_VERSION = BigEndian.decodeInt32(new byte[]{0, 'p', 'o', 'a'});
}
