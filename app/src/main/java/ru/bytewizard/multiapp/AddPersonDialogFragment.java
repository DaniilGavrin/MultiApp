package ru.bytewizard.multiapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddPersonDialogFragment extends DialogFragment {

    private OnPersonListUpdatedListener listener;

    public void setListener(OnPersonListUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_person, null);

        EditText lastNameEditText = view.findViewById(R.id.editTextLastName);
        EditText firstNameEditText = view.findViewById(R.id.editTextFirstName);
        EditText middleNameEditText = view.findViewById(R.id.editTextMiddleName);
        EditText dateEditText = view.findViewById(R.id.editTextDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lastName = lastNameEditText.getText().toString();
                        String firstName = firstNameEditText.getText().toString();
                        String middleName = middleNameEditText.getText().toString();
                        String date = dateEditText.getText().toString();

                        if (TextUtils.isEmpty(lastName) || TextUtils.isEmpty(firstName) ||
                                TextUtils.isEmpty(middleName) || TextUtils.isEmpty(date)) {
                            Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                        } else {
                            savePerson(lastName, firstName, middleName, date);
                            if (listener != null) {
                                listener.onPersonListUpdated();
                            }
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private void savePerson(String lastName, String firstName, String middleName, String date) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("PersonPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int personId = sharedPreferences.getInt("personCount", 0) + 1;
        editor.putInt("personCount", personId);
        editor.putString("person_" + personId + "_lastName", lastName);
        editor.putString("person_" + personId + "_firstName", firstName);
        editor.putString("person_" + personId + "_middleName", middleName);
        editor.putString("person_" + personId + "_date", date);
        editor.apply();

        Toast.makeText(getContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
    }

    public interface OnPersonListUpdatedListener {
        void onPersonListUpdated();
    }
}