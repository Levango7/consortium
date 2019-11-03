package org.wisdom.consortium.service;

import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.wisdom.common.*;

import java.util.List;

@Service
public class ConsortiumRepositoryService implements ConsortiumRepository {
    @Qualifier("blockRepositoryService")
    @Autowired
    @Delegate
    private BlockRepository blockRepository;

    @Qualifier("transactionRepositoryService")
    @Autowired
    @Delegate
    private TransactionRepository transactionRepository;

    @Override
    public Block getLastConfirmed() {
        return null;
    }

    @Override
    public List<Block> getUnconfirmed() {
        return null;
    }

    @Override
    public void setProvider(ConfirmedBlocksProvider provider) {

    }
}
