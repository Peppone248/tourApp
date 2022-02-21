package com.example.listview.Dealer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.listview.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*ListCouponFragment: Fragment che mostra tutti i coupon che ha creato il deler. Il Fragment contiene metodi per:
* 1) Creazione di una recylerView con il suo Adapter: riempita dai coupon creati, presenti nel database;
* 2) Possibilità di cancellare dalla lista (e dal database) un coupon.
* Il layout è fragment_list_coupon.xml
* */

public class ListCouponFragment extends Fragment implements RcViewAdapter.OnItemClickListener {

    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    List<Coupon> list = new ArrayList<>();
    RecyclerView recyclerView;
    RcViewAdapter adapter;

    public ListCouponFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_coupon, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getContext().getResources().getString(R.string.loading));
        progressDialog.show();
        databaseReference = FirebaseDatabase.getInstance().getReference("all_coupons").child("coupon").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Coupon coupons = snapshot.getValue(Coupon.class);
                    coupons.setKey(snapshot.getKey());
                    list.add(coupons);
                }
                adapter = new RcViewAdapter(getContext(), list);
                adapter.setOnItemClickListener(ListCouponFragment.this);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();

                // AlertDialog se non ci sono coupon
                if(list.isEmpty()){
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


    @Override
    public void onItemClick(int position) {

    }

    //Metodo per la cancellazione di un coupon
    @Override
    public void onDeleteItemClick(int position) {
        final Coupon selectedItem = list.get(position); //recupero la posizione del coupon nella lista
        final String selectedKey = selectedItem.getKey(); //recupero la chiave del coupon
        databaseReference.child(selectedKey).removeValue(); //eliminazione coupon dal database attraverso la chiave
        getFragmentManager().beginTransaction().replace(R.id.fragment_container_dealer, new ListCouponFragment()).commit();
    }
}
