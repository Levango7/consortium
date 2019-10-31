package org.wisdom.consortium.dao;

import org.wisdom.consortium.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockDao extends JpaRepository<Block, byte[]> {
    Optional<Block> findTopByOrderByHeightAsc();
    Optional<Block> findByHeight(long height);
}
