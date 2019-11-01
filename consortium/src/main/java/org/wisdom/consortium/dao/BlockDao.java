package org.wisdom.consortium.dao;

import org.wisdom.consortium.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockDao extends JpaRepository<Block, byte[]> {
    Optional<Block> findTopByOrderByHeightDesc();
    Optional<Block> findByHeight(long height);
}
