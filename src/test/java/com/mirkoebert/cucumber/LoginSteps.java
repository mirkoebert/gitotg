package com.mirkoebert.cucumber;

import com.mirkoebert.config.LocaleOAuth2SuccessHandler;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    @Autowired
    private LocaleOAuth2SuccessHandler loginSuccessHandler;

    private MockHttpServletResponse response;

    @When("the user successfully logs in")
    public void theUserSuccessfullyLogsIn() throws Exception {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "gherkin-login-user", "name", "Gherkin Golfer"),
                "sub");
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        response = new MockHttpServletResponse();

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @Then("the cockpit page is loaded")
    public void theCockpitPageIsLoaded() {
        assertThat(response.getRedirectedUrl()).isEqualTo("/cockpit");
    }
}
