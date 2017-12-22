package com.example.coinkarasu.api.coincheck.data;

public class Rate {

    public String fromSymbol;
    public String toSymbol;
    public double value;

    public Rate(String fromSymbol, String toSymbol) {
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.value = 0.0;
    }

    public String toString() {
        return fromSymbol + ", " + toSymbol + ", " + value;
    }
}
