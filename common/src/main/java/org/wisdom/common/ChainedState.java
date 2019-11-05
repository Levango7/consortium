package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

// if T not implements Chained, wrap it as Chained
public class ChainedState<T extends State<T>> implements State<ChainedState<T>>, Chained{
    private HexBytes hashPrev;
    private HexBytes hash;
    private T state;

    @Override
    public void update(Block b, Transaction t) throws StateUpdateException {
        state.update(b, t);
        hashPrev = b.getHashPrev();
        hash = b.getHash();
    }

    @Override
    public void update(Block b) throws StateUpdateException {
        state.update(b);
        hashPrev = b.getHashPrev();
        hash = b.getHash();
    }

    public ChainedState<T> clone() {
        return new ChainedState<>(hashPrev,  hash, state.clone());
    }

    public HexBytes getHashPrev() {
        return hashPrev;
    }


    public HexBytes getHash() {
        return hash;
    }

    public T get(){
        return state;
    }

    public ChainedState(HexBytes hashPrev, HexBytes hash, T state) {
        this.hashPrev = hashPrev;
        this.hash = hash;
        this.state = state;
    }
}
