package com.eappcat.datax.web.loader;

import com.alibaba.fastjson.JSONObject;
import com.eappcat.datax.web.conf.DataxProperties;
import com.eappcat.datax.web.dto.DataxPluginDTO;
import com.eappcat.datax.web.dto.JobDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DataxLoader {
    private transient URLClassLoader loader;
    private Map<String, DataxPluginDTO> plugins=new HashMap<>();
    @Autowired
    private DataxProperties dataxProperties;
    @PostConstruct
    public void init() throws IOException{
        System.setProperty("datax.home",dataxProperties.getHome());
        File root=new File(dataxProperties.getHome());
        File libs=new File(root,"lib");
        File plugins= new File(root,"plugin");
        ArrayList<URL> urls=new ArrayList<>();
        scanLibs(libs,urls);
        scanPlugins(plugins,urls);
        loader=new URLClassLoader(urls.toArray(new URL[]{}),Thread.currentThread().getContextClassLoader());
    }

    private void scanPlugins(File plugins, ArrayList<URL> urls) throws IOException {
        File writerPlugins=new File(plugins,"writer");
        File readerPlugins=new File(plugins,"reader");
        scanPlugins(urls, readerPlugins);
        scanPlugins(urls, writerPlugins);

    }

    private void scanPlugins(ArrayList<URL> urls, File readerPlugins) throws IOException {
        for(File reader: readerPlugins.listFiles()){
            if(reader.isDirectory()){
                DataxPluginDTO dataxPluginDTO=new DataxPluginDTO();
                dataxPluginDTO.setName(reader.getName());


                scanLibs(reader,urls);
                File pluginLibs=new File(reader,"libs");
                if(pluginLibs.isDirectory()){
                   scanLibs(pluginLibs,urls);
                }
                File pluginJson=new File(reader,"plugin.json");
                if(pluginJson.exists()){
                    String text=FileUtils.readFileToString(pluginJson);
                    dataxPluginDTO.setPlugin(JSONObject.parseObject(text));
                }
                File templateJson=new File(reader,"plugin_job_template.json");

                if(templateJson.exists()){
                    String template=FileUtils.readFileToString(templateJson);
                    dataxPluginDTO.setPluginTemplate(JSONObject.parseObject(template));

                }
                plugins.put(reader.getName(),dataxPluginDTO);

            }

        }
    }

    private void scanLibs(File libs, ArrayList<URL> urls) {
        if(libs.isDirectory()){
            File[] files=libs.listFiles(pathname -> {
                return pathname.getName().endsWith(".jar");
            });
            for (File file:files){
                try {
                    urls.add(file.toURL());
                } catch (MalformedURLException e) {
                    log.info("load url failed {}",file.getAbsolutePath());
                }
            }
        }

    }

    public int executeJob(String job) throws Exception {
        Class clazz=this.loader.loadClass("com.alibaba.datax.core.Engine");
        Method main=clazz.getDeclaredMethod("entry",String[].class);
        File jobDir=new File(dataxProperties.getHome(),"job");

        File jobFile=new File(jobDir,job.concat(".json"));
        if(jobFile.exists()){
            main.invoke(null,new Object[]{new String[]{"-mode","standalone","-jobid","-1","-job",jobFile.getAbsolutePath()}});
            return 1;
        }
        return 0;
    }
    public Map<String, DataxPluginDTO> getPlugins() {
        return plugins;
    }

    public void saveJob(JobDTO jobDTO) throws IOException {
        File jobDir=new File(dataxProperties.getHome(),"job");
        FileUtils.writeStringToFile(new File(jobDir,jobDTO.getName().concat(".json")),jobDTO.getConfig().toJSONString());
    }

    public JobDTO loadJob(String jobName) throws IOException {
        File jobDir=new File(dataxProperties.getHome(),"job");
        File jobFile=new File(jobDir,jobName.concat(".json"));
        JobDTO jobDTO=new JobDTO();
        jobDTO.setName(jobName);
        if(jobFile.exists()){
            String text=FileUtils.readFileToString(jobFile);
            jobDTO.setConfig(JSONObject.parseObject(text));
            return jobDTO;
        }
        return null;
    }
    @PreDestroy
    public void close() throws IOException {
        loader.close();
    }

}
