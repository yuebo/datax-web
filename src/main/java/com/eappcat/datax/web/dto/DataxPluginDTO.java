package com.eappcat.datax.web.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class DataxPluginDTO {
    private String name;
    private JSONObject plugin;
    private JSONObject pluginTemplate;
}
