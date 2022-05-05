package cn.leftsite.deepltrans.driver;

import cn.hutool.core.lang.Validator;
import cn.leftsite.deepltrans.constant.RedisKeyConst;
import cn.leftsite.deepltrans.entity.TranslateResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public Chrome(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        //创建无Chrome无头参数
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1200");
        this.driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    public TranslateResult query(String q) throws InterruptedException, JsonProcessingException {
        q = q.trim();
        Assert.hasLength(q, "q must not be empty");

        boolean isSingleWord = !q.contains(" ");
        if (isSingleWord) {
            // 单词，先判断缓存中有没有
            String key = String.format(RedisKeyConst.DEEPL_TEMPLATE, q);
            String cache = stringRedisTemplate.opsForValue().get(key);
            if (Validator.isNotEmpty(cache)) {
                log.info("cache hit: {}", q);
                return objectMapper.readValue(cache, TranslateResult.class);
            }
        }

        driver.get("https://www.deepl.com/zh/translator#en/zh/" + q);

        Thread.sleep(1000);
        for (int i = 0; i < 50; i++) {
            TranslateResult result = getTranslateResult(driver.getPageSource());
            if (Validator.hasChinese(String.join("\n", result.getAlternatives()))) {
                Thread.sleep(200);
                // 如果是单词，则缓存
                if (isSingleWord) {
                    String key = String.format(RedisKeyConst.DEEPL_TEMPLATE, q);
                    stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result));
                }
                return getTranslateResult(driver.getPageSource());
            }
            Thread.sleep(200);
        }
        throw new RuntimeException("query timeout");
    }

    private TranslateResult getTranslateResult(String pageSource) {
        Document doc = Jsoup.parse(pageSource);
        TranslateResult result = new TranslateResult();

        String target = Objects.requireNonNull(doc.getElementById("target-dummydiv")).text();
        result.setTarget(target);

        List<String> alternatives = doc.getElementsByClass("lmt__translations_as_text__text_btn").stream().map(Element::text).collect(Collectors.toList());
        result.setAlternatives(alternatives);

        String source = Objects.requireNonNull(doc.getElementById("source-dummydiv")).text();
        result.setSrc(source);
        return result;
    }

    @PreDestroy
    public void destroy() {
        driver.quit();
    }
}
