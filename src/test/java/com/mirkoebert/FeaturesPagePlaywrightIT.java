package com.mirkoebert;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeaturesPagePlaywrightIT {
        @LocalServerPort
        private int port;
        private Playwright playwright;
        private Browser browser;
        private Page page;

        @BeforeEach
        void startBrowser() {
                playwright = Playwright.create();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                page = browser.newPage();
        }

        @AfterEach
        void stopBrowser() {
                if (browser != null) {
                        browser.close();
                }
                if (playwright != null) {
                        playwright.close();
                }
        }

        @Test
        void featuresPage_showsTitleAndMainFeatures() {
                page.navigate("http://localhost:" + port + "/");

                assertThat(page).hasTitle("GITOTG");
                assertThat(page.locator("h1")).hasText("Features");
                assertThat(page.locator(".card a[href='/short-game-index']")).isVisible();
                assertThat(page.locator(".card a[href='/handicap/input']")).isVisible();
                assertThat(page.locator(".card a[href='/goal/break100']")).isVisible();
                assertThat(page.locator(".card a[href='/gmetric']")).isVisible();
                assertThat(page.locator(".card a[href='/golfcourse']")).isVisible();
                assertThat(page.locator(".card a[href='/cockpit']")).isVisible();

                page.navigate("http://localhost:" + port + "/?lang=de");
                assertThat(page).hasTitle("GITOTG");
                assertThat(page.locator("h1")).hasText("Funktionen");
        }
}
