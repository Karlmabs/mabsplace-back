package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceAccountRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ServiceAccountRepository serviceAccountRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender, ServiceAccountRepository serviceAccountRepository) {
        this.mailSender = mailSender;
        this.serviceAccountRepository = serviceAccountRepository;
    }

    @Async
    public void sendEmail(EmailRequest request) throws MessagingException {
        logger.info("Preparing to send email to: {}, subject: {}", request.getTo(), request.getSubject());

        MimeMessage message = mailSender.createMimeMessage();
        String defaultFromEmail = "noreply@mabsplace.com";
        message.setFrom(new InternetAddress(request.getFromEmail() != null ? request.getFromEmail() : defaultFromEmail));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(request.getTo()));

        if (request.getCc() != null) {
            message.setRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(String.join(",", request.getCc())));
        }
        if (request.getBcc() != null) {
            message.setRecipients(MimeMessage.RecipientType.BCC, InternetAddress.parse(String.join(",", request.getBcc())));
        }

        message.setSubject(request.getSubject());

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
                        
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        
                        body {
                            font-family: 'Poppins', Arial, sans-serif;
                            background-color: #f4f7fa;
                            color: #333;
                            line-height: 1.6;
                        }
                        
                        .email-wrapper {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                        }
                        
                        .email-header {
                            background: linear-gradient(135deg, #1a73e8, #0d47a1);
                            padding: 30px 20px;
                            text-align: center;
                        }
                        
                        .logo {
                            margin-bottom: 15px;
                        }
                        
                        .logo img {
                            height: 40px;
                        }
                        
                        .header-title {
                            color: #ffffff;
                            font-size: 24px;
                            font-weight: 600;
                            margin: 0;
                        }
                        
                        .email-body {
                            padding: 40px 30px;
                            color: #4a4a4a;
                        }
                        
                        .greeting {
                            font-size: 18px;
                            font-weight: 500;
                            margin-bottom: 20px;
                        }
                        
                        .content-block {
                            margin-bottom: 25px;
                        }
                        
                        .content-block p {
                            margin-bottom: 15px;
                            font-size: 16px;
                        }
                        
                        .highlight-box {
                            background-color: #f0f7ff;
                            border-left: 4px solid #1a73e8;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        
                        .cta-button {
                            display: inline-block;
                            background-color: #1a73e8;
                            color: #ffffff !important;
                            text-decoration: none;
                            padding: 12px 30px;
                            border-radius: 6px;
                            font-weight: 500;
                            margin: 20px 0;
                            text-align: center;
                            transition: background-color 0.3s ease;
                        }
                        
                        .cta-button:hover {
                            background-color: #0d47a1;
                        }
                        
                        .divider {
                            height: 1px;
                            background-color: #e0e0e0;
                            margin: 30px 0;
                        }
                        
                        .email-footer {
                            background-color: #f8f9fa;
                            padding: 25px 30px;
                            text-align: center;
                            color: #757575;
                            font-size: 14px;
                            border-top: 1px solid #e0e0e0;
                        }
                        
                        .social-links {
                            margin: 15px 0;
                        }
                        
                        .social-links a {
                            display: inline-block;
                            margin: 0 8px;
                            color: #1a73e8;
                            text-decoration: none;
                        }
                        
                        .footer-links {
                            margin: 15px 0;
                        }
                        
                        .footer-links a {
                            color: #1a73e8;
                            text-decoration: none;
                            margin: 0 10px;
                        }
                        
                        .copyright {
                            margin-top: 15px;
                            font-size: 13px;
                        }
                        
                        @media only screen and (max-width: 600px) {
                            .email-wrapper {
                                border-radius: 0;
                            }
                            
                            .email-body {
                                padding: 30px 20px;
                            }
                            
                            .email-footer {
                                padding: 20px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-wrapper">
                        <div class="email-header">
                            <div class="logo">
                                <!-- You can add your logo here -->
                                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
                            </div>
                            <h1 class="header-title">%s</h1>
                        </div>
                        
                        <div class="email-body">
                            <div class="greeting">Hello,</div>
                            <div class="content-block">
                                %s
                            </div>
                        </div>
                        
                        <div class="email-footer">
                            <div class="social-links">
                                <!-- Social media links -->
                                <a href="https://facebook.com/mabsplace">Facebook</a>
                                <a href="https://instagram.com/mabs.place">Instagram</a>
                            </div>
                            
                            <div class="footer-links">
                                <a href="https://mabsplace.com/privacy">Privacy Policy</a>
                                <a href="https://mabsplace.com/terms">Terms of Service</a>
                            </div>
                            
                            <div class="copyright">
                                %s
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                request.getSubject(),
                request.getHeaderText(),
                request.getBody(),
                request.getFooterText() != null ? request.getFooterText() : "© " + Year.now().getValue() + " " + request.getCompanyName() + ". All rights reserved."
        );

        message.setContent(htmlContent, "text/html; charset=utf-8");

        mailSender.send(message);
        logger.info("Email sent successfully to: {}", request.getTo());
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
    public void notifyUpcomingPayments() throws MessagingException {
        logger.info("Scheduled task started: Checking for upcoming payments.");

        Date today = new Date();
        List<ServiceAccount> serviceAccounts = serviceAccountRepository.findAll();
        logger.info("Found {} service accounts to check.", serviceAccounts.size());

        for (ServiceAccount serviceAccount : serviceAccounts) {
            if (serviceAccount.getPaymentDate() != null) {
                long diffInMillies = Math.abs(serviceAccount.getPaymentDate().getTime() - today.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                if (diff <= 3) {
                    logger.info("Sending payment reminder for service account ID: {}, due in {} days.", serviceAccount.getId(), diff);

                    EmailRequest request = EmailRequest.builder()
                            .to("maboukarl2@gmail.com")
                            .cc(List.of("yvanos510@gmail.com"))
                            .subject("Upcoming Subscription Payment Reminder")
                            .headerText("Upcoming Subscription Payment Reminder")
                            .body("<p>This is a reminder that your subscription for " + serviceAccount.getMyService().getName() + " on the account " + serviceAccount.getLogin() + " is due for renewal on " + serviceAccount.getPaymentDate() + ".\n\nPlease make sure to renew your subscription to avoid any interruptions.\n\nThank you.</p>")
                            .companyName("MabsPlace")
                            .build();

                    sendEmail(request);
                    logger.info("Payment reminder sent for service account ID: {}", serviceAccount.getId());
                }
            }
        }

        logger.info("Scheduled task completed: Upcoming payments check.");
    }

}
