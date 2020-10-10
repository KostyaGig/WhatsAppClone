package ru.kostya.whatsapp.views.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import ru.kostya.whatsapp.views.display.ViewImageActivity;
import ru.kostya.whatsapp.views.startup.WelcomeActivity;
import ru.kostya.whatsapp.common.Common;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private BottomSheetDialog bottomSheetDialogGallery;
    private BottomSheetDialog bottomSheetDialogEditName;

    private ProgressDialog pd;

    public static final int REQUEST_CODE_GALLERY = 100;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile);

        setSupportActionBar(binding.toolbar);

        //Провеярет getSupport action bar на null если все хорошо то устанавливает кнопку назад
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        createProgressDialog();


        if (currentUser != null) {
            getInfo();
        }
        initClick();


    }

    private void initClick() {
        //По нажатию на добавить фото
        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPickPhoto();
            }
        });

        //По нажатию на блок с изменением имени
        binding.linearNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetEditName();
            }
        });

        //По нажатию на фото профиля
        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Код который будет запихивать фото из профиля в нашу переменную IMAGE_BITMAP НАШЕГО СОЗДАННОГО КЛАССА CommonВ ПАКЕТЕ Common
            binding.imageProfile.invalidate();
                Drawable drawable = binding.imageProfile.getDrawable();
                //WARNING!!!Чтобы юзать класс GlideBitmapDrawable нужна версия glide 3.7.0
                Common.IMAGE_BITMAP = ((GlideBitmapDrawable)drawable.getCurrent()).getBitmap();
                //3 параметр это Transition name,его мы должны указать атрибутом в circle view,тоесть картинку,с которой мы работаем это нужно для анимации
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this,binding.imageProfile,"image");

                Intent intent = new Intent(ProfileActivity.this, ViewImageActivity.class);
                startActivity(intent,activityOptionsCompat.toBundle());
            }
        });

        //По нажатию на выход
        binding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogSignOut();
            }
        });

    }

    private void showDialogSignOut() {
        //Диалоог при нажатии на выход
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setMessage("Вы действительно хотите выйти?");
        builder.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Если выходим,скрываем диалог,разрываем аунтефикацию с юзером и перебрасываем его на велком активити
                dialog.cancel();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, WelcomeActivity.class));
            }
        });
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Если не выходим,скрываем диалог
                dialog.cancel();
            }
        });

        //Создаем диалог и показываем
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showBottomSheetEditName() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name,null);


        //По нажатию на отмена закрываем ботом щит
        ((View) view.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogEditName.dismiss();
            }
        });

        //По id находим editText,в которое будем вписывать новое имя
        final EditText edNewName = view.findViewById(R.id.ed_username);


        //По нажатию на сохранить делаем следующее
        ((View) view.findViewById(R.id.btn_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newName = edNewName.getText().toString();
                //Если по нажатию сохранить поле имя не пустое,то
                if (!TextUtils.isEmpty(newName)) {
                    pd.show();

                    HashMap<String, Object> map = new HashMap();
                    map.put("userName", newName);

                    firestore.collection("Users").document(currentUser.getUid()).update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Ваше имя успешно обновилось!", Toast.LENGTH_SHORT).show();
                                    getInfo();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Log.d("Error update name", e.getMessage());
                            Toast.makeText(ProfileActivity.this, "Ошибка,смотри лог", Toast.LENGTH_SHORT).show();
                        }
                    });
                    bottomSheetDialogEditName.dismiss();
                } else {
                    Toast.makeText(ProfileActivity.this, "Введите имя!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        bottomSheetDialogEditName = new BottomSheetDialog(this);

        bottomSheetDialogEditName.setContentView(view);


        //При закрытии ботом щита
        bottomSheetDialogEditName.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bottomSheetDialogEditName = null;
            }
        });
        bottomSheetDialogEditName.show();

    }

    private void showBottomSheetPickPhoto() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick,null);

        //По нажатию на линеар с галлереей
        ((View) view.findViewById(R.id.ln_gallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                bottomSheetDialogGallery.dismiss();
            }
        });

        //По нажатию на линеар с камерой
        ((View) view.findViewById(R.id.ln_camera)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Camera",Toast.LENGTH_SHORT).show();
                bottomSheetDialogGallery.dismiss();
            }
        });


        bottomSheetDialogGallery = new BottomSheetDialog(this);

        bottomSheetDialogGallery.setContentView(view);


        //При закрытии ботом щита
        bottomSheetDialogGallery.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bottomSheetDialogGallery = null;
            }
        });
        bottomSheetDialogGallery.show();
    }

    private void openGallery() {
        //создаем интент на получение фото
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), REQUEST_CODE_GALLERY);
    }

    private void getInfo(){
        firestore.collection("Users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.getString("userName");
                String userPhone = documentSnapshot.getString("userPhone");
                String imageUrl = documentSnapshot.getString("imageProfile");

                binding.tvUsername.setText(userName);
                binding.tvPhone.setText(userPhone);
                Glide.with(ProfileActivity.this).load(imageUrl).into(binding.imageProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ErrorSetting",e.getMessage());
                Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){

            imageUri = data.getData();

            uploadToFirebase();

//             try {
//                     Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                     binding.imageProfile.setImageBitmap(bitmap);
//
//                 }catch (Exception e){
//                     e.printStackTrace();
//                 }

            }
    }

    private void uploadToFirebase() {
        if (imageUri != null){
            pd.show();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImagesProfile/" + System.currentTimeMillis()+"."+getFileExtention(imageUri));

            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Бдуем получать сюда uri's
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                    //Пока не без успешно,тоесть пока есть uri Мы их получаем
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    //Получаем url просто приводим uri к toString()
                    String url = String.valueOf(downloadUrl);

                    //Нужна дляя помещений url'a в FireStore (document)
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageProfile", url);

                    pd.dismiss();
                    //После получения конечного url мы добавляем его в наш FireStore document
                    //Если быть точнее,то мы обновляем пустоту по ключу imageProfile в FireStore на url,теперь мы можем загружать фото по url))
                    firestore.collection("Users").document(currentUser.getUid()).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"upload successfully",Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            getInfo();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"upload don't successfully",Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "Добавьте фото!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setTitle("Загрузка");
        pd.setMessage("Пожалуйста,подождите...");
        pd.setCancelable(false);
    }

}