package com.lec.packages.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer{

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        int cntCore = Runtime.getRuntime().availableProcessors();
        threadPool.setPoolSize(cntCore * 2);
        threadPool.initialize();

        taskRegistrar.setTaskScheduler(threadPool);
    }
    
}
