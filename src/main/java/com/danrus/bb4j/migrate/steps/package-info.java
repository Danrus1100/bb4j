/**
 * Concrete {@link com.danrus.bb4j.migrate.MigrationStep} implementations.
 *
 * <p>Each step upgrades one aspect of an older {@code .bbmodel} towards the
 * current format:
 * <ul>
 *   <li>{@link com.danrus.bb4j.migrate.steps.HeaderMigration} — meta/header fields.</li>
 *   <li>{@link com.danrus.bb4j.migrate.steps.CompatibilityMigration} — general
 *       compatibility fix-ups.</li>
 *   <li>{@link com.danrus.bb4j.migrate.steps.OutlinerPre32Migration} — outliner
 *       layout used before format 3.2.</li>
 *   <li>{@link com.danrus.bb4j.migrate.steps.AnimationPre50Migration} — animation
 *       structure used before format 5.0.</li>
 * </ul>
 * Steps are registered, in order, by {@link com.danrus.bb4j.migrate.Migrator}.
 */
package com.danrus.bb4j.migrate.steps;
