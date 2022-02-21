package com.example.listview.User;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
    Classe principale lato utente, che contiene la visualizzazione della mappa, creazione dei marker e selezione dei coupon da essi
    Compresa di bussola, geolocalizzazione e interazione con i marker creati
 */

public class ExploreFragment extends Fragment implements SensorEventListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mMapView;

    private FirebaseDatabase mDatabaseDealers;
    private DatabaseReference mReferenceDealers;
    private List<Dealer> dealers = new ArrayList<>();

    private FirebaseDatabase mDatabaseCoupons;
    private DatabaseReference mReferenceCoupons;
    private List<Coupon> coupons = new ArrayList<>();

    TextView tv_step;
    SensorManager sensorManager;
    boolean running = false;
    private TabLayout tabLayout;
    ArrayList<Float> valori = new ArrayList<>();
    private DatabaseReference dbRef;
    String selectedCategory = null;
    private GoogleMap mapCopy = null;   //copia di map per la selezione di un marker specifico presente nella TabLayout

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_explore, container, false);
        mMapView = v.findViewById(R.id.user_list_map);

        tabLayout = v.findViewById(R.id.tabs);
        tabLayout.setTabTextColors(Color.parseColor("#ffffff"), Color.parseColor("#C0C70E"));

        tv_step = v.findViewById(R.id.stepView);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

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

        /*
         * acquisisco sequenzialmente tutti i coupon che sono presenti nel db, creati dal dealer
         */

        mDatabaseCoupons = FirebaseDatabase.getInstance();
        mReferenceCoupons = mDatabaseCoupons.getReference("all_coupons").child("coupon");
        mReferenceCoupons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                coupons.clear();
                List<String> keys = new ArrayList<>();
                List<String> keyNo = new ArrayList<>();
                for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                    keys.add(keyNode.getKey());
                    for (DataSnapshot keyN : keyNode.getChildren()) {
                        keyNo.add(keyN.getKey());
                        Coupon coupon = keyN.getValue(Coupon.class);
                        coupons.add(coupon);
                        Log.d("coupon address", coupon.getCodice());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        initGoogleMap(savedInstanceState);

        //tabLayout posizionata in alto nella sezione 'esplora', serve per la selezione dei marker desiderati sulla mappa
        //ogni item del tab corrisponde ad una categoria
        //ogni case pulisce la mappa e imposta la categoria selezionata con il nome indicato
        //viene chiamato onMapReady per reimpostare la mappa
        tabLayout.setOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()) {
                    case 0:     //primo item della tab per visualizzare tutti i marker
                        selectedCategory = null;
                        onMapReady(mapCopy);
                        //setVisibleMarker(selectedCategory);
                        break;
                    case 1:
                        selectedCategory = "Hotel";
                        mapCopy.clear();        //pulisco la mappa
                        onMapReady(mapCopy);
                        break;
                    case 2:
                        //
                        selectedCategory = "Bed and Breakfast";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;
                    case 3:
                        selectedCategory = "Pizzeria";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;
                        //
                    case 4:
                        selectedCategory = "Restaurant";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;

                    case 5:
                        selectedCategory = "Ice-cream parlour";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;
                    case 6:
                        selectedCategory = "Church";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;

                    case 7:
                        selectedCategory = "Shore";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;

                    case 8:
                        selectedCategory = "Museum";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;

                    case 9:
                        selectedCategory = "Fun";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;

                    case 10:
                        selectedCategory = "Dance Club";
                        mapCopy.clear();
                        onMapReady(mapCopy);
                        break;


                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return v;
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
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
        }catch (NullPointerException e) {}
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null){
            sensorManager.registerListener(ExploreFragment.this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(getContext(), R.string.noSensorFound, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

        SharedPreferences prefs = getActivity().getSharedPreferences("StepCounter", Context.MODE_PRIVATE);
        float passi = prefs.getFloat("Steps", 0);

            SharedPreferences.Editor editor = getActivity().getSharedPreferences("StepCounter", Context.MODE_PRIVATE).edit();
            try{
                editor.putFloat("Steps", valori.get(1) + passi);
                editor.apply();
            } catch (IndexOutOfBoundsException e){
                editor.putFloat("Steps", 0 + passi);
                editor.apply();
            }



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
     * Metodo che serve per creare un'immagine da dare in pasto al creatore del marker
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
        if (dealers.isEmpty()) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(ExploreFragment.this).attach(ExploreFragment.this).commit();
        }
        for (Dealer dealer: dealers) {
            //Toast.makeText(getContext(), dealer.getCategory(), Toast.LENGTH_LONG).show();
            int i = coupons.size();
        if (selectedCategory== null){       //vengono visualizzati tutti i marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(getLocationFromAddress(getContext(), dealer.getCompleteAddress()))
                        .title(dealer.getName())
                        .snippet(dealer.getAddress())
                        .icon(generateBitmapDescriptorFromRes(getContext(), selectImageMarker(dealer)));

                map.addMarker(markerOptions);
            } else if (selectedCategory.equalsIgnoreCase(dealer.getCategory())){        //vengono visualizzati i marker della categoria selezionata
                MarkerOptions markerOptions = new MarkerOptions().position(getLocationFromAddress(getContext(), dealer.getCompleteAddress()))
                        .title(dealer.getName())
                        .snippet(dealer.getAddress())
                        .icon(generateBitmapDescriptorFromRes(getContext(), selectImageMarker(dealer)));
                //.icon(icon);
                map.addMarker(markerOptions);
            }

        }
    }

    //Creazione mappa
    @Override
    public void onMapReady(GoogleMap map) {
        mapCopy = map;
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_photo_black_24dp);
        createMarker(map);
        showMap(map);
        map.setOnInfoWindowClickListener(this);
    }

    /*
    Posizione attuale e attributi per la visualizzazione della mappa
     */
    private void showMap (GoogleMap map){
        //permessi
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if ((getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            map.setMyLocationEnabled(true);
            /*
            codice utilizzato per centrare la mappa sulla posizione corrente
             */
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
            {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(41.117143,16.871871))      // Imposta il centro della mappa a Bari
                        .zoom(14)                   // Setta lo zoom
                        .bearing(0)                // Impostal'orientamento della mappa a est
                        .tilt(40)                   // Imposta l'inclinazione della camera su 30 gradi
                        .build();                   // Crea un CameraPosition dal builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // Posizionamento geolocalizzazione
                View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                // posizionato a destra
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                rlp.setMargins(0, 170, 35, 0);
                //rlp.leftMargin = 20;

                // Posizionamento bussola
                View compassButton = mMapView.findViewWithTag("GoogleMapCompass");
                RelativeLayout.LayoutParams rip = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
                rip.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rip.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rip.addRule(RelativeLayout.ALIGN_PARENT_START,0);
                rip.topMargin = 1700;
                rip.leftMargin = 35;
            }

        } else {

            //getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreFragment()).commit();   //aggiorna
        }
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try{
            mMapView.onDestroy();
        }catch (NullPointerException e) {}
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /*
        Funzione importante che si attiva quando si seleziona un marker, permette di scegliere il coupon da aggiungere
        alla propria lista o visualizzare la posizione dell'attività
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        final ArrayAdapter<String> arrayDesc = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        builderSingle.setTitle(R.string.msgCouponAlertUser);
        for (Coupon cp : coupons){
            if(cp.getAddress().equals(marker.getSnippet())){
                arrayAdapter.add(cp.getNomeCoupon());
                arrayDesc.add(cp.getDescription());
            }
        }
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                String strDesc = arrayDesc.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                builderInner.setMessage(strName + ": " + strDesc);
                dbRef = FirebaseDatabase.getInstance().getReference("coupon_user").child("coupon").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                for (final Coupon cp : coupons){
                    // Controllo unicità coupon
                   if (cp.getNomeCoupon().equals(arrayAdapter.getItem(which))){
                       final String id = dbRef.push().getKey();
                       dbRef.addValueEventListener(new ValueEventListener() {
                           boolean exists = false;
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               for (DataSnapshot data : dataSnapshot.getChildren()) {
                                   Map<String, Object> model = (Map<String, Object>) data.getValue();
                                   if (model.get("nomeCoupon").equals(cp.getNomeCoupon())) {
                                       exists = true;
                                   }
                               }
                               if (exists==true){
                                   Toast.makeText(getContext(), R.string.couponAlready, Toast.LENGTH_LONG).show();
                               } else {
                                   dbRef.child(id).setValue(cp);
                               }
                           }
                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });
                   }
                }
                builderInner.setTitle(R.string.msgSelectedCpAlert);
                builderInner.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog2 = builderInner.create();
                dialog2.show();
                dialog2.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog2.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));
            }
        });
        AlertDialog dialog = builderSingle.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        valori.add(sensorEvent.values[0]);
        valori.add(sensorEvent.values[0]);

        valori.set(1,(sensorEvent.values[0] - valori.get(0)));
        //tv_step.setText(String.valueOf(valori.get(1)));
        //Log.d("passi", String.valueOf(sensorEvent.values[0] - valore));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
