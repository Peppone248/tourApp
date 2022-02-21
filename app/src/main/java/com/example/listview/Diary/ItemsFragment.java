package com.example.listview.Diary;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.listview.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/*ItemsFragment: Fragment, permette la visualizzazione del diario del viaggiatore */

public class ItemsFragment extends Fragment implements RecyclerAdapter.OnItemClickListener {

    private View view;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private ProgressBar mProgressBar;
    private Button uploadBtn;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<DiaryElement> mDElements;

    // Schermata dettagliata della foto caricata
    private void openDetailActivity(String[] data) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("NAME_KEY", data[0]);
        intent.putExtra("DESCRIPTION_KEY", data[1]);
        intent.putExtra("IMAGE_KEY", data[2]);
        startActivity(intent);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_items, container, false);
        mRecyclerView = view.findViewById(R.id.mRecyclerView);
        uploadBtn = view.findViewById(R.id.uploadBtn1);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgressBar = view.findViewById(R.id.myDataLoaderProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN );
        mDElements = new ArrayList<>();
        mAdapter = new RecyclerAdapter(getActivity(), mDElements);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ItemsFragment.this);
        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("all_media").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("images"); //ricerca del riferimento nel DB contenente i file necessari
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {

            //sfrutto il metodo asincrono onDataChange per aggiungere alla lista, gli elementi caricati dal DB
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDElements.clear();
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    DiaryElement upload = imageSnapshot.getValue(DiaryElement.class);
                    upload.setKey(imageSnapshot.getKey());
                    mDElements.add(upload);
                }
                mAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment selectedFragment = null;
                selectedFragment = new UploadFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        });
        return view;
    }

    @Override
    public void onItemClick(int position) {
        DiaryElement clickedElement = mDElements.get(position);
        String[] elementsData = {clickedElement.getName(), clickedElement.getDescription(), clickedElement.getImageUrl()};
        openDetailActivity(elementsData);
    }

    // Visulizzazione della foto caricata
    @Override
    public void onShowItemClick(int position) {
        DiaryElement clickedElement = mDElements.get(position);
        String[] elementsData = {clickedElement.getName(), clickedElement.getDescription(), clickedElement.getImageUrl()};
        openDetailActivity(elementsData);
    }

    // Processo di rimozione della foto caricata
    @Override
    public void onDeleteItemClick(int position) {
        final DiaryElement selectedItem = mDElements.get(position);
        final String selectedKey = selectedItem.getKey();
        final StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(getActivity(), R.string.delete, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

}

