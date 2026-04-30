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

import java.util.Locale;

public class MargUserDashboard extends AppCompatActivity {
    private SharedPreferences prefs;
    TextView txtUserID, txtUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_marg_user_dashboard);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Bind views
        txtUserID = findViewById(R.id.txtMargUserID);
        txtUserName = findViewById(R.id.txtMargUserName);

        findViewById(R.id.btnNewMargEntry).setOnClickListener(v -> {
            startActivity(new Intent(MargUserDashboard.this, NewMargEntry.class));
        });
        findViewById(R.id.btnViewMargReport).setOnClickListener(v -> {
            startActivity(new Intent(MargUserDashboard.this, GetExpenses.class));
        });
        findViewById(R.id.btnMargLogout).setOnClickListener(v -> {
            MargLogout();
        });
        loadDashboardData();
    }
    private void loadDashboardData() {

        String userId = prefs.getString("userid", "N/A");
        String userName = prefs.getString("username", "N/A");

        // Set values
        txtUserID.setText("User ID: " + userId);
        txtUserName.setText("User Name: " + userName);

    }

    public void MargLogout(){
        //sharedPreferences = getSharedPreferences("YourPrefName", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();  // This removes all data
        editor.apply();  // Or editor.commit();

        Intent intent = new Intent(MargUserDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}