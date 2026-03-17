package com.danrus.bb4j.migrate.steps;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.meta.FormatVersion;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.migrate.MigrationStep;

public class HeaderMigration implements MigrationStep {
    @Override
    public boolean shouldMigrate(BbModelDocument document) {
        Meta meta = document.getMeta();
        if (meta == null) {
            return false;
        }
        return meta.getFormatVersion() == null;
    }

    @Override
    public void migrate(BbModelDocument document) {
        Meta meta = document.getMeta();
        if (meta == null) {
            return;
        }
        if (meta.getFormatVersion() == null) {
            if (meta.getExtra() != null && meta.getExtra().containsKey("format")) {
                Object format = meta.getExtra().get("format");
                if (format instanceof String) {
                    meta.setFormatVersion((String) format);
                }
            }
        }
    }
}
