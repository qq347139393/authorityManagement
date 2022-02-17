package com.planet.system;

import com.planet.util.springBoot.SpringBootQuartzManager;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBootQuartzConfig {
    @Autowired
    private Scheduler scheduler;

    @Bean("springBootQuartzManager")
    public SpringBootQuartzManager springBootQuartzManager(){
        SpringBootQuartzManager springBootQuartzManager=new SpringBootQuartzManager();
        springBootQuartzManager.setScheduler(scheduler);
        return springBootQuartzManager;
    }
}
