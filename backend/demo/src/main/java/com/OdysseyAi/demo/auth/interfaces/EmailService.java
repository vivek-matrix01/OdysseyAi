package com.OdysseyAi.demo.auth.interfaces;

public interface EmailService {

    void sendVerificationEmail(
            String recipientEmail,
            String verificationLink
    );
    void sendPasswordResetEmail(
            String recipientEmail,
            String resetLink
    );
}