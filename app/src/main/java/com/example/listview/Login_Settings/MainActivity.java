package com.example.listview.Login_Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.listview.Dealer.DealerPage;
import com.example.listview.R;
import com.example.listview.User.UserPage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;


/*Activty principale, appare dopo la splashScreen, consente l'accesso all'app tramite login o registrazione
  contiene l'implementazione per il multilingua
 */
public class MainActivity extends AppCompatActivity {

    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";
    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private EditText email, password;
    private CheckBox rememberMe, showPassword;


    //variabile responsabile per l'accesso all'applicazione in base ai permessi
    //se i permessi per il gps sono stati accettati o meno verrà assegnato rispettivamente vero o falso
    private boolean mLocationPermissionGranted = false;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }


    // Alert per i permessi del GPS
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msgPermission)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }


    /*
     * Si richiede il permesso di localizzazione, in modo che si possa ottenere la posizione del
     * dispositivo. Il risultato della richiesta di autorizzazione è gestito da un callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getHome();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    public void getHome() {
        Intent intent = new Intent(getApplicationContext(), UserPage.class);
        startActivity(intent);
    }


    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //tutto va bene e l'utente può effettuare richieste per la mappa
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //si è verificato un errore ma possiamo risolverlo
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getHome();
                } else {
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.change_lang_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.language:
                showChangeLanguageDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);

        TextView register = findViewById(R.id.signIn);
        TextView forgotPass = findViewById(R.id.changePass);
        register.setPaintFlags(register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotPass.setPaintFlags(forgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailEtID);
        password = findViewById(R.id.passwordEtID);
        rememberMe = findViewById(R.id.rememberMe);
        showPassword = findViewById(R.id.chekShowPass);

        Button login = findViewById(R.id.loginBID);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = email.getText().toString(); // Email in input
                if (isOnline()) {
                    if (isEmailValid() & isPasswordValid()) {
                        AppLogin(emailStr);
                    }
                } else {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.nonConnesso), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Implementazione GuestMode
        Button guest = findViewById(R.id.guestButton);
        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog TempDialog;
                CountDownTimer CDT;
                final int[] i = {3};

                if (isOnline()) {
                TempDialog = new ProgressDialog(MainActivity.this, R.style.MyAlertDialogStyle);
                TempDialog.setTitle(R.string.guestMode);
                TempDialog.setCancelable(false);
                TempDialog.setProgress(i[0]);
                TempDialog.show();

                CDT = new CountDownTimer(2000, 1000)
                {
                    public void onTick(long millisUntilFinished)
                    {
                        TempDialog.setMessage(getString(R.string.loading));
                    }

                    public void onFinish()
                    {
                        TempDialog.dismiss();
                        Intent guest = new Intent(getApplicationContext(), GuestActivity.class);
                        startActivity(guest);
                    }
                }.start();

                } else {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.nonConnesso), Toast.LENGTH_LONG).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterForm.class);
                startActivity(registerIntent);
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resetPswIntent = new Intent(getApplicationContext(), ResetPswActivity.class);
                startActivity(resetPswIntent);
            }
        });

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    private void showChangeLanguageDialog() {
        final String[] languageItem = {"Italiano", "English", "Français"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(R.string.chooseLng);
        mBuilder.setSingleChoiceItems(languageItem, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    setLocale("it");
                    recreate();
                } else if (i == 1) {
                    setLocale("en");
                    recreate();
                } else if (i == 2) {
                    setLocale("fr");
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }


    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        // salva dati nelle sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My language", lang);
        editor.apply();
    }


    // carica lingua salvata nelle sharedPref
    public void loadLocale() {
        final String PREFS_NAME = "MyPrefs";
        SharedPreferences first = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My language", "");
        //Settaggio della lingua del dispositivo al primo avvio
        if (first.getBoolean("my_first_time", true)) {
            Log.d("comments", "First launch");
            setLocale(Locale.getDefault().getLanguage());
            first.edit().putBoolean("my_first_time", false).commit();
        } else {
            setLocale(language);
        }
    }


    // Funzione per controllare tramite mail se colui che effettua il login è utente o dealer.
    private void AppLogin(final String QueryEmail) {
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("dealers");
        final String emailStr = email.getText().toString().toLowerCase().trim();
        final String pwdStr = password.getText().toString().trim();

        // Dialog animazione login
        final ProgressDialog nDialog;
        nDialog = new ProgressDialog(MainActivity.this, R.style.MyAlertDialogStyle);
        nDialog.setMessage(getString(R.string.loading));
        nDialog.setTitle(getString(R.string.Login));
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(true);
        savePreferences();

        //Login con rispettivi controlli e riconoscimenti
        if (!TextUtils.isEmpty(QueryEmail)) {
            final Query phoneNumReference = mDatabaseReference.orderByChild("email").equalTo(QueryEmail);

            ValueEventListener phoneNumValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        nDialog.show();
                        mAuth.signInWithEmailAndPassword(emailStr, pwdStr)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            nDialog.hide();
                                            Toast.makeText(MainActivity.this, R.string.failLogin, Toast.LENGTH_LONG).show();
                                        } else {
                                            Intent dealerIntent = new Intent(getApplicationContext(), DealerPage.class);
                                            startActivity(dealerIntent);
                                        }
                                    }
                                });
                    } else {
                        nDialog.show();
                        mAuth.signInWithEmailAndPassword(emailStr, pwdStr)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            nDialog.hide();
                                            Toast.makeText(MainActivity.this, R.string.failLogin, Toast.LENGTH_LONG).show();
                                        } else {
                                            Intent userIntent = new Intent(getApplicationContext(), UserPage.class);
                                            startActivity(userIntent);
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            phoneNumReference.addListenerForSingleValueEvent(phoneNumValueEventListener);

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        // rememberMe.toggle();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }

    // Salvataggio SharedPreferences
    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        SharedPreferences settingsCB = getSharedPreferences("mysettings", 0);
        SharedPreferences.Editor editorCB = settingsCB.edit();

        if (rememberMe.isChecked()) {
            // recupero di email e password
            final String emailStr = email.getText().toString();
            final String pwdStr = password.getText().toString();
            editor.putString(PREF_UNAME, emailStr);
            editor.putString(PREF_PASSWORD, pwdStr);
            editor.apply();

            // Conferma checkbox "ricorda credenziali" (true)
            boolean checkBoxValue = rememberMe.isChecked();
            editorCB.putBoolean("rememberMe", checkBoxValue);
            editorCB.apply();
        } else {
            // recupero email e psw se campi vuoti
            editor.putString(PREF_UNAME, null);
            editor.putString(PREF_PASSWORD, null);
            editor.commit();

            // Conferma checkbox "ricorda credenziali" (false)
            editorCB.putBoolean("rememberMe", false);
            editorCB.apply();
        }

    }


    private void loadPreferences() {
        // Carica le preferenze salvate della email/password e dello stato della checkbox
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences settingsCB = getSharedPreferences("mysettings", 0);
        boolean checkBoxValue = rememberMe.isChecked();

        String defaultUnameValue = "";
        String unameValue = settings.getString(PREF_UNAME, defaultUnameValue);
        String defaultPasswordValue = "";
        String passwordValue = settings.getString(PREF_PASSWORD, defaultPasswordValue);
        email.setText(unameValue);
        password.setText(passwordValue);
        rememberMe.setChecked(settingsCB.getBoolean("rememberMe", checkBoxValue));
    }


    private boolean isOnline() {
        // Check connessione internet
        ConnectivityManager connectivityMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connectivityMgr != null) {
            activeNetwork = connectivityMgr.getActiveNetworkInfo();
        }
        return activeNetwork != null;
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

        if (passwordInput.isEmpty()) {
            password.setError(getText(R.string.errorMsgField));
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

}