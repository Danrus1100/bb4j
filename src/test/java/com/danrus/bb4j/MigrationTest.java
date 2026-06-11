package com.danrus.bb4j;

import com.danrus.bb4j.api.BbException;
import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.VersionPolicy;
import com.danrus.bb4j.migrate.steps.AnimationPre50Migration;
import com.danrus.bb4j.migrate.steps.HeaderMigration;
import com.danrus.bb4j.migrate.steps.OutlinerPre32Migration;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.animation.Animator;
import com.danrus.bb4j.model.animation.DataPoint;
import com.danrus.bb4j.model.animation.Keyframe;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for each migration step and version-policy behavior.
 */
class MigrationTest {

    // -----------------------------------------------------------------------
    // 1. HeaderMigration
    // -----------------------------------------------------------------------

    @Test
    void headerMigration_shouldMigrate_trueWhenFormatVersionNull() {
        HeaderMigration step = new HeaderMigration();

        // New Meta() starts with null formatVersion — do NOT call setFormatVersion(null)
        // because the String overload constructs a FormatVersion object and would NPE.
        BbModelDocument doc = new BbModelDocument();
        // formatVersion is null by default in a freshly constructed Meta
        assertNull(doc.getMeta().getFormatVersion(), "pre-condition: formatVersion is null");

        assertTrue(step.shouldMigrate(doc));
    }

    @Test
    void headerMigration_shouldMigrate_falseWhenFormatVersionSet() {
        HeaderMigration step = new HeaderMigration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("5.0");

        assertFalse(step.shouldMigrate(doc));
    }

    @Test
    void headerMigration_migrate_backfillsFormatVersionFromExtra() {
        HeaderMigration step = new HeaderMigration();

        BbModelDocument doc = new BbModelDocument();
        Meta meta = doc.getMeta();
        // Simulate old file with "format" key but no format_version
        meta.setExtra(new HashMap<>(Map.of("format", "4.0")));
        // formatVersion is null (default new Meta())

        assertTrue(step.shouldMigrate(doc));
        step.migrate(doc);

        assertNotNull(meta.getFormatVersion());
        assertEquals("4.0", meta.getFormatVersion().getRaw());
    }

    // -----------------------------------------------------------------------
    // 2. OutlinerPre32Migration
    // -----------------------------------------------------------------------

    @Test
    void outlinerPre32_shouldMigrate_trueForVersionBefore32() {
        OutlinerPre32Migration step = new OutlinerPre32Migration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("3.0");

        assertTrue(step.shouldMigrate(doc));
    }

    @Test
    void outlinerPre32_shouldMigrate_falseFor32() {
        OutlinerPre32Migration step = new OutlinerPre32Migration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("3.2");

        assertFalse(step.shouldMigrate(doc));
    }

    @Test
    void outlinerPre32_shouldMigrate_falseFor50() {
        OutlinerPre32Migration step = new OutlinerPre32Migration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("5.0");

        assertFalse(step.shouldMigrate(doc));
    }

    @Test
    void outlinerPre32_migrate_negatesZRotationRecursively() {
        OutlinerPre32Migration step = new OutlinerPre32Migration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("3.0");

        // Parent group (rotation does not matter for the assertion, but verify
        // it is also processed)
        OutlinerGroupNode parent = new OutlinerGroupNode("parent");
        parent.setRotation(new Double[]{1.0, 2.0, 6.0});

        // Child group nested inside parent — this is the target assertion
        OutlinerGroupNode child = new OutlinerGroupNode("child");
        child.setRotation(new Double[]{10.0, 20.0, 30.0});
        parent.addChild(child);

        doc.setOutliner(new ArrayList<>(List.of(parent)));

        step.migrate(doc);

        // Z component of child should be negated
        assertEquals(-30.0, child.getRotation()[2], 1e-9);
        // X and Y of child unchanged
        assertEquals(10.0, child.getRotation()[0], 1e-9);
        assertEquals(20.0, child.getRotation()[1], 1e-9);

        // Parent's Z should also be negated (migration processes all nodes)
        assertEquals(-6.0, parent.getRotation()[2], 1e-9);
    }

    // -----------------------------------------------------------------------
    // 3. AnimationPre50Migration
    // -----------------------------------------------------------------------

    @Test
    void animationPre50_shouldMigrate_trueFor40() {
        AnimationPre50Migration step = new AnimationPre50Migration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("4.0");

        assertTrue(step.shouldMigrate(doc));
    }

    @Test
    void animationPre50_shouldMigrate_falseFor50() {
        AnimationPre50Migration step = new AnimationPre50Migration();

        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion("5.0");

        assertFalse(step.shouldMigrate(doc));
    }

    @Test
    void animationPre50_migrate_inverts_X_and_Y_for_rotation_channel() {
        AnimationPre50Migration step = new AnimationPre50Migration();

        BbModelDocument doc = buildDocWithKeyframe("4.0", "rotation",
                new DataPoint("10", "20", "30"));

        step.migrate(doc);

        DataPoint dp = getFirstDataPoint(doc);
        assertEquals("-10", dp.getX(), "X must be inverted for rotation");
        assertEquals("-20", dp.getY(), "Y must be inverted for rotation");
        assertEquals("30", dp.getZ(), "Z must be untouched for rotation");
    }

    @Test
    void animationPre50_migrate_inverts_X_only_for_position_channel() {
        AnimationPre50Migration step = new AnimationPre50Migration();

        BbModelDocument doc = buildDocWithKeyframe("4.0", "position",
                new DataPoint("5", "6", "7"));

        step.migrate(doc);

        DataPoint dp = getFirstDataPoint(doc);
        assertEquals("-5", dp.getX(), "X must be inverted for position");
        assertEquals("6", dp.getY(), "Y must NOT be inverted for position");
        assertEquals("7", dp.getZ(), "Z must be untouched for position");
    }

    @Test
    void animationPre50_migrate_skips_scale_channel() {
        AnimationPre50Migration step = new AnimationPre50Migration();

        BbModelDocument doc = buildDocWithKeyframe("4.0", "scale",
                new DataPoint("1", "2", "3"));

        step.migrate(doc);

        DataPoint dp = getFirstDataPoint(doc);
        // scale channel is not position or rotation — nothing inverted
        assertEquals("1", dp.getX());
        assertEquals("2", dp.getY());
        assertEquals("3", dp.getZ());
    }

    // -----------------------------------------------------------------------
    // 4. VersionPolicy behavior via BbModel.read
    // -----------------------------------------------------------------------

    private static final String UNSUPPORTED_JSON =
            "{\"meta\":{\"format_version\":\"6.0\",\"model_format\":\"free\"},"
            + "\"resolution\":{\"width\":16,\"height\":16}}";

    @Test
    void versionPolicyStrict_throwsBbExceptionWithUnsupportedVersionCode() {
        BbException ex = assertThrows(BbException.class, () ->
                BbModel.read(UNSUPPORTED_JSON,
                        ReadOptions.builder().versionPolicy(VersionPolicy.STRICT)));

        assertEquals("UNSUPPORTED_VERSION", ex.getErrorCode());
    }

    @Test
    void versionPolicyWarn_readSucceeds_andAddsUnsupportedVersionWarning() {
        BbModelDocument doc = BbModel.read(UNSUPPORTED_JSON,
                ReadOptions.builder().versionPolicy(VersionPolicy.WARN));

        assertFalse(doc.getWarnings().isEmpty(), "Expected at least one warning");
        assertTrue(doc.getWarnings().stream()
                .anyMatch(w -> w.getType() ==
                        BbModelDocument.Warning.WarningType.UNSUPPORTED_VERSION),
                "Expected UNSUPPORTED_VERSION warning");
    }

    @Test
    void versionPolicyIgnore_readSucceeds_andNoWarningsAdded() {
        BbModelDocument doc = BbModel.read(UNSUPPORTED_JSON,
                ReadOptions.builder().versionPolicy(VersionPolicy.IGNORE));

        assertTrue(doc.getWarnings().isEmpty(),
                "No warnings expected under IGNORE policy");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Build a minimal document containing one animation with one keyframe. */
    private static BbModelDocument buildDocWithKeyframe(String version,
                                                         String channel,
                                                         DataPoint dp) {
        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setFormatVersion(version);

        Keyframe keyframe = new Keyframe(0.0, channel);
        keyframe.setDataPoints(new ArrayList<>(List.of(dp)));

        Animator animator = new Animator("bone");
        animator.setKeyframes(new ArrayList<>(List.of(keyframe)));

        Animation animation = new Animation("test_anim");
        animation.setAnimators(new HashMap<>(Map.of("bone", animator)));

        doc.setAnimations(new ArrayList<>(List.of(animation)));
        return doc;
    }

    private static DataPoint getFirstDataPoint(BbModelDocument doc) {
        return doc.getAnimations().get(0)
                .getAnimators().get("bone")
                .getKeyframes().get(0)
                .getDataPoints().get(0);
    }
}
