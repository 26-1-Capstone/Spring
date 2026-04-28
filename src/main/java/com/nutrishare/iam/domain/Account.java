package com.nutrishare.iam.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(
        name = "uk_accounts_provider_user_id",
        columnNames = {"provider", "providerUserId"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 30)
    private String provider;

    @Column(length = 100)
    private String providerUserId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Account(String email, String nickname, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public static Account create(String email, String nickname) {
        return new Account(email, nickname, Role.USER);
    }

    public static Account createSocial(String email, String nickname, String provider, String providerUserId) {
        Account account = create(email, nickname);
        account.updateSocialProfile(nickname, provider, providerUserId);
        return account;
    }

    public void changeNickname(String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new IllegalArgumentException("Nickname cannot be empty");
        }
        this.nickname = newNickname;
    }

    public void updateSocialProfile(String nickname, String provider, String providerUserId) {
        changeNickname(nickname);
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider cannot be empty");
        }
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Provider user ID cannot be empty");
        }
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public boolean isKakaoAccount() {
        return "kakao".equals(provider);
    }

    @Embedded
    private Address address;

    public void updateProfile(String nickname, Address address) {
        changeNickname(nickname);
        this.address = address;
    }

    public enum Role {
        USER, ADMIN
    }
}
