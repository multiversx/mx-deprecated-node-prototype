package network.elrond.data;

import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.crypto.Signature;
import network.elrond.crypto.SignatureService;
import network.elrond.service.AppServiceProvider;

public class SecureObjectUtil {


    public static <E> SecureObject<E> create(E object, PrivateKey privateKey, PublicKey publicKey) {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] _privateKey = privateKey.getValue();
        byte[] _publicKey = publicKey.getValue();
        byte[] hash = AppServiceProvider.getSerializationService().getHash(object);
        //TODO !!!!!
        // Signature signature = signatureService.signMessage(hash, _privateKey, _publicKey);
        Signature signature = new Signature();
        return new SecureObject<E>(object, signature, _publicKey);
    }


}
