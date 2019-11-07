package org.wisdom.consortium.net;

public interface Plugin {
    void onMessage(Context context);
    void onClose(Peer remote);
}
