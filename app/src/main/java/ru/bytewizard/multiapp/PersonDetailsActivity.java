package ru.bytewizard.multiapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PersonDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView personImageView;
    private String personImagePath; // Путь к фото

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        personImageView = findViewById(R.id.personImageView);

        // Проверяем, есть ли фото у этого человека
        if (personImagePath == null || personImagePath.isEmpty()) {
            // Если фото нет, показываем заглушку
            personImageView.setImageResource(R.drawable.ic_no_photo);
        } else {
            // Если фото есть, загружаем его
            Bitmap bitmap = BitmapFactory.decodeFile(personImagePath);
            personImageView.setImageBitmap(bitmap);
        }

        // Открытие галереи при клике на ImageView
        personImageView.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            personImageView.setImageURI(imageUri);

            // Здесь добавим код для сохранения изображения
            saveImageToInternalStorage(imageUri);
        }
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        // Логика для сохранения изображения в памяти устройства
        // Здесь нужно реализовать копирование изображения и сохранение пути к нему
        // personImagePath = путь к сохранённому изображению;
    }
}
