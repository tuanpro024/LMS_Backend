package com.lms.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class RsaKeyUtil {
    private static final Logger log = LoggerFactory.getLogger(RsaKeyUtil.class);

    private RsaKeyUtil() {
    }

    public static RSAPrivateKey loadPrivateKey(String location) {
        log.debug("Loading RSA private key from: {}", location);
        byte[] bytes = readAllBytes(location);
        String pem = new String(bytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
            log.info("Successfully loaded RSA private key from {}, algorithm: {}, modulus length: {} bits",
                    location, privateKey.getAlgorithm(), privateKey.getModulus().bitLength());
            log.debug("Private key modulus (first 32 chars): {}...",
                    privateKey.getModulus().toString().substring(0,
                            Math.min(32, privateKey.getModulus().toString().length())));
            return privateKey;
        } catch (Exception e) {
            log.error("Failed to load private key from {}", location, e);
            throw new IllegalStateException("Failed to load private key from " + location, e);
        }
    }

    public static RSAPublicKey loadPublicKey(String location) {
        log.debug("Loading RSA public key from: {}", location);
        byte[] bytes = readAllBytes(location);
        String pem = new String(bytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(decoded));
            log.info("Successfully loaded RSA public key from {}, algorithm: {}, modulus length: {} bits",
                    location, publicKey.getAlgorithm(), publicKey.getModulus().bitLength());
            log.debug("Public key modulus (first 32 chars): {}...",
                    publicKey.getModulus().toString().substring(0,
                            Math.min(32, publicKey.getModulus().toString().length())));
            return publicKey;
        } catch (Exception e) {
            log.error("Failed to load public key from {}", location, e);
            throw new IllegalStateException("Failed to load public key from " + location, e);
        }
    }

    /**
     * Validate that a public and private key pair match by comparing their moduli.
     * RSA key pairs share the same modulus value.
     * 
     * @param publicKey  the public key
     * @param privateKey the private key
     * @return true if the keys form a valid pair, false otherwise
     */
    public static boolean validateKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        log.debug("Validating RSA key pair");
        boolean isValid = publicKey.getModulus().equals(privateKey.getModulus());
        if (isValid) {
            log.info("RSA key pair validation successful - moduli match");
        } else {
            log.error("RSA key pair validation FAILED - moduli do not match!");
            log.error("Public key modulus: {}...", publicKey.getModulus().toString().substring(0, 50));
            log.error("Private key modulus: {}...", privateKey.getModulus().toString().substring(0, 50));
        }
        return isValid;
    }

    private static byte[] readAllBytes(String location) {
        if (!StringUtils.hasText(location)) {
            throw new IllegalStateException("Key location must not be empty");
        }
        try {
            if (location.startsWith("classpath:")) {
                String path = location.substring("classpath:".length());
                Resource resource = new ClassPathResource(path);
                return resource.getInputStream().readAllBytes();
            }
            if (location.startsWith("file:")) {
                return Files.readAllBytes(Path.of(location.substring("file:".length())));
            }
            return Files.readAllBytes(Path.of(location));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read key at " + location, e);
        }
    }
}
