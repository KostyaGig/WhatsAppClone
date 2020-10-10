package ru.kostya.whatsapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.StatusFragmentBinding;

public class StatusFragment extends Fragment {

    private StatusFragmentBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.status_fragment,container,false);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Для отображения фото,имени и т.д юзера в status_fragment.xml
        getProfile();

        return binding.getRoot();
    }

    private void getProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestore.collection("Users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //Если все успешно
                String imageUrl = documentSnapshot.getString("imageProfile");

                Glide.with(getContext()).load(imageUrl).into(binding.profileImage);
                binding.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            //Если есть ошибка
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("fail","Fail " +  e.getMessage());
                binding.profileImage.setImageDrawable(getContext().getDrawable(R.drawable.profile));
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
