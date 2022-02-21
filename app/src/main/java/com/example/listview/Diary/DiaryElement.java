package com.example.listview.Diary;

import com.google.firebase.database.Exclude;

/* DiaryElement: Classe dell'elemento che andrà inserito nel diaro dell'utnete. Possiede i seguenti attributi:
 * (String) nome, percoso di una immagine, key, descrizione
 * (int) posizione (per la lista)
 * La classe prevede tutti i metodi di get() e set() per gli attributi
 */

public class DiaryElement {
    private String name, imageUrl, key, description;
    private int position;

    public DiaryElement(){

    }

    public DiaryElement(int position) {
        this.position=position;
    }

    public DiaryElement(String name, String description, String imageUrl) {
        if (name.trim().equals("")){
            name="No name";
        }
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
