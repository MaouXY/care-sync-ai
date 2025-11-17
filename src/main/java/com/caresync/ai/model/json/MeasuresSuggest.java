package com.caresync.ai.model.json;

import lombok.Data;

@Data
public class MeasuresSuggest {
    private String week;//每周服务tag建议
    private MeasuresSuggestDetails[] details;//每周任务列表
}
