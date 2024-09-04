package ru.bytewizard.multiapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TimeMainActivity extends AppCompatActivity implements OnPersonListUpdatedListener {

    private LinearLayout personListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_main);

        personListLayout = findViewById(R.id.personListLayout);

        // Кнопка для добавления нового человека
        ImageButton addPersonButton = findViewById(R.id.addPersonButton);
        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPersonDialog();
            }
        });

        // Загрузка и отображение списка людей
        loadPersonList();
    }

    private void openAddPersonDialog() {
        AddPersonDialogFragment dialog = new AddPersonDialogFragment();
        dialog.setListener(this); // Передача слушателя
        dialog.show(getSupportFragmentManager(), "AddPersonDialog");
    }

    @Override
    public void onPersonListUpdated() {
        loadPersonList(); // Перезагрузка списка
    }

    // Метод для загрузки списка людей
    public void loadPersonList() {
        // Очистка существующих элементов
        personListLayout.removeAllViews();

        SharedPreferences sharedPreferences = getSharedPreferences("PersonPrefs", MODE_PRIVATE);
        int personCount = sharedPreferences.getInt("personCount", 0);

        for (int i = 1; i <= personCount; i++) {
            String lastName = sharedPreferences.getString("person_" + i + "_lastName", "Unknown");
            String firstName = sharedPreferences.getString("person_" + i + "_firstName", "Unknown");
            String middleName = sharedPreferences.getString("person_" + i + "_middleName", "Unknown");
            String date = sharedPreferences.getString("person_" + i + "_date", "Unknown");

            // Создание и настройка вида для каждой записи
            View personView = getLayoutInflater().inflate(R.layout.person_list_item, personListLayout, false);

            TextView nameTextView = personView.findViewById(R.id.personNameTextView);
            TextView dateTextView = personView.findViewById(R.id.personDateTextView);

            nameTextView.setText(firstName + " " + lastName);
            dateTextView.setText(date);

            // Установка кликабельности на элемент списка для перехода к деталям
            personView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPersonDetailsActivity(i); // Используем ID для открытия деталей
                }
            });

            personListLayout.addView(personView);
        }
    }

    private void openPersonDetailsActivity(int personId) {
        Intent intent = new Intent(TimeMainActivity.this, PersonDetailsActivity.class);
        intent.putExtra("personId", personId);
        startActivity(intent);
    }
}
