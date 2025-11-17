package com.caresync.ai.model.json;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "建议列表")
public class Recommendations {
    private String title;
    private String description;
    private String priority;
}
