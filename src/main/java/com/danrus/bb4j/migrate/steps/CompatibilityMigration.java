package com.danrus.bb4j.migrate.steps;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.meta.FormatVersion;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.model.meta.ModelFormatId;
import com.danrus.bb4j.migrate.MigrationStep;

import java.util.ArrayList;
import java.util.List;

public class CompatibilityMigration implements MigrationStep {
    @Override
    public boolean shouldMigrate(BbModelDocument document) {
        Meta meta = document.getMeta();
        if (meta == null || meta.getModelFormat() == null) {
            return true;
        }
        return false;
    }

    @Override
    public void migrate(BbModelDocument document) {
        Meta meta = document.getMeta();
        if (meta == null) {
            return;
        }

        if (meta.getModelFormat() == null) {
            if (Boolean.TRUE.equals(meta.getBoneRig())) {
                meta.setModelFormat(new ModelFormatId(ModelFormatId.BEDROCK_OLD));
            } else {
                meta.setModelFormat(new ModelFormatId(ModelFormatId.JAVA_BLOCK));
            }
        }

        if (document.getRawData() != null && document.getRawData().containsKey("cubes")) {
            List<Object> cubes = (List<Object>) document.getRawData().get("cubes");
            if (cubes != null && document.getElements() == null) {
                document.setElements(new ArrayList<>());
            }
        }

        if (document.getRawData() != null && document.getRawData().containsKey("geometry_name")) {
            String geometryName = (String) document.getRawData().get("geometry_name");
            if (geometryName != null && meta.getModelIdentifier() == null) {
                meta.setModelIdentifier(geometryName);
            }
        }

        FormatVersion version = meta.getFormatVersion();
        if (version != null && document.getElements() != null) {
            FormatVersion v450 = new FormatVersion("4.5");
            if (version.compareTo(v450) < 0 && Boolean.TRUE.equals(meta.getBoxUv())) {
                for (Element element : document.getElements()) {
                    if (Boolean.FALSE.equals(element.getShade())) {
                        element.setMirrorUv(true);
                    }
                }
            }
        }
    }
}
