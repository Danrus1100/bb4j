package com.danrus.bb4j.migrate.steps;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.meta.FormatVersion;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.migrate.MigrationStep;

import java.util.List;

public class OutlinerPre32Migration implements MigrationStep {
    @Override
    public boolean shouldMigrate(BbModelDocument document) {
        Meta meta = document.getMeta();
        if (meta == null || meta.getFormatVersion() == null) {
            return false;
        }
        FormatVersion version = meta.getFormatVersion();
        FormatVersion v32 = new FormatVersion("3.2");
        return version.compareTo(v32) < 0;
    }

    @Override
    public void migrate(BbModelDocument document) {
        List<OutlinerNode> outliner = document.getOutliner();
        if (outliner == null) {
            return;
        }
        processNodes(outliner);
    }

    private void processNodes(List<OutlinerNode> nodes) {
        if (nodes == null) {
            return;
        }
        for (OutlinerNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                processNodes(node.getChildren());
            }
            if (node.getRotation() != null && node.getRotation().length >= 3) {
                node.getRotation()[2] = -node.getRotation()[2];
            }
        }
    }
}
