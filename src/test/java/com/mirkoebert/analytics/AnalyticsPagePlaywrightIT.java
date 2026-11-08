package com.mirkoebert.analytics;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AnalyticsPagePlaywrightIT.PlaywrightUserSecurity.class)
class AnalyticsPagePlaywrightIT {

    static final String USER_ID = "analytics-page-user";

    @LocalServerPort
    private int port;

    @Autowired
    private HcpRepository hcpRepository;

    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeEach
    void startBrowserAndSeedHandicap() {
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(USER_ID)
                .date(LocalDate.now())
                .hcp(25.0)
                .build());
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @AfterEach
    void stopBrowserAndClearHandicap() {
        hcpRepository.findByUserId(USER_ID).forEach(hcpRepository::delete);
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void analyticsPage_listsWeaknessesAndLinksFromTheGoalMenu() {
        page.navigate("http://localhost:" + port + "/about");
        page.locator("ul.dropdown-menu a[href='/analytics']").evaluate("el => el.click()");

        assertThat(page).hasTitle("Gitotg: Analytics");
        assertThat(page.locator("h1")).hasText("Analytics");
        assertThat(page.locator("ul.dropdown-menu a[href='/analytics']")).hasCount(1);
        assertThat(page.locator("#weaknesses")).containsText("course management");
        assertThat(page.locator("#weaknesses")).containsText("mental game");
        assertThat(page.locator("#weaknesses")).containsText("playing");
        assertThat(page.locator("#weaknesses")).containsText("short game");
    }

    /**
     * Opens /analytics for this test without the Google or GitHub login redirect.
     * The signed-in user is the same id the test seeds.
     */
    @TestConfiguration
    static class PlaywrightUserSecurity {

        @Bean
        @Order(0)
        SecurityFilterChain analyticsPlaywrightChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/analytics")
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .addFilterAfter(new SignedInUserFilter(), SecurityContextHolderFilter.class);
            return http.build();
        }
    }

    static final class SignedInUserFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            OAuth2User principal = new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    Map.of("sub", USER_ID, "name", "Analytics Tester"),
                    "sub");
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new OAuth2AuthenticationToken(
                    principal, principal.getAuthorities(), "playwright"));
            SecurityContextHolder.setContext(context);
            try {
                filterChain.doFilter(request, response);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}
