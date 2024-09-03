package ru.bytewizard.multiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class TimeMainActivity extends AppCompatActivity {

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

        // Здесь нужно загрузить и отобразить список людей
        loadPersonList();
    }

    private void openAddPersonDialog() {
        // Здесь откроем диалоговое окно для добавления нового человека
        // После добавления обновим список
    }

    private void loadPersonList() {
        // Здесь вы загрузите список людей из хранилища и отобразите их в personListLayout
    }

    private void openPersonDetailsActivity(int personId) {
        Intent intent = new Intent(TimeMainActivity.this, PersonDetailsActivity.class);
        intent.putExtra("personId", personId);
        startActivity(intent);
    }
}