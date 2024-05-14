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
    JobDetail updateTransactionReccurenceJobDetail() {
        return JobBuilder.newJob().ofType(UpdateTransactionReccuringJob.class).storeDurably()
                .withIdentity("uupdateTransactionReccringsJob")
                .withDescription("Update Transaction Recurring job").build();
    }

    @Bean
    Trigger jobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(updateBillsJobDetail())
                .withIdentity("jobTrigger")
                .withDescription("Trigger for job")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(17, 39))
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