package com.github.cdmatta.experiment.dpkg.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Data
public class DPackage {

    private String name;

    private String depends;

    private String description;

    private Map<String, Boolean> upstreamDependencies = new HashMap<>();

    private List<String> downStreamDependencies = new ArrayList<>();

    public void setDepends(String value) {
        depends = value;
        var nameAndVersions = value.split(",|\\|");
        for (var d : nameAndVersions) {
            var name = substringBefore(trimToEmpty(d), " ");
            upstreamDependencies.put(name, false);
        }
    }
}
