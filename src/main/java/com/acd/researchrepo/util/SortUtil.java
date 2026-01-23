package com.acd.researchrepo.util;

import java.util.Map;

import org.springframework.data.domain.Sort;

public class SortUtil {

    /**
     * Creates a Sort object based on the provided sort field and order.
     *
     * @param sortBy        The field to sort by
     * @param sortOrder     The sort order ("asc" for ascending, anything else for
     *                      descending)
     * @param allowedFields A map of external field names to internal entity field
     *                      names
     * @param defaultField  The default field to sort by if sortBy is null/empty
     * @return A Sort object configured with the specified parameters
     */
    public static Sort createSort(
            String sortBy,
            String sortOrder,
            Map<String, String> allowedFields,
            String defaultField) {

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = defaultField;
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Check if the field is in the allowed fields map, otherwise use default
        String mappedField = allowedFields.getOrDefault(sortBy, defaultField);

        return Sort.by(direction, mappedField);
    }

    /**
     * Creates a Sort object with default descending order.
     *
     * @param sortBy        The field to sort by
     * @param allowedFields A map of external field names to internal entity field
     *                      names
     * @param defaultField  The default field to sort by if sortBy is null/empty
     * @return A Sort object configured with the specified parameters
     */
    public static Sort createSort(String sortBy, Map<String, String> allowedFields, String defaultField) {
        return createSort(sortBy, "desc", allowedFields, defaultField);
    }
}
