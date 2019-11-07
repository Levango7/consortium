package org.wisdom.consortium.net;

import java.util.*;
import java.util.stream.Collectors;

// peers cache for peer searching/discovery
public class PeersCache {
    private static final int PEER_SCORE = 32;
    private static final int EVIL_SCORE = -(1 << 10);
    private int maximumPeers;

    private Map<Integer, Map<String, PeerImpl>> peers;


    private Set<PeerImpl> bootstraps;

    private Set<PeerImpl> blocked;

    private Set<PeerImpl> pended;

    private PeerImpl self;


    private boolean enableDiscovery;

    public PeersCache(
            PeerImpl self,
            Collection<? extends PeerImpl> bootstraps,
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

    public boolean hasPeer(PeerImpl peer) {
        int idx = self.subTree(peer);
        return peers.containsKey(idx) && peers.get(idx).containsKey(peer.getID().toString());
    }

    public void pend(PeerImpl peer) {
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

    public void keepPeer(PeerImpl peer) {
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
        Optional<Map<String, PeerImpl>> bucket = peers.values().stream()
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
    public void removePeer(PeerImpl peer) {
        if (!enableDiscovery || blocked.contains(peer)) {
            return;
        }
        int idx = self.subTree(peer);
        if (!peers.containsKey(idx)) {
            return;
        }
        peers.get(idx).remove(peer.getID().toString());
    }


    public List<PeerImpl> getBootstraps() {
        return new ArrayList<>(bootstraps);
    }

    public List<PeerImpl> getBlocked() {
        return new ArrayList<>(blocked);
    }

    public List<PeerImpl> getPended() {
        return new ArrayList<>(pended);
    }

    public List<PeerImpl> popPended() {
        List<PeerImpl> res = new ArrayList<>(pended);
        pended.clear();
        return res;
    }

    public PeerImpl getSelf() {
        return self;
    }

    // get limit peers randomly
    public List<PeerImpl> getPeers(int limit) {
        List<PeerImpl> res = getPeers();
        Random rand = new Random();
        while (res.size() > 0 && res.size() > limit) {
            int idx = Math.abs(rand.nextInt()) % res.size();
            res.remove(idx);
        }
        return res;
    }

    public List<PeerImpl> getPeers() {
        if (!enableDiscovery) {
            Set<PeerImpl> tmp = new HashSet<>(bootstraps);
            return new ArrayList<>(tmp);
        }
        List<PeerImpl> res = peers.values().stream()
                .map(x ->
                        Arrays.asList(x.values().toArray(new PeerImpl[]{}))
                )
                .reduce(new ArrayList<>(), (id, x) -> {
                    List<PeerImpl> tmp = new ArrayList<>(id);
                    tmp.addAll(x);
                    return tmp;
                });
        if (res.size() > 0) {
            return res;
        }
        return bootstraps.stream().filter(p -> !blocked.contains(p)).collect(Collectors.toList());
    }

    public void blockPeer(PeerImpl peer) {
        removePeer(peer);
        peer.score = EVIL_SCORE;
        blocked.add(peer);
    }

    // decrease score of peer
    public void half(PeerImpl peer) {
        peer.score /= 2;
        if (peer.score == 0) {
            removePeer(peer);
            blocked.remove(peer);
        }
    }

    // decrease score of all peer
    public void half() {
        List<PeerImpl> toRemove = new ArrayList<>();
        for (Map<String, PeerImpl> bucket : peers.values()) {
            for (PeerImpl peer : bucket.values()) {
                peer.score /= 2;
                if (peer.score == 0) {
                    toRemove.add(peer);
                }
            }
        }
        List<PeerImpl> toRestore = new ArrayList<>();
        toRemove.forEach(this::removePeer);
        for (PeerImpl p : blocked) {
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
