package ru.kostya.whatsapp.views.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.adapter.ChatsAdapter;
import ru.kostya.whatsapp.adapter.ContactAdapter;
import ru.kostya.whatsapp.databinding.ActivityUserProfileBinding;
import ru.kostya.whatsapp.views.chats.ChatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private String userName;
    private String userProfileUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_user_profile);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Принимаем интент и имя с фото юзера из chatActivity по клику на фото юзера в тулбаре чекнидля пониятия activity_chat.xml
        Intent getIntent = getIntent();

        userName = getIntent.getStringExtra(ContactAdapter.USER_NAME);
        userProfileUrl = getIntent.getStringExtra(ContactAdapter.IMAGE_PROFILE);

        //Если url юзера равен пустоте ничему
        if (userProfileUrl.equals("")){
            binding.profileImage.setImageDrawable(getDrawable(R.drawable.profile));
        }else {
            Glide.with(this).load(userProfileUrl).placeholder(R.mipmap.ic_launcher).into(binding.profileImage);
        }
        initToolbar();
    }

    private void initToolbar() {

        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}