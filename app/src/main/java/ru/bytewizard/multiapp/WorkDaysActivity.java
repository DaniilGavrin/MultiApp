package ru.bytewizard.multiapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WorkDaysActivity extends AppCompatActivity {

    private EditText dateEditText;
    private TextView resultTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_days);

        dateEditText = findViewById(R.id.dateInput);
        resultTextView = findViewById(R.id.resultTextView);
        Button calculateButton = findViewById(R.id.calculateButton);
        Button clearButton = findViewById(R.id.clearButton);
        sharedPreferences = getSharedPreferences("work_days_prefs", MODE_PRIVATE);

        // Загружаем сохраненные рабочие дни
        loadSavedWorkDays();

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateWorkDays();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearWorkDays();
            }
        });
    }

    private void calculateWorkDays() {
        String inputDate = dateEditText.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date startDate = dateFormat.parse(inputDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            StringBuilder result = new StringBuilder();
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy (EEEE)", new Locale("ru"));

            // Проходим по дням и отмечаем рабочие и выходные дни
            for (int i = 0; i < 1000; i++) { // Генерируем 10 дней для примера
                if (i % 3 == 0) { // Первый день рабочий, остальные два выходные
                    result.append(outputFormat.format(calendar.getTime())).append("\n");
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Переходим к следующему дню
            }

            resultTextView.setText(result.toString());
            saveWorkDays(result.toString());
        } catch (Exception e) {
            resultTextView.setText("Ошибка формата даты. Используйте формат дд-мм-гггг.");
        }
    }

    private void saveWorkDays(String workDays) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("work_days", workDays);
        editor.apply();
    }

    private void loadSavedWorkDays() {
        String savedWorkDays = sharedPreferences.getString("work_days", "");
        if (!savedWorkDays.isEmpty()) {
            resultTextView.setText(savedWorkDays);
        }
    }

    private void clearWorkDays() {
        resultTextView.setText("");
        sharedPreferences.edit().clear().apply();
    }
}