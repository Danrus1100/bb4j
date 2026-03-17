package com.danrus.bb4j.migrate;

import com.danrus.bb4j.api.VersionPolicy;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.meta.FormatVersion;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.migrate.steps.*;

import java.util.ArrayList;
import java.util.List;

public class Migrator {
    private static final List<MigrationStep> STEPS = new ArrayList<>();

    static {
        STEPS.add(new HeaderMigration());
        STEPS.add(new CompatibilityMigration());
        STEPS.add(new OutlinerPre32Migration());
        STEPS.add(new AnimationPre50Migration());
    }

    public static void migrateIfNeeded(BbModelDocument document, VersionPolicy policy) {
        Meta meta = document.getMeta();
        if (meta == null || meta.getFormatVersion() == null) {
            return;
        }

        FormatVersion version = meta.getFormatVersion();
        String versionStr = version.getRaw();

        if (!SupportedVersions.isSupported(versionStr)) {
            String warning = "Format version " + version + " is not supported (supported: " + 
                           SupportedVersions.getMinSupported() + " - " + SupportedVersions.getMaxSupported() + ")";
            
            switch (policy) {
                case STRICT -> throw new com.danrus.bb4j.api.BbException("UNSUPPORTED_VERSION", warning);
                case WARN -> document.addWarning(new BbModelDocument.Warning(
                    warning, 
                    BbModelDocument.Warning.WarningType.UNSUPPORTED_VERSION
                ));
                case IGNORE -> {}
            }
        }

        for (MigrationStep step : STEPS) {
            if (step.shouldMigrate(document)) {
                step.migrate(document);
            }
        }
    }

    public static void migrateToLatest(BbModelDocument document) {
        migrateIfNeeded(document, VersionPolicy.IGNORE);
    }

    public static String getMaxSupportedVersion() {
        return SupportedVersions.getMaxSupported();
    }

    public static String getMinSupportedVersion() {
        return SupportedVersions.getMinSupported();
    }

    public static boolean isVersionSupported(String version) {
        return SupportedVersions.isSupported(version);
    }

    public static boolean needsMigration(String version) {
        return SupportedVersions.needsMigration(version);
    }
}
