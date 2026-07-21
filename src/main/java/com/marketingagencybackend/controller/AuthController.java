package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.ForgotPasswordRequestDTO;
import com.marketingagencybackend.dto.LoginRequest;
import com.marketingagencybackend.dto.ResetPasswordRequestDTO;
import com.marketingagencybackend.dto.TokenResponse;
import com.marketingagencybackend.dto.VerifyOtpRequestDTO;
import com.marketingagencybackend.security.CustomUserDetails;
import com.marketingagencybackend.security.JwtService;
import com.marketingagencybackend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    @Operation(summary = "Login API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "403", description = "Account disabled or blocked"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDTO<TokenResponse>> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticate(loginRequest);
        System.out.println(authentication);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(principal);

        TokenResponse tokenResponse = TokenResponse.of(
                accessToken,
                jwtService.getTokenExpiration(),
                principal.getUsername(),
                principal.getRole().name()
        );

        log.info("Successful login for id={}, role={}", principal.getId(), principal.getRole());

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "Login Successfully",
                        tokenResponse
                )
        );

    }


    //Forgot Password - Send OTP
    @PostMapping("/forgot-password")
    @Operation(summary = "Send OTP to email for password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "404", description = "Email not found")
    })
    public ResponseEntity<ApiResponseDTO<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {

        log.info("Forgot password request for email: {}", request.email());
        passwordResetService.forgotPassword(request.email());

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "OTP sent to your email successfully.",
                        null
                )
        );
    }

    //Verify OTP
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP for password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
            @ApiResponse(responseCode = "404", description = "No OTP found")
    })
    public ResponseEntity<ApiResponseDTO<Object>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDTO request) {

        log.info("OTP verification request for email: {}", request.email());
        passwordResetService.verifyOtp(request.email(), request.otp());

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "OTP verified successfully.",
                        null
                )
        );
    }

    //Reset Password
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using verified OTP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "OTP not verified or invalid"),
            @ApiResponse(responseCode = "404", description = "No OTP or account found")
    })
    public ResponseEntity<ApiResponseDTO<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {

        log.info("Password reset request for email: {}", request.email());
        passwordResetService.resetPassword(
                request.email(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "Password reset successfully.",
                        null
                )
        );
    }


    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            log.info("Failed login attempt for email={}", loginRequest.email());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
