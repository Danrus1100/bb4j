/**
 * Version detection and document upgrading.
 *
 * <p>{@link com.danrus.bb4j.migrate.Migrator} runs an ordered list of
 * {@link com.danrus.bb4j.migrate.MigrationStep}s (see
 * {@link com.danrus.bb4j.migrate.steps}). Each step declares
 * {@code shouldMigrate(doc)} and {@code migrate(doc)}. Migration is triggered
 * automatically during {@code read} unless
 * {@link com.danrus.bb4j.api.VersionPolicy#IGNORE} is selected.
 *
 * <p>{@link com.danrus.bb4j.api.VersionPolicy} (STRICT/WARN/IGNORE) decides what
 * happens when a document declares an unsupported version: throw, attach a
 * {@link com.danrus.bb4j.model.BbModelDocument.Warning}, or silently proceed. The
 * supported range lives in {@link com.danrus.bb4j.migrate.SupportedVersions}, and
 * {@link com.danrus.bb4j.model.meta.FormatVersion} is the comparable
 * {@code major.minor[-beta.x]} value object versions are expressed in.
 */
package com.danrus.bb4j.migrate;
