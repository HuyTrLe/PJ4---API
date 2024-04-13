package com.mytech.api.config.sheduling;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mytech.api.services.bill.CreateNewBillsJob;


import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;

@Configuration
public class QuartzSchedulerConfig {

	@Bean
	JobDetail createNewBillsJobDetail() {
		return JobBuilder.newJob().ofType(CreateNewBillsJob.class).storeDurably().withIdentity("createNewBillsJob")
				.withDescription("Create new bills job").build();
	}

	@Bean
	Trigger createNewBillsJobTrigger() {
	    return TriggerBuilder.newTrigger()
	            .forJob(createNewBillsJobDetail())
	            .withIdentity("createNewBillsJobTrigger")
	            .withDescription("Trigger for create new bills job")
	            .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(16, 37))
	            .build();
	}
}