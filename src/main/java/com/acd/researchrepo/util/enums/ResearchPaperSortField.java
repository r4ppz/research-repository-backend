package com.acd.researchrepo.util.enums;

import lombok.Getter;

@Getter
public enum ResearchPaperSortField {
    TITLE("title", "title"),
    AUTHOR_NAME("authorName", "authorName"),
    SUBMISSION_DATE("submissionDate", "submissionDate");

    private final String apiField;
    private final String entityField;

    ResearchPaperSortField(String apiField, String entityField) {
        this.apiField = apiField;
        this.entityField = entityField;
    }

    public static String fromApiField(String apiField, String defaultField) {
        for (ResearchPaperSortField field : values()) {
            if (field.apiField.equalsIgnoreCase(apiField)) {
                return field.entityField;
            }
        }
        return defaultField;
    }
}
