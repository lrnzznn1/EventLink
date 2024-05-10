package com.example.eventlink;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public void Send(String emailReceiver, String oggetto, String testo) {
        String emailSender = "EventLinkAuth@gmail.com";

        // Configurazione delle proprietà per la connessione al server SMTP
        Properties props = new Properties();
        putIfMissing(props, "mail.smtp.host", "smtp.office365.com");
        putIfMissing(props, "mail.smtp.port", "587");
        putIfMissing(props, "mail.smtp.auth", "true");
        putIfMissing(props, "mail.smtp.starttls.enable", "true");

        // Autenticazione al server SMTP
        String pw = "4iasi2903jfinosq230vASonfeq!"; // Imposta la password dell'account email
        javax.mail.Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("eventlinkauth@gmail.com", pw);
            }
        });

        // Creazione del messaggio
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(emailSender));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(emailReceiver));
            mimeMessage.setSubject(oggetto);
            //mimeMessage.setText(testo);

            // Invio dell'email
            Transport.send(mimeMessage);
        } catch (MessagingException ex) {
        }
    }

    private static void putIfMissing(Properties props, String key, String value) {
        if (!props.containsKey(key)) {
            props.put(key, value);
        }
    }
}
