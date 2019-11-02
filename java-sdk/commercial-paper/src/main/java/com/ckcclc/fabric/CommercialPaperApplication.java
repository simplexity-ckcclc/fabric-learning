package com.ckcclc.fabric;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Author:  ckcclc <wchuang5900@163.com>
 * Created: 2019/10/31
 */

@SpringBootApplication
@ServletComponentScan
public class CommercialPaperApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommercialPaperApplication.class, args);
    }
}
