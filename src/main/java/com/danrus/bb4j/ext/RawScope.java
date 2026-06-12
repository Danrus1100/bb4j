package com.danrus.bb4j.ext;

/**
 * Identifies <em>which kind of object</em> an unrecognized ("extra") field
 * belongs to, so a {@link RawFieldCodec} can be bound to a specific place in the
 * document rather than to a bare key name.
 *
 * <p>Every value here corresponds to a model object that carries an
 * {@code extra} {@code Map<String,Object>} (see {@code BbModelReader} — the keys
 * not modeled explicitly are captured there). {@link #ROOT} is the document
 * itself ({@code BbModelDocument.rawData}). {@link #ANY} is a wildcard used when
 * registering a codec that should match a key regardless of the owning object.
 *
 * @see ParseExtensions
 */
public enum RawScope {
    /** Top-level document keys ({@code BbModelDocument.rawData}). */
    ROOT,
    META,
    RESOLUTION,
    TEXTURE,
    ELEMENT,
    /** A single cube {@code Face} (entry of {@code Element.faces}). */
    FACE,
    GROUP,
    /** An {@code OutlinerGroupNode} in the outliner tree. */
    OUTLINER_GROUP,
    ANIMATION,
    ANIMATOR,
    KEYFRAME,
    DATA_POINT,
    ANIMATION_CONTROLLER,
    /** A single {@code Display.DisplaySlot}. */
    DISPLAY_SLOT,
    REFERENCE_IMAGE,
    EDITOR_STATE,
    HISTORY,
    /** A single {@code History.HistoryEntry}. */
    HISTORY_ENTRY,
    COLLECTION,
    TEXTURE_GROUP,
    /** Wildcard: matches the given key in <em>any</em> scope. */
    ANY
}
