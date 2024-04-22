package com.mytech.api.models.bill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.mytech.api.models.category.Category;
import com.mytech.api.models.recurrence.Recurrence;
import com.mytech.api.models.user.User;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int billId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String billName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;
    
    @ManyToOne
    @JoinColumn(name = "recurrence_id")
    private Recurrence recurrence;

    @ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;
    
    public Bill(Bill bill) {
        this.billId = bill.getBillId();
        this.user = bill.getUser();
        this.billName = bill.getBillName();
        this.amount = bill.getAmount();
        this.dueDate = bill.getDueDate();
        this.recurrence = bill.getRecurrence();
    }
}