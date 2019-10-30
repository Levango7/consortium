package org.wisdom.consortium.dao;

import org.wisdom.consortium.entity.Header;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HeaderDao extends JpaRepository<Header, String> {
    Optional<Header> getByHash(byte[] hash);
    List<Header> getHeadersByHeightBetween(long start, long end);
}
