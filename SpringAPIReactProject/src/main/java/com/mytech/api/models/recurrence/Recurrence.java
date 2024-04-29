package com.mytech.api.models.recurrence;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.transaction.TransactionRecurring;
import com.mytech.api.models.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recurrences")
public class Recurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recurrenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FrequencyType frequencyType; // Daily, monthly, yearly

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek; // mondey, tuesday...

    @Enumerated(EnumType.STRING)
    private MonthOption monthOption; // thứ 5 của tuần thứ 4, cùng ngày tạo date

    private Integer every; // khoảng cách số lần lặp lại

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "You need to choose Forever or Until or Times to repeat")
    private EndType endType; // forever, until, times hiển thị số lần lặp lại

    @Temporal(TemporalType.DATE)
    private LocalDate endDate; // if choosing until

    private int times; // if choosing times

    @OneToMany(mappedBy = "recurrence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bill> bills = new ArrayList<>();

    @OneToMany(mappedBy = "recurrence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionRecurring> recurringTransactions = new ArrayList<>();

}