package cn.leftsite.deepltrans.entity;

import lombok.Data;

import java.util.List;

@Data
public class TranslateResult {
    private String src;
    private String target;
    private List<String> alternatives;
    private long cost;
}
