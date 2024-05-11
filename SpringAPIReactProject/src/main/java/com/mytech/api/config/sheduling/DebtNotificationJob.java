package com.mytech.api.config.sheduling;

import java.util.List;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mytech.api.models.debt.Debt;
import com.mytech.api.services.debt.DebtService;

@Component
public class DebtNotificationJob implements Job {

    @Autowired
    private DebtService debtService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	JobDataMap dataMap = context.getJobDetail().getJobDataMap(); 
        Long userId = dataMap.getLong("userId");

        // Use the userId to fetch debts specific to the user and process them
        System.out.println("Processing debt notifications for user ID: " + userId);
        List<Debt> activeDebts = debtService.findDebtActive(userId);
        for (Debt debt : activeDebts) {
            debtService.checkAndSendDebtNotifications(debt);
        }
    }
}