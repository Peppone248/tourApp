package com.example.listview.Dealer;

import androidx.annotation.NonNull;

/*Classe Dealer, definisce chi è il venditore e possiede i seguenti attributi: name, email, address, telephone, category, town;
* un lista (String) di categorie: categories */

public class Dealer
{
    private String name, email, address, telephone, category, town;

    public Dealer(@NonNull String name, @NonNull String address, @NonNull String telephone, @NonNull String email, @NonNull String category, @NonNull String town) {
        this.name = name;
        this.address = address;
        this.telephone = telephone;
        this.email = email;
        this.town=town;
        this.category=category;
    }

    public Dealer()  {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTown() {
        return town;
    }

    public String getCompleteAddress (){
        return address + ", " + town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setAdress(String adress) {
        this.address = adress;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }
}
