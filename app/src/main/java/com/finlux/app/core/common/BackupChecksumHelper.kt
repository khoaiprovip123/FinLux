package com.finlux.app.core.common

import java.security.MessageDigest

/**
 * Shared SHA-256 checksum utilities for the Backup & Restore Engine.
 *
 * Rationale: Centralised here (core/common) per AGENTS.md §10 Zero Local Duplication,
 * so both ExportBackupUseCase and ValidateBackupUseCase consume the exact same
 * implementation without copy-paste divergence.
 */
object BackupChecksumHelper {

    private const val ALGORITHM = "SHA-256"

    /**
     * Computes the SHA-256 hex digest of the given [payload] string (UTF-8 encoded).
     * Returns a lowercase 64-character hex string, e.g. "a3f1…".
     */
    fun computeChecksum(payload: String): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val hashBytes = digest.digest(payload.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    /**
     * Returns true if [checksum] matches the SHA-256 of [payload].
     * Used by ValidateBackupUseCase to verify file integrity.
     */
    fun verifyChecksum(payload: String, checksum: String): Boolean {
        return computeChecksum(payload).equals(checksum, ignoreCase = true)
    }
}
