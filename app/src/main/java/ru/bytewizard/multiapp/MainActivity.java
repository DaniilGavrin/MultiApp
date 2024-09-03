package ru.bytewizard.multiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Найти элементы по ID
        LinearLayout workDaysButton = findViewById(R.id.workDaysButton);
        LinearLayout timeButton = findViewById(R.id.timeButton);

        // Обработчик нажатия для первого квадрата
        workDaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WorkDaysActivity.class);
                startActivity(intent);
            }
        });

        // Пока заглушка для второго квадрата
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TimeMainActivity.class);
                startActivity(intent);
            }
        });
    }
}