package org.wisdom.consortium.net;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// peers cache for peer searching/discovery
public class PeersCache {
    private static final int PEER_SCORE = 32;
    private static final int EVIL_SCORE = -(1 << 10);
    private int maximumPeers;

    static class Bucket {
        Map<PeerImpl, Channel> channels = new ConcurrentHashMap<>();
    }

    private Bucket[] peers = new Bucket[256];

    private Set<PeerImpl> bootstraps;

    private Set<PeerImpl> blocked = new HashSet<>();

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
        this.enableDiscovery = enableDiscovery;
        this.maximumPeers = maximumPeers;
    }

    public int size() {
        return Stream.of(peers)
                .filter(Objects::nonNull)
                .map(x -> x.channels)
                .map(Map::size)
                .reduce(0, Integer::sum);
    }

    public boolean has(PeerImpl peer) {
        int idx = self.subTree(peer);
        return peers[idx] != null && peers[idx].channels.containsKey(peer);
    }

    public void keep(PeerImpl peer, Channel channel) {
        if (peer.equals(self)) {
            return;
        }
        // if discovery is disabled, the peers is always bootstraps
        if (!enableDiscovery) {
            return;
        }
        // blocked peer
        if (blocked.contains(peer)) {
            channel.close();
            return;
        }
        peer.score = PEER_SCORE;
        int idx = self.subTree(peer);
        if (peers[idx] == null) {
            peers[idx] = new Bucket();
        }

        Optional<PeerImpl> p = peers[idx].channels.keySet().stream()
                .filter(k -> k.equals(peer)).findFirst();
        // increase its score
        if (p.isPresent()) {
            p.get().score += PEER_SCORE;
            return;
        }

        if (size() < maximumPeers) {
            peers[idx].channels.put(peer, channel);
            return;
        }

        // when neighbours is full, check whether some neighbours could be removed
        // 1. the bucket of the new neighbour is empty
        if (peers[idx].channels.size() > 0) {
            channel.close();
            return;
        }

        // 2. exists some bucket which contains more than one peer
        Optional<Map<PeerImpl, Channel>> bucket = Stream.of(peers)
                .filter(Objects::nonNull)
                .map(x -> x.channels)
                .max(Comparator.comparingInt(Map::size));

        if (!bucket.isPresent() || bucket.get().size() <= 1) {
            channel.close();
            return;
        }

        // the conditions above are both filled
        bucket.get().keySet()
                .stream().findFirst()
                .ifPresent(this::remove);

        peers[idx].channels.put(peer, channel);
    }

    // remove the peer and close the channel
    public void remove(PeerImpl peer) {
        if (!enableDiscovery || blocked.contains(peer)) {
            return;
        }
        int idx = self.subTree(peer);
        if (peers[idx] == null) {
            return;
        }
        Channel ch = peers[idx].channels.get(peer);
        peers[idx].channels.remove(peer);
        if (ch == null) return;
        ch.close();
    }


    public List<PeerImpl> getBootstraps() {
        return new ArrayList<>(bootstraps);
    }

    public List<PeerImpl> getBlocked() {
        return new ArrayList<>(blocked);
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
        List<PeerImpl> res = new ArrayList<>();
        Stream.of(peers)
                .filter(Objects::nonNull)
                .map(x -> x.channels.keySet())
                .forEach(res::addAll);
        if (res.size() > 0) {
            return res;
        }
        return bootstraps.stream().filter(p -> !blocked.contains(p)).collect(Collectors.toList());
    }

    public void block(PeerImpl peer) {
        remove(peer);
        peer.score = EVIL_SCORE;
        blocked.add(peer);
    }

    // decrease score of peer
    public void half(PeerImpl peer) {
        peer.score /= 2;
        if (peer.score == 0) {
            remove(peer);
            blocked.remove(peer);
        }
    }

    // decrease score of all peer
    public void half() {
        List<PeerImpl> toRemove = new ArrayList<>();
        for (Bucket bucket : peers) {
            for (PeerImpl peer : bucket.channels.keySet()) {
                peer.score /= 2;
                if (peer.score == 0) {
                    toRemove.add(peer);
                }
            }
        }
        List<PeerImpl> toRestore = new ArrayList<>();
        toRemove.forEach(this::remove);
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
