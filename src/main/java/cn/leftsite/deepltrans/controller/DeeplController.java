package cn.leftsite.deepltrans.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
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
            TimeInterval timer = DateUtil.timer();
            TranslateResult result = chrome.query(q);
            result.setCost(timer.interval());
            return R.ok(result);
        } catch (Exception e) {
            log.error("deepl error", e);
            return R.error(e.getMessage());
        }
    }
}
