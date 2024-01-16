package com.mabsplace.mabsplaceback.security.listeners;

import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.security.events.OnRegistrationCompleteEvent;
import com.mabsplace.mabsplaceback.security.services.UserServiceSec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    private UserServiceSec service;

    @Autowired
    private MessageSource messages;

    /*@Autowired
    private JavaMailSender mailSender;*/

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl
                = event.getAppUrl() + "/auth/registrationConfirm?token=" + token;
        String message = "You have been registered successfully, now please click on this link to activate your account";

        /*SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("noreply@example.com");
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + "http://localhost:8080" + confirmationUrl);
        mailSender.send(email);*/
    }
}
