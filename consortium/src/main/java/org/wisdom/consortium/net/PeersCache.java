package org.wisdom.consortium.net;

import java.util.*;
import java.util.stream.Collectors;

// peers cache for peer searching/discovery
public class PeersCache {
    private static final int PEER_SCORE = 32;
    private static final int EVIL_SCORE = -(1 << 10);
    private int maximumPeers;

    private Map<Integer, Map<String, Peer>> peers;


    private Set<Peer> bootstraps;

    private Set<Peer> blocked;

    private Set<Peer> pended;

    private Peer self;


    private boolean enableDiscovery;

    public PeersCache(
            Peer self,
            Collection<? extends Peer> bootstraps,
            boolean enableDiscovery,
            int maximumPeers
    ) {
        this.self = self;
        this.bootstraps = new HashSet<>(bootstraps);
        this.blocked = new HashSet<>();
        this.peers = new HashMap<>();
        this.pended = new HashSet<>();
        this.enableDiscovery = enableDiscovery;
        this.maximumPeers = maximumPeers;
    }

    public int size() {
        return peers.values().stream().map(Map::size).reduce(Integer::sum).orElse(0);
    }

    public boolean hasPeer(Peer peer) {
        int idx = self.subTree(peer);
        return peers.containsKey(idx) && peers.get(idx).containsKey(peer.getID().toString());
    }

    public void pend(Peer peer) {
        if (size() >= maximumPeers) {
            return;
        }
        if (peer.equals(self)) {
            return;
        }
        if (hasPeer(peer) || blocked.contains(peer) || bootstraps.contains(peer)) {
            return;
        }
        pended.add(peer);
    }

    public void keepPeer(Peer peer) {
        if (peer.equals(self)) {
            return;
        }
        // if discovery is disabled, the peers is always bootstraps
        if (!enableDiscovery) {
            return;
        }
        // blocked peer
        if (blocked.contains(peer)) {
            return;
        }
        peer.score = PEER_SCORE;
        int idx = self.subTree(peer);
        if (!peers.containsKey(idx)) {
            peers.put(idx, new HashMap<>());
        }

        // increase its score
        if (hasPeer(peer)) {
            peers.get(idx).get(peer.getID().toString()).score += PEER_SCORE;
            return;
        }

        if (size() < maximumPeers) {
            peers.get(idx).put(peer.getID().toString(), peer);
            return;
        }

        // when neighbours is full, check whether some neighbours could be removed
        // 1. the bucket of the new neighbour is empty
        if (peers.get(idx).size() > 0) {
            return;
        }

        // 2. exists some bucket which contains more than one peer
        Optional<Map<String, Peer>> bucket = peers.values().stream()
                .max(Comparator.comparingInt(Map::size));

        if (!bucket.isPresent() || bucket.get().size() <= 1) {
            return;
        }

        // the conditions above are both filled
        String key = (String) bucket.get().keySet().toArray()[0];
        bucket.get().remove(key);

        peers.get(idx).put(peer.getID().toString(), peer);
    }

    // remove the peer and the peer can not be connected for a while
    public void removePeer(Peer peer) {
        if (!enableDiscovery || blocked.contains(peer)) {
            return;
        }
        int idx = self.subTree(peer);
        if (!peers.containsKey(idx)) {
            return;
        }
        peers.get(idx).remove(peer.getID().toString());
    }


    public List<Peer> getBootstraps() {
        return new ArrayList<>(bootstraps);
    }

    public List<Peer> getBlocked() {
        return new ArrayList<>(blocked);
    }

    public List<Peer> getPended() {
        return new ArrayList<>(pended);
    }

    public List<Peer> popPended() {
        List<Peer> res = new ArrayList<>(pended);
        pended.clear();
        return res;
    }

    public Peer getSelf() {
        return self;
    }

    // get limit peers randomly
    public List<Peer> getPeers(int limit) {
        List<Peer> res = getPeers();
        Random rand = new Random();
        while (res.size() > 0 && res.size() > limit) {
            int idx = Math.abs(rand.nextInt()) % res.size();
            res.remove(idx);
        }
        return res;
    }

    public List<Peer> getPeers() {
        if (!enableDiscovery) {
            Set<Peer> tmp = new HashSet<>(bootstraps);
            return new ArrayList<>(tmp);
        }
        List<Peer> res = peers.values().stream()
                .map(x ->
                        Arrays.asList(x.values().toArray(new Peer[]{}))
                )
                .reduce(new ArrayList<>(), (id, x) -> {
                    List<Peer> tmp = new ArrayList<>(id);
                    tmp.addAll(x);
                    return tmp;
                });
        if (res.size() > 0) {
            return res;
        }
        return bootstraps.stream().filter(p -> !blocked.contains(p)).collect(Collectors.toList());
    }

    public void blockPeer(Peer peer) {
        removePeer(peer);
        peer.score = EVIL_SCORE;
        blocked.add(peer);
    }

    // decrease score of peer
    public void half(Peer peer) {
        peer.score /= 2;
        if (peer.score == 0) {
            removePeer(peer);
            blocked.remove(peer);
        }
    }

    // decrease score of all peer
    public void half() {
        List<Peer> toRemove = new ArrayList<>();
        for (Map<String, Peer> bucket : peers.values()) {
            for (Peer peer : bucket.values()) {
                peer.score /= 2;
                if (peer.score == 0) {
                    toRemove.add(peer);
                }
            }
        }
        List<Peer> toRestore = new ArrayList<>();
        toRemove.forEach(this::removePeer);
        for (Peer p : blocked) {
            p.score /= 2;
            if (p.score == 0) {
                toRestore.add(p);
            }
        }
        toRestore.forEach(p -> blocked.remove(p));
    }

    public boolean isFull() {
        return size() == maximumPeers;
    }
}
