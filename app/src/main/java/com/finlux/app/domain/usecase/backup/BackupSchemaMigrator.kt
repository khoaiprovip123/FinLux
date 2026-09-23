package com.finlux.app.domain.usecase.backup

import com.finlux.app.domain.model.backup.FinluxBackupSnapshot

/**
 * Handles schema migration for FinLux backup snapshots.
 *
 * Current schema version: 1.
 * When newer schema versions are introduced in future releases, migration functions
 * (e.g. migrateV1toV2) will be chained sequentially here.
 */
object BackupSchemaMigrator {

    const val CURRENT_SCHEMA_VERSION = FinluxBackupSnapshot.CURRENT_SCHEMA_VERSION
    const val MIN_SUPPORTED_SCHEMA_VERSION = FinluxBackupSnapshot.MIN_SUPPORTED_SCHEMA_VERSION

    /**
     * Migrates the given [snapshot] up to [CURRENT_SCHEMA_VERSION].
     *
     * @throws IllegalArgumentException if the snapshot schema version is older than
     *         [MIN_SUPPORTED_SCHEMA_VERSION] or newer than [CURRENT_SCHEMA_VERSION].
     */
    fun migrate(snapshot: FinluxBackupSnapshot): FinluxBackupSnapshot {
        require(snapshot.schemaVersion >= MIN_SUPPORTED_SCHEMA_VERSION) {
            "Bản sao lưu quá cũ (schema v${snapshot.schemaVersion}). " +
                "Phiên bản tối thiểu được hỗ trợ là v$MIN_SUPPORTED_SCHEMA_VERSION."
        }
        require(snapshot.schemaVersion <= CURRENT_SCHEMA_VERSION) {
            "Bản sao lưu được tạo bởi phiên bản FinLux mới hơn (schema v${snapshot.schemaVersion}). " +
                "Vui lòng nâng cấp ứng dụng."
        }

        var current = snapshot
        // Future migrations:
        // if (current.schemaVersion == 1 && CURRENT_SCHEMA_VERSION >= 2) current = migrateV1toV2(current)
        return current
    }
}
