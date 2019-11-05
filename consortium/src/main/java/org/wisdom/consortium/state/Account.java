package org.wisdom.consortium.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.wisdom.common.Block;
import org.wisdom.common.ForkAbleState;
import org.wisdom.common.HexBytes;
import org.wisdom.common.Transaction;
import org.wisdom.consortium.account.PublicKeyHash;
import org.wisdom.consortium.account.Utils;
import org.wisdom.exception.StateUpdateException;

import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class Account implements ForkAbleState<Account> {
    private PublicKeyHash publicKeyHash;

    private long balance;

    private HexBytes hashPrev;
    private long height;
    private HexBytes hash;

    @Override
    public String getIdentifier() {
        return publicKeyHash.getHex();
    }

    @Override
    public Set<String> getIdentifiersOf(Transaction transaction) {
        Set<String> res = new HashSet<>();
        res.add(Utils.publicKeyToAddress(transaction.getFrom().getBytes()));
        res.add(Utils.publicKeyHashToAddress(transaction.getTo().getBytes()));
        return res;
    }

    @Override
    public Account createEmpty(String id) {
        return new Account(PublicKeyHash.fromHex(id).get(), 0, new HexBytes(), 0, new HexBytes());
    }

    @Override
    public void update(Block b) throws StateUpdateException {

    }

    @Override
    public void update(Block b, Transaction t) throws StateUpdateException {
        if (Utils.publicKeyToAddress(t.getFrom().getBytes()).equals(publicKeyHash.getAddress())){
            balance -= t.getAmount();
        }
        if (Utils.publicKeyHashToAddress(t.getTo().getBytes()).equals(publicKeyHash.getAddress())){
            balance += t.getAmount();
        }
        hashPrev = b.getHashPrev();
        height = b.getHeight();
        hash = b.getHash();
    }

    @Override
    public Account clone() {
        return new Account(publicKeyHash, balance, hashPrev, height, hash);
    }
}
