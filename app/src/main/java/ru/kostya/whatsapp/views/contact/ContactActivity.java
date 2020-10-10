package ru.kostya.whatsapp.views.contact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ru.kostya.whatsapp.adapter.ContactAdapter;
import ru.kostya.whatsapp.model.User;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.ActivityContactBinding;

public class ContactActivity extends AppCompatActivity {

    ActivityContactBinding binding;

    private List<User> userList;
    private LinearLayoutManager manager;
    private ContactAdapter adapter;
    private RecyclerView contactRecyclerView;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_contact);

        userList = new ArrayList<>();

        manager = new LinearLayoutManager(this);
        adapter = new ContactAdapter(userList,this);

        binding.contactRecyclerView.setAdapter(adapter);
        binding.contactRecyclerView.setLayoutManager(manager);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        //Если текущий юзер зареган,получаем лист юзеров из firebase
        if (currentUser != null){
            getContactList();
        }

    }

    private void getContactList() {
        //Получаем ветку Users
        firestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //Проходимся по всем существующим юзерам
                //Именно в цикле получаем новые данные
                //Таким образом мы получаем нужныеы нам данные и каждый цикл будут записаны новые данные о новом юезере))
                //snapshot - user
                for (QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                    //Получаем данные о каждом юзере
                    String userId = snapshot.getString("userID");
                    String userName = snapshot.getString("userName");
                    String imageUrl = snapshot.getString("imageProfile");
                    String bio = snapshot.getString("bio");

                    //Крутимся по циклу и добавляем данные о каждом существующем юзере в лист
                    User user = new User(userId,userName,"",imageUrl,"","","","","",bio);

                    //Если Id юзера из firebase не совпадает с id,под которым он сейчас,таким образом мы понимаем,что это не один и тот же юзер
                    if (userId != null && !userId.equals(String.valueOf(currentUser.getUid()))){
                        //Если этот не он,то добавляем юзера в лист из юзеров,это нужно для того чтобы пользователь не видел в списпке контактов самого себя!!!
                        userList.add(user);
                    }
                }
                //Не забываем обновить адаптер
                adapter.notifyDataSetChanged();
            }
        });
    }
}