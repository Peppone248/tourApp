package com.example.listview.Diary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.listview.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*DetailsActivity: Activity, mostra le specifiche dell'elemento selezionato dal diario */

public class DetailsActivity extends AppCompatActivity {

    TextView nameDetailTextView, descriptionDetailTextView, dateDetailTextView;
    ImageView detailImageView;

    private void initializeWidgets() {
        nameDetailTextView = findViewById(R.id.nameDetailTextView);
        descriptionDetailTextView = findViewById(R.id.descriptionDetailTextView);
        dateDetailTextView = findViewById(R.id.dateDetailTextView);
        detailImageView = findViewById(R.id.detailImageView);
    }


    //metodo per il recupero della data odierna
    private String getDateToday() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String today = dateFormat.format(date);
        return today;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initializeWidgets();
        //Recupera i dati da ItemsFragment
        Intent i = this.getIntent();
        String name = i.getExtras().getString("NAME_KEY");
        String description = i.getExtras().getString("DESCRIPTION_KEY");
        String imageURL = i.getExtras().getString("IMAGE_KEY");
        //Imposta i dati ricevuti da TextView e da ImageView
        nameDetailTextView.setText(name);
        descriptionDetailTextView.setText(description);
        dateDetailTextView.setText("" + getDateToday());
        Picasso.with(this)
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(detailImageView);

    }
}
