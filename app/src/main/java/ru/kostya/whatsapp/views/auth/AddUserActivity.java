package ru.kostya.whatsapp.views.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import ru.kostya.whatsapp.views.MainActivity;
import ru.kostya.whatsapp.model.User;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.ActivityAddUserBinding;

public class AddUserActivity extends AppCompatActivity {

    private ActivityAddUserBinding binding;
    private ProgressDialog pd;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_user);
        createProgressDialog();

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 name = binding.edName.getText().toString().trim();

                if (TextUtils.isEmpty(name)){
                    Toast.makeText(getApplicationContext(),"Please input username", Toast.LENGTH_SHORT).show();
                } else {
                    pd.show();
                    doUpdate();
                }
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pickImage();
                // I will do next video
                Toast.makeText(getApplicationContext(),"This function is not ready to use",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void doUpdate() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null){
            String userId = currentUser.getUid();
            String phoneNumber = currentUser.getPhoneNumber();

            User user = new User(
                    userId,
                    name,
                    phoneNumber,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
            firebaseFirestore.collection("Users").document(userId).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(),"Update Successful",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Log.d("Update", "onFailure: "+e.getMessage());
                    Toast.makeText(getApplicationContext(),"Error update :"+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(),"Cначала вам нужно войти в систему!",Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }

    private void createProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setTitle("Загрузка");
        pd.setMessage("Пожалуйста,подождите...");
        pd.setCancelable(false);
    }
}