package com.ahu.ahutong.data.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Card {
    @SerializedName("balance")
    private Double balance;
    @SerializedName("transitionBalance")
    private Double transitionBalance;

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
