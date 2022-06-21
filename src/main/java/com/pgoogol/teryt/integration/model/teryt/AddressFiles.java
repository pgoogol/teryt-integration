package com.pgoogol.teryt.integration.model.teryt;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class AddressFiles {

    private List<File> files = new LinkedList<>();
    private List<File> filesZip = new LinkedList<>();
    private String verId;
    private String terytId;

    private List<String> errors = Collections.emptyList();

    public void addZipFile(File file) {
        filesZip.add(file);
    }

    public void addFile(File file) {
        files.add(file);
    }

    public void addFile(List<File> files) {
        this.files.addAll(files);
    }

    public void addFile(File[] files) {
        this.files.addAll(List.of(files));
    }

}
