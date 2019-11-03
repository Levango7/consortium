package org.wisdom.common;

import java.util.Collection;
import java.util.List;

public interface OrphansPool<T extends Chained> extends BlockRepositoryListener{
    List<T> filterAndCacheOrphans(Collection<? extends T> nodes);

    List<T> get(int page, int size);

    List<T> getAll();

    int size();

    List<T> getOrphanChainHeads();
}
