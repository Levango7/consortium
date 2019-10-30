package org.wisdom.common;

import java.util.List;
import java.util.Optional;

public interface BlockStore {
    void subscribe(BlockStoreListener... listeners);

    Block getGenesis();

    boolean hasBlock(byte[] hash);

    Header getBestHeader();

    Block getBestBlock();

    Optional<Header> getHeader(byte[] hash);

    Optional<Block> getBlock(byte[] hash);

    List<Header> getHeaders(long startHeight, int limit);

    List<Block> getBlocks(long startHeight, int limit);

    List<Header> getHeadersBetween(long startHeight, long stopHeight);

    List<Block> getBlocksBetween(long startHeight, long stopHeight);

    List<Header> getHeadersBetween(long startHeight, long stopHeight, int limit, boolean descend);

    List<Block> getBlocksBetween(long startHeight, long stopHeight, int limit, boolean descend);

    Optional<Header> getHeaderByHeight(long height);

    Optional<Block> getBlockByHeight(long height);

    Optional<Header> getAncestorHeader(byte[] hash, long ancestorHeight);

    Block getAncestorBlock(byte[] hash, long ancestorHeight);

    List<Header> getAncestorHeaders(byte[] hash, int limit);

    List<Block> getAncestorBlocks(byte[] hash, int limit);

    boolean writeBlock(Block block);
}
