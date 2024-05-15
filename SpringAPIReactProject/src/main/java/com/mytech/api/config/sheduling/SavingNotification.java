package com.mytech.api.config.sheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mytech.api.services.saving_goals.SavingGoalsServiceImpl;

@Component
public class SavingNotification implements Job{
	
	@Autowired
	private SavingGoalsServiceImpl savingServiceImp;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		savingServiceImp.checkAndSendSavingGoalNotifications();
	}
}
