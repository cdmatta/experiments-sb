package com.github.cdmatta.experiment.dpkg.util;

import com.github.cdmatta.experiment.dpkg.domain.DPackage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.readAllLines;
import static java.nio.file.Paths.get;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Component
public class StatusFileReader {

    private final Map<String, DPackage> installedPackages;

    public StatusFileReader() {
        installedPackages = readInstalledPackages();
        populateDependencyMetaData();
    }

    private Map<String, DPackage> readInstalledPackages() {
        try {
            Map<String, DPackage> installedPackages = new HashMap<>();
            var paramValue = "";
            var paramName = "";
            var currentPackage = "";
            var currentField = "";
            var currentValue = "";
            var dpkgStatusLines = readAllLines(get("/var/lib/dpkg/status"));
            for (var line : dpkgStatusLines) {
                if (line.startsWith(" ")) {
                    currentValue += line;
                    continue;
                }
                if (isBlank(line)) {
                    continue;
                }
                var split = line.split(":", 2);
                paramName = split[0];
                paramValue = split[1];

                if ("Package".equals(paramName)) {
                    var p = new DPackage();
                    currentPackage = trimToEmpty(paramValue);
                    p.setName(currentPackage);
                    installedPackages.put(currentPackage, p);
                    continue;
                }
                if ("Depends".equals(currentField)) {
                    installedPackages.get(currentPackage).setDepends(currentValue);
                }
                if ("Description".equals(currentField)) {
                    installedPackages.get(currentPackage).setDescription(currentValue);
                }
                currentField = paramName;
                currentValue = paramValue;
            }
            return installedPackages;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read status file");
        }
    }

    private void populateDependencyMetaData() {
        installedPackages.forEach((name, dpkg) -> {
            var upstream = dpkg.getUpstreamDependencies();
            upstream.keySet().forEach(requiredPackageName -> {
                var requiredPackage = installedPackages.get(requiredPackageName);
                if (requiredPackage != null) {
                    requiredPackage.getDownStreamDependencies().add(name);
                    upstream.put(requiredPackageName, true);
                }
            });
        });
    }

    public Map<String, DPackage> getInstalledPackages() {
        return installedPackages;
    }
}
