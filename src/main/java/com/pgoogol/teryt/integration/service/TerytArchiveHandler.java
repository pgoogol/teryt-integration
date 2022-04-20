package com.pgoogol.teryt.integration.service;

import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
public class TerytArchiveHandler implements MessageHandler {

    private final Logger log = LogManager.getLogger(TerytArchiveHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        List<AddressFiles> filesList = (List<AddressFiles>) message.getPayload();
        log.debug("Teryt files started archived");
        Stream.concat(
                filesList.stream().flatMap(files -> files.getFiles().stream()),
                filesList.stream().flatMap(files -> files.getFilesZip().stream())
        ).forEach(file -> {
            try {
                Files.deleteIfExists(Paths.get(file.getPath()));
            } catch (IOException e) {
                log.error(String.format("Unable to remove file: %s", message), e.getMessage(), e);
            }
        });
        log.debug("Teryt files archived successfully");
    }

}
