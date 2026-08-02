package com.example.commitmate.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "phone_verification_tb", indexes = @Index(name = "idx_phone_verification_phone", columnList = "phone_number"))
@NoArgsConstructor
public class PhoneVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 11)
    private String phoneNumber;
    @Column(nullable = false, length = 64)
    private String codeHash;
    @Column(length = 64)
    private String verificationTokenHash;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private LocalDateTime lastSentAt;
    @Column(nullable = false)
    private int attempts;
    private LocalDateTime verifiedAt;
    private LocalDateTime consumedAt;

    public PhoneVerification(String phoneNumber, String codeHash) {
        this.phoneNumber = phoneNumber;
        this.codeHash = codeHash;
        this.expiresAt = LocalDateTime.now().plusMinutes(3);
        this.lastSentAt = LocalDateTime.now();
    }

    public boolean isCodeAvailable() {
        return verifiedAt == null && attempts < 5 && expiresAt.isAfter(LocalDateTime.now());
    }

    public void failAttempt() { attempts++; }

    public void verify(String verificationTokenHash) {
        this.verifiedAt = LocalDateTime.now();
        this.verificationTokenHash = verificationTokenHash;
    }

    public boolean canConsume() {
        return verifiedAt != null && consumedAt == null && verifiedAt.plusMinutes(10).isAfter(LocalDateTime.now());
    }

    public void consume() { this.consumedAt = LocalDateTime.now(); }
}
