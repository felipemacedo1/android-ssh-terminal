package com.ktar.ssh

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

/**
 * Initializes security providers for SSH connections.
 * Ensures BouncyCastle is properly registered as a security provider.
 */
object SecurityProviderInitializer {
    
    private var initialized = false
    
    /**
     * Initializes BouncyCastle provider if not already initialized.
     * This is required for SSH connections using modern cryptographic algorithms.
     */
    @Synchronized
    fun initialize() {
        if (!initialized) {
            // Remove existing BC provider if present
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            
            // Add BouncyCastle as the preferred security provider
            Security.insertProviderAt(BouncyCastleProvider(), 1)
            
            initialized = true
        }
    }
}
