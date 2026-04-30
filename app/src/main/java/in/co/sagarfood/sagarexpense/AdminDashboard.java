package in.co.sagarfood.sagarexpense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class AdminDashboard extends AppCompatActivity {

    TextView txtAdminID, txtAdminName;
    TextView txtTodayExpense, txtTodayIncome, txtTodayStatement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        txtAdminID = findViewById(R.id.txtAdminID);
        txtAdminName = findViewById(R.id.txtAdminName);
        txtTodayExpense = findViewById(R.id.txtTodayExpense);
        txtTodayIncome = findViewById(R.id.txtTodayIncome);
        txtTodayStatement = findViewById(R.id.txtTodayStatement);

        loadAdminData();
        setupButtons();
    }

    private void loadAdminData() {

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        int adminID = prefs.getInt("sno", 0);
        String adminName = prefs.getString("username", "");
        //String godownName = prefs.getString("godownName", "");

        float todayExpense = prefs.getFloat("ttlExpense", 0f);
        float todayIncome = prefs.getFloat("ttlIncome", 0f);

        txtAdminID.setText("Admin ID: " + adminID);
        txtAdminName.setText("Admin Name: " + adminName);

        double PaL = todayIncome - todayExpense;

        txtTodayExpense.setText("Today's Expense: ₹ " + String.format(Locale.getDefault(), "%.2f", todayExpense));
        txtTodayIncome.setText("Today's Income: ₹ " + String.format(Locale.getDefault(), "%.2f", todayIncome));
        if(PaL>=0){
            txtTodayStatement.setText("Today Profit: ₹ " + String.format(Locale.getDefault(), "%.2f", PaL));
            txtTodayStatement.setBackgroundColor(getResources().getColor(R.color.green));
            txtTodayStatement.setTextColor(getResources().getColor(R.color.white));
        }
        else if(PaL<0){
            PaL  = todayExpense - todayIncome;
            txtTodayStatement.setText("Today Loss: ₹ " + String.format(Locale.getDefault(), "%.2f", PaL));
            txtTodayStatement.setBackgroundColor(getResources().getColor(R.color.red));
            txtTodayStatement.setTextColor(getResources().getColor(R.color.white));
        }
    }
    private void setupButtons() {

        findViewById(R.id.btnAddIncome).setOnClickListener(v -> {
            startActivity(new Intent(this, AddIncomeActivity.class));
        });

        findViewById(R.id.btnIncomeReport).setOnClickListener(v -> {
            startActivity(new Intent(this, GetIncomes.class));
        });

        findViewById(R.id.btnApproveExpenses).setOnClickListener(v -> {
            startActivity(new Intent(this, UnApprovedExpList.class));
        });

        findViewById(R.id.btnExpenseReport).setOnClickListener(v -> {
            startActivity(new Intent(this, GetExpenses.class));
        });

        findViewById(R.id.btnPLReport).setOnClickListener(v -> {
            startActivity(new Intent(this, GetPandLReport.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadAdminData();
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}