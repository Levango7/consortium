package org.wisdom.common;

// if T not implements Chained, wrap it as Chained
public class ChainedState<T extends State<T>> implements Chained{
    private HexBytes hashPrev;
    private HexBytes hash;
    private T state;

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
