package ru.bytewizard.multiapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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

    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;

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

        // Получаем ID человека из Intent
        personId = getIntent().getIntExtra("personId", -1);

        dbHelper = new PersonDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        if (personId != -1) {
            loadPersonDetails();
        }

        // Открытие галереи при клике на ImageView
        personImageView.setOnClickListener(v -> openGallery());

        // Настройка кнопок
        Button backButton = findViewById(R.id.backButton);
        FloatingActionButton editPersonButton = findViewById(R.id.editPersonButton);

        backButton.setOnClickListener(v -> finish());
        editPersonButton.setOnClickListener(v -> openEditDialog());
    }

    private void loadPersonDetails() {
        Cursor cursor = database.query("people", null, "id = ?", new String[]{String.valueOf(personId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int lastNameIndex = cursor.getColumnIndex("last_name");
            int firstNameIndex = cursor.getColumnIndex("first_name");
            int middleNameIndex = cursor.getColumnIndex("middle_name");
            int dateOfMeetingIndex = cursor.getColumnIndex("date_of_meeting");
            int imagePathIndex = cursor.getColumnIndex("image_path");

            if (lastNameIndex != -1 && firstNameIndex != -1 && middleNameIndex != -1 && dateOfMeetingIndex != -1) {
                String lastName = cursor.getString(lastNameIndex);
                String firstName = cursor.getString(firstNameIndex);
                String middleName = cursor.getString(middleNameIndex);
                String dateOfMeeting = cursor.getString(dateOfMeetingIndex);
                personImagePath = cursor.getString(imagePathIndex);

                // Обновление UI
                lastNameTextView.setText("Фамилия: " + lastName);
                firstNameTextView.setText("Имя: " + firstName);
                middleNameTextView.setText("Отчество: " + middleName);
                meetingDateTextView.setText("Дата знакомства: " + dateOfMeeting);
                timePassedTextView.setText(calculateTimeSinceMeeting(dateOfMeeting));

                if (personImagePath == null || personImagePath.isEmpty()) {
                    personImageView.setImageResource(R.drawable.ic_no_photo);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(personImagePath);
                    personImageView.setImageBitmap(bitmap);
                }
            } else {
                // Логика обработки случая, когда столбцы не найдены
                Log.e("PersonDetailsActivity", "One or more columns not found");
            }

            cursor.close();
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
            ContentValues values = new ContentValues();
            values.put("image_path", personImagePath);
            database.update("people", values, "id = ?", new String[]{String.valueOf(personId)});
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

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText dateEditText = dialogView.findViewById(R.id.dateEditText);

        // Загрузка текущих данных в EditText
        nameEditText.setText(lastNameTextView.getText());
        dateEditText.setText(meetingDateTextView.getText().toString().replace("Дата знакомства: ", ""));

        new AlertDialog.Builder(this)
                .setTitle("Редактировать информацию")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String updatedName = nameEditText.getText().toString();
                    String updatedDate = dateEditText.getText().toString();
                    updatePersonDetails(updatedName, updatedDate);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updatePersonDetails(String fullName, String dateOfMeeting) {
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("date_of_meeting", dateOfMeeting);

        database.update("people", values, "id = ?", new String[]{String.valueOf(personId)});

        // Обновление UI
        lastNameTextView.setText("Фамилия: " + fullName);
        meetingDateTextView.setText("Дата знакомства: " + dateOfMeeting);
        timePassedTextView.setText(calculateTimeSinceMeeting(dateOfMeeting));
    }

    private void deletePerson() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Вы уверены, что хотите удалить этого человека?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    database.delete("people", "id = ?", new String[]{String.valueOf(personId)});
                    finish(); // Закрытие активности
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
