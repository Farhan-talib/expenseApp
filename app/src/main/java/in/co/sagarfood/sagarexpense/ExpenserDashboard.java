package in.co.sagarfood.sagarexpense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenserDashboard extends AppCompatActivity {
    private SharedPreferences prefs;
    TextView txtUserID, txtUserName, txtGodown, txtTotalExpense;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expenser_dashboard);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Bind views
        txtUserID = findViewById(R.id.txtUserID);
        txtUserName = findViewById(R.id.txtUserName);
        txtGodown = findViewById(R.id.txtGodown);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);

        loadDashboardData();
    }
    private void loadDashboardData() {

        String userId = prefs.getString("userid", "N/A");
        String userName = prefs.getString("username", "N/A");
        String godown = prefs.getString("godownName", "N/A");

        float totalExpense = prefs.getFloat("ttlExpense", 0f);

        // Set values
        txtUserID.setText("User ID: " + userId);
        txtUserName.setText("User Name: " + userName);
        txtGodown.setText("Godown: " + godown);

        txtTotalExpense.setText("₹ " + String.format(Locale.getDefault(), "%.2f", totalExpense));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        loadDashboardData();
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    public void ExpNewEntry(View v) {
        startActivity(new Intent(ExpenserDashboard.this, NewExpenseEntry.class));
    }
    public void ExpViewReport(View v){
        startActivity(new Intent(ExpenserDashboard.this, GetExpenses.class));
    }
    public void ExpLogout(View v){
        //sharedPreferences = getSharedPreferences("YourPrefName", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();  // This removes all data
        editor.apply();  // Or editor.commit();

        Intent intent = new Intent(ExpenserDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}