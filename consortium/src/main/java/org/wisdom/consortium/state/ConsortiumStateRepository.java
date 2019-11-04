package org.wisdom.consortium.state;

import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.*;
import org.wisdom.exception.StateUpdateException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ConsortiumStateRepository implements StateRepository {
    private Map<String, StateFactory> factories;

    private Map<String, ForkAbleStatesTree> trees;


    public ConsortiumStateRepository() {
        factories = new HashMap<>();
        trees = new HashMap<>();
    }

    @Override
    public <T extends State<T>> void register(Block genesis, T genesisState) throws StateUpdateException {
        factories.put(genesisState.getClass().toString(), new InMemoryStateFactory(genesis, genesisState));
    }

    @Override
    public <T extends ForkAbleState<T>> void register(Block genesis, T... forkAbleStates) {
        if (forkAbleStates.length == 0) throw new RuntimeException("requires at least one state");
        trees.put(forkAbleStates[0].getClass().toString(), new ForkAbleStatesTree<>(genesis, forkAbleStates));
    }

    @Override
    public <T extends State<T>> Optional<T> getState(Block last, Class<T> clazz) {
        if (!factories.containsKey(clazz.toString())) return Optional.empty();
        return factories.get(clazz.toString()).get(last);
    }

    @Override
    public <T extends ForkAbleState<T>> Optional<T> getForkAbleState(Block last, String id, Class<T> clazz) {
        if (!trees.containsKey(clazz.toString())) return Optional.empty();
        Optional o = trees.get(clazz.toString()).get(id, last.getHash());
        if (!o.isPresent()) return Optional.empty();
        return Optional.of((T) o.get());
    }

    @Override
    public void update(Block b) {
        factories.values().forEach(f -> f.update(b));
        trees.values().forEach(t -> t.update(b));
    }

    @Override
    public void confirm(Block b) {
        factories.values().forEach(f -> f.confirm(b));
        trees.values().forEach(t -> t.confirm(b));
    }
}
