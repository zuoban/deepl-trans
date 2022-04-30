package cn.leftsite.deepltrans.driver;

import cn.leftsite.deepltrans.entity.TranslateResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Chrome {
    private ChromeDriver driver;

    @PostConstruct
    public void init() {
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        //创建无Chrome无头参数
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1200");
        this.driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    public TranslateResult query(String q) throws InterruptedException {
        Assert.hasLength(q, "q must not be empty");
        driver.get("https://www.deepl.com/zh/translator#en/zh/" + q);

        int oneCharCount = 0;

        for (int i = 0; i < 30; i++) {
            String result = getTranslateResult(driver.getPageSource());
            if (result.length() ==1) {
                oneCharCount++;
            }

            if ((result.length() > 1 && !result.contains("[...]")) || oneCharCount > 2) {
                TranslateResult translateResult = new TranslateResult();
                translateResult.setAlternatives(getAlternatives(driver.getPageSource()));
                translateResult.setTarget(result);
                translateResult.setSrc(q);
                return translateResult;
            }
            Thread.sleep(200);
        }
        throw new RuntimeException("timeout");
    }

    private String getTranslateResult(String pageSource) {
        Document doc = Jsoup.parse(pageSource);
        return Objects.requireNonNull(doc.getElementById("target-dummydiv")).text();
    }

    private List<String> getAlternatives(String pageSource) {
        Document doc = Jsoup.parse(pageSource);
        return doc.getElementsByClass("lmt__translations_as_text__text_btn").stream().map(Element::text).collect(Collectors.toList());
    }


    @PreDestroy
    public void destroy() {
        driver.quit();
    }
}
