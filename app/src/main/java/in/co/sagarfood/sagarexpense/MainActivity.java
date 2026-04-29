package in.co.sagarfood.sagarexpense;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        // ✅ Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            // Auto-login
            String savedUsername = sharedPreferences.getString("userid", null);
            String savedPassword = sharedPreferences.getString("password", null);

            if (sharedPreferences.getBoolean("isLoggedIn", false) && savedUsername != null && savedPassword != null) {
                login(savedUsername,savedPassword);
            }
        }
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String userId = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            login(userId,password);
        });
    }
    private void login(final String username, final String password) {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setCancelable(false);
        pd.show();
        try {


            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("userid", username);
            jsonRequest.put("password", password);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.LOGIN_URL,
                    jsonRequest,
                    response -> {
                        try {
                            JSONObject jsonResponse = response.getJSONObject("d"); // .asmx returns { "d": { ... } }

                            boolean valid = jsonResponse.getBoolean("valid");
                            if (valid) {
                                SQLHelper.UserInfo user = new SQLHelper.UserInfo();
                                user.setValid(valid);
                                user.setSno(jsonResponse.getInt("sno"));
                                user.setUserid(jsonResponse.getString("userid"));
                                user.setUsername(jsonResponse.getString("username"));
                                user.setPassword(password);//jsonResponse.getString("password"));
                                user.setMobile(jsonResponse.getString("mobile"));
                                user.setUsertype(jsonResponse.getString("usertype"));
                                user.setStatus(jsonResponse.getString("status"));
                                user.setGodownNo(jsonResponse.getInt("godownNo"));
                                user.setGodownName(jsonResponse.getString("godownName"));
                                user.setTtlExpense(jsonResponse.getDouble("ttlExp"));
                                user.setTtlIncome(jsonResponse.getDouble("ttlInc"));

                                //System.out.println(response.toString());

                                if(user.getSno()==0 || (!user.getUsertype().toUpperCase().equals("ADMIN") && !user.getUsertype().toUpperCase().equals("EXPENSER")&& !user.getUsertype().equals("MARG USER"))){
                                    throw new Exception(user.getStatus()+"-"+user.getUsertype());
                                }

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putInt("sno", user.getSno());
                                editor.putString("userid", user.getUserid());
                                editor.putString("username", user.getUsername());
                                editor.putString("password", user.getPassword());
                                editor.putString("mobile", user.getMobile());
                                editor.putString("usertype", user.getUsertype());
                                editor.putInt("godownNo", user.getGodownNo());
                                editor.putString("godownName", user.getGodownName());
                                editor.putFloat("ttlExpense", (float) user.getTtlExpense());
                                editor.putFloat("ttlIncome", (float) user.getTtlIncome());
                                editor.apply();

                                if(user.getUsertype().equals("ADMIN")) {
                                    Intent i = new Intent(MainActivity.this, AdminDashboard.class);
                                    startActivity(i);
                                    finishAffinity();
                                } else if(user.getUsertype().equals("EXPENSER")){
                                    Intent i = new Intent(MainActivity.this, ExpenserDashboard.class);
                                    startActivity(i);
                                    finishAffinity();
                                } else if(user.getUsertype().equals("MARG USER")) {
                                    Intent i = new Intent(MainActivity.this, MargUserDashboard.class);
                                    startActivity(i);
                                    finishAffinity();
                                } else{
                                    Toast.makeText(MainActivity.this, "Invalid user!", Toast.LENGTH_SHORT).show();
                                }

                                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid login credentials!", Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Response parsing failed!", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    },
                    error -> {
                        Log.e("Login", "Volley error: " + error.toString());
                        Toast.makeText(MainActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            // Add to request queue
            Volley.newRequestQueue(this).add(jsonObjectRequest);
        }
        catch (JSONException ex){
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, "Error parsing response!", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
        catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, "Error parsing response!", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }
}