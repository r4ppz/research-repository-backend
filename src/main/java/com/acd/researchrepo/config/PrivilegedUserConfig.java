package com.acd.researchrepo.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegedUserConfig {

  @JsonProperty("super_admins")
  private List<String> superAdmins;

  private List<String> faculty;

  @JsonProperty("department_admins")
  private List<DepartmentAdminEntry> departmentAdmins;

  /** Helper: get map for fast lookup */
  public Map<String, Integer> getDepartmentAdminsMap() {
    return departmentAdmins == null
        ? Collections.emptyMap()
        : departmentAdmins.stream()
            .collect(Collectors.toMap(e -> e.email.toLowerCase(), e -> e.departmentId));
  }

  @Setter
  @Getter
  public static class DepartmentAdminEntry {
    public String email;

    @JsonProperty("department_id")
    public Integer departmentId;
  }
}
