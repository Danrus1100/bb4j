package com.danrus.bb4j.migrate.steps;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.*;
import com.danrus.bb4j.model.meta.FormatVersion;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.migrate.MigrationStep;
import com.danrus.bb4j.molang.MolangInverter;

import java.util.List;
import java.util.Map;

public class AnimationPre50Migration implements MigrationStep {
    @Override
    public boolean shouldMigrate(BbModelDocument document) {
        Meta meta = document.getMeta();
        if (meta == null || meta.getFormatVersion() == null) {
            return false;
        }
        FormatVersion version = meta.getFormatVersion();
        FormatVersion v50 = new FormatVersion("5.0");
        return version.compareTo(v50) < 0;
    }

    @Override
    public void migrate(BbModelDocument document) {
        List<Animation> animations = document.getAnimations();
        if (animations == null) {
            return;
        }

        for (Animation animation : animations) {
            Map<String, Animator> animators = animation.getAnimators();
            if (animators == null) {
                continue;
            }

            for (Animator animator : animators.values()) {
                List<Keyframe> keyframes = animator.getKeyframes();
                if (keyframes == null) {
                    continue;
                }

                for (Keyframe keyframe : keyframes) {
                    String channel = keyframe.getChannel();
                    if (channel == null) {
                        continue;
                    }

                    boolean isPositionOrRotation = "position".equals(channel) || "rotation".equals(channel);

                    if (isPositionOrRotation) {
                        List<DataPoint> dataPoints = keyframe.getDataPoints();
                        if (dataPoints != null) {
                            for (DataPoint dp : dataPoints) {
                                if (dp.getX() != null) {
                                    dp.setX(MolangInverter.invert(dp.getX()));
                                }
                                if ("rotation".equals(channel) && dp.getY() != null) {
                                    dp.setY(MolangInverter.invert(dp.getY()));
                                }
                            }
                        }
                    }

                    if (keyframe.getInterpolation() != null && keyframe.getInterpolation().isBezier()) {
                        if (isPositionOrRotation) {
                            if (keyframe.getBezierLeftValue() != null && keyframe.getBezierLeftValue().length >= 2) {
                                keyframe.getBezierLeftValue()[0] = -keyframe.getBezierLeftValue()[0];
                            }
                            if (keyframe.getBezierRightValue() != null && keyframe.getBezierRightValue().length >= 2) {
                                keyframe.getBezierRightValue()[0] = -keyframe.getBezierRightValue()[0];
                            }
                        }
                        if ("rotation".equals(channel)) {
                            if (keyframe.getBezierLeftValue() != null && keyframe.getBezierLeftValue().length >= 2) {
                                keyframe.getBezierLeftValue()[1] = -keyframe.getBezierLeftValue()[1];
                            }
                            if (keyframe.getBezierRightValue() != null && keyframe.getBezierRightValue().length >= 2) {
                                keyframe.getBezierRightValue()[1] = -keyframe.getBezierRightValue()[1];
                            }
                        }
                    }
                }
            }
        }
    }
}
