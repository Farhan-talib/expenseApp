package in.co.sagarfood.sagarexpense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GetExpenses extends AppCompatActivity {
    Spinner spStatus;
    String[] statuses = {"-", "Posted", "Approved", "Marg Entry Done"};
    EditText etDateFrom, etDateTo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_expenses);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        spStatus = findViewById(R.id.spStatus);
        etDateFrom = findViewById(R.id.etFromDate);
        etDateTo = findViewById(R.id.etToDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);
        setupDatePicker(etDateFrom);
        setupDatePicker(etDateTo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDateFrom.setText(sdf.format(new Date()));
        etDateTo.setText(sdf.format(new Date()));

    }
    private void setupDatePicker(EditText et) {
        et.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        String date = y + "-" +
                                String.format("%02d", m + 1) + "-" +
                                String.format("%02d", d);
                        et.setText(date);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            dp.getDatePicker().setMaxDate(System.currentTimeMillis()); // no future
            dp.show();
        });
    }

    public void GetExpensesReport(View v){
        try{
            String fromDate = etDateFrom.getText().toString();
            String toDate = etDateTo.getText().toString();

            if (fromDate.isEmpty() || toDate.isEmpty()) {
                toast("Both dates required");
                return;
            }

            if (fromDate.compareTo(toDate) > 0) {
                toast("From date cannot be after To date");
                return;
            }
        }
        catch (Exception ex){

        }
    }
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}