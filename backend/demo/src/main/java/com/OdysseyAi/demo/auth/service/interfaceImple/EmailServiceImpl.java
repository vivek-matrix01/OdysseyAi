package com.OdysseyAi.demo.auth.service.interfaceImple;

import com.OdysseyAi.demo.auth.configuration.EmailProperties;

import com.OdysseyAi.demo.auth.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public void sendVerificationEmail(
            String recipientEmail,
            String verificationLink
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(emailProperties.getFrom());
        message.setTo(recipientEmail);
        message.setSubject("Verify your OdysseyAI email");
        message.setText(
                """
                Welcome to OdysseyAI!

                Please verify your email address using the link below:

                %s

                This verification link will expire in 24 hours.

                If you did not create an OdysseyAI account, you can ignore this email.
                """.formatted(verificationLink)
        );

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(
            String recipientEmail,
            String resetLink
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(emailProperties.getFrom());
        message.setTo(recipientEmail);
        message.setSubject("Reset your OdysseyAI password");
        message.setText(
                """
                We received a request to reset your OdysseyAI password.

                Use the link below to reset your password:

                %s

                This link will expire in 15 minutes.

                If you did not request a password reset, you can safely ignore this email.
                """.formatted(resetLink)
        );

        mailSender.send(message);
    }
}