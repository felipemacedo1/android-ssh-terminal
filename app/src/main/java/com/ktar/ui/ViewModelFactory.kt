package com.ktar.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ktar.data.datastore.HostDataStore
import com.ktar.data.security.SecurityManager
import com.ktar.ssh.SSHManager
import com.ktar.ui.screens.connection.ConnectionViewModel
import com.ktar.ui.screens.hostlist.HostListViewModel

/**
 * Factory for creating ViewModels with dependencies.
 */
class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val hostDataStore by lazy { HostDataStore(context) }
    private val securityManager by lazy { SecurityManager() }
    private val sshManager by lazy { SSHManager() }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ConnectionViewModel::class.java) -> {
                ConnectionViewModel(hostDataStore, securityManager, sshManager) as T
            }
            modelClass.isAssignableFrom(HostListViewModel::class.java) -> {
                HostListViewModel(hostDataStore) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
