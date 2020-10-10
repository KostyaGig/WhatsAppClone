package ru.kostya.whatsapp.views.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.adapter.ChatsAdapter;
import ru.kostya.whatsapp.adapter.ContactAdapter.*;
import ru.kostya.whatsapp.databinding.ActivityChatBinding;
import ru.kostya.whatsapp.dialog.DialogReviewSendImage;
import ru.kostya.whatsapp.interfaces.OnReadChatCallBack;
import ru.kostya.whatsapp.managers.ChatService;
import ru.kostya.whatsapp.managers.FirebaseService;
import ru.kostya.whatsapp.model.Chats;
import ru.kostya.whatsapp.views.MainActivity;
import ru.kostya.whatsapp.views.profile.UserProfileActivity;

import static ru.kostya.whatsapp.adapter.ContactAdapter.*;



public class ChatActivity extends AppCompatActivity {

    public static final String CHAT_ERROR_LOG = "ChatError";
    private static final int REQUEST_CORD_PERMISSION = 222;
    private ActivityChatBinding binding;
    private String receiverId;
    private ChatsAdapter adapter;
    private List<Chats> chatsList;

    private String userIdContactUser;
    private String nameContactUser;
    private String  imageContactUser;

    private boolean isActionShown = false;
    private ChatService chatService;

    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;

    private ProgressDialog pd;

    private MediaRecorder mediaRecorder;
    private String audio_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat);


        //Голосовое сообщение

        //Получаем intent из нашего contactAdapter(adapter contactRecyclerView)
        Intent getIntent = getIntent();

        //Константы из ContactAdapter
        //Получаем нужные данные по нажавшему юзеру,мы их получаем из ContactAdapter и ChatsAdapter
         userIdContactUser = getIntent.getStringExtra(USER_ID);
         nameContactUser = getIntent.getStringExtra(USER_NAME);
         imageContactUser = getIntent.getStringExtra(IMAGE_PROFILE);

        //Для получения id получателя
        receiverId = userIdContactUser;

        //init chatService
        chatService = new ChatService(this,receiverId);

        //При клике из адаптера мы получаем url картинки и если он окажется равным пустоте,то ставим фото по дефолту
        if (imageContactUser.equals("")){
            binding.imageProfile.setImageDrawable(getDrawable(R.drawable.profile));
        } else {
            Glide.with(this).load(imageContactUser).into(binding.imageProfile);
        }

        loadDataContactUserInToolbar(nameContactUser,imageContactUser);


        chatsList = new ArrayList<>();
        adapter = new ChatsAdapter(this,chatsList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,true);
        linearLayoutManager.setStackFromEnd(true);
        binding.chatsRecView.setLayoutManager(linearLayoutManager);
        binding.chatsRecView.setAdapter(adapter);

        createProgressDialog();
        readChatsFromFirebase();

        initClick();

       binding.recordButton.setRecordView(binding.recordView);
    }

    private void readChatsFromFirebase() {
        //Наше метод для считывания данных их Firebase
        chatService.readChatDataBase(new OnReadChatCallBack() {
            @Override
            public void onReadSuccess(List<Chats> chatsList) {
                //При успешной считке
                //Наше метод в адаптере,устанавливает лист и обновляет адаптер
                //Устанавливается List нашему адаптеру из метода readChatDataBase,класса ChatsService
                //Метод устанавивает лист адаптеру и сразу обновляет адаптер
                adapter.setChatsList(chatsList);
            }

            @Override
            public void onReadFailed() {
                Log.d("readFail","Read is Fail");
            }
        });
    }

    private void initClick() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Вешаем слушатель на editText,срабатывающий при изменении текста
        binding.edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Если при изменении текста в editText TEXT EDITTEXT ПУСТОЙ,ТО
                if (!TextUtils.isEmpty(binding.edMessage.getText().toString().trim())){
                    //Если НЕ пустой будет эдит текст,то мы будем показывать нашу fab с иконкой send
                    //И скрываем нашу голосовую кнопку
                    binding.recordButton.setVisibility(View.GONE);
                    binding.btnSend.setVisibility(View.VISIBLE);
                    //Иконка send и так стоит по умолчанию в нашем layout_chat.xml,но пусть будет эта строчка кода
                    //Вдруг пригодится
                    binding.btnSend.setImageDrawable(getDrawable(R.drawable.ic_baseline_send_24));

                } else {
                    //Иначе
                    binding.recordButton.setVisibility(View.VISIBLE);
                    binding.btnSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(binding.edMessage.getText().toString())){
                    //Метод который отправляет текстовое сообщение из editText
                    //Наш метод класса ChatService,такие методы нужны для того,чтобы не захломлять код
                    chatService.sendTextMsg(binding.edMessage.getText().toString(), new OnReadChatCallBack() {
                        @Override
                        public void onReadSuccess(List<Chats> chatsList) {

                        }

                        @Override
                        public void onReadFailed() {
                            Log.d("No","NO");
                        }
                    });
                    //Очищаем editText
                    binding.edMessage.setText("");
                }
            }
        });
        //По нажатию на фото юзера в тулбаре
        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent userProfileIntent = new Intent(ChatActivity.this, UserProfileActivity.class);
                //Передаем имя и фото ющ
                userProfileIntent.putExtra(USER_NAME,nameContactUser);
                userProfileIntent.putExtra(IMAGE_PROFILE,imageContactUser);
                startActivity(userProfileIntent);
            }
        });

        //По нажатию на прикрепить файл
        binding.btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Показываем наш кардвью с выбором

                //Если показано
                if (isActionShown){
                    //Скрываем нашу карточку и isActionShown = false
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;
                }
                //Если не показано,то показываем и делаем isActionShown = true
                else {
                    binding.layoutActions.setVisibility(View.VISIBLE);
                    isActionShown = true;
                }
                //ВСЕ ЭТО НУЖНО,ЧТОБЫ ПО НАЖАТИЮ НА СКРЕПКУ (ЗАКРЕПИТЬ ФАЙЛ) НАША КАРТОЧКА ПОКАЗЫВАЛАСЬ И ЕСЛИ ОНА ПОКАЗАНА СКРЫВАЛАСЬ
            }
        });

        //По нажатию на фотку с галлереей,которая находится в нашей карточке,короче перейди по ссылку ctrl + lcm
        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        //Listener recordView (голосовое сообщение)

        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Log.d(CHAT_ERROR_LOG,"onStart()");
                //Start Recording..
                //Если пермишионы не ОТКАЗАНЫ тоесть все прошло успешно
                if (!checkPermissionFromDevice()) {
                    binding.btnEmoji.setVisibility(View.INVISIBLE);
                    binding.btnFile.setVisibility(View.INVISIBLE);
                    binding.btnCamera.setVisibility(View.INVISIBLE);
                    binding.edMessage.setVisibility(View.INVISIBLE);

                    startRecord();
                    //Создаем vibrator
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        //Если не равен Null вибрируем 100 MillSec
                        vibrator.vibrate(100);
                    }
                } else {
                    //Если permission отказаны,то вызываем метод,который опять же запрашивает разрешения
                    //В реальном времени
                    requestPermission();
                }

            }

            @Override
            public void onCancel() {
//                Если отмена
                try {
                    //Сбрасываем mediaRecorder
                    mediaRecorder.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(long recordTime) {
                //После окночания
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.btnCamera.setVisibility(View.VISIBLE);
                binding.edMessage.setVisibility(View.VISIBLE);

//                Stop Recording..
//                try {
//                    sTime = getHumanTimeText(recordTime);
                    stopRecord();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onLessThanSecond() {
                //Если нажатие (удерживание на микрофон) действует менее 1 секунды
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.btnCamera.setVisibility(View.VISIBLE);
                binding.edMessage.setVisibility(View.VISIBLE);
            }
        });
        //Конец анимации
        binding.recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                //При окончании анимации с микрофоно (чекни) сраабатывает этот метод,отображаются все элементы,
                // которые мы скрывали при onStart методе данной библиотеки с голосовой возможность
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.btnCamera.setVisibility(View.VISIBLE);
                binding.edMessage.setVisibility(View.VISIBLE);
            }
        });

    }

    private void openGallery() {
        //По нажатию на фотку с галлереей,которая находится в нашей карточке
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_GALLERY_REQUEST);
    }

    private void loadDataContactUserInToolbar(String nameContactUser, String imageContactUser) {
        //Устанавливаем имя нажавшего юзера
        binding.tvUsername.setText(nameContactUser);
        //Загружаем фото нажавшего юзера
        Glide.with(this).load(imageContactUser).placeholder(R.mipmap.ic_launcher).into(binding.imageProfile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            //Загрузка данных в фаeрбэйс();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private void reviewImage(Bitmap bitmap) {
        //Метод загружает а firebase
        //Наш собственный метод
        new DialogReviewSendImage(this,bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                if(imageUri != null){
                    //Если фото не пустое,показываем диалог,скрываем наш кардвью и isActionShown = false
                    pd.show();
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;

                    new FirebaseService(ChatActivity.this).uploadImageToFireBaseStorage(imageUri, new FirebaseService.OnCallBack() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            //Если все хорошо и наша фотка добавилась в Storage
                            //Посмотри что делает этот метод
                            //Вкратце он создает обЪект класса Chats и пушит его в RealtimeDataBase
                            chatService.sendImage(imageUrl);
                            pd.dismiss();
                            Toast.makeText(ChatActivity.this, "Успешно фото", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            //При ошибке
                            Log.d("error chatActivity",e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void createProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setTitle("Загрузка");
        pd.setMessage("Пожалуйста,подождите...");
        pd.setCancelable(false);
    }

    private boolean checkPermissionFromDevice() {
        //Засовываем в наши переменные нам нужные permission
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordAudioPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);

        //Возвращаем булево значение
        //DENIED ( С АНГЛ. - ОТКАЗАНО
        return writeExternalStoragePermission == PackageManager.PERMISSION_DENIED || recordAudioPermission == PackageManager.PERMISSION_DENIED;
    }

    private void requestPermission() {

        //Запрос разрешения
        //В массиве указываем разрешения,которые мы хотим получить
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.RECORD_AUDIO},REQUEST_CORD_PERMISSION);

    }

    private void startRecord(){
        Log.d(CHAT_ERROR_LOG,"startRecorder");
        setUpMediaRecorder();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
              Toast.makeText(ChatActivity.this, "Recording...", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(CHAT_ERROR_LOG,"startRecorderError");
            Toast.makeText(ChatActivity.this, "Recording Error , Please restart your app ", Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecord(){

//        Метод вызвывается,при методе OnFinish() библиотеки для голосовго сообщения (строчка 307)
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;

//                sendVoice();
                //При окончании голосового сообщения мы отправляем его на сервер,чекай метод sendVoice класса ChatService
                chatService.sendVoice(audio_path);

            } else {
                Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Stop Recording Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void setUpMediaRecorder() {
        Log.d(CHAT_ERROR_LOG,"setup");
        //Путь записи звука (если честно не знаю)
        String path_save = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "audio_record.m4a";
        //нашему пути присваиваем этот путь (audio_path - наша ГЛОБАЛЬНАЯ переменная)
        audio_path = path_save;

        //Создаем медиа рекордер
        mediaRecorder = new MediaRecorder();

        //Оборачиваем в try (на всякий случай)
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(path_save);
        } catch (Exception e) {
            Log.d(CHAT_ERROR_LOG, "setUpMediaRecord: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CORD_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //Если все проверки проходят
        }

    }
}