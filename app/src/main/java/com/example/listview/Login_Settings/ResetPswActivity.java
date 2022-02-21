package com.example.listview.Login_Settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.listview.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/*ResetPswActivity: Activity. Si prevede il cambio della password da parte dell'utente*/

public class ResetPswActivity extends AppCompatActivity {

    private EditText emailReset;
    private Button sendBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_psw);

        mAuth = FirebaseAuth.getInstance();
        emailReset = (EditText) findViewById(R.id.emailReset);
        sendBtn = (Button) findViewById(R.id.sendBtn);

        /*Firebase semplifica questo processo e consente il cambio della password tramite indirizzo email
         */
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userEmail = emailReset.getText().toString();
                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPswActivity.this, R.string.correctMail, Toast.LENGTH_SHORT).show();
                } else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPswActivity.this, R.string.checkMail, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPswActivity.this, MainActivity.class));
                            } else{
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPswActivity.this, R.string.error + ": " + message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });
    }
}