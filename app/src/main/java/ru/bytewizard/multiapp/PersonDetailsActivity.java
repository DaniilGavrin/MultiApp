package ru.bytewizard.multiapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView personImageView;
    private TextView lastNameTextView;
    private TextView firstNameTextView;
    private TextView middleNameTextView;
    private TextView meetingDateTextView;
    private TextView timePassedTextView;
    private String personImagePath; // Путь к фото
    private int personId; // ID человека

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        personImageView = findViewById(R.id.personImageView);
        lastNameTextView = findViewById(R.id.lastNameTextView);
        firstNameTextView = findViewById(R.id.firstNameTextView);
        middleNameTextView = findViewById(R.id.middleNameTextView);
        meetingDateTextView = findViewById(R.id.meetingDateTextView);
        timePassedTextView = findViewById(R.id.timePassedTextView);

        sharedPreferences = getSharedPreferences("PersonPrefs", MODE_PRIVATE);

        // Получаем ID человека из Intent
        personId = getIntent().getIntExtra("personId", -1);

        if (personId != -1) {
            loadPersonDetails();
        }

        // Открытие галереи при клике на ImageView
        personImageView.setOnClickListener(v -> openGallery());

        // Настройка кнопок
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        FloatingActionButton editPersonButton = findViewById(R.id.editPersonButton);

        editPersonButton.setOnClickListener(v -> openEditDialog());
    }

    private void loadPersonDetails() {
        String lastName = sharedPreferences.getString("person_" + personId + "_lastName", "Unknown");
        String firstName = sharedPreferences.getString("person_" + personId + "_firstName", "Unknown");
        String middleName = sharedPreferences.getString("person_" + personId + "_middleName", "Unknown");
        String dateOfMeeting = sharedPreferences.getString("person_" + personId + "_date", "Unknown");
        personImagePath = sharedPreferences.getString("person_" + personId + "_imagePath", "");

        // Обновление UI
        lastNameTextView.setText("Фамилия: " + lastName);
        firstNameTextView.setText("Имя: " + firstName);
        middleNameTextView.setText("Отчество: " + middleName);
        meetingDateTextView.setText("Дата знакомства: " + dateOfMeeting);
        timePassedTextView.setText(calculateTimeSinceMeeting(dateOfMeeting));

        if (personImagePath.isEmpty()) {
            personImageView.setImageResource(R.drawable.ic_no_photo);
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(personImagePath);
            personImageView.setImageBitmap(bitmap);
        }
    }

    private String calculateTimeSinceMeeting(String dateOfMeeting) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date meetingDate = sdf.parse(dateOfMeeting);
            if (meetingDate != null) {
                long diff = new Date().getTime() - meetingDate.getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                long years = days / 365;
                long months = (days % 365) / 30;
                days = (days % 365) % 30;

                return String.format(Locale.getDefault(), "%d лет %d месяцев %d дней (%d дней)", years, months, days, days);
            }
        } catch (ParseException e) {
            Log.e("PersonDetailsActivity", "Error calculating time since meeting", e);
        }
        return "Неизвестно";
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

            // Сохранение изображения в внутреннем хранилище
            saveImageToInternalStorage(imageUri);
        }
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File imageFile = createImageFile();
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            // Обновление пути к изображению
            personImagePath = imageFile.getAbsolutePath();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("person_" + personId + "_imagePath", personImagePath);
            editor.apply();
        } catch (IOException e) {
            Log.e("PersonDetailsActivity", "Error saving image", e);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir(); // Внутреннее хранилище
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openEditDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_person, null);

        EditText lastNameEditText = dialogView.findViewById(R.id.editTextLastName);
        EditText firstNameEditText = dialogView.findViewById(R.id.editTextFirstName);
        EditText middleNameEditText = dialogView.findViewById(R.id.editTextMiddleName);
        EditText dateEditText = dialogView.findViewById(R.id.editTextDate);

        // Загрузка текущих данных в EditText
        lastNameEditText.setText(lastNameTextView.getText().toString().replace("Фамилия: ", ""));
        firstNameEditText.setText(firstNameTextView.getText().toString().replace("Имя: ", ""));
        middleNameEditText.setText(middleNameTextView.getText().toString().replace("Отчество: ", ""));
        dateEditText.setText(meetingDateTextView.getText().toString().replace("Дата знакомства: ", ""));

        new AlertDialog.Builder(this)
                .setTitle("Редактировать информацию")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String updatedLastName = lastNameEditText.getText().toString();
                    String updatedFirstName = firstNameEditText.getText().toString();
                    String updatedMiddleName = middleNameEditText.getText().toString();
                    String updatedDate = dateEditText.getText().toString();

                    updatePersonDetails(updatedLastName, updatedFirstName, updatedMiddleName, updatedDate);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updatePersonDetails(String lastName, String firstName, String middleName, String dateOfMeeting) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("person_" + personId + "_lastName", lastName);
        editor.putString("person_" + personId + "_firstName", firstName);
        editor.putString("person_" + personId + "_middleName", middleName);
        editor.putString("person_" + personId + "_date", dateOfMeeting);
        editor.apply();

        // Обновление UI
        lastNameTextView.setText("Фамилия: " + lastName);
        firstNameTextView.setText("Имя: " + firstName);
        middleNameTextView.setText("Отчество: " + middleName);
        meetingDateTextView.setText("Дата знакомства: " + dateOfMeeting);
        timePassedTextView.setText(calculateTimeSinceMeeting(dateOfMeeting));
    }
}