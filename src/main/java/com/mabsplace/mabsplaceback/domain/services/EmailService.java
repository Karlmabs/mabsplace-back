package com.mabsplace.mabsplaceback.domain.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setFrom(new InternetAddress("sender@example.com"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>2FA Code</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f9f9f9;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            width: 100%;\n" +
                "            max-width: 600px;\n" +
                "            margin: 50px auto;\n" +
                "            background-color: #ffffff;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background-color: #007BFF;\n" +
                "            color: #ffffff;\n" +
                "            text-align: center;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .header h1 {\n" +
                "            margin: 0;\n" +
                "            font-size: 24px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            padding: 30px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .content p {\n" +
                "            font-size: 18px;\n" +
                "            line-height: 1.6;\n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "        .code {\n" +
                "            display: inline-block;\n" +
                "            background-color: #f1f1f1;\n" +
                "            color: #333333;\n" +
                "            font-size: 24px;\n" +
                "            padding: 10px 20px;\n" +
                "            border-radius: 5px;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            background-color: #f1f1f1;\n" +
                "            color: #888888;\n" +
                "            text-align: center;\n" +
                "            padding: 20px;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Two-Factor Authentication</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hello dear customer,</p>\n" +
                "            <p>To complete your login, please use the following 2FA code:</p>\n" +
                "            <div class=\"code\">[ "+body+" ]</div>\n" +
                "            <p>If you did not request this code, please secure your account immediately.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2024 Your Company. All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
        message.setContent(htmlContent, "text/html; charset=utf-8");

        mailSender.send(message);
    }

}