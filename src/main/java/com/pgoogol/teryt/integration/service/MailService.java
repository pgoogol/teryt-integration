package com.pgoogol.teryt.integration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class MailService {

    public static final String SUBJECT = "Proces integracji TERYT";
    public static final String TEXT = "Wystąpił nieoczekiwany błąd podczas integracji danych z TERYT. Szczegóły błędów zamieszczone poniżej.";

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final List<String> recipients;

    public MailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail,
                        @Value("${notifications.recipients}") List<String> recipients
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.recipients = recipients;
    }


    public void sendMail(List<String> errors) throws MessagingException {
        MimeMessage message = this.mailSender.createMimeMessage();
        MimeMessageHelper msgHelper = new MimeMessageHelper(message, true);

        msgHelper.setFrom(this.fromEmail);
        msgHelper.setSubject(SUBJECT);

        String text = String.format("%s \n\n %s", TEXT, String.join("\n", errors));

        msgHelper.setText(text);
        msgHelper.setTo(this.recipients.toArray(String[]::new));

        this.mailSender.send(message);
    }

}
