package in.co.sagarfood.sagarexpense;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class NewMargEntry extends AppCompatActivity {
    Spinner spGodown, spExpenseType;;
    EditText etDateFrom, etDateTo;
    RecyclerView rvExpenses;
    List<SQLHelper.GodownInfo> godownList = new ArrayList<>();
    List<SQLHelper.ExpenseInfo> list = new ArrayList<>();
    ArrayAdapter<SQLHelper.GodownInfo> adapter;
    NewMargEntry.UnverifiedExpenseAdapter expAdapter;
    Button btnSelect, btnApprove;
    TextView tvHeader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_un_approved_exp_list);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Approved Expenses");
        spGodown = findViewById(R.id.spGodownU);
        spExpenseType = findViewById(R.id.spExpenseTypeU);
        etDateFrom = findViewById(R.id.etFromDateU);
        etDateTo = findViewById(R.id.etToDateU);
        rvExpenses = findViewById(R.id.rvExpensesU);
        btnSelect = findViewById(R.id.btnSelect);
        btnApprove = findViewById(R.id.btnApprove);
        btnApprove.setText("Marg Done");

        ArrayAdapter<CharSequence> adapterExpType = ArrayAdapter.createFromResource(
                this,
                R.array.expense_types,
                android.R.layout.simple_spinner_item
        );

        adapterExpType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExpenseType.setAdapter(adapterExpType);

        setupDatePicker(etDateFrom);
        setupDatePicker(etDateTo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDateFrom.setText(sdf.format(new Date()));
        etDateTo.setText(sdf.format(new Date()));
        loadGodowns();
        btnSelect.setOnClickListener(v -> {
            if(btnSelect.getText().equals("Select All")) {
                for (SQLHelper.ExpenseInfo item : list) {
                    item.isSelected = true;
                }
                btnSelect.setText("Deselect All");
            } else{
                for (SQLHelper.ExpenseInfo item : list) {
                    item.isSelected = false;
                }
                btnSelect.setText("Select All");
            }
            expAdapter.notifyDataSetChanged();
        });
        btnApprove.setOnClickListener(v->{
            String selectedExpenseSno="";
            int noOfExpenses=0;
            for (SQLHelper.ExpenseInfo item : list) {
                if(item.isSelected) {
                    if(!selectedExpenseSno.equalsIgnoreCase(""))
                        selectedExpenseSno+=",";
                    selectedExpenseSno += ""+item.getSno();
                    noOfExpenses++;
                }
            }
            if(noOfExpenses>0)
                markMargEntryDone(selectedExpenseSno);
            else
                toast("Select atleast 1 expense to approve");
        });
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
                            loadReport();
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
    private void markMargEntryDone(String expenses) {
        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            int uno = prefs.getInt("sno", 0);

            JSONObject json = new JSONObject();
            json.put("entryBy", uno);
            json.put("expensesList", expenses);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.MARK_MARG_ENTRY_DONE,
                    json,
                    response -> {
                        try {
                            JSONObject d = response.getJSONObject("d");
                            if(d.getBoolean("valid")) {
                                toast(d.getString("message"));
                                loadReport();
                            } else
                                throw new Exception(d.getString("message"));

                        } catch (Exception e) {
                            e.printStackTrace();
                            toast(e.getMessage());
                        }
                    },
                    error -> Toast.makeText(this, "Failed to Approve Expenses", Toast.LENGTH_SHORT).show()
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
            loadReport();
        }
        catch (Exception ex){
            toast(ex.getMessage());
        }
    }
    private void loadReport() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait . . .");
        pd.setCancelable(false);
        pd.show();
        try {
            btnSelect.setText("Select All");
            btnSelect.setVisibility(View.GONE);
            btnApprove.setVisibility(View.GONE);
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

            SQLHelper.GodownInfo selected = (SQLHelper.GodownInfo) spGodown.getSelectedItem();
            int godownNo = (selected != null) ? selected.sno : 0;
            int uno = 0;//prefs.getInt("sno", 0);

            String fromDate = etDateFrom.getText().toString();
            String toDate = etDateTo.getText().toString();
            String expType = spExpenseType.getSelectedItem().toString();
            String status = "Approved";

            JSONObject json = new JSONObject();
            try {
                json.put("godownNo", godownNo);
                json.put("uno", uno);
                json.put("expType", expType);
                json.put("status", status);
                json.put("dateFrom", fromDate);
                json.put("dateTo", toDate);
            } catch (Exception e) {
                toast(e.getMessage());
            }
            System.out.println(json);
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.GET_EXPENSE_REPORT,
                    json,
                    response -> {
                        System.out.println(response);
                        try {
                            JSONObject d = response.getJSONObject("d");

                            if (!d.getBoolean("valid")) {
                                toast("Error");
                                return;
                            }

                            JSONArray arr = d.getJSONArray("data");
                            if(arr.length()>0) {
                                list = new ArrayList<>();
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);
                                    SQLHelper.ExpenseInfo ei = new SQLHelper.ExpenseInfo();
                                    ei.setSno(obj.getInt("sno"));
                                    ei.setGodownNo(obj.getInt("godownno"));
                                    ei.setUserno(obj.getInt("userno"));
                                    ei.setDateOfExpense(obj.getString("DateOfExpense"));
                                    ei.setExpenseName(obj.getString("ExpenseName"));
                                    ei.setExpenseBY(obj.getString("ExpenseBY"));
                                    ei.setSubmittedOn(obj.getString("submittedOn"));
                                    ei.setSubmittedBY(obj.getString("submittedBY"));
                                    ei.setSupportingBill(obj.getString("supportingBill"));
                                    ei.setSignsrc(obj.getString("signsrc"));
                                    ei.setVerifiedByAdmin(obj.getString("verifiedByAdmin"));
                                    ei.setStatus(obj.getString("status"));
                                    ei.setExpenseAmount(obj.getDouble("ExpenseAmount"));
                                    ei.setSelected(false);
                                    list.add(ei);
                                }
                                btnSelect.setVisibility(View.VISIBLE);
                                btnApprove.setVisibility(View.VISIBLE);
                                rvExpenses.setLayoutManager(new LinearLayoutManager(this));
                                expAdapter = new NewMargEntry.UnverifiedExpenseAdapter();
                                rvExpenses.setAdapter(expAdapter);
                            }
                            else{
                                toast("No data found");
                                rvExpenses.setAdapter(null);
                            }
                            pd.dismiss();

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            pd.dismiss();
                        }
                    },
                    error -> {
                        toast("Failed to load report");
                        pd.dismiss();
                    }
            );

            Volley.newRequestQueue(this).add(req);
        }
        catch (Exception ex){
            pd.dismiss();
            toast(ex.getMessage());
        }
    }

    class UnverifiedExpenseAdapter extends RecyclerView.Adapter<NewMargEntry.UnverifiedExpenseAdapter.VH> {

        //        UnverifiedExpenseAdapter(List<SQLHelper.ExpenseInfo> list) {
//            this.list = list;
//        }
        public List<SQLHelper.ExpenseInfo> getData(){
            return list;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView name, amount, date, expBy, godownName;
            LinearLayout llExpense, llInner;
            CheckBox chkSelect;

            VH(View v) {
                super(v);
                godownName = v.findViewById(R.id.txtGodown);
                llExpense = v.findViewById(R.id.llExpenses);
                llInner = v.findViewById(R.id.llInner);
                name = v.findViewById(R.id.txtName);
                amount = v.findViewById(R.id.txtAmount);
                date = v.findViewById(R.id.txtDate);
                expBy = v.findViewById(R.id.txtExpenseBy);
                chkSelect = v.findViewById(R.id.chkSelect);
            }
        }

        @Override
        public NewMargEntry.UnverifiedExpenseAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.helper_expense_items, parent, false);
            return new NewMargEntry.UnverifiedExpenseAdapter.VH(v);
        }

        @Override
        public void onBindViewHolder(NewMargEntry.UnverifiedExpenseAdapter.VH h, int i)
        {
            try {
                SQLHelper.ExpenseInfo obj = list.get(i);

                h.chkSelect.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)h.llInner.getLayoutParams();
                lp.weight = 9f;
                h.llInner.setLayoutParams(lp);


                // ❗ STEP 1: Remove old listener
                h.chkSelect.setOnCheckedChangeListener(null);

                // ❗ STEP 2: Set correct state from model
                h.chkSelect.setChecked(obj.isSelected);

                // ❗ STEP 3: Add listener again
                h.chkSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    obj.isSelected = isChecked;
                });

                String gName = godownList.stream()
                        .filter(g -> g.getSno() == obj.getGodownNo())
                        .map(g -> g.getName())
                        .findFirst()
                        .orElse("---"+obj.getGodownNo());
                h.godownName.setText(gName);
                h.name.setText(obj.getExpenseName());
                h.amount.setText("₹ " + obj.getExpenseAmount());
                h.date.setText(obj.getDateOfExpense());
                h.expBy.setText("Exp By - "+obj.getExpenseBY());

                if(i%2==0)
                    h.llExpense.setBackgroundColor(getResources().getColor(R.color.white));
                else
                    h.llExpense.setBackgroundColor(getResources().getColor(R.color.light_grey));


                String billPath = obj.getSupportingBill();

                h.itemView.findViewById(R.id.tvViewBill).setOnClickListener(v -> {
                    openBillImage(billPath);
                });



            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private void openBillImage(String path) {

        String fullUrl = APIHelper.BASE_URL + path;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(fullUrl), "image/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open image", Toast.LENGTH_SHORT).show();
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}