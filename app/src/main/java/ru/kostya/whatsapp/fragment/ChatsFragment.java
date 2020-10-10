package ru.kostya.whatsapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.kostya.whatsapp.adapter.ChatListAdapter;
import ru.kostya.whatsapp.databinding.ChatsFragmentBinding;
import ru.kostya.whatsapp.model.ChatList;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.model.User;

public class ChatsFragment extends Fragment {

    private ChatListAdapter adapter;
    private List<ChatList> chatList;
    private ChatsFragmentBinding binding;
    private FirebaseUser currentUser;
    private Handler handler = new Handler();
    private List<String> allUserId;

    private DatabaseReference reference;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.chats_fragment, container, false);


        binding.chatsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
        allUserId = new ArrayList<>();

        adapter = new ChatListAdapter(chatList, getContext());
        binding.chatsRecView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            getChatList();
        }
        return binding.getRoot();
    }

    private void getChatList() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        reference.child("ChatList").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Очищаем лист со старыми id юзеров,и лист с юзерами
                allUserId.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    //Таким образом мы получаем все id юзеров
                    String userId = data.child("chatId").getValue().toString();
                    Log.d("userId", userId);
                    allUserId.add(userId);
                }
                getUserData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData() {
        //В потоке другом
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Проходимся по всем id шникам,которые есть в нашем листе id
                for (final String uId : allUserId) {
                    //Получаем данные юзера через FireStore,так как данные пользователя хранятся в Users.currentuser.getUid
                    //Через цикл в методе getChatList мы получаем эти Id
                    firestore.collection("Users").document(uId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //Получаем нужные нам данные всех юзеров и добавляем их в список chat list
                            try {
                                String name = documentSnapshot.getString("userName");
                                String urlImage = documentSnapshot.getString("imageProfile");
                                String date = "";
                                String description = "";

                                ChatList currentChatList = new ChatList(uId, name, description, date, urlImage);
                                chatList.add(currentChatList);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (adapter != null) {

                                adapter.notifyDataSetChanged();
                            }
                            binding.progressCircular.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error ", e.getMessage());
                        }
                    });
                }
            }
        });
    }
}
