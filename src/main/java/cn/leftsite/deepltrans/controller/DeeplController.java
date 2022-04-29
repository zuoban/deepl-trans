package cn.leftsite.deepltrans.controller;

import cn.leftsite.deepltrans.driver.Chrome;
import cn.leftsite.deepltrans.entity.R;
import cn.leftsite.deepltrans.entity.TranslateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class DeeplController {
    private final Chrome chrome;

    public DeeplController(Chrome chrome) {
        this.chrome = chrome;
    }


    @GetMapping
    public R deepl(String q) {
        try {
            long start = System.currentTimeMillis();
            TranslateResult result = chrome.query(q);
            long cost = System.currentTimeMillis() - start;
            result.setCost(cost);
            return R.ok(result);
        } catch (Exception e) {
            log.error("deepl error", e);
            return R.error(e.getMessage());
        }
    }
}
