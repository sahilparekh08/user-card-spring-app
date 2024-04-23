package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    // TODO: Credit card's owner. For detailed hint, please see User class
    // Some field here <> owner;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // TODO: Credit card's balance history. It is a requirement that the dates in the balanceHistory
    //       list must be in chronological order, with the most recent date appearing first in the list.
    //       Additionally, the last object in the "list" must have a date value that matches today's date,
    //       since it represents the current balance of the credit card. For example:
    //       [
    //         {date: '2023-04-10', balance: 800},
    //         {date: '2023-04-11', balance: 1000},
    //         {date: '2023-04-12', balance: 1200},
    //         {date: '2023-04-13', balance: 1100},
    //         {date: '2023-04-16', balance: 900},
    //       ]
    // ADDITIONAL NOTE: For the balance history, you can use any data structure that you think is appropriate.
    //        It can be a list, array, map, pq, anything. However, there are some suggestions:
    //        1. Retrieval of a balance of a single day should be fast
    //        2. Traversal of the entire balance history should be fast
    //        3. Insertion of a new balance should be fast
    //        4. Deletion of a balance should be fast
    //        5. It is possible that there are gaps in between dates (note the 04-13 and 04-16)
    //        6. In the condition that there are gaps, retrieval of "closest" balance date should also be fast. Aka, given 4-15, return 4-16 entry tuple
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    private List<BalanceHistory> balanceHistory = new ArrayList<>();

    public void addBalanceHistory(BalanceHistory bH) {
        balanceHistory.add(bH);
        balanceHistory.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        int index = Collections.binarySearch(balanceHistory, bH, (a, b) -> b.getDate().compareTo(a.getDate()));
        double increment = index == balanceHistory.size() - 1
                ? 0 : balanceHistory.get(index).getBalance() - balanceHistory.get(index + 1).getBalance();
        for (int i = 0; i < index; i++) {
            balanceHistory.get(i).setBalance(balanceHistory.get(i).getBalance() + increment);
        }
    }

    // No usage for thee below method.
    // Below method is just to showcase how would a getBalance function would work for a given date
    public double getBalance(LocalDate date) {
        int index = getIndexForBalanceQuery(date);
        if (index == -1) {
            return 0;
        }
        return balanceHistory.get(index).getBalance();
    }

    // No usage for thee below method.
    // Below method is just to showcase how would a getBalance function wwould ork for a given date
    // by doing a closest date search as described in the above TODOs
    public double getBalanceOnClosestDate(LocalDate date) {
        int index = getIndexForBalanceQuery(date);
        if (index == -1) {
            return 0;
        }
        // If the date is found, return the balance
        if (balanceHistory.get(index).getDate().isEqual(date)) {
            return balanceHistory.get(index).getBalance();
        }
        // If the date is not found, return the balance of the closest date which is defined by the index or index+1
        // compare dates at index and index+1 with the given date
        if (index == 0 || index == balanceHistory.size() - 1) {
            return balanceHistory.get(index).getBalance();
        }
        LocalDate date1 = balanceHistory.get(index).getDate();
        LocalDate date2 = balanceHistory.get(index + 1).getDate();
        long diff1 = Math.abs(date1.toEpochDay() - date.toEpochDay());
        long diff2 = Math.abs(date2.toEpochDay() - date.toEpochDay());
        return diff1 < diff2 ? balanceHistory.get(index).getBalance() : balanceHistory.get(index + 1).getBalance();
    }

    // Helper function for the above getBalance and getBalanceOnClosestDate methods
    private int getIndexForBalanceQuery(LocalDate date) {
        if (balanceHistory == null || balanceHistory.isEmpty()) {
            return -1;
        }
        if (date.isAfter(balanceHistory.get(0).getDate())) {
            return 0;
        }
        if (date.isBefore(balanceHistory.get(balanceHistory.size() - 1).getDate())) {
            return -1;
        }
        BalanceHistory bH = new BalanceHistory();
        bH.setDate(date);
        int index = Collections.binarySearch(balanceHistory, bH, (a, b) -> b.getDate().compareTo(a.getDate()));
        if (index >= 0) {
            return index;
        }
        return -index - 1;
    }
}
