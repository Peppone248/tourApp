package com.example.listview.Login_Settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.listview.Dealer.RegisterDealerForm;
import com.example.listview.R;
import com.example.listview.User.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

/*RegisterForm: Activity. Form della registrazione, si prevede:
* 1) Sicurezza per gli input inseriti con i vari controlli
* 2) Aggiunta al database
* */

public class RegisterForm extends AppCompatActivity {

    // Password con almeno una maiuscola, almeno un numero e lunga almeno 4 caratteri
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z]).(?=.*[0-9]).{4,}$");


    private FirebaseDatabase db;                            //field per l'istanza del database
    private FirebaseAuth mAuth;                             //field per l'istanza del sistema di autenticazione
    private FirebaseAuth.AuthStateListener mAuthListener;   //field per il listener in ascolto dello stato di autenticazione
    private static final String TAG = "RegisterForm";       //costante per il tag dei log
    private EditText email, password, confirmPass, nome, cognome;
    private CheckBox ifDealer;
    private Button registra;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form);
        mAuth = FirebaseAuth.getInstance();         //recupero dell'istanza del sistema di autenticazione

        // ACQUISIZIONE DATI
        email = findViewById(R.id.UserEmail);
        password = findViewById(R.id.UserPsw);
        confirmPass = findViewById(R.id.ConfirmUserPsw);
        registra = findViewById(R.id.RegisterUserBtn);
        nome = findViewById(R.id.firstName);
        cognome = findViewById(R.id.surnameText);
        ifDealer = findViewById(R.id.CheckDealer);

        ifDealer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ifDealer.toggle();
                Intent intent = new Intent(getApplicationContext(), RegisterDealerForm.class);
                startActivity(intent);
            }
        });

        registra.setOnClickListener(new View.OnClickListener() {
            // QUESTA E' LA REGISTRAZIONE
            @Override
            public void onClick(View v) {
                String emailStr = email.getText().toString().trim().toLowerCase();
                String pwdStr = password.getText().toString().trim();
                if (isPasswordValid() & isEmailValid() & isFirstNameValid() & isSecondNameValid()) {
                    mAuth.createUserWithEmailAndPassword(emailStr, pwdStr)
                            .addOnCompleteListener(RegisterForm.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthUserCollisionException existEmail) {
                                            Log.d(TAG, "onComplete: exist_email");
                                            email.setError(getString(R.string.emailInvalid));

                                        } catch (Exception e) {
                                            Log.d(TAG, "onComplete: " + e.getMessage());
                                        }
                                        Toast.makeText(RegisterForm.this, R.string.errorMsgRegistration, Toast.LENGTH_LONG).show();
                                    } else {
                                        // QUI AVVIENE LA SCRITTURA SU DB
                                        Toast.makeText(RegisterForm.this, R.string.regAccount, Toast.LENGTH_LONG).show();
                                        User user = new User(nome.getText().toString(), cognome.getText().toString(), email.getText().toString().toLowerCase().trim());
                                        db.getInstance().getReference("users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterForm.this, R.string.regAccount, Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(RegisterForm.this, MainActivity.class));
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //recupero dei dati dell'utente loggato
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "user signed in");
                } else {
                    Log.d(TAG, "user signed out");
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

    //controllo sulla email
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

    //controllo sulla password
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

    //controllo sul nome
    public boolean isFirstNameValid() {
        String nameInput = nome.getText().toString();

        if (nameInput.isEmpty()) {
            nome.setError(getText(R.string.errorMsgField));
            return false;
        } else {
            nome.setError(null);
            return true;
        }
    }

    //controllo sul cognome
    public boolean isSecondNameValid() {
        String surnameInput = cognome.getText().toString();

        if (surnameInput.isEmpty()) {
            cognome.setError(getText(R.string.errorMsgField));
            return false;
        } else {
            cognome.setError(null);
            return true;
        }
    }
}

