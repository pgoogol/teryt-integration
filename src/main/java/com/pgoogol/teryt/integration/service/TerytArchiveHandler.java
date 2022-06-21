package com.pgoogol.teryt.integration.service;

import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TerytArchiveHandler implements MessageHandler {

    private final Logger log = LogManager.getLogger(TerytArchiveHandler.class);

    private final MailService mailService;

    public TerytArchiveHandler(MailService mailService) {
        this.mailService = mailService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message<?> message) {
        List<AddressFiles> filesList = (List<AddressFiles>) message.getPayload();
        sendMail(filesList);
        archiveFiles(message, filesList);
    }

    private void archiveFiles(Message<?> message, List<AddressFiles> filesList) {
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
    }

    private void sendMail(List<AddressFiles> filesList) {
        List<String> errorsList = filesList
                .stream()
                .map(AddressFiles::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        try {
            mailService.sendMail(errorsList);
        } catch (MessagingException e) {
            log.error("Unable to send email", e);
        }
    }

}
