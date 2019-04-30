package com.srsj;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication(scanBasePackages = "com")
//@EnableEurekaClient
//@ServletComponentScan
@Controller
public class ServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ServiceApplication.class, args);
//        RtmpServer.main(new String[]{"1935"});
//        HttpFileServer.main(new String[]{"8080"});
    }

    @Value("${server.port}")
    String port;

    @RequestMapping(value = "/console", produces = {"application/json"})
    public String home(@RequestParam(required = false) String name) {
        return "hi " + name + ",i am from port:" + port;
    }

    @RequestMapping(value = "/")
    public String index() {
        return "/index.html";
    }

}
