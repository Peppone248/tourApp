package com.example.listview.Dealer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.listview.Login_Settings.MainActivity;
import com.example.listview.R;
import com.example.listview.Login_Settings.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

/*DealerPage: Activity del Dealer
* Il layout è activity_dealer_page.xml*/

public class DealerPage extends AppCompatActivity {

    private FirebaseDatabase db;                            //field per l'istanza del database
    private DatabaseReference dbRef;                        //field per il riferimento alla posizione dei dati
    private FirebaseAuth mAuth;                             //field per l'istanza del sistema di autenticazione
    private FirebaseAuth.AuthStateListener mAuthListener;   //field per il listener in ascolto dello stato di autenticazione

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_dropdown, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:   //Logout: esce dal profilo e torna nella pagina di Login
                mAuth.signOut();
                Intent logoutIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(logoutIntent);
                break;

            case R.id.profile:  //Profile: Mostra i dati inseriti al momento della registrazione
                Intent profilo = new Intent(getApplicationContext(), ProfileInformationDealer.class);
                startActivity(profilo);
                break;

            case R.id.language: //Language: Permette di cambiare lingua
                showChangeLanguageDialog();
                break;

            case R.id.settings: //Apre le impostazioni
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_dealer_page);

        db = FirebaseDatabase.getInstance();        //recupero dell'istanza del database
        mAuth = FirebaseAuth.getInstance();         //recupero dell'istanza del sistema di autenticazione

        //creazione della bottom bar navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_dealer);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_dealer, new AddCouponFragment()).commit();
    }

    //creazione bottomNavigation con tutti i fragment
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_addCoupon:
                            selectedFragment = new AddCouponFragment();
                            break;
                        case R.id.nav_list_coupon:
                            selectedFragment = new ListCouponFragment();
                            break;
                        default:        //per questioni di warning, inserisco un valore di default
                            selectedFragment = new AddCouponFragment();

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_dealer, selectedFragment).commit();
                    return true;
                }
            };

    //metodo che permette di cambiare lingua (Italiano, Francese, Inglese) a seconda della scelta
    private void showChangeLanguageDialog() {
        final String[] languageItem = {"Italiano", "English", "Français"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DealerPage.this);
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
        // save data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My language", lang);
        editor.apply();
    }

    // load language saved in shared preferences
    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My language", "");
        setLocale(language);
    }
}
