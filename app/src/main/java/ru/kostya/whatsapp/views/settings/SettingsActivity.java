package ru.kostya.whatsapp.views.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ru.kostya.whatsapp.views.profile.ProfileActivity;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_settings);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            getInfo();
        } else {
            Toast.makeText(this, "Сначала войдите в аккаунт!", Toast.LENGTH_SHORT).show();
        }

        //Если мы наживаем на блок сетинг активити связанный с профилем юзера,то перекидываем его на profile activity,где он сможет изменить свое имя или ще что то
        binding.linearProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileActivity);
            }
        });
    }


    private void getInfo(){
        firestore.collection("Users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.get("userName").toString();
                String imageUrl = documentSnapshot.get("imageProfile").toString();

                binding.tvUsername.setText(userName);
                Glide.with(SettingsActivity.this).load(imageUrl).into(binding.imageProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ErrorSetting",e.getMessage());
                Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

}