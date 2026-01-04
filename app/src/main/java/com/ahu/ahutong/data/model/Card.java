package com.ahu.ahutong.data.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Card {
    @SerializedName("balance")
    private Double balance = 0.0;
    @SerializedName("transitionBalance")
    private Double transitionBalance = 0.0;

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getTransitionBalance() {
        return transitionBalance;
    }

    public void setTransitionBalance(Double transitionBalance) {
        this.transitionBalance = transitionBalance;
    }

    @NonNull
    @Override
    public String toString() {
        return "Card{" +
                "balance=" + balance +
                ", transitionBalance=" + transitionBalance +
                '}';
    }
}
