package ru.kostya.whatsapp.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.kostya.whatsapp.interfaces.OnReadChatCallBack;
import ru.kostya.whatsapp.model.Chats;
import ru.kostya.whatsapp.views.chats.ChatActivity;

public class ChatService {
    private Context context;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String receiverID;

    public ChatService(Context context, String receiverID) {
        this.context = context;
        this.receiverID = receiverID;
    }

    public void readChatDataBase(final OnReadChatCallBack onCallBack) {
        //Считка данных из firebase,всех наших "пушей"
        final List<Chats> list = new ArrayList<>();
        //Получаем доступ по ссылку к Chats
        reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //В chats хранятся только id,созданные пушами
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Проходим по детям ссылки Chats
                    //Таким образом мы проходимся по всем созданным chat'am получаем,запихиваем в chatList
                    //Получаем данные в класс chats,это все делаем в цикле
                    Chats chats = data.getValue(Chats.class);

                    //Не понимаю смысл данной проверки,я понимаю,что она означает,но не знаю зачем она
                    if (chats != null && String.valueOf(chats.getSender()).equals(String.valueOf(currentUser.getUid())) && chats.getReceiver().equals(receiverID)
                            || String.valueOf(chats.getReceiver()).equals(currentUser.getUid()) && String.valueOf(chats.getSender()).equals(receiverID)) {
                        //Добавляем этого chats в List,адаптер которого будет отображать нужные данные,чекай ChatsAdapter
                        list.add(chats);
                    }
                }
                onCallBack.onReadSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onCallBack.onReadFailed();
            }
        });
    }

    public void sendTextMsg(String text, final OnReadChatCallBack onReadChatCallBack) {

        Chats chats = new Chats(getCurrentDate(),
                text,"","TEXT",
                currentUser.getUid(),
                receiverID);

        reference.child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Проверка на успешное добавление обЪекта chats in reference

                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Проверка на неуспешное добавление обЪекта chats in reference
                //Вызываем метод нашего интерфейса
                onReadChatCallBack.onReadFailed();
                Log.d("Send", "onFailure: "+e.getMessage());
            }
        });

        //Получаем доступ к ссылку Chats.child(отправитель).child(получатель).child(chatId).setVALUE(id получателя),тоесть получается,что у id
        //чата будет совпадать с id получаателя сообщения
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid()).child(receiverID);
        chatRef1.child("chatId").setValue(receiverID);

        //
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(currentUser.getUid());
        chatRef2.child("chatId").setValue(currentUser.getUid());

    }

    public String getCurrentDate(){

        //Дата
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        return today+", "+currentTime;
    }

    public void sendImage(String imageUrl){
//При отправки фото
        Chats chats = new Chats(
                getCurrentDate(),
                "",
                imageUrl,
                "IMAGE",
                currentUser.getUid(),
                receiverID
        );

        reference.child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //При успешной отправки данных в fb
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //При неуспешной отправки данных в fb
            }
        });


        //Получаем доступ к ссылку Chats.child(отправитель).child(получатель).child(chatId).setVALUE(id получателя),тоесть получается,что у id
        //чата будет совпадать с id получаателя сообщения
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid()).child(receiverID);
        chatRef1.child("chatId").setValue(receiverID);

        //
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(currentUser.getUid());
        chatRef2.child("chatId").setValue(currentUser.getUid());
    }

    public void sendVoice(String audioPath){
        //При отправке голосового сообщения
        //Получаем uri нашего гол. сообщения,лежащегго по адресу new File(audioPath)
        final Uri uriAudio = Uri.fromFile(new File(audioPath));
        //Ссылка storagereference,где хранятся картинки и т.п
        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot audioSnapshot) {

                //При успешной отправке файла

                Task<Uri> urlTask = audioSnapshot.getStorage().getDownloadUrl();
                //Пока есть url'ы
                while (!urlTask.isSuccessful()) ;
                //Записываем их в переменную download url это uri,чтобы получить url нужно привести его к типу String
                Uri downloadUrl = urlTask.getResult();
                //Приводим к типу String
                String voiceUrl = String.valueOf(downloadUrl);

                //Создаем обЪект класа Chats
                Chats chats = new Chats(
                        getCurrentDate(),
                        "",
                        voiceUrl,
                        "VOICE",
                        currentUser.getUid(),
                        receiverID
                );


                //Отпраялем его в RealtimeDataBase,точнее "Пушим " при пуше создается уникальный идентификатор,в данном случае
                //При пуше создастся id  для chats
                reference.child("Chats").push().setValue(chats).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Успешно
                        Log.d("Send", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Без успешно
                        Log.d("Send", "onFailure: "+e.getMessage());
                    }
                });

                //Add to ChatList
                //Это все по дефолту,это я обЪяснял раннее
                DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid()).child(receiverID);
                chatRef1.child("chatid").setValue(receiverID);

                //
                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(currentUser.getUid());
                chatRef2.child("chatid").setValue(currentUser.getUid());
            }
        });

}
}
