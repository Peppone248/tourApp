package com.example.listview.User;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.listview.Dealer.Coupon;
import com.example.listview.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*ListCouponUserFragment: Fragment. Pagina dedicata ai coupon che vengono scelti dall'utente sulla mappa. Tutti i coupon che aggiunge
* verranno visualizzati in questa pagina, precisamente in una lista. Si prevede che l'utente possa decidere di attivarlo per poterlo
* utilizzare.
* Il suo layout è: fragment_list_coupon_user.xml*/

public class ListCouponUserFragment extends Fragment implements UserRCViewAdapter.OnItemClickListener {

    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    List<Coupon> cpList = new ArrayList<>();
    RecyclerView userRecyclerView;
    UserRCViewAdapter uAdapter;

    public ListCouponUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_coupon_user, container, false);

        userRecyclerView = (RecyclerView) view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getContext().getResources().getString(R.string.loading));
        progressDialog.show();
        databaseReference = FirebaseDatabase.getInstance().getReference("coupon_user").child("coupon").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        cpList.clear();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Coupon coupons = snapshot.getValue(Coupon.class);
                        coupons.setKey(snapshot.getKey());
                        cpList.add(coupons);
                    }
                    uAdapter = new UserRCViewAdapter(getContext(), cpList);
                    uAdapter.setOnItemClickListener(ListCouponUserFragment.this);
                    userRecyclerView.setAdapter(uAdapter);
                    progressDialog.dismiss();

                    if(cpList.isEmpty()){
                    final AlertDialog.Builder builderEmpty = new AlertDialog.Builder(getActivity());
                    Resources res = getResources();
                    String couponFound = res.getQuantityString(R.plurals.noCoupon, 0);
                    builderEmpty.setTitle(couponFound);
                    builderEmpty.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builderEmpty.show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        return view;
    }



// Attivazione e conseguente rimozione del coupon dalla lista
    @Override
    public void onItemClick(final int position) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle(R.string.activatedCp);
        builderSingle.setMessage(R.string.usedCoupon);
        builderSingle.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Coupon couponz = cpList.get(position);
                final String selectedKey = couponz.getKey(); //recupero la chiave del coupon
                databaseReference.child(selectedKey).removeValue();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListCouponUserFragment()).commit();
            }
        });
        builderSingle.setNegativeButton(R.string.no, null);
        AlertDialog dialog = builderSingle.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));
    }

    @Override
    public void onClick(int position) {

    }
}
