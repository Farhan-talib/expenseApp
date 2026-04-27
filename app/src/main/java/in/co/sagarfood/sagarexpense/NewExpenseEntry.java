package in.co.sagarfood.sagarexpense;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewExpenseEntry extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PERMISSION_CAMERA = 101;
    private static final int PERMISSION_GALLERY = 102;
    ImageView imgPreview;
    Uri imageUri;
    Bitmap bitmap;
    Spinner spExpenseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_expense_entry);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        imgPreview = findViewById(R.id.imgPreview);

        findViewById(R.id.btnUpload).setOnClickListener(v -> showImageOptions());
        spExpenseType = findViewById(R.id.spExpenseType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_types,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExpenseType.setAdapter(adapter);
        EditText etDate = findViewById(R.id.etDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));

        etDate.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitExpense());

    }
    private void showDatePicker() {

        final Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    String date = selectedYear + "-" +
                            String.format("%02d", (selectedMonth + 1)) + "-" +
                            String.format("%02d", selectedDay);

                    ((EditText)findViewById(R.id.etDate)).setText(date);

                },
                year, month, day
        );

        // 🔥 Restrict future dates
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePicker.show();
    }
    private void showImageOptions() {
        String[] options = {"Camera", "Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Upload Bill")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                }).show();
    }
    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                return;
            }
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }
    private void openGallery() {

        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, PERMISSION_GALLERY);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == PERMISSION_CAMERA) {
                openCamera();
            } else if (requestCode == PERMISSION_GALLERY) {
                openGallery();
            }

        } else {
            toast("Permission denied");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            try {
                if (requestCode == PICK_IMAGE) {
                    imageUri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } else if (requestCode == CAMERA_REQUEST) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                }

                // 🔥 Compress Image
                bitmap = compressImage(bitmap);

                imgPreview.setImageBitmap(bitmap);
                imgPreview.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private Bitmap compressImage(Bitmap image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int maxWidth = 800;
        int maxHeight = 800;

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int finalWidth = Math.round(width * ratio);
        int finalHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }
    private String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void submitExpense() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait . . .");
        pd.setCancelable(false);
        pd.show();
        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

            int godownNo = prefs.getInt("godownNo", 0);
            int entryByID = prefs.getInt("sno", 0);
            String entryByName = prefs.getString("username", "");

            String expDate = ((EditText)findViewById(R.id.etDate)).getText().toString();
            String expType = ((Spinner)findViewById(R.id.spExpenseType)).getSelectedItem().toString();
            String expRem = ((EditText)findViewById(R.id.etRemarks)).getText().toString();
            String expBy = ((EditText)findViewById(R.id.etExpenseBy)).getText().toString();
            String expAmtStr = ((EditText)findViewById(R.id.etAmount)).getText().toString();
            // 🔥 Strict validation
            if (expDate.isEmpty()) {
                toast("Select expense date");
                return;
            }

            if (expType.equals("Select Expense")) {
                toast("Select expense type");
                return;
            }

            if (expRem.isEmpty()) {
                toast("Enter remarks");
                return;
            }

            if (expBy.isEmpty()) {
                toast("Enter expense by");
                return;
            }

            if (expAmtStr.isEmpty()) {
                toast("Enter amount");
                return;
            }

            double expAmt = Double.parseDouble(expAmtStr);

            String base64Image = "";
            String fileType = "jpg";

            if (bitmap != null) {
                base64Image = convertToBase64(bitmap);
            } else {
                toast("Upload bill");
                return;
            }

            JSONObject json = new JSONObject();
            json.put("godownNo", godownNo);
            json.put("expDate", expDate);
            json.put("expType", expType);
            json.put("expRem", expRem);
            json.put("expBy", expBy);
            json.put("expAmt", expAmt);
            json.put("base64Image", base64Image);
            json.put("fileType", fileType);
            json.put("entryByID", entryByID);
            json.put("entryByName", entryByName);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.NEW_EXPENSE_ENTRY,
                    json,
                    response -> {
                        try{
                            JSONObject jsonResponse = response.getJSONObject("d");
                            boolean valid = jsonResponse.getBoolean("valid");
                            String msg = jsonResponse.getString("message");
                            if (valid) {

                                float currentTotal = prefs.getFloat("ttlExpense", 0f);
                                // add current expense
                                float updatedTotal = currentTotal + (float) expAmt;
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putFloat("ttlExpense", updatedTotal);
                                editor.apply();

                                toast(msg);
                                finish();
                            } else{
                                throw new Exception(msg);
                            }
                            pd.dismiss();
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                            toast(ex.getMessage());
                            pd.dismiss();
                        }
                    },
                    error -> {
                        Log.e("Login", "Volley error: " + error.toString());
                        Toast.makeText(NewExpenseEntry.this, "Expense Entry Failed!!!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
            ){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            Volley.newRequestQueue(this).add(req);

            //sendRequest(json);

        } catch (Exception e) {
            e.printStackTrace();
            pd.dismiss();
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}