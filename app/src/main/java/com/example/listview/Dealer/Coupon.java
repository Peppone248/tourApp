package com.example.listview.Dealer;

/*Classe Coupon, definisce quello che sarà il Coupon e possiede i seguenti attributi: (String) codice, nomeCoupon, address, key, dealerName, category
                                                                                      (int) percentuale
  Con tutti i rispettivi get() e set() */

public class Coupon {
    private String codice, description, nomeCoupon, address, key, dealerName, category;
    private int percenutale;

    Coupon() {

    }

    Coupon(String codice, int percenutale, String nomeCoupon, String address, String dealerName, String description) {
        this.codice = codice;
        this.nomeCoupon = nomeCoupon;
        this.percenutale = percenutale;
        this.address = address;
        this.dealerName = dealerName;
        this.description=description;
    }

    public int getPercenutale() {
        return percenutale;
    }

    public void setPercenutale(int percenutale) {
        this.percenutale = percenutale;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public void setNomeCoupon(String nomeCoupon) {
        this.nomeCoupon = nomeCoupon;
    }

    public String getNomeCoupon() {
        return nomeCoupon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public String getDealerName() {
        return dealerName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return this.getAddress()
                + this.getCodice()
                + this.getKey()
                + this.getNomeCoupon();
    }

}
