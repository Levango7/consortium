package org.wisdom.common;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.wisdom.exception.StateUpdateException;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class StateTreeTests {
    private static final HexBytes ADDRESS_A = new HexBytes(new byte[]{0x0a});
    private static final HexBytes ADDRESS_B = new HexBytes(new byte[]{0x0b});

    // account to test
    public static class Account implements ForkAbleState<Account>{
        private String address;
        private long balance;

        @Override
        public String getIdentifier() {
            return address;
        }

        @Override
        public Set<String> getIdentifiersOf(Transaction transaction) {
            return new HashSet<>(Arrays.asList(transaction.getFrom().toString(), transaction.getTo().toString()));
        }

        @Override
        public Account createEmpty(String id) {
            return new Account(id, 0);
        }

        @Override
        public void update(Block b, Transaction t) throws StateUpdateException {
            if(t.getFrom().toString().equals(address)){
                balance -= t.getAmount();
            }
            if(t.getTo().toString().equals(address)){
                balance += t.getAmount();
            }
        }

        @Override
        public void update(Header header) throws StateUpdateException {

        }

        @Override
        public Account clone() {
            return new Account(address, balance);
        }

        public String getAddress() {
            return address;
        }

        public long getBalance() {
            return balance;
        }

        public Account(String address, long balance) {
            this.address = address;
            this.balance = balance;
        }
    }

    private ForkAbleStatesTree<Account> getTree() throws Exception{
        Map<String, Block> blocks = StateFactoryTests.getBlocks().stream()
                .collect(Collectors.toMap(
                        b -> b.getHash().toString(),
                        b -> b
                ));
        blocks.get("0002").getBody().add(Transaction.builder().from(ADDRESS_A).to(ADDRESS_B).amount(50).build());
        blocks.get("0102").getBody().add(Transaction.builder().from(ADDRESS_A).to(ADDRESS_B).amount(60).build());
        ForkAbleStatesTree<Account> tree = new ForkAbleStatesTree<>(
                blocks.get("0000"),
                new Account(ADDRESS_A.toString(), 100),
                new Account(ADDRESS_B.toString(), 100)
        );
        blocks.values().stream().sorted(Comparator.comparingLong(Block::getHeight)).forEach(tree::update);
        return tree;
    }

    @Test
    public void testGetTree() throws Exception{
        getTree();
    }

    @Test
    public void testGetState() throws Exception{
        Optional<Account> o = getTree().get(ADDRESS_A.toString(), Hex.decodeHex("0000".toCharArray()));
        assert o.isPresent();
        assert o.get().balance == 100;
    }

    @Test
    public void testUpdate() throws Exception{
        Optional<Account> o = getTree().get(ADDRESS_A.toString(), Hex.decodeHex("0002".toCharArray()));
        assert o.isPresent();
        assert o.get().balance == 50;
        o = getTree().get(ADDRESS_B.toString(), Hex.decodeHex("0002".toCharArray()));
        assert o.isPresent();
        assert o.get().balance == 150;
        o = getTree().get(ADDRESS_A.toString(), Hex.decodeHex("0102".toCharArray()));
        assert o.isPresent();
        assert o.get().balance == 40;
        o = getTree().get(ADDRESS_B.toString(), Hex.decodeHex("0102".toCharArray()));
        assert o.isPresent();
        assert o.get().balance == 160;
//        o = getTree().get(ADDRESS_B.toString(), Hex.decodeHex("0105".toCharArray()));
//        assert o.isPresent();
//        assert o.get().balance == 160;
    }

    @Test
    public void testConfirm() throws Exception{
        ForkAbleStatesTree<Account> tree = getTree();
        tree.confirm(Hex.decodeHex("0001".toCharArray()));
        tree.confirm(Hex.decodeHex("0102".toCharArray()));
        Optional<Account> o = getTree().get(ADDRESS_B.toString(), Hex.decodeHex("0002".toCharArray()));
        assert !o.isPresent();
    }
}
