package com.lms.common.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public final class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private JwtUtils() {
    }

    public static String generateAccessToken(String userId,
            String email,
            Set<String> roles,
            boolean emailVerified,
            Duration ttl,
            String issuer,
            RSAPrivateKey privateKey) {
        return signToken(userId, email, roles, emailVerified, ttl, issuer, privateKey, UUID.randomUUID().toString());
    }

    public static String generateRefreshToken(String userId,
            Duration ttl,
            String issuer,
            RSAPrivateKey privateKey,
            String parentJti) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .jwtID(UUID.randomUUID().toString())
                .claim("typ", "refresh")
                .claim("prt", parentJti)
                .build();
        return sign(claims, privateKey);
    }

    public static JwtPayload verify(String token, RSAPublicKey publicKey) {
        log.debug("Starting JWT verification process");
        SignedJWT jwt = parse(token);

        boolean valid = verifySignature(jwt, publicKey);
        if (!valid) {
            log.warn("JWT signature verification failed");
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid token signature");
        }
        log.debug("JWT signature verified successfully");

        try {
            Date expTime = jwt.getJWTClaimsSet().getExpirationTime();
            if (expTime != null) {
                log.debug("Checking token expiration (exp: {})", expTime);
                if (expTime.before(new Date())) {
                    log.warn("JWT token has expired at {}", expTime);
                    throw new ApiException(ErrorCode.UNAUTHORIZED, "Token expired");
                }
            }
        } catch (ParseException e) {
            log.error("Failed to parse JWT claims", e);
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid token claims", e);
        }

        JwtPayload payload = toPayload(jwt);
        log.debug("JWT verification successful for user: {}", payload.userId());
        return payload;
    }

    private static JwtPayload toPayload(SignedJWT jwt) {
        try {
            log.debug("Extracting JWT payload claims");
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String userId = claims.getSubject();
            String email = claims.getStringClaim("email");
            @SuppressWarnings("unchecked")
            Set<String> roles = claims.getStringListClaim(SecurityConstants.CLAIM_ROLES) != null
                    ? Set.copyOf(claims.getStringListClaim(SecurityConstants.CLAIM_ROLES))
                    : Set.of();
            boolean emailVerified = Boolean.TRUE.equals(claims.getBooleanClaim(SecurityConstants.CLAIM_EMAIL_VERIFIED));

            log.debug("Extracted claims: userId={}, email={}, roles={}, emailVerified={}",
                    userId, email, roles, emailVerified);

            return new JwtPayload(
                    claims.getJWTID(),
                    userId,
                    email,
                    roles,
                    emailVerified,
                    claims.getClaim("typ"),
                    claims.getClaim("prt"));
        } catch (ParseException e) {
            log.error("Failed to extract JWT payload", e);
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid token claims", e);
        }
    }

    private static String signToken(String userId,
            String email,
            Set<String> roles,
            boolean emailVerified,
            Duration ttl,
            String issuer,
            RSAPrivateKey privateKey,
            String jti) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .jwtID(jti)
                .claim("email", email)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_ROLES, roles)
                .claim(SecurityConstants.CLAIM_EMAIL_VERIFIED, emailVerified)
                .claim("typ", "access")
                .build();
        return sign(claims, privateKey);
    }

    private static String sign(JWTClaimsSet claims, RSAPrivateKey privateKey) {
        try {
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
            signedJWT.sign(new RSASSASigner(privateKey));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to sign JWT", e);
        }
    }

    private static boolean verifySignature(SignedJWT jwt, RSAPublicKey publicKey) {
        try {
            String algorithm = jwt.getHeader().getAlgorithm().getName();
            log.debug("Verifying JWT signature using algorithm: {}", algorithm);
            boolean isValid = jwt.verify(new RSASSAVerifier(publicKey));
            log.debug("Signature verification result: {}", isValid);
            return isValid;
        } catch (JOSEException e) {
            log.error("JWT signature verification error", e);
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid token signature", e);
        }
    }

    private static SignedJWT parse(String token) {
        try {
            log.debug("Parsing JWT token");
            SignedJWT jwt = SignedJWT.parse(token);
            log.debug("JWT token parsed successfully");
            return jwt;
        } catch (ParseException e) {
            log.error("Failed to parse JWT token", e);
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Malformed token", e);
        }
    }

    public record JwtPayload(String jti,
            String userId,
            String email,
            Set<String> roles,
            boolean emailVerified,
            Object type,
            Object parentJti) {
    }
}
