package com.example.loginmodule.Course;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DateSetListener dateSetListener;

    public interface DateSetListener {
        void onDateSet(int year, int month, int day);
    }

    public void setDateSetListener(DateSetListener listener) {
        this.dateSetListener = listener;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d("DatePicker", "onDateSet: " + year + "-" + month + "-" + day);
        if (dateSetListener != null) {
            dateSetListener.onDateSet(year, month, day);
        }
    }

}