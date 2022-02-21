package com.example.listview.Dealer;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.listview.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

/*AddCouponFragment è un fragment che gestisce l'aggiunta di coupon da parte del Delaer
 * Un coupon prevede un nome(String), codice(String), e percentuale di sconto con range 1-100(int)
 * La classe prevede metodi per:
 * 1) Aggiunta al database
 * 2) Generazione automatica randomica di un codice
 * 3) Controlli su tutti i campi del coupon
 * Il layout è fragment_add_coupon.xml*/

public class AddCouponFragment extends Fragment {

    //Dichiarazione di tutti gli Items
    private Button submitButton, generateCodButton;
    private FirebaseUser fbUser;
    private EditText nameEditText, codeEditText;
    private EditText descriptionEditText;
    private SeekBar PercentSeekbar;
    private TextView percentView;
    private FirebaseDatabase db;                            //field per l'istanza del database
    private DatabaseReference dbRef, dealerRef;             //field per il riferimento alla posizione dei dati
    private FirebaseAuth mAuth;
    private String nameHolder, codeHolder, addressHolder, descriptionHolder, userID, nameDealerHolder, category;
    private int percentHolder;


    public AddCouponFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_coupon, container, false);
        db = FirebaseDatabase.getInstance();        //recupero dell'istanza del database
        // posiziono il riferimento al database in modo da poter aggiungere il coupon su firebase
        dbRef = FirebaseDatabase.getInstance().getReference("all_coupons").child("coupon").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        percentView = (TextView) view.findViewById(R.id.percentView);
        PercentSeekbar = (SeekBar) view.findViewById(R.id.percentCoupon);
        PercentSeekbar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN );
        submitButton = (Button) view.findViewById(R.id.submit);
        nameEditText = (EditText) view.findViewById(R.id.couponName);
        descriptionEditText = (EditText) view.findViewById(R.id.descriptionCoupon);
        codeEditText = (EditText) view.findViewById(R.id.couponCode);
        generateCodButton = (Button) view.findViewById(R.id.genCodButt);

        // Inizializzo seekbar per la creazione di un coupon
        PercentSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                percentView.setText("" + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Controllo dei campi: "Nome", "Codice" e "Percentuale";
        //Condizioni soddisfatte: chiamata al metodo insertData();
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameEditText.getText().toString().isEmpty() || codeEditText.getText().toString().isEmpty())
                    Toast.makeText(getContext(), R.string.errorMsgField, Toast.LENGTH_LONG).show();
                else if (nameEditText.length() > 15)
                    Toast.makeText(getContext(), R.string.errorMsgCpName, Toast.LENGTH_LONG).show();
                else if (codeEditText.length() > 10)
                    Toast.makeText(getContext(), R.string.errorMsgCpCode, Toast.LENGTH_LONG).show();
                else if (PercentSeekbar.getProgress() == 0)
                    Toast.makeText(getContext(), R.string.errorMsgCpPercent, Toast.LENGTH_LONG).show();
                else if(descriptionEditText.length()>20)
                    Toast.makeText(getContext(), R.string.errorMsgCpDescription, Toast.LENGTH_LONG).show();
                else {
                    insertData();
                }
            }
        });

        // metodo del pulsante di generazione casuale del codice coupon
        generateCodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeEditText.setText(generateString().toString());
            }
        });
        return view;
    }

    // metodo che inizializza le variabili del coupon dalle View presenti nel layout
    private void getDataFromFields() {
        nameHolder = nameEditText.getText().toString().trim();
        codeHolder = codeEditText.getText().toString().trim();
        descriptionHolder=descriptionEditText.getText().toString().trim();
        percentHolder = PercentSeekbar.getProgress();
    }

    // Metodo per generare una stringa casuale di 10 caratteri per il codice del coupon
    private StringBuffer generateString() {
        Random rand = new Random();
        StringBuffer tempStr = new StringBuffer();
        tempStr.append("");
        for (int i = 0; i < 10; i++) {
            int c = rand.nextInt(122 - 48) + 48;
            if ((c >= 58 && c <= 64) || (c >= 91 && c <= 96)) {
                i--;
                continue;
            }
            tempStr.append((char) c);
        }
        return tempStr;
    }

    // metodo per l'aggiunta del coupon al database
    private void insertData() {
        mAuth = FirebaseAuth.getInstance();
        fbUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("all_coupons").child("coupon").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dealerRef = FirebaseDatabase.getInstance().getReference("dealers");
        userID = fbUser.getUid();
        dealerRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Dealer d = dataSnapshot.getValue(Dealer.class);
                addressHolder = d.getAddress();
                nameDealerHolder = d.getName();
                category = d.getCategory();
                Coupon coupon = new Coupon();
                getDataFromFields();
                //Istanzio tutte i parametri della classe Coupon
                coupon.setNomeCoupon(nameHolder);
                coupon.setCodice(codeHolder);
                coupon.setPercenutale(percentHolder);
                coupon.setDescription(descriptionHolder);
                coupon.setAddress(addressHolder);
                coupon.setDealerName(nameDealerHolder);
                coupon.setCategory(category);
                // Ottengo l'ID da Firebase-Database.
                String cpId = dbRef.push().getKey();
                dbRef.child(cpId).setValue(coupon);
                // Mostro un Toast message dopo l'aggiunta andata a buon fine.
                Resources res = getResources();
                String couponFound = res.getQuantityString(R.plurals.addCouponMsg, 1);
                Toast.makeText(getContext(), couponFound, Toast.LENGTH_LONG).show();
                //Reset dei campi per l'aggiunta di un nuovo coupon
                if (nameEditText.getText().toString() != null && codeEditText.getText().toString() != null && PercentSeekbar.getProgress() != 0 && descriptionEditText.getText().toString() != null) {
                    nameEditText.setText("");
                    codeEditText.setText("");
                    descriptionEditText.setText("");
                    percentView.setText(null);
                    PercentSeekbar.setProgress(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
