package com.nutrishare.infrastructure.security.oauth2;

import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AccountRepository accountRepository;

    {
        log.info("CustomOAuth2UserService Bean Initialized");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() called");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("Social Login Provider: {}", registrationId);

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2Attribute attributes = OAuth2Attribute.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        log.info("OAuth2 Attributes: {}", attributes.convertToMap());

        Account account = saveOrUpdate(attributes);
        log.info("Account saved/found: {}", account.getEmail());

        return new CustomOAuth2User(account, attributes.convertToMap());
    }

    private Account saveOrUpdate(OAuth2Attribute attributes) {
        Account account = accountRepository.findByEmail(attributes.getEmail())
                .map(entity -> {
                    entity.changeNickname(attributes.getName());
                    return entity;
                })
                .orElse(Account.create(attributes.getEmail(), attributes.getName()));

        return accountRepository.save(account);
    }
}
