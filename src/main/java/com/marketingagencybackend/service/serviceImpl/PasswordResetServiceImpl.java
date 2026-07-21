package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.entity.Admin;
import com.marketingagencybackend.entity.Client;
import com.marketingagencybackend.entity.PasswordResetOtp;
import com.marketingagencybackend.exception.ResourceNotFoundException;
import com.marketingagencybackend.repository.AdminRepository;
import com.marketingagencybackend.repository.ClientRepository;
import com.marketingagencybackend.repository.PasswordResetOtpRepository;
import com.marketingagencybackend.service.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final AdminRepository adminRepository;
    private final ClientRepository clientRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void forgotPassword(String email) {

        // Find the user's name from either Admin or Client table
        String userName = findUserName(email);

        // Generate 6-digit OTP
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        // Save OTP record
        PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .build();

        otpRepository.save(otpEntity);

        // Send OTP via email
        sendOtpEmail(email, otp, userName);

        log.info("OTP sent to email: {}", email);
    }

    @Override
    public void verifyOtp(String email, String otp) {

        PasswordResetOtp otpEntity = otpRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No OTP found for email: " + email
                ));

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!otpEntity.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP.");
        }

        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        log.info("OTP verified for email: {}", email);
    }

    @Override
    public void resetPassword(String email, String newPassword) {

        PasswordResetOtp otpEntity = otpRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No OTP found for email: " + email
                ));

        if (!otpEntity.getVerified()) {
            throw new IllegalArgumentException("OTP is not verified. Please verify OTP first.");
        }

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        // Update password in the correct table
        String encodedPassword = passwordEncoder.encode(newPassword);

        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            admin.get().setPassword(encodedPassword);
            adminRepository.save(admin.get());
            log.info("Password reset successfully for admin: {}", email);
        } else {
            Optional<Client> client = clientRepository.findByEmail(email);
            if (client.isPresent()) {
                client.get().setPassword(encodedPassword);
                clientRepository.save(client.get());
                log.info("Password reset successfully for client: {}", email);
            } else {
                throw new ResourceNotFoundException(
                        "No account found with email: " + email
                );
            }
        }

        // Cleanup OTP records for this email
        otpRepository.deleteByEmail(email);
    }

    private String findUserName(String email) {
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return admin.get().getFullName();
        }

        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return client.get().getOwnerName();
        }

        throw new ResourceNotFoundException("No account found with email: " + email);
    }

    private void sendOtpEmail(String toEmail, String otp, String userName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("support@caryanam.com");
            helper.setTo(toEmail);
            helper.setSubject("Marketing Agency – Password Reset Request");
            helper.setText(buildOtpEmailHtml(userName, otp), true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email. Please check email credentials or try again later.");
        }
    }

    private String buildOtpEmailHtml(String userName, String otp) {
        String displayName = (userName != null && !userName.isBlank()) ? userName : "User";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Marketing Agency</title>
                </head>

                <body style="margin:0;padding:0;background:#FDF8EC;font-family:'Segoe UI',Arial,sans-serif;color:#202124;">

                <table width="100%" cellpadding="0" cellspacing="0">
                <tr>
                <td align="center" style="padding:40px 20px;">

                <table width="600" cellpadding="0" cellspacing="0"
                style="background:#FFFFFF;border-radius:18px;box-shadow:0 12px 35px rgba(0,0,0,0.08);overflow:hidden;">

                <!-- Header -->
                <tr>
                <td style="padding:35px 40px 25px;background:linear-gradient(135deg, #2ECC71 0%, #0D9488 100%);">

                <h2 style="margin:0;font-size:28px;font-weight:700;color:#FFFFFF;">
                Marketing Agency
                </h2>

                <p style="margin-top:6px;font-size:15px;color:#E8FFF3;">
                Secure Account Verification
                </p>

                </td>
                </tr>

                <!-- Content -->
                <tr>
                <td style="padding:40px;">

                <p style="margin:0;font-size:16px;line-height:28px;color:#202124;">
                Hello <strong>{{NAME}}</strong>,
                </p>

                <p style="margin-top:20px;font-size:16px;line-height:28px;color:#3c4043;">
                We received a request to verify your email address for your
                <strong>Marketing Agency</strong> account.
                </p>

                <p style="margin-top:20px;font-size:16px;line-height:28px;color:#3c4043;">
                Use the verification code below to continue.
                </p>

                <!-- OTP -->

                <table width="100%" cellpadding="0" cellspacing="0">
                <tr>
                <td align="center">

                <div style="
                display:inline-block;
                margin:30px 0;
                padding:18px 45px;
                font-size:42px;
                font-weight:700;
                letter-spacing:10px;
                background:linear-gradient(90deg, #22C55E 0%, #0F9D8A 100%);
                border-radius:14px;
                color:#FFFFFF;
                box-shadow:0 4px 15px rgba(15,157,138,0.25);
                ">

                {{OTP}}

                </div>

                </td>
                </tr>
                </table>

                <p style="font-size:15px;line-height:26px;color:#5f6368;">
                This verification code will expire in
                <strong style="color:#0F766E;">5 minutes</strong>.
                </p>

                <p style="font-size:15px;line-height:26px;color:#5f6368;">
                For your security, never share this code with anyone.
                Marketing Agency will never ask for your OTP by email,
                phone call, or message.
                </p>

                <p style="font-size:15px;line-height:26px;color:#5f6368;">
                If you didn't request this verification,
                you can safely ignore this email.
                </p>

                </td>
                </tr>

                <!-- Footer -->
                <tr>
                <td style="padding:30px 40px;background:#FAF8F5;border-top:1px solid #ececec;">

                <p style="margin:0;font-size:13px;color:#80868b;">
                This is an automated email. Please do not reply.
                </p>

                <p style="margin-top:12px;font-size:13px;color:#80868b;">
                © 2026 Marketing Agency. All rights reserved.
                </p>

                </td>
                </tr>

                </table>

                </td>
                </tr>
                </table>

                </body>
                </html>
                """
                .replace("{{NAME}}", displayName)
                .replace("{{OTP}}", otp);
    }
}
