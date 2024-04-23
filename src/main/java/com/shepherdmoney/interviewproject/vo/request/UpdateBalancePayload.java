package com.shepherdmoney.interviewproject.vo.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBalancePayload {

    private String creditCardNumber;

    private LocalDate balanceDate;

    private double balanceAmount;
}
