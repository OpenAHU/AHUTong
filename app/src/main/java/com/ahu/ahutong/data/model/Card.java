package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

public class Card {
    @SerializedName("balance")
    private Double balance;
    @SerializedName("transitionBalance")
    private Integer transitionBalance;

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Integer getTransitionBalance() {
        return transitionBalance;
    }

    public void setTransitionBalance(Integer transitionBalance) {
        this.transitionBalance = transitionBalance;
    }

    @Override
    public String toString() {
        return "Card{" +
                "balance=" + balance +
                ", transitionBalance=" + transitionBalance +
                '}';
    }
}
