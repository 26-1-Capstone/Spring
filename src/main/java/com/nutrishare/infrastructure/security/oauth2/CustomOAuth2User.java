package com.nutrishare.infrastructure.security.oauth2;

import com.nutrishare.iam.domain.Account;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Account account;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Account account, Map<String, Object> attributes) {
        this.account = account;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
    }

    @Override
    public String getName() {
        // Return Account ID as the identifier
        return String.valueOf(account.getId());
    }

    public String getEmail() {
        return account.getEmail();
    }
}
