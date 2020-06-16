package org.citopt.connde.service.crypto;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Service for generating new RSA key pairs that may be used for establishing SSH sessions between the MBP
 * and devices.
 */
@Service
public class SSHKeyPairGenerator {
    /*
    Configuration
     */
    private static final int KEY_TYPE = com.jcraft.jsch.KeyPair.RSA;

    private JSch jsch;

    /**
     * Instantiates the key generator service with a new SSH session.
     */
    public SSHKeyPairGenerator() {
        //Create new key pair generator with the given algorithm
        jsch = new JSch();
    }

    /**
     * Generates a new RSA key pair that may be used for establishing SSH connections.
     *
     * @return The generated key pair
     */
    public KeyPair generateKeyPair() {
        //Generate key pair
        com.jcraft.jsch.KeyPair keyPair = null;
        try {
            keyPair = com.jcraft.jsch.KeyPair.genKeyPair(jsch, KEY_TYPE);
        } catch (JSchException ignored) {
        }

        //Sanity check
        if (keyPair == null) {
            throw new IllegalStateException("Failed to generate key pair.");
        }

        //Create output streams for both keys
        ByteArrayOutputStream privateKeyOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream publicKeyOutput = new ByteArrayOutputStream();

        //Write keys to streams (with empty comment for public key)
        keyPair.writePrivateKey(privateKeyOutput);
        keyPair.writePublicKey(publicKeyOutput, "");

        //Read converted keys from streams
        String privateKey = new String(privateKeyOutput.toByteArray());
        String publicKey = new String(publicKeyOutput.toByteArray());

        //Create new key pair object and add the keys
        KeyPair keyPairObject = new KeyPair();
        keyPairObject.setPrivateKey(privateKey);
        keyPairObject.setPublicKey(publicKey);

        return keyPairObject;
    }
}
