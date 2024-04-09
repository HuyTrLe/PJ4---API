package com.mytech.api.models.recurrence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mytech.api.models.bill.Bill;
import com.mytech.api.models.user.User;

@Entity
@Getter @Setter
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
    private RecurrenceType recurrenceType;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @Temporal(TemporalType.DATE)
    private LocalDate endDate;
    
    private Integer intervalAmount;
    
    @OneToMany(mappedBy = "recurrence", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Bill> bills = new ArrayList<>();
}