package network.elrond.crypto;

import network.elrond.crypto.digest.SHA256Digest;

public class SHA256Helper {
    public static byte[] sha256(byte[] message) {
        return sha256(message, new SHA256Digest());
    }

    private static byte[] sha256(byte[] message, SHA256Digest digest) {
        return doSha256(message, digest);
    }

    private static byte[] doSha256(byte[] message, SHA256Digest digest) {
        byte[] hash = new byte[digest.getDigestSize()];

        if (message.length != 0) {
            digest.update(message, 0, message.length);
        }
        digest.doFinal(hash, 0);
        return hash;
    }
}
