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
                        :root {
                            --primary-color: #1a73e8;
                            --secondary-color: #f8f9fa;
                            --text-color: #202124;
                            --muted-color: #5f6368;
                        }
                        
                        body {
                            font-family: 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                            background-color: #ffffff;
                            margin: 0;
                            padding: 0;
                            color: var(--text-color);
                            line-height: 1.6;
                        }
                        
                        .container {
                            width: 100%%;
                            max-width: 600px;
                            margin: 40px auto;
                            background-color: #ffffff;
                            border: 1px solid #dadce0;
                            border-radius: 8px;
                            overflow: hidden;
                        }
                        
                        .header {
                            background-color: var(--primary-color);
                            color: #ffffff;
                            text-align: center;
                            padding: 24px 20px;
                        }
                        
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                            font-weight: 500;
                        }
                        
                        .content {
                            padding: 32px 24px;
                            background-color: #ffffff;
                        }
                        
                        .content p {
                            font-size: 16px;
                            margin: 16px 0;
                            color: var(--text-color);
                        }
                        
                        .message-box {
                            background-color: var(--secondary-color);
                            border-radius: 4px;
                            padding: 16px;
                            margin: 24px 0;
                        }
                        
                        .button {
                            display: inline-block;
                            background-color: var(--primary-color);
                            color: #ffffff;
                            text-decoration: none;
                            padding: 12px 24px;
                            border-radius: 4px;
                            margin: 16px 0;
                            font-weight: 500;
                        }
                        
                        .footer {
                            background-color: var(--secondary-color);
                            color: var(--muted-color);
                            text-align: center;
                            padding: 20px;
                            font-size: 14px;
                            border-top: 1px solid #dadce0;
                        }
                        
                        @media only screen and (max-width: 600px) {
                            .container {
                                margin: 0;
                                border-radius: 0;
                                border: none;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            %s
                        </div>
                        <div class="footer">
                            <p>%s</p>
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

    /**
     * Send package subscription confirmation email
     */
    @Async
    public void sendPackageSubscriptionConfirmationEmail(String email, String packageName, Date endDate) 
            throws MessagingException {
        logger.info("Sending package subscription confirmation email to: {}", email);
        
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Package Subscription Confirmation")
                .headerText("Package Subscription Confirmation")
                .body("<p>Thank you for subscribing to the " + packageName + " package. " +
                      "Your subscription is now active and will expire on " + endDate + ".</p>" +
                      "<p>This package includes multiple streaming services, all accessible with a single subscription.</p>" +
                      "<p>Enjoy your premium entertainment experience!</p>")
                .companyName("MabsPlace")
                .build();
        
        sendEmail(request);
        logger.info("Package subscription confirmation email sent to: {}", email);
    }
    
    /**
     * Send package subscription cancellation email
     */
    @Async
    public void sendPackageSubscriptionCancellationEmail(String email, String packageName) 
            throws MessagingException {
        logger.info("Sending package subscription cancellation email to: {}", email);
        
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Package Subscription Cancelled")
                .headerText("Package Subscription Cancelled")
                .body("<p>Your subscription to the " + packageName + " package has been cancelled. " +
                      "You will no longer have access to the services included in this package.</p>" +
                      "<p>If you cancelled this by mistake, please contact our support team or resubscribe " +
                      "from your account dashboard.</p>" +
                      "<p>We hope to see you again soon!</p>")
                .companyName("MabsPlace")
                .build();
        
        sendEmail(request);
        logger.info("Package subscription cancellation email sent to: {}", email);
    }
    
    /**
     * Send package subscription renewal email
     */
    @Async
    public void sendPackageSubscriptionRenewalEmail(String email, String packageName, Date newEndDate) 
            throws MessagingException {
        logger.info("Sending package subscription renewal email to: {}", email);
        
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Package Subscription Renewed")
                .headerText("Package Subscription Renewed")
                .body("<p>Good news! Your subscription to the " + packageName + " package has been renewed. " +
                      "Your subscription will now expire on " + newEndDate + ".</p>" +
                      "<p>Thank you for choosing to continue with our service. " +
                      "We appreciate your business and hope you continue to enjoy your entertainment experience.</p>")
                .companyName("MabsPlace")
                .build();
        
        sendEmail(request);
        logger.info("Package subscription renewal email sent to: {}", email);
    }
    
    /**
     * Send package subscription renewal failed email
     */
    @Async
    public void sendPackageSubscriptionRenewalFailedEmail(String email, String packageName, int attempts) 
            throws MessagingException {
        logger.info("Sending package subscription renewal failed email to: {}", email);
        
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Package Subscription Renewal Failed")
                .headerText("Package Subscription Renewal Failed")
                .body("<p>We were unable to renew your subscription to the " + packageName + 
                      " package due to insufficient funds or payment issues.</p>" +
                      "<p>This is attempt " + attempts + " out of 4. After 4 failed attempts, " +
                      "your subscription will be cancelled.</p>" +
                      "<p>Please update your payment information or add funds to your wallet " +
                      "to continue enjoying our services.</p>")
                .companyName("MabsPlace")
                .build();
        
        sendEmail(request);
        logger.info("Package subscription renewal failed email sent to: {}", email);
    }
    
    /**
     * Send package subscription expired email
     */
    @Async
    public void sendPackageSubscriptionExpiredEmail(String email, String packageName) 
            throws MessagingException {
        logger.info("Sending package subscription expired email to: {}", email);
        
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Package Subscription Expired")
                .headerText("Package Subscription Expired")
                .body("<p>Your subscription to the " + packageName + " package has expired. " +
                      "You no longer have access to the services included in this package.</p>" +
                      "<p>To restore access, please renew your subscription from your account dashboard.</p>" +
                      "<p>We hope to see you again soon!</p>")
                .companyName("MabsPlace")
                .build();
        
        sendEmail(request);
        logger.info("Package subscription expired email sent to: {}", email);
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
