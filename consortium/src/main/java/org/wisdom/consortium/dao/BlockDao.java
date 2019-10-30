package org.wisdom.consortium.dao;

import org.wisdom.consortium.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockDao extends JpaRepository<Block, String> {
    Optional<Block> getByHash(byte[] hash);
    List<Block> getBlocksByHeightBetween(long start, long end);
}
