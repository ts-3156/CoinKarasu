package com.example.coinkarasu.api.coincheck.data;

public class Rate {

    public String fromSymbol;
    public String toSymbol;
    public double value;

    public Rate(String fromSymbol, String toSymbol) {
        this(fromSymbol, toSymbol, 0.0);
    }

    public Rate(String fromSymbol, String toSymbol, double value) {
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.value = value;
    }

    public String toString() {
        return fromSymbol + ", " + toSymbol + ", " + value;
    }
}
