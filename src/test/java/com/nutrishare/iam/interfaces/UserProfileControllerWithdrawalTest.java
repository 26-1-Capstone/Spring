package com.nutrishare.iam.interfaces;

import com.nutrishare.iam.application.MyPageQueryService;
import com.nutrishare.iam.application.UserProfileCommandService;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.infrastructure.configuration.SecurityConfig;
import com.nutrishare.infrastructure.security.jwt.JwtTokenProvider;
import com.nutrishare.infrastructure.security.oauth2.CustomOAuth2UserService;
import com.nutrishare.infrastructure.security.oauth2.CustomOidcUserService;
import com.nutrishare.infrastructure.security.oauth2.OAuth2SuccessHandler;
import com.nutrishare.review.application.ReviewCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@Import(SecurityConfig.class)
class UserProfileControllerWithdrawalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageQueryService myPageQueryService;

    @MockBean
    private UserProfileCommandService userProfileCommandService;

    @MockBean
    private ReviewCommandService reviewCommandService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private CustomOidcUserService customOidcUserService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void authenticatedDeleteWithdrawsCurrentUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me").with(user("123").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"))
                .andExpect(jsonPath("$.data.userId").value(123));

        verify(userProfileCommandService).withdraw(123L);
    }

    @Test
    void unauthenticatedDeleteIsRejected() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
