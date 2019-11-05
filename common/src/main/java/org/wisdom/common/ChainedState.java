package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

// if T not implements Chained, wrap it as Chained
public class ChainedState<T extends State<T>> implements State<ChainedState<T>>, Chained{
    private HexBytes hashPrev;
    private HexBytes hash;
    private T state;
    private Chained chained;

    @Override
    public void update(Block b, Transaction t) throws StateUpdateException {
        state.update(b, t);
        if (chained != null) {
            chained = (Chained) state;
            return;
        }
        hashPrev = b.getHashPrev();
        hash = b.getHash();
    }

    public ChainedState<T> clone() {
        return new ChainedState<>(hashPrev,  hash, state.clone());
    }

    public HexBytes getHashPrev() {
        if(chained != null) return chained.getHashPrev();
        return hashPrev;
    }


    public HexBytes getHash() {
        if (chained != null) return chained.getHash();
        return hash;
    }

    public T getState(){
        return state;
    }

    public ChainedState(HexBytes hashPrev, HexBytes hash, T state) {
        this.hashPrev = hashPrev;
        this.hash = hash;
        this.state = state;
        if (state instanceof Chained) this.chained = (Chained) state;
    }
}
