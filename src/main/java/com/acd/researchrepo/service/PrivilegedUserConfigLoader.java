package com.acd.researchrepo.service;

import com.acd.researchrepo.config.PrivilegedUserConfig;
import com.acd.researchrepo.environment.AppProperties;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PrivilegedUserConfigLoader implements InitializingBean {

  private final AppProperties appProperties;
  private PrivilegedUserConfig privilegedUserConfig;

  public PrivilegedUserConfigLoader(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    loadConfig();
  }

  public void loadConfig() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      File yamlFile = new File(appProperties.getPrivilegedUsersYamlPath());
      this.privilegedUserConfig = mapper.readValue(yamlFile, PrivilegedUserConfig.class);

      validate();

    } catch (Exception e) {
      log.debug("Exception while loading YAML: " + e.getMessage());
      throw new IllegalStateException("Failed to load privileged users YAML.", e);
    }
  }

  private void validate() {
    Set<String> emailSet = new HashSet<>();

    if (privilegedUserConfig.getSuperAdmins() != null) {
      emailSet.addAll(privilegedUserConfig.getSuperAdmins());
    }

    if (privilegedUserConfig.getTeachers() != null) {
      emailSet.addAll(privilegedUserConfig.getTeachers());
    }

    if (privilegedUserConfig.getDepartmentAdmins() != null) {
      for (PrivilegedUserConfig.DepartmentAdminEntry entry :
          privilegedUserConfig.getDepartmentAdmins()) {
        if (entry.getEmail() == null || entry.getDepartmentId() == null) {
          throw new ApiException(
              ErrorCode.SERVICE_UNAVAILABLE,
              "Invalid configuration: department_admin entry missing email or department_id");
        }
        emailSet.add(entry.getEmail());
      }
    }
  }

  // Expose config accessors for injection
  public PrivilegedUserConfig getPrivilegedUserConfig() {
    return privilegedUserConfig;
  }
}
