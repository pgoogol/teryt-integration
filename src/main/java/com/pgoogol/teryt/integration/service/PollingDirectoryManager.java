package com.pgoogol.teryt.integration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class PollingDirectoryManager {

    private final String pollingDirectory;

    private File pollingDir;

    public PollingDirectoryManager(@Value("${teryt.file.polling.directory}") String pollingDirectory) {
        this.pollingDirectory = pollingDirectory;
    }

    @PostConstruct
    private void init() {
        File root = File.listRoots()[0];
        this.pollingDir = new File(root, pollingDirectory);
        if (!pollingDir.exists()) {
            pollingDir.mkdirs();
        }
    }

    public File getPollingDir() {
        return pollingDir;
    }

}
