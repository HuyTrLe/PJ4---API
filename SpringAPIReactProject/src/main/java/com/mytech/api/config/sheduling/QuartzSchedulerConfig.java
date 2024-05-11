package com.mytech.api.config.sheduling;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzSchedulerConfig {	

    @Bean
    JobDetail updateBillsJobDetail() {
        return JobBuilder.newJob().ofType(UpdateBillsJob.class).storeDurably().withIdentity("updateBillsJob")
                .withDescription("Update bills job").build();
    }

    @Bean
    Trigger updateBillsJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(updateBillsJobDetail())
                .withIdentity("updateBillsJobTrigger")
                .withDescription("Trigger for update bills job")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 9))
                .build();
    }
    
    @Bean
    JobDetail debtNotificationJobDetail() {
        return JobBuilder.newJob(DebtNotificationJob.class)
                .withIdentity("debtNotificationJob")
                .storeDurably()
                .build();
    }

    @Bean
    Trigger debtNotificationJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(debtNotificationJobDetail())
                .withIdentity("debtNotificationTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(12, 0)) // Run at 9:00 AM every day
                .build();
    }
}