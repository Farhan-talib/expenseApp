package in.co.sagarfood.sagarexpense;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {
    EditText etIncomeDate, etAmount, etRemarks;
    Spinner spGodown;

    List<SQLHelper.GodownInfo> godownList = new ArrayList<>();
    ArrayAdapter<SQLHelper.GodownInfo> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_income);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        etIncomeDate = findViewById(R.id.etIncomeDate);
        etAmount = findViewById(R.id.etAmount);
        etRemarks = findViewById(R.id.etRemarks);
        spGodown = findViewById(R.id.spGodown);
        setupDatePicker();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etIncomeDate.setText(today);

        loadGodowns();

        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitIncome());
    }
    private void setupDatePicker() {
        etIncomeDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        String date = y + "-" +
                                String.format("%02d", m + 1) + "-" +
                                String.format("%02d", d);
                        etIncomeDate.setText(date);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            dp.getDatePicker().setMaxDate(System.currentTimeMillis());
            dp.show();
        });
    }
    private void loadGodowns() {
        try {
            JSONObject json = new JSONObject();
            json.put("godownId", 0);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.GET_GODOWNS, // same API gives godowns
                    json,
                    response -> {
                        try {
                            JSONObject d = response.getJSONObject("d");
                            JSONArray arr = d.getJSONArray("godowns");

                            godownList.clear();

                            // Default option
                            SQLHelper.GodownInfo def = new SQLHelper.GodownInfo();
                            def.sno = 0;
                            def.name = "Select Godown";
                            godownList.add(def);

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);

                                SQLHelper.GodownInfo g = new SQLHelper.GodownInfo();
                                g.sno = o.getInt("sno");
                                g.name = o.getString("name");

                                godownList.add(g);
                            }

                            adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item,
                                    godownList);

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spGodown.setAdapter(adapter);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Failed to load godowns", Toast.LENGTH_SHORT).show()
            );

            Volley.newRequestQueue(this).add(req);
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private void submitIncome() {


        String date = etIncomeDate.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();
        String remarks = etRemarks.getText().toString().trim();

        if (date.isEmpty()) {
            toast("Select date");
            return;
        }

        if (amtStr.isEmpty()) {
            toast("Enter amount");
            return;
        }

        double amount = Double.parseDouble(amtStr);

        if (amount <= 0) {
            toast("Amount must be greater than 0");
            return;
        }

        if (remarks.isEmpty()) {
            toast("Enter remarks");
            return;
        }
        SQLHelper.GodownInfo selected = (SQLHelper.GodownInfo) spGodown.getSelectedItem();
        int godownNo = (selected != null) ? selected.sno : 0;

        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setCancelable(false);
        pd.show();

        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            JSONObject json = new JSONObject();
            json.put("incomeDate", date);
            json.put("amount", amount);
            json.put("remarks", remarks);
            json.put("godownNo", godownNo);
            json.put("entryBy",prefs.getInt("sno",0));
            json.put("entryByName",prefs.getString("username","-"));

            System.out.println(json.toString());

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.NEW_INCOME_ENTRY,
                    json,
                    response -> {
                        pd.dismiss();
                        try {
                            JSONObject d = response.getJSONObject("d");
                            if (d.getBoolean("valid")) {

                                float currentTotal = prefs.getFloat("ttlIncome", 0f);
                                // add current expense
                                float updatedTotal = currentTotal + (float) amount;
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putFloat("ttlIncome", updatedTotal);
                                editor.apply();

                                toast("Income saved");
                                finish();
                            } else {
                                toast(d.getString("message"));
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    },
                    error -> {
                        toast("Failed to save -- ");
                        pd.dismiss();
                    }
            );

            Volley.newRequestQueue(this).add(req);

        } catch (Exception e) {
            e.printStackTrace();
            pd.dismiss();
        }
    }
    public void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}