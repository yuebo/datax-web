package com.eappcat.datax.web.loader;

import com.eappcat.datax.web.conf.DataxProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class JobEventListener {

    @Autowired
    private DataxProperties dataxProperties;

    @EventListener
    @Async
    public void listen(DataxJobEvent jobEvent) throws Exception {
        ProcessBuilder processBuilder=new ProcessBuilder();
        processBuilder.directory(new File(dataxProperties.getHome()));
        String cp="conf:lib/*:plugin/**";
        if(System.getProperties().getProperty("os.name").indexOf("windows")>=0){
            cp.replaceAll(":",";");
        }
        processBuilder.command("java","-classpath",cp,"-Ddatax.home=".concat(dataxProperties.getHome()),"com.alibaba.datax.core.Engine", "-mode","standalone","-jobid","-1","-job","./job/".concat(jobEvent.getJob()).concat(".json"));

        processBuilder.redirectErrorStream(true);
        File logDir=new File(dataxProperties.getHome(),"logs");
        if(!logDir.exists()){
            logDir.mkdir();
        }
        processBuilder.redirectOutput(new File(logDir,jobEvent.getJob().concat("_").concat(jobEvent.getTimestamp()).concat(".log")));

        Process process=processBuilder.start();
        int exitCode=process.waitFor();
        log.info("job {} exit with code {}",jobEvent.getJob(),exitCode);

    }

}
