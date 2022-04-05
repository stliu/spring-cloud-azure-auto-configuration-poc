package com.azure.spring.autoconfigure;

//import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DemoApplicationRunner implements ApplicationRunner {

//    @Autowired
//    private BlobServiceClientBuilder builder;
    @Override
    public void run(ApplicationArguments args) throws Exception {
//        Objects.requireNonNull(builder, "builder");

        System.out.println("===========================================================");
    }
}
