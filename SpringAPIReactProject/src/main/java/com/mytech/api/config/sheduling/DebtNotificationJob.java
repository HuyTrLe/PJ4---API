package com.mytech.api.config.sheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mytech.api.services.debt.DebtServiceImpl;

@Component
public class DebtNotificationJob implements Job {

	@Autowired
	private DebtServiceImpl debtService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
        debtService.checkAndSendDebtNotifications();
	}
}