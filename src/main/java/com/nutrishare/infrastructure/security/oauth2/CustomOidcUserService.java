package com.nutrishare.infrastructure.security.oauth2;

import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import java.util.Map;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final AccountRepository accountRepository;

    {
        log.info("CustomOidcUserService Bean Initialized");
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOidcUserService.loadUser() called");
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OIDC Provider: {}", registrationId);

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        @SuppressWarnings("unchecked")
        OAuth2Attribute attributes = OAuth2Attribute.of(registrationId, userNameAttributeName,
                (Map<String, Object>) (Map) oidcUser.getAttributes());

        log.info("OIDC Attributes: {}", attributes.convertToMap());

        Account account = saveOrUpdate(attributes);
        log.info("Account saved/found: {}", account.getEmail());

        // For OIDC, we need to return an OidcUser.
        // We can create a CustomOidcUser or just return the default,
        // BUT we need the getName() to be the Account ID for JWT generation.
        return new CustomOidcUser(account, oidcUser);
    }

    private Account saveOrUpdate(OAuth2Attribute attributes) {
        Account account = accountRepository
                .findByProviderAndProviderUserId(attributes.getProvider(), attributes.getProviderUserId())
                .or(() -> accountRepository.findByEmail(attributes.getEmail()))
                .map(entity -> {
                    entity.updateSocialProfile(
                            attributes.getName(),
                            attributes.getProvider(),
                            attributes.getProviderUserId());
                    return entity;
                })
                .orElse(Account.createSocial(
                        attributes.getEmail(),
                        attributes.getName(),
                        attributes.getProvider(),
                        attributes.getProviderUserId()));

        return accountRepository.save(account);
    }
}
