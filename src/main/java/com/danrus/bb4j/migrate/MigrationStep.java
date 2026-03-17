package com.danrus.bb4j.migrate;

import com.danrus.bb4j.model.BbModelDocument;

public interface MigrationStep {
    boolean shouldMigrate(BbModelDocument document);
    void migrate(BbModelDocument document);
}
