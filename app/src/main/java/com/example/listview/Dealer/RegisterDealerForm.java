package com.example.listview.Dealer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.listview.Login_Settings.MainActivity;
import com.example.listview.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/*RegisterDealerForm (Activity) Si occupa della registrazione di un venditore, si prevede:
* 1) Scrittura sul database del nuovo venditore registrato;
* 2) Controlli su tutti i campi che il venditore dovrà riempire;
* Il layout è activity_register_dealer_form.xml
* */


public class RegisterDealerForm extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z]).(?=.*[0-9]).{4,}$"); //Pattern password:  almeno una maiuscola, almeno un numero e lunga almeno 4 caratteri
    private FirebaseDatabase db;                                  //field per l'istanza del database
    private DatabaseReference dbRef;                              //field per il riferimento alla posizione dei dati
    private FirebaseAuth mAuth;                                   //field per l'istanza del sistema di autenticazione
    private FirebaseAuth.AuthStateListener mAuthListener;         //field per il listener in ascolto dello stato di autenticazione
    private static final String TAG = "RegisterDealerForm";       //costante per il tag dei log
    private EditText email, name, address, password, confirmPass, phoneNumber, city;
    private Spinner spin;
    boolean isSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_dealer_form);

        //db = FirebaseDatabase.getInstance();        //recupero dell'istanza del database
        mAuth = FirebaseAuth.getInstance();           //recupero dell'istanza del sistema di autenticazione
        email = findViewById(R.id.emailD);
        name = findViewById(R.id.nameD);
        address = findViewById(R.id.addressD);
        city = findViewById(R.id.city);
        phoneNumber = findViewById(R.id.PhoneNumber);
        password = findViewById(R.id.DealerPsw);
        confirmPass = findViewById(R.id.ConfirmDealerPsw);
        spin = findViewById(R.id.categoryList);
        Button registra = findViewById(R.id.registerDealer);

        String[] plants = new String[]{
                getText(R.string.selectCategory).toString()
        };

        final List<String> plantsList = new ArrayList<>(Arrays.asList(plants));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(RegisterDealerForm.this, android.R.layout.simple_spinner_item, plantsList) {
            @Override
            public boolean isEnabled(int position){
                if (position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        /*Set di una mappa per semplificare il riconoscimento delle categorie, necessaria per il filtraggio delle
          categoria nella sezione ExploreFragment
         */
        String[] myResArray = getResources().getStringArray(R.array.category_names);
        List<String> myResArrayList = Arrays.asList(myResArray);
        final Map<String, String> originalCat = new HashMap<>();
        originalCat.put(getString(R.string.hotel), "Hotel");
        originalCat.put(getString(R.string.risto), "Restaurant");
        originalCat.put(getString(R.string.disco), "Dance Club");
        originalCat.put(getString(R.string.bAndb), "Bed and Breakfast");
        originalCat.put(getString(R.string.shore), "Shore");
        originalCat.put(getString(R.string.fun), "Fun");
        originalCat.put(getString(R.string.iceCream), "Ice-cream parlour");
        originalCat.put(getString(R.string.pizza), "Pizzeria");
        originalCat.put(getString(R.string.church), "Church");
        originalCat.put(getString(R.string.museum), "Museum");

        int i;
        for (String s : myResArrayList) {
            arrayAdapter.add(s);
        }

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(arrayAdapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // Se l'utente cambia l'item selezionato
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText(getApplicationContext(),  getString(R.string.selected) + " : " + selectedItemText, Toast.LENGTH_SHORT).show();
                    isSet = true;
                } else {
                    isSet = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        registra.setOnClickListener(new View.OnClickListener() {    // Questa è la parte della registrazione
            @Override
            public void onClick(View v) {
                String emailStr = email.getText().toString().toLowerCase().trim();
                String pwdStr = password.getText().toString().trim();
                if (isSet) {
                    if (isPasswordValid() & isEmailValid() & isCityValid() & isAddressValid() & isValidPhoneNumber() & isNameValid()) {
                        mAuth.createUserWithEmailAndPassword(emailStr, pwdStr).
                                addOnCompleteListener(RegisterDealerForm.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthUserCollisionException existEmail) {
                                                Log.d(TAG, "onComplete: exist_email");
                                                email.setError(getText(R.string.emailInvalid));

                                            } catch (Exception e) {
                                                Log.d(TAG, "onComplete: " + e.getMessage());
                                            }
                                            //Creo un Toast message per avvisare che qualcosa è andato storto durante la registrazione
                                            Toast.makeText(RegisterDealerForm.this, R.string.errorMsgRegistration, Toast.LENGTH_LONG).show();
                                        } else {
                                            // Qui avviene la scrittura sul DB
                                            Toast.makeText(RegisterDealerForm.this, R.string.regAccount, Toast.LENGTH_LONG).show();
                                            Dealer dealer = new Dealer(name.getText().toString(), address.getText().toString(), phoneNumber.getText().toString(), email.getText().toString().trim().toLowerCase(), originalCat.get(spin.getSelectedItem().toString()), city.getText().toString());
                                            db.getInstance().getReference("dealers")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(dealer).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(RegisterDealerForm.this, R.string.regAccount, Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(RegisterDealerForm.this, MainActivity.class));
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                } else {    //controllo sulla categoria
                    Toast.makeText(RegisterDealerForm.this, R.string.noCategory, Toast.LENGTH_LONG).show();
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //recupero dei dati dell'utente loggato
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "dealer signed in");
                } else {
                    Log.d(TAG, "dealer signed out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //metodo per il controllo sulla email
    private boolean isEmailValid() {
        String emailInput = email.getText().toString().trim();

        if (emailInput.isEmpty()) {
            email.setError(getText(R.string.errorMsgField));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError(getText(R.string.emailInvalid));
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    //metodo per il controllo sulla password
    public boolean isPasswordValid() {
        String passwordInput = password.getText().toString().trim();
        String passConfirm = confirmPass.getText().toString().trim();

        if (passwordInput.isEmpty()) {
            password.setError(getText(R.string.errorMsgField));
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            password.setError(getText(R.string.pswWeak));
            return false;
        } else if (!passwordInput.equals(passConfirm)) {
            confirmPass.setError(getText(R.string.errorMsgDifferentPsw));
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    //metodo per il controllo sull'indirizzo
    public boolean isAddressValid() {

        String addressInput = address.getText().toString();

        if (addressInput.isEmpty()) {
            address.setError(getText(R.string.errorMsgField));
            return false;
        } else {
            address.setError(null);
            return true;
        }
    }
    //metodo per il controllo sul nome
    public boolean isNameValid() {

        String nameInput = name.getText().toString().trim();

        if (nameInput.isEmpty()) {
            name.setError(getText(R.string.errorMsgField));
            return false;
        } else {
            name.setError(null);
            return true;
        }
    }

    //metodo per il controllo sulla città
    public boolean isCityValid() {

        String cityInput = city.getText().toString().trim();

        if (cityInput.isEmpty()) {
            city.setError(getText(R.string.errorMsgField));
            return false;
        } else {
            city.setError(null);
            return true;
        }
    }


    // Verifica numero di telefono per dealer
    private boolean isValidPhoneNumber() {
        String phone = phoneNumber.getText().toString().trim();
        Log.d(TAG, "phone: " + phone);

        if (TextUtils.isEmpty(phone)) {
            phoneNumber.setError(getText(R.string.errorMsgField));
            return false;
        } else if (phone.length() != 10) {
            phoneNumber.setError(getText(R.string.phoneNumbError));
            return false;
        } else {
            phoneNumber.setError(null);
            return Patterns.PHONE.matcher(phone).matches();
        }
    }
}

