package com.example.listview.Login_Settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.listview.Dealer.Coupon;
import com.example.listview.Dealer.Dealer;
import com.example.listview.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
Questa activity gestisce la GuestMode, sono presenti i metodi per la visualizzazione della mappa
caricamento di coupon e dealer.
 */
public class GuestActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mMapView;

    private FirebaseDatabase mDatabaseDealers;
    private DatabaseReference mReferenceDealers;
    private List<Dealer> dealers = new ArrayList<>();
    private List<Coupon> coupons = new ArrayList<>();
    String selectedCategory = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);
        mMapView = findViewById(R.id.guest_list_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
         * acquisisco sequenzialmente tutti i dealers che sono presenti nel db
         */


        mDatabaseDealers = FirebaseDatabase.getInstance();
        mReferenceDealers = mDatabaseDealers.getReference("dealers");
        mReferenceDealers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dealers.clear();
                List<String> keys = new ArrayList<>();
                for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                    keys.add(keyNode.getKey());
                    Dealer dealer = keyNode.getValue(Dealer.class);
                    dealers.add(dealer);
                    Log.d("dealers", Integer.toString(dealers.size()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        warnUser();
        initGoogleMap(savedInstanceState);

    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView richiede che il pacchetto che passi contenga _ONLY_ MapView SDK
        // oggetti o Bundle.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    // refresh della mappa volontario
    public void onRefreshButton (View v){
        recreate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        try{
            mMapView.onSaveInstanceState(mapViewBundle);
        } catch (NullPointerException e) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(GuestActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    /*
    Funzione per trasformare gli indirizzi con coordinate lat/lng
     */
    private LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IndexOutOfBoundsException exception) {

            return new LatLng(0, 0);
        }

        return p1;
    }


    /**
     * Metodo che serve per creare un'immagine da dare utilizzare come marker nella mappa
     * @param context il contesto
     * @param resId la risorsa che deve essere convertita in bitmap
     * @return l'immagine per creare il marker
     */
    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Funzione che seleziona l'immagine da piazzare sul marker a seconda della categoria esaminata
     * @param dealer utente le cui categorie devono essere esaminate
     * @return l'immagine da piazzare sul marker
     */
    public int selectImageMarker (Dealer dealer){
        switch (dealer.getCategory()){
            case "Hotel":
                return R.drawable.hotel;
            case "Bed and Breakfast":
                return R.drawable.breakfast;
            case "Pizzeria":
                return R.drawable.ic_pizza;
            case "Restaurant":
            case "Ristorante":
                return R.drawable.ic_restaurant;
            case "Ice-cream parlour":
            case "Gelateria":
            case "Glacier":
                return R.drawable.ic_ice_cream;
            case "Church":
            case "Chiesa":
            case "Église":
                return R.drawable.ic_church;
            case "Shore":
            case "Lido":
            case "Rivage":
                return R.drawable.ic_beach;
            case "Museum":
            case "Museo":
            case "Musèe":
                return R.drawable.ic_museum;
            case "Fun":
            case "Svago":
            case "Amusement":
                return R.drawable.ic_svago_famiglia;
            case"Monumento":
            case "Monument":
                return R.drawable.ic_monument;
            case "Dance Club":
            case "Discoteca":
            case "Disco":
                return R.drawable.disco;
            default:
                return R.drawable.placeholder;
        }
    }
    /*
    Lettura dealer per creazione dei marker sulla mappa
    */
    private void createMarker (GoogleMap map){
        if (dealers.isEmpty()) { //se i dealers non sono stati scaricati da fb, ricarichiamo la pagina
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        }
        for (Dealer dealer: dealers) {
            int i = coupons.size();
            // Controllo sulla categoria
            if (selectedCategory== null){       //vengono visualizzati tutti i marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(getLocationFromAddress(getApplicationContext(), dealer.getCompleteAddress()))
                        .title(dealer.getName())
                        .snippet(dealer.getAddress())
                        .icon(generateBitmapDescriptorFromRes(getApplicationContext(), selectImageMarker(dealer)));

                map.addMarker(markerOptions);
            } else if (selectedCategory.equalsIgnoreCase(dealer.getCategory())){        //vengono visualizzati i marker della categoria selezionata
                MarkerOptions markerOptions = new MarkerOptions().position(getLocationFromAddress(getApplicationContext(), dealer.getCompleteAddress()))
                        .title(dealer.getName())
                        .snippet(dealer.getAddress())
                        .icon(generateBitmapDescriptorFromRes(getApplicationContext(), selectImageMarker(dealer)));
                //.icon(icon);
                map.addMarker(markerOptions);
            }

        }
    }

    //Creazione mappa
    @Override
    public void onMapReady(GoogleMap map) {
        createMarker(map);
        showMap(map);
        map.setOnInfoWindowClickListener(this);
    }

    /*
    Posizione attuale e attributi per la visualizzazione della mappa
     */
    private void showMap (GoogleMap map){
        //permessi
        ActivityCompat.requestPermissions(GuestActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if ((getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            /*
            codice utilizzato per centrare la mappa sulla posizione corrente
             */
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
            {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(41.117143,16.871871))      // setta il centro della mappa a Bari
                        .zoom(14)                   // Setta lo zoom
                        .bearing(0)                // Impostal'orientamento della mappa a est
                        .tilt(40)                   // Imposta l'inclinazione della camera su 30 gradi
                        .build();                   // Crea un CameraPosition dal builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // Posizionamento bussola
                View compassButton = mMapView.findViewWithTag("GoogleMapCompass");
                RelativeLayout.LayoutParams rip = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
                rip.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rip.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rip.addRule(RelativeLayout.ALIGN_PARENT_START,0);
                rip.topMargin = 1700;
                rip.leftMargin = 35;
            }

        }
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            mMapView.onDestroy();
        } catch (NullPointerException e) {}
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }



    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    public void warnUser() {
            AlertDialog dialog = new AlertDialog.Builder(GuestActivity.this)
                    .setTitle(R.string.Welcome)
                    .setMessage(R.string.msgGuestMode)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
            dialog.show();
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));

        }

}
