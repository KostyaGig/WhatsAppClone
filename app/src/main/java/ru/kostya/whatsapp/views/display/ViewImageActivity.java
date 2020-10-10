package ru.kostya.whatsapp.views.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import ru.kostya.whatsapp.common.Common;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.databinding.ActivityViewImageBinding;

public class ViewImageActivity extends AppCompatActivity {

    ActivityViewImageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_view_image);

        //Устанавливаем фото из нешей переменной битмап
        binding.imageView.setImageBitmap(Common.IMAGE_BITMAP);
    }
}