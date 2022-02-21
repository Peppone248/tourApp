package com.example.listview.Dealer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
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

import com.example.listview.Login_Settings.MainActivity;
import com.example.listview.R;
import com.example.listview.User.ProfileImage;
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

/*Classe che mostra le informazioni del Dealer registrato all'app, prendendo i dati dal DB*/

public class ProfileInformationDealer extends AppCompatActivity {

    private static final String TAG = "ProfileDealer";
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profile;
    private Uri mImageUri;
    private TextView upload;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private StorageTask mUploadTask;
    private String userID;

// Ricarica la pagina
    public void onBackPressed() {
        Intent refresh = new Intent(ProfileInformationDealer.this, DealerPage.class);
        startActivity(refresh);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information_dealer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile = findViewById(R.id.dealerProfImg);
        upload = findViewById(R.id.uploadPicD);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        Button delete = findViewById(R.id.deleteDBtn);
        mStorageRef = FirebaseStorage.getInstance().getReference("profile");
        userID = user.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("prof_images").child(userID);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    // toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    // toastMessage("Successfully signed out.");
                }
                // ...
            }
        };

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ProfileInformationDealer.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                openFileChooser();
            }
        });

        mDatabaseRef.child("imgKey").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    ProfileImage img = dataSnapshot.getValue(ProfileImage.class);
                    Picasso.with(ProfileInformationDealer.this).load(img.getUrl()).placeholder(R.drawable.blankavatar).into(profile);
                } catch (NullPointerException e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Riferimento del database che ottiene tutti gli utenti (child(dealers)) e che trova
        // l'utente loggato attraverso child(userID).
        myRef.child("dealers").child(userID).addValueEventListener(new ValueEventListener() {
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

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPic();
            }
        });

        // Metodo per la modifica di una mail tramite un button
        Button updateMailDealer = findViewById(R.id.updateMailDealer);
        updateMailDealer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputEditTextField = new EditText(ProfileInformationDealer.this);

                AlertDialog dialog = new AlertDialog.Builder(ProfileInformationDealer.this)
                        .setTitle(R.string.updateMail)
                        .setMessage(R.string.newMailInput)
                        .setView(editLayout(inputEditTextField))
                        .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.updateEmail(inputEditTextField.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful() & isEmailValid(inputEditTextField)) {
                                                    myRef = FirebaseDatabase.getInstance().getReference("dealers").child(userID).child("email");
                                                    myRef.setValue(inputEditTextField.getText().toString().toLowerCase());
                                                    refreshProfile();
                                                    Toast.makeText(ProfileInformationDealer.this, getString(R.string.up), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(ProfileInformationDealer.this, R.string.updateMailError, Toast.LENGTH_SHORT).show();
                                                    refreshProfile();
                                                }
                                            }
                                        });
                            }
                        })

                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));

            }
        });

        // Metodo per la modifica dell'indirizzo, stessa implementazione della modifica e-mail
        TextView updateAddress = findViewById(R.id.updateAddress);
        updateAddress.setPaintFlags(updateAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        updateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputEditTextField = new EditText(ProfileInformationDealer.this);
                AlertDialog dialog = new AlertDialog.Builder(ProfileInformationDealer.this)
                        .setTitle(getString(R.string.chgAddress))
                        .setMessage(getString(R.string.inputHere))
                        .setView(editLayout(inputEditTextField))
                        .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myRef = FirebaseDatabase.getInstance().getReference("dealers").child(userID).child("address");
                                String editTextInput = inputEditTextField.getText().toString();
                                myRef.setValue(editTextInput);
                                refreshProfile();
                                Toast.makeText(ProfileInformationDealer.this, getString(R.string.up), Toast.LENGTH_LONG).show();
                            }
                        })

                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));

            }
        });

        // Metodo per la modifica del numero di telefono, stessa implementazione delle precedenti
        TextView updatePhone = findViewById(R.id.updatePhone);
        updatePhone.setPaintFlags(updatePhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        updatePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputEditTextField = new EditText(ProfileInformationDealer.this);

                AlertDialog dialog = new AlertDialog.Builder(ProfileInformationDealer.this)
                        .setTitle(getString(R.string.chgPhone))
                        .setMessage(getString(R.string.inputHere))
                        .setView(editLayout(inputEditTextField))
                        .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myRef = FirebaseDatabase.getInstance().getReference("dealers").child(userID).child("telephone");
                                String editTextInput = inputEditTextField.getText().toString();
                                myRef.setValue(editTextInput);
                                refreshProfile();
                                Toast.makeText(ProfileInformationDealer.this, getString(R.string.up), Toast.LENGTH_LONG).show();
                            }
                        })

                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));

            }
        });

        // Metodo per la modifica del nome, stessa implementazione delle precedenti
        TextView updateName = findViewById(R.id.updateName);
        updateName.setPaintFlags(updateName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputEditTextField = new EditText(ProfileInformationDealer.this);


                AlertDialog dialog = new AlertDialog.Builder(ProfileInformationDealer.this)
                        .setTitle(getString(R.string.chgName))
                        .setMessage(getString(R.string.inputHere))
                        .setView(editLayout(inputEditTextField))
                        .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myRef = FirebaseDatabase.getInstance().getReference("dealers").child(userID).child("name");
                                String editTextInput = inputEditTextField.getText().toString();
                                myRef.setValue(editTextInput);
                                refreshProfile();
                                Toast.makeText(ProfileInformationDealer.this, getString(R.string.up), Toast.LENGTH_LONG).show();
                            }
                        })

                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));

            }
        });

        // Rimozione dell'account dal database
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(ProfileInformationDealer.this)
                        .setTitle(R.string.DeleteAccount)
                        .setMessage(R.string.deleteConfirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myRef = FirebaseDatabase.getInstance().getReference("dealers").child(userID);
                                            myRef.removeValue();
                                            user.reload();
                                            Toast.makeText(ProfileInformationDealer.this, getString(R.string.delAccount), Toast.LENGTH_LONG).show();
                                            Intent login = new Intent(ProfileInformationDealer.this, MainActivity.class);
                                            startActivity(login);
                                        } else {
                                            Log.d(TAG, "Can't delete account for some reason");
                                        }

                                    }
                                });
                            }
                        })

                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#00574B"));
                dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00574B"));
            }
        });
    }

    private void openFileChooser() {
        // Richiesta dei permessi sullo Storage
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(ProfileInformationDealer.this, R.string.msgPermissionStorage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
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
                    Toast.makeText(ProfileInformationDealer.this, R.string.uploadSucc, Toast.LENGTH_LONG).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            final ProfileImage prova = new ProfileImage(url);
                            String id = "imgKey";
                            //Toast.makeText(getApplicationContext(), id.toString(), Toast.LENGTH_LONG).show();
                            mDatabaseRef.child(id).setValue(prova);
                            //isEmpty=false;
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(ProfileInformationDealer.this, DealerPage.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showData(@NonNull DataSnapshot dataSnapshot) {

        Dealer dInfo = dataSnapshot.getValue(Dealer.class); // Ottiene fisicamente tutti i dati degli utenti secondo la classe creata

        if(mAuth.getCurrentUser() != null) {

            //Visualizzazione dei dati
            TextView showEmailD = findViewById(R.id.showEmailD);  // Assegnamento del pulsante al campo
            TextView emailDeal = findViewById(R.id.dealEmail);
            showEmailD.setText(showEmailD.getText().toString() + ":");  // Scrittura del campo
            emailDeal.setText(dInfo.getEmail());

            TextView showAddress = findViewById(R.id.showAddress);
            TextView addressDeal = findViewById(R.id.dealAddress);
            showAddress.setText(showAddress.getText().toString() + ":");
            addressDeal.setText(dInfo.getAddress());

            TextView showName = findViewById(R.id.showNameD);
            TextView nameDeal = findViewById(R.id.dealName);
            showName.setText(showName.getText().toString() + ":");
            nameDeal.setText(dInfo.getName());

            TextView showPhoneNumber = findViewById(R.id.PhoneNumber);
            TextView phoneDeal = findViewById(R.id.dealPhone);
            showPhoneNumber.setText(showPhoneNumber.getText().toString() + ":");
            phoneDeal.setText(dInfo.getTelephone());

            TextView showCategory = findViewById(R.id.showCat);
            TextView catDeal = findViewById(R.id.dealCat);
            showCategory.setText(showCategory.getText().toString() + ":");
            catDeal.setText(dInfo.getCategory());

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


    public void refreshProfile () {
        Intent refresh = new Intent(ProfileInformationDealer.this, ProfileInformationDealer.class);
        startActivity(refresh);
    }

    // Set Layout

    public RelativeLayout editLayout (EditText edit) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(36, 36, 36, 36);
        edit.setLayoutParams(lp);
        RelativeLayout container = new RelativeLayout(ProfileInformationDealer.this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(rlParams);
        container.addView(edit);
        return container;
    }

    // Controlli sulla mail
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
