package com.acd.researchrepo.util.enums;

import lombok.Getter;

@Getter
public enum DocumentRequestSortField {
    CREATED_AT("createdAt", "createdAt"),
    STATUS("status", "status"),
    PAPER_TITLE("paper.title", "paper.title"),
    USER_FULL_NAME("user.fullName", "user.fullName");

    private final String apiField;
    private final String entityField;

    DocumentRequestSortField(String apiField, String entityField) {
        this.apiField = apiField;
        this.entityField = entityField;
    }

    public static String fromApiField(String apiField, String defaultField) {
        for (DocumentRequestSortField field : values()) {
            if (field.apiField.equalsIgnoreCase(apiField)) {
                return field.entityField;
            }
        }
        return defaultField;
    }
}
