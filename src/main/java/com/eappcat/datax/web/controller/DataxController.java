package com.eappcat.datax.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.eappcat.datax.web.dto.JobDTO;
import com.eappcat.datax.web.loader.DataxLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
public class DataxController {
    @Autowired
    private DataxLoader dataxLoader;
    @GetMapping("/plugins")
    public DataxLoader index() throws Exception {
        return dataxLoader;
    }
    @PostMapping("/addJob/{job}")
    public int addJob(@PathVariable("job")String jobName, @RequestBody String job) throws IOException {
        dataxLoader.saveJob(new JobDTO(jobName,JSONObject.parseObject(job)));
        return 1;
    }
    @GetMapping("/getJob/{job}")
    public JobDTO addJob(@PathVariable("job")String jobName) throws IOException {
        return dataxLoader.loadJob(jobName);
    }

    @GetMapping("/runJob/{job}")
    public JSONObject runJob(@PathVariable("job") String job){
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("jobId",dataxLoader.executeJobProcess(job));
        }catch (Exception e){
            jsonObject.put("error",e.getMessage());
        }
        return jsonObject;

    }
    @GetMapping(value = "/log/{jobId}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String log(@PathVariable("jobId") String jobId) throws IOException {
        String data=dataxLoader.getLog(jobId);
        return data;
    }
}
