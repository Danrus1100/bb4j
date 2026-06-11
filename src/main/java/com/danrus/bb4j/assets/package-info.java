/**
 * Pluggable resolution and loading of texture/asset content.
 *
 * <p>{@link com.danrus.bb4j.assets.AssetResolver} is the resolution abstraction;
 * {@link com.danrus.bb4j.assets.FileSystemAssetResolver} resolves paths on disk,
 * while {@link com.danrus.bb4j.assets.DataUrl} /
 * {@link com.danrus.bb4j.assets.DataUrlDecoder} handle inline {@code data:} URLs.
 * {@link com.danrus.bb4j.assets.TextureContentLoader} loads the actual bytes, and
 * {@link com.danrus.bb4j.assets.PathPolicy} governs how paths are interpreted.
 */
package com.danrus.bb4j.assets;
