package com.srsj;


import com.flazr.rtmp.server.RtmpServer;
import com.srsj.server.HttpFileServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com")
//@EnableEurekaClient
//@ServletComponentScan
@RestController
public class ServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ServiceApplication.class, args);
//        RtmpServer.main(new String[]{"1935"});
//        HttpFileServer.main(new String[]{"8080"});
    }

    @Value("${server.port}")
    String port;

    @RequestMapping("/console")
    public String home(@RequestParam(required = false) String name) {
        return "hi " + name + ",i am from port:" + port;
    }


}
