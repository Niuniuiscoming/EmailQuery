package org.fenixsoft;

import org.fenixsoft.listener.ApplicationStartListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueryApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryApplication.class);

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(QueryApplication.class);
        springApplication.addListeners(new ApplicationStartListener());
        springApplication.run(args);
    }
}
