package com.example.listview.User;
/*
 *Questa classe è un'activity che ha tre fragment per il viaggiatore (CouponFragment, DiaryFragment, ExploreFragment)
 * Il layout è activity_user_page.xml
 */

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.listview.Diary.DiaryFragment;
import com.example.listview.Login_Settings.MainActivity;
import com.example.listview.R;
import com.example.listview.Login_Settings.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class UserPage extends AppCompatActivity {

    private FirebaseDatabase db;                            //field per l'istanza del database
    private DatabaseReference dbRef;                        //field per il riferimento alla posizione dei dati
    private FirebaseAuth mAuth;                             //field per l'istanza del sistema di autenticazione
    private FirebaseAuth.AuthStateListener mAuthListener;   //field per il listener in ascolto dello stato di autenticazione

    private Fragment selectedFragment;
    private BottomNavigationView bottomNav;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_dropdown, menu);
        return true;
    }

    //Actionbar per la selezione di logout, profilo, lingua, impostazioni
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                mAuth.signOut();
                Intent logoutIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(logoutIntent);
                break;

            case R.id.profile:
                Intent profile = new Intent(getApplicationContext(), ProfileInformation.class);
                startActivity(profile);
                break;

            case R.id.language:
                showChangeLanguageDialog();
                break;

            case R.id.settings:
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_user_page);

        db = FirebaseDatabase.getInstance();        //recupero dell'istanza del database
        mAuth = FirebaseAuth.getInstance();         //recupero dell'istanza del sistema di autenticazione

        //creazione della bottom bar navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreFragment()).commit();
    }

    //creazione bottomNavigation con tutti i fragment
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.nav_explore:
                            selectedFragment = new ExploreFragment();
                            break;
                        case R.id.nav_coupon:
                            selectedFragment = new ListCouponUserFragment();
                            break;
                        case R.id.nav_diary:
                            selectedFragment = new DiaryFragment();
                            break;
                    }

                    replaceFragment(selectedFragment, null, false, true);
                    return true;
                }
            };


    public void replaceFragment(Fragment fragment, @Nullable Bundle bundle, boolean popBackStack, boolean findInStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        String tag = fragment.getClass().getName();
        Fragment parentFragment;
        if (findInStack && fm.findFragmentByTag(tag) != null) {
            parentFragment = fm.findFragmentByTag(tag);
        } else {
            parentFragment = fragment;
        }
        // if user passes the @bundle in not null, then can be added to the fragment
        if (bundle != null)
            parentFragment.setArguments(bundle);
        else parentFragment.setArguments(null);
        // this is for the very first fragment not to be added into the back stack.
        if (popBackStack) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            ft.addToBackStack(parentFragment.getClass().getName() + "");
        }
        ft.replace(R.id.fragment_container, parentFragment, tag);
        ft.commit();
        fm.executePendingTransactions();
    }

    private void showChangeLanguageDialog() {
        final String[] languageItem = {"Italiano", "English", "Français"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserPage.this);
        mBuilder.setTitle(getString(R.string.chooseLng));
        mBuilder.setSingleChoiceItems(languageItem, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    setLocale("it");
                    bottomNav.setSelectedItemId(R.id.nav_explore);
                    recreate();
                } else if (i == 1) {
                    setLocale("en");
                    bottomNav.setSelectedItemId(R.id.nav_explore);
                    recreate();
                } else if (i == 2) {
                    setLocale("fr");
                    bottomNav.setSelectedItemId(R.id.nav_explore);
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (selectedFragment != null) {
            replaceFragment(selectedFragment, null, false, true);
        }
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

    // carica la lingua salvata nelle shared preferences
    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My language", "");
        setLocale(language);
    }
}
