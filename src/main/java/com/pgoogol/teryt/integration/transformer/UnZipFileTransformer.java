package com.pgoogol.teryt.integration.transformer;

import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import lombok.SneakyThrows;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class UnZipFileTransformer implements GenericTransformer<Message<List<AddressFiles>>, Message<List<AddressFiles>>> {

    private final Logger log = LogManager.getLogger(UnZipFileTransformer.class);

    @SneakyThrows(ZipException.class)
    @Override
    public Message<List<AddressFiles>> transform(Message<List<AddressFiles>> source) {

        for (AddressFiles files : source.getPayload()) {
            log.debug(String.format("start unzip version %s", files.getVerId()));
            for (File file : files.getFilesZip()) {
                String destPath = file.getPath().substring(0, file.getPath().length() - 4);
                ZipFile zipFile = new ZipFile(file);
                zipFile.extractAll(destPath);
                files.addFile(new File(destPath).listFiles());
            }
            log.debug(String.format("end unzip version %s", files.getVerId()));
        }
        return source;
    }

}
