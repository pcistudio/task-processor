package com.contact.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.StandardEnvironment;

@SpringBootApplication
public class Application implements ApplicationRunner {

    @Autowired
    private StandardEnvironment env;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

//       env.getPropertySources().forEach(propertySource -> {
//           System.out.println(propertySource.getName());
//           System.out.println(propertySource.getSource());
//        });
    }
}
