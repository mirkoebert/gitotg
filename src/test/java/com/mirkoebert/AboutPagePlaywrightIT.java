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
class AboutPagePlaywrightIT {

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
    void aboutPage_showsHeadingAuthorAndGitHubLink() {
        page.navigate("http://localhost:" + port + "/about");

        assertThat(page).hasTitle("Gitotg: About");
        assertThat(page.locator("h1")).hasText("About Gitotg");
        assertThat(page.locator("a[href='https://github.com/mirkoebert/gitotg']")).isVisible();
        assertThat(page.getByText("Author: Mirko Ebert")).isVisible();
    }
}
