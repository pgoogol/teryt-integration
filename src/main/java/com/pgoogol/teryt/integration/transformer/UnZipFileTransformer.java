package com.pgoogol.teryt.integration.transformer;

import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
public class UnZipFileTransformer implements GenericTransformer<Message<List<AddressFiles>>, Message<List<AddressFiles>>> {

    private final Logger log = LogManager.getLogger(UnZipFileTransformer.class);

    @Override
    public Message<List<AddressFiles>> transform(Message<List<AddressFiles>> source) {
        for (AddressFiles files : source.getPayload()) {
            log.debug(String.format("START UNZIP VERSION %s TERYT ID %s", files.getVerId(), files.getTerytId()));
            for (File file : files.getFilesZip()) {
                if (Files.exists(file.toPath())) {
                    String[] splitPath = file.getPath().split("\\.");
                    int endIndex = file.getPath().length() - (splitPath[splitPath.length - 1].length() + 1);
                    String destPath = file.getPath().substring(0, endIndex);
                    ZipFile zipFile = new ZipFile(file);
                    try {
                        zipFile.extractAll(destPath);
                    } catch (ZipException e) {
                        files.addError(String.format("Unzip error in VERSION %s, TERYT ID %s", files.getVerId(), files.getTerytId()));
                        log.error("Extract exception", e);
                    } finally {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            log.error("Close ZipFile exception", e);
                        }
                    }
                    files.addFile(new File(destPath).listFiles());
                }
            }
            log.debug("END UNZIP VERSION");
        }
        return source;
    }

}
