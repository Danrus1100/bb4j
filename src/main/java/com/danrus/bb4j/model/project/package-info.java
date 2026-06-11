/**
 * Editor- and project-level state that is not part of the geometry itself.
 *
 * <ul>
 *   <li>{@link com.danrus.bb4j.model.project.Resolution} — texture resolution.</li>
 *   <li>{@link com.danrus.bb4j.model.project.Display} — per-slot display transforms
 *       (Java item/block display settings).</li>
 *   <li>{@link com.danrus.bb4j.model.project.EditorState} — transient UI state
 *       (selection, paths, view mode); excluded from writes by default.</li>
 *   <li>{@link com.danrus.bb4j.model.project.History} — undo history; excluded from
 *       writes by default.</li>
 * </ul>
 */
package com.danrus.bb4j.model.project;
