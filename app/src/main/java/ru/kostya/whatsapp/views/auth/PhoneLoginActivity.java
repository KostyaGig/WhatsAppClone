package ru.kostya.whatsapp.views.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.ActivityPhoneLoginBinding;

public class PhoneLoginActivity extends AppCompatActivity {

    public static final String VERIFY = "VERIFY";
    private FirebaseAuth mAuth;
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    private ActivityPhoneLoginBinding binding;
    private ProgressDialog pd;

    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_phone_login);

        createProgressDialog();
        mAuth = FirebaseAuth.getInstance();

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.btnNext.getText().toString().equals("Next")) {

                    String phone = "+" + binding.edCodeCountry.getText().toString() + binding.edPhone.getText().toString();
                    phoneNumberVerification(phone);
                } else {
                    pd.setMessage("Verifying ..");
                    pd.show();
                    verifyPhoneNumberWithCode(mVerificationId, binding.edCode.getText().toString());
                }
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //Если верифиация прошла успешно
                signInWithPhoneAuthCredential(phoneAuthCredential);
                pd.dismiss();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationId = verificationId;
                mResendToken = forceResendingToken;

                binding.btnNext.setText("Confirm");
                pd.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "На ваш телефон был отправлен код", Toast.LENGTH_SHORT).show();
            }
        };

    }

    private void createProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setTitle("Загрузка");
        pd.setMessage("Пожалуйста,подождите...");
        pd.setCancelable(false);
    }

    private void phoneNumberVerification(String phoneNumber){
        //Проверяем номер на верификацию
        pd.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallBacks);        // OnVerificationStateChangedCallbacks
    }

    //Метод отправляет код на номер телефона
    private void verifyPhoneNumberWithCode(String mVerificationId,String code){
        PhoneAuthCredential credential  = PhoneAuthProvider.getCredential(mVerificationId,code);

        //Вызывается метод,в который мы отправляем id и code который придет юзеру смс
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //Будем отслеживать,успешно ли прошла регистрация или
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            //Получаем текущего юзера,если все успешно
                            Toast.makeText(PhoneLoginActivity.this, "Успешно", Toast.LENGTH_SHORT).show();
                            Intent userActivity = new Intent(getApplicationContext(), AddUserActivity.class);
                            startActivity(userActivity);
                             user = task.getResult().getUser();

                        } else {
                            // Sign in failed, display a message and update the UI

                            Toast.makeText(PhoneLoginActivity.this, "Провал!", Toast.LENGTH_SHORT).show();
                            user = null;
                            pd.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null){
            Intent userActivity = new Intent(getApplicationContext(),AddUserActivity.class);
            startActivity(userActivity);
        }
    }
}