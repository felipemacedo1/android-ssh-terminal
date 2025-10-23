package com.ktar

import android.app.Application
import com.ktar.ssh.SecurityProviderInitializer

/**
 * KTAR Application class.
 * Handles app-level initialization.
 */
class KTARApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize BouncyCastle security provider early
        SecurityProviderInitializer.initialize()
    }
}
