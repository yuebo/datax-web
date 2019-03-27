package com.eappcat.datax.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.eappcat.datax.web.dto.JobDTO;
import com.eappcat.datax.web.loader.DataxLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public int runJob(@PathVariable("job") String job){
        try {
            return dataxLoader.executeJob(job);
        }catch (Exception e){
            log.error("error to run job {}",e.getMessage());
            return 0;
        }

    }
}
