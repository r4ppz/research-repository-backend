package com.acd.researchrepo.util;

import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;

public final class RequestParamValidator {

    private RequestParamValidator() {
    }

    /**
     * Validates pagination parameters
     *
     * @param page The page number (0-indexed)
     * @param size The page size
     */
    public static void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid query parameter: page must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid query parameter: size must be between 1 and 100");
        }
    }

    /**
     * Validates sort parameters
     *
     * @param sortBy        The field to sort by
     * @param sortOrder     The sort order (asc or desc)
     * @param allowedFields The allowed sort fields
     */
    public static void validateSortParams(String sortBy, String sortOrder, String... allowedFields) {
        if (sortBy != null && !isValidSortByField(sortBy, allowedFields)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid sort field");
        }
        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid query parameter: sortOrder must be 'asc' or 'desc'");
        }
    }

    /**
     * Validates if the sort field is in the allowed list
     *
     * @param field         The field to validate
     * @param allowedFields The allowed fields
     * @return True if the field is allowed, false otherwise
     */
    private static boolean isValidSortByField(String field, String... allowedFields) {
        if (field == null) {
            return true;
        }
        for (String allowedField : allowedFields) {
            if (field.equals(allowedField)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates status parameter against allowed statuses
     *
     * @param statusStr       The status string to validate
     * @param allowedStatuses The allowed statuses
     */
    public static void validateStatus(String statusStr, String... allowedStatuses) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return;
        }

        String[] statusArray = statusStr.split(",");
        for (String status : statusArray) {
            status = status.trim();
            boolean isValid = false;
            for (String allowedStatus : allowedStatuses) {
                if (status.equalsIgnoreCase(allowedStatus)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                throw new ApiException(ErrorCode.INVALID_REQUEST,
                        "Invalid status. Must be one of: " + String.join(", ", allowedStatuses));
            }
        }
    }
}
