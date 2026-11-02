package com.AddisBuilders.Platform.Model.enums;

/**
 * Content format of a lesson.
 *
 * <p>The original catalogue used {@code TEXT}, {@code VIDEO_URL},
 * {@code PDF_DOWNLOAD} and {@code LINK}. The mentor curriculum builder speaks
 * in learner-facing terms, so {@code VIDEO}, {@code ARTICLE} and {@code LAB}
 * were added while keeping the legacy values for backwards compatibility with
 * existing rows.</p>
 */
public enum ContentType {
    TEXT,
    VIDEO_URL,
    PDF_DOWNLOAD,
    LINK,
    VIDEO,
    ARTICLE,
    LAB
}
