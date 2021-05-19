package com.app;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


public class WebCrawlerTest {

    private WebCrawler webCrawler = new WebCrawler();

    @Test
    public void grepUrl_withAbsoluteUrl() {
        try {
            String content = "<link rel=\"canonical\" href=\"https://monzo.com/\">\n";
            String givenStartDomain = "https://monzo.com/";
            String matchedUrl = webCrawler.grepUrl(givenStartDomain,content);
            Assert.assertEquals("https://monzo.com/", matchedUrl);
        } catch (Exception ie) {
            System.out.print(ie.getMessage());
        }
    }

    @Test
    public void grepUrl_withRelativeUrl() {
        try {
            String content = "<a href=\"/i/current-account/\" class=\"main-navigation__links__link\">Monzo Current Accounts</a>";
            String givenStartDomain = "https://monzo.com/";
            String matchedUrl = webCrawler.grepUrl(givenStartDomain,content);
            Assert.assertEquals("/i/current-account/", matchedUrl);
        } catch (Exception ie) {
            System.out.print(ie.getMessage());
        }
    }
}