package com.mytech.api.config.sheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mytech.api.services.budget.BudgetServiceImpl;

@Component
public class BudgetNotificationJob implements Job{
	
	@Autowired
	private BudgetServiceImpl budgetServiceImpl;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		budgetServiceImpl.checkAndSendNotificationDue();
	}
}
