package com.mytech.api.services.bill;

import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.bill.BillDTO;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.recurrence.RecurrenceDTO;
import com.mytech.api.models.user.User;
import com.mytech.api.services.recurrence.RecurrenceService;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateNewBillsJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(CreateNewBillsJob.class);

    @Autowired
    private BillService billService;

    @Autowired
    private RecurrenceService recurrenceService;
    
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LocalDate currentDate = LocalDate.now();
        List<BillDTO> billsDueToday = billService.findBillsDueToday(currentDate).stream().map(this::mapBillToDTO)
                .collect(Collectors.toList());

        logger.info("Found {} bills due today.", billsDueToday.size());

        for (BillDTO billDTO : billsDueToday) {
            RecurrenceDTO originalRecurrence = billDTO.getRecurrence();
            if (originalRecurrence != null) {
                LocalDate endDate = originalRecurrence.getEndDate();
                if (endDate != null && currentDate.isAfter(endDate)) {
                    logger.info("End date {} is before current date, skipping.", endDate);
                    continue;
                }

                Recurrence originalRecurrenceEntity = recurrenceService
                        .findRecurrenceById(originalRecurrence.getRecurrenceId());
                if (originalRecurrenceEntity != null) {
                    LocalDate nextDueDate = calculateNextDueDate(originalRecurrence, currentDate);
                    Bill newBill = new Bill();
                    newBill.setUser(modelMapper.map(billDTO.getUser(), User.class));
                    newBill.setBillName(billDTO.getBillName());
                    newBill.setAmount(billDTO.getAmount());
                    newBill.setDueDate(nextDueDate);
                    newBill.setRecurrence(originalRecurrenceEntity);
                    billService.addNewBill(newBill);
                    logger.info("Created new bill: {}", newBill);
                }
            }
        }
    }

    private LocalDate calculateNextDueDate(RecurrenceDTO recurrence, LocalDate currentDueDate) {
        switch (recurrence.getRecurrenceType()) {
            case DAILY:
                return currentDueDate.plusDays(recurrence.getIntervalAmount());
            case WEEKLY:
                return currentDueDate.plusWeeks(recurrence.getIntervalAmount());
            case MONTHLY:
                return currentDueDate.plusMonths(recurrence.getIntervalAmount());
            case ANNUALLY:
                return currentDueDate.plusYears(recurrence.getIntervalAmount());
            default:
                throw new IllegalArgumentException("Unsupported recurrence type: " + recurrence.getRecurrenceType());
        }
    }

    private BillDTO mapBillToDTO(Bill bill) {
        BillDTO billDTO = modelMapper.map(bill, BillDTO.class);
        return billDTO;
    }
}
