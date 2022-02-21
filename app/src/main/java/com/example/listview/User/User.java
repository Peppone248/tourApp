package com.example.listview.User;

import androidx.annotation.NonNull;

/*Classe User, Utente che si può registrare e loggare nel sistema, possiede i seguenti attributi: nome, cognome, email
* con tutti i metodi get() e set()*/

public class User {

    private String firstName, lastName, email;

    public User(@NonNull String firstName, @NonNull String lastName, @NonNull String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public User()
    {

    }

    public String getFirstName() {

        return firstName;
    }
    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }
    public String getLastName() {

        return lastName;
    }
    public void setLastName(String lastName) {

        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}