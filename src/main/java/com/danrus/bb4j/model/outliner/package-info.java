/**
 * The outliner: the scene tree that gives elements and groups their hierarchy.
 *
 * <p>A {@link com.danrus.bb4j.model.outliner.OutlinerNode} is either an
 * {@link com.danrus.bb4j.model.outliner.OutlinerGroupNode} (a named group/bone
 * with transform and children) or an
 * {@link com.danrus.bb4j.model.outliner.OutlinerElementRefNode} (a bare UUID
 * string referencing an {@link com.danrus.bb4j.model.geometry.Element}). In JSON
 * the tree mixes objects and plain strings accordingly, and parsing/serialization
 * is recursive.
 */
package com.danrus.bb4j.model.outliner;
