package com.example.listview.User;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.example.listview.Login_Settings.MainActivity;
import com.example.listview.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/*ProfileInformation: Activity, sezione dove l'utente loggato potrà consultare e/o modficiare i suoi dati inseriti nel momento della registrazione*/

public class ProfileInformation extends AppCompatActivity {

    private static final String TAG = "UserProfile";
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profile;
    private Uri mImageUri;
    private static boolean isEmpty=true;
    private TextView upload;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference myRef;
    private StorageTask mUploadTask;
    private String userID;

    public void onBackPressed() {
        Intent refresh = new Intent(ProfileInformation.this, UserPage.class);
        startActivity(refresh);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile = findViewById(R.id.profileImg);
        upload = findViewById(R.id.uploadImgBtn);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference("profile");
        userID = user.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("prof_images").child(userID);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // L'utente ha effettuato l'accesso
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    // toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // L'utente è disconnesso
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    // toastMessage("Successfully signed out.");
                }
                // ...
            }
        };

       mDatabaseRef.child("imgKey").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    ProfileImage img = dataSnapshot.getValue(ProfileImage.class);
                    Picasso.with(ProfileInformation.this).load(img.getUrl()).placeholder(R.drawable.blankavatar).into(profile);
                } catch (NullPointerException e){
                    
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPic();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ProfileInformation.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                openFileChooser();
            }
        });

        //metodo per l'eliminazione del profilo
        Button delete = findViewById(R.id.deleteBtn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(ProfileInformation.this)
                        .setTitle(R.string.DeleteAccount)
                        .setMessage(R.string.deleteConfirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            myRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
                                                            myRef.removeValue();
                                                            user.reload();
                                                            Toast.makeText(ProfileInformation.this, getString(R.string.delAccount), Toast.LENGTH_LONG).show();
                                                            Intent login = new Intent(ProfileInformation.this, MainActivity.class);
                                                            startActivity(login);
                                                        } else {
                                                            Log.d(TAG, "Can't delete account for some reason");
                                                        }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));
            }
        });

        //metodo per la modifica della email
        Button updateMail = findViewById(R.id.updateMail);
        updateMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputEditTextField = new EditText(ProfileInformation.this);

                AlertDialog dialog = new AlertDialog.Builder(ProfileInformation.this)
                        .setTitle(R.string.updateMail)
                        .setMessage(R.string.newMailInput)
                        .setView(editLayout(inputEditTextField))
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.updateEmail(inputEditTextField.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful() & isEmailValid(inputEditTextField)) {
                                                    myRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("email");
                                                    myRef.setValue(inputEditTextField.getText().toString().toLowerCase());
                                                    Intent refresh = new Intent(ProfileInformation.this, ProfileInformation.class);
                                                    startActivity(refresh);
                                                    Toast.makeText(ProfileInformation.this, R.string.up, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(ProfileInformation.this, R.string.updateMailError, Toast.LENGTH_SHORT).show();
                                                    Intent refresh = new Intent(ProfileInformation.this, ProfileInformation.class);
                                                    startActivity(refresh);
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));
            }
        });

        // Riferimento del database che ottiene tutti gli utenti (child(dealers)) e che trova
        // l'utente loggato attraverso child(userID).
        // NON cambiare eventListener con singleValue perché bisogna iterare tutte le istanze
        myRef.child("users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(ProfileInformation.this, UserPage.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openFileChooser() {
        // Richiesta dei permessi sullo Storage
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(ProfileInformation.this, R.string.msgPermissionStorage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();
            Picasso.with(this).load(mImageUri).placeholder(R.drawable.blankavatar).into(profile);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadPic() {
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ProfileInformation.this, R.string.uploadSucc, Toast.LENGTH_LONG).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            final ProfileImage prova = new ProfileImage(url);
                            String id = "imgKey";
                            //Toast.makeText(getApplicationContext(), id.toString(), Toast.LENGTH_LONG).show();
                            mDatabaseRef.child(id).setValue(prova);
                            isEmpty=false;
                        }
                    });
                }
            });
        }
    }

    private void showData(@NonNull DataSnapshot dataSnapshot) {

        User uInfo = dataSnapshot.getValue(User.class); // Ottiene fisicamente tutti i dati degli utenti secondo la classe creata

        if (mAuth.getCurrentUser() != null) {

            TextView showEmail = findViewById(R.id.showEmail);  // Assegnamento del pulsante al campo
            TextView email = findViewById(R.id.emailView);
            email.setText(uInfo.getEmail());
            showEmail.setText(showEmail.getText().toString() + ":");  // Scrittura del campo

            TextView showFirstName = findViewById(R.id.showFirstName);
            TextView firstName = findViewById(R.id.firstName);
            firstName.setText(uInfo.getFirstName());
            showFirstName.setText(showFirstName.getText().toString() + ":");


            TextView showLastName = findViewById(R.id.showLastName);
            TextView lastName = findViewById(R.id.lastNameView);
            lastName.setText(uInfo.getLastName());
            showLastName.setText(showLastName.getText().toString() + ":");


            SharedPreferences prefs = getSharedPreferences("StepCounter", Context.MODE_PRIVATE);
            float passi = prefs.getFloat("Steps", 0);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("Steps", passi);
            editor.apply();

            TextView showStepCounter = findViewById(R.id.stepView);
            showStepCounter.setText(String.valueOf((int)passi));

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //modifica del layout
    public RelativeLayout editLayout (EditText edit) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(36, 36, 36, 36);
        edit.setLayoutParams(lp);
        RelativeLayout container = new RelativeLayout(ProfileInformation.this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(rlParams);
        container.addView(edit);
        return container;
    }


    //controllo sulla email
    private boolean isEmailValid(EditText editText) {
        String emailInput = editText.getText().toString().trim();

        if (emailInput.isEmpty()) {
            editText.setError(getText(R.string.errorMsgField));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            editText.setError(getText(R.string.emailInvalid));
            return false;
        } else {
            editText.setError(null);
            return true;
        }
    }

}