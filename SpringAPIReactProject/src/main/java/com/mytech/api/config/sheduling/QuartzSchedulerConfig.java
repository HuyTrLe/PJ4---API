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
				.withIdentity("updateTransactionReccringsJob").withDescription("Update Transaction Recurring job")
				.build();
	}

	@Bean
	Trigger jobTrigger() {
		return TriggerBuilder.newTrigger().forJob(updateBillsJobDetail()).withIdentity("jobTrigger")
				.withDescription("Trigger for job").withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(13, 23))
				.build();
	}

	@Bean
	Trigger transactionJobTrigger() {
		return TriggerBuilder.newTrigger().forJob(updateTransactionReccurenceJobDetail())
				.withIdentity("TransactionJobTrigger")
				.withDescription("Trigger for job").withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(13, 26))
				.build();
	}

	@Bean
	JobDetail debtNotificationJobDetail() {
		return JobBuilder.newJob(DebtNotificationJob.class).withIdentity("debtNotificationJob").storeDurably().build();
	}

	@Bean
	Trigger debtNotificationJobTrigger() {
		return TriggerBuilder.newTrigger().forJob(debtNotificationJobDetail()).withIdentity("debtNotificationTrigger")
				.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(12, 0)) // Run at 9:00 AM every day
				.build();
	}

	@Bean
	JobDetail savingNotificationJobDetail() {
		return JobBuilder.newJob(SavingNotification.class).withIdentity("savingNotificationJob").storeDurably().build();
	}

	@Bean
	Trigger savingNotificationJobTrigger() {
		return TriggerBuilder.newTrigger().forJob(savingNotificationJobDetail())
				.withIdentity("savingNotificationTrigger").withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(9, 0))
				.build();
	}

	@Bean
	JobDetail budgetNotificationJobDetail() {
		return JobBuilder.newJob(BudgetNotificationJob.class).withIdentity("budgetNotificationJob").storeDurably()
				.build();
	}

	Trigger budgetNotificationJobTrigger() {
		return TriggerBuilder.newTrigger().forJob(budgetNotificationJobDetail())
				.withIdentity("budgetNotificationTrigger").withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(9, 0))
				.build();
	}

}