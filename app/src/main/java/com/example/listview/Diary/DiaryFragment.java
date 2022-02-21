package com.example.listview.Diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.listview.R;

/* DiaryFragment: Fragment. Pagina principale del diario. L'utente attraverso i pulsanti verrà reindirizzato nella sezione da lui scelta*/

public class DiaryFragment extends Fragment {

    private Button openImgBtn, openUploadBtn;
    View view;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_diary, container, false);

        openImgBtn = (Button) view.findViewById(R.id.openImgBtn);
        openImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment selectedFragment = null;
                selectedFragment = new ItemsFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        });

        openUploadBtn = (Button) view.findViewById(R.id.openUploadBtn);
        openUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment selectedFragment = null;
                selectedFragment = new UploadFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        });
        return view;
    }
}


