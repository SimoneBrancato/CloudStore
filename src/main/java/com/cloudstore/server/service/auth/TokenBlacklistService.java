package com.cloudstore.server.service.auth;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;

/**
 * Manages a Redis-backed blacklist of revoked JWT IDs (JTI).
 * Each entry is stored with TTL = remaining token lifetime, so Redis
 * auto-evicts it exactly when the token would have expired naturally.
 *
 * Fail-open: if Redis is unreachable, operations log a warning but do
 * not throw, because availability trumps perfect revocation in this context.
 */
public class TokenBlacklistService {

    private final RedisClient redisClient;
    private static final String PREFIX = "blacklist:";

    /**
     * Reads REDIS_HOST / REDIS_PORT from environment variables.
     * Defaults: localhost / 6379.
     * Connection timeout: 2 s; socket timeout: 2 s; pool size: 8 connections.
     */
    public TokenBlacklistService() {    
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port;
        try {
            port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        } catch (NumberFormatException e) {
            port = 6379;
        }

        // Pool configuration
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxWait(Duration.ofSeconds(2));

        // Client configuration (timeouts)
        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(2000)
                .socketTimeoutMillis(2000)
                .build();

        // Build RedisClient using the recommended builder
        this.redisClient = RedisClient.builder()
                .hostAndPort(host, port)
                .poolConfig(poolConfig)              
                .clientConfig(clientConfig)
                .build();

        // Shutdown hook to close the client when the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                redisClient.close();
            } catch (Exception e) {
                // ignore
            }
        }));
    }

    /**
     * Marks a JTI as revoked in Redis.
     * Uses SET with EX params so the key expires automatically when the token would have.
     *
     * @param jti        The JWT ID claim to blacklist
     * @param ttlSeconds Seconds until the token expires (exp - now).
     * If <= 0 the token is already expired; nothing is written.
     */
    public void revoke(String jti, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            return; // Token already expired
        }
        try {
            redisClient.set(PREFIX + jti, "1", SetParams.setParams().ex(ttlSeconds));
        } catch (Exception e) {
            System.err.println("[WARN] TokenBlacklistService.revoke: Redis unavailable, skip blacklist for JTI="
                    + jti + " — " + e.getMessage());
        }
    }

    /**
     * Returns true if the given JTI has been blacklisted (i.e. the token was revoked).
     *
     * @param jti The JWT ID to check
     * @return true if revoked, false if valid or Redis is unreachable (fail-open)
     */
    public boolean isRevoked(String jti) {
        try {
            return redisClient.exists(PREFIX + jti);
        } catch (Exception e) {
            System.err.println("[WARN] TokenBlacklistService.isRevoked: Redis unavailable for JTI="
                    + jti + " — " + e.getMessage());
            return false;
        }
    }
}