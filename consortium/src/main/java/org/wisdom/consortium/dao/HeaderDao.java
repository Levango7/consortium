package org.wisdom.consortium.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wisdom.consortium.entity.Header;

import java.util.List;
import java.util.Optional;

public interface HeaderDao extends JpaRepository<Header, byte[]> {
    Optional<Header> getByHash(byte[] hash);
    List<Header> getHeadersByHeightBetween(long start, long end);

    Optional<Header> findTopByOrderByHeightAsc();
}
