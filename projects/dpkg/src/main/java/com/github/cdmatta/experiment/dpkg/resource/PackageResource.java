package com.github.cdmatta.experiment.dpkg.resource;

import com.github.cdmatta.experiment.dpkg.domain.DPackage;
import com.github.cdmatta.experiment.dpkg.util.StatusFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PackageResource {

    @Autowired
    private StatusFileReader statusFileReader;

    @GetMapping("/all")
    public Map<String, DPackage> allPackages() {
        return statusFileReader.getInstalledPackages();
    }
}