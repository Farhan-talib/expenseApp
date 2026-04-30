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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GetExpenses extends AppCompatActivity {
    Spinner spStatus, spExpenseType, spGodown;
    String[] statuses = {"-", "Posted", "Approved", "Marg Entry Done"};
    EditText etDateFrom, etDateTo;
    RecyclerView rvExpenses;
    FloatingActionButton fabPdf;
    FloatingActionButton fabExcel;
    LinearLayout llFilterSpinner;
    ArrayAdapter<SQLHelper.GodownInfo> godownAdapter;
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

        llFilterSpinner = findViewById(R.id.llFilterSpinner);
        spStatus = findViewById(R.id.spStatus);
        spExpenseType = findViewById(R.id.spExpenseType);
        spGodown = findViewById(R.id.spGodown);
        etDateFrom = findViewById(R.id.etFromDate);
        etDateTo = findViewById(R.id.etToDate);
        rvExpenses = findViewById(R.id.rvExpenses);

        fabPdf = findViewById(R.id.fabDownloadPdf);
        fabExcel = findViewById(R.id.fabDownloadExcel);




        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterExpType = ArrayAdapter.createFromResource(
                this,
                R.array.expense_types,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExpenseType.setAdapter(adapterExpType);

        setupDatePicker(etDateFrom);
        setupDatePicker(etDateTo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDateFrom.setText(sdf.format(new Date()));
        etDateTo.setText(sdf.format(new Date()));
        godownsList = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String role = prefs.getString("usertype","");
        LinearLayout.LayoutParams llFParams = (LinearLayout.LayoutParams)llFilterSpinner.getLayoutParams();
        if(role.equals("ADMIN") || role.equals("MARG USER")){
            spGodown.setVisibility(View.VISIBLE);
            llFParams.weight=3;
            llFilterSpinner.setLayoutParams(llFParams);
            loadGodowns();
        }
        else {
            spGodown.setVisibility(View.GONE);
            llFParams.weight = 2;
            llFilterSpinner.setLayoutParams(llFParams);
            loadReport();
        }

        fabPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, "ExpenseReport_" + System.currentTimeMillis() + ".pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 101);
                //generatePdfReport();
            }
        });

        fabExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "ExpenseReport_" + System.currentTimeMillis() + ".csv");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 102);
                //generateExcelReport();
            }
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

                            godownsList.clear();

                            // Default option
                            SQLHelper.GodownInfo def = new SQLHelper.GodownInfo();
                            def.sno = 0;
                            def.name = "Select Godown";
                            godownsList.add(def);

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);

                                SQLHelper.GodownInfo g = new SQLHelper.GodownInfo();
                                g.sno = o.getInt("sno");
                                g.name = o.getString("name");

                                godownsList.add(g);
                            }

                            godownAdapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item,
                                    godownsList);

                            godownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spGodown.setAdapter(godownAdapter);
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == 101) {
            //writePdf(data.getData());
            exportTablePdfToUri(data.getData());
        }
        if (requestCode == 102) {
            writeCsv(data.getData());
        }
    }
    private void writeCsv(Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os)))
        {
            writer.write("Godown Name,Expense Date,Expense Name,Amount,Expense By,Entry By\n");
            List<JSONObject> list = ((ExpenseAdapter) rvExpenses.getAdapter()).getData();
            double amountTtl=0;
            for (JSONObject e : list) {
                String gName = godownsList.stream()
                    .filter(g -> {
                        try {
                            return g.getSno() == e.getInt("godownno");
                        } catch (JSONException ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .map(g -> g.getName())
                    .findFirst()
                    .orElse("");

                writer.write(
                 gName + "," +
                    e.getString("DateOfExpense") + "," +
                     (e.getString("ExpenseName").replaceAll(",","|")) + "," +
                    e.getDouble("ExpenseAmount") + "," +
                    e.getString("ExpenseBY") + "," +
                    e.getString("submittedBY") + "\n"
                );
                amountTtl+=e.getDouble("ExpenseAmount");
            }
            writer.write(",,Total Amount,"+amountTtl+",,");

            writer.flush();
            Toast.makeText(this, "Excel (CSV) saved to Downloads", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "CSV write failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void exportTablePdfToUri(Uri uri) {
        try {
            RecyclerView.Adapter adapter = rvExpenses.getAdapter();
            if (!(adapter instanceof ExpenseAdapter)) return;

            List<JSONObject> list = ((ExpenseAdapter) adapter).getData();

            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) throw new Exception("Stream is null");

            PdfWriter writer = new PdfWriter(os);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Expense Report").setBold().setFontSize(12));
            document.setFontSize(10);
            Table table = new Table(6);

            table.addHeaderCell("Godown Name");
            table.addHeaderCell("Expense Date");
            table.addHeaderCell("Expense Name");
            table.addHeaderCell("Amount");
            table.addHeaderCell("Expense By");
            table.addHeaderCell("Entry By");
            double ttlAmount = 0;
            for (JSONObject e : list) {
                String gName = godownsList.stream()
                        .filter(g -> {
                            try {
                                return g.getSno() == e.getInt("godownno");
                            } catch (JSONException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .map(g -> g.getName())
                        .findFirst()
                        .orElse("");
                table.addCell(gName);
                table.addCell(e.getString("DateOfExpense"));
                table.addCell(e.getString("ExpenseName"));
                table.addCell(String.valueOf(e.getDouble("ExpenseAmount")));
                table.addCell(e.getString("ExpenseBY"));
                table.addCell(e.getString("submittedBY"));
                ttlAmount+=e.getDouble("ExpenseAmount");
            }
            table.addCell("");
            table.addCell("");
            table.addCell("Total Amount");
            table.addCell(String.valueOf(ttlAmount));
            table.addCell("");
            table.addCell("");

            document.add(table);
            document.close();
            os.close();

            Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "PDF export failed", Toast.LENGTH_SHORT).show();
        }
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
            loadReport();
        }
        catch (Exception ex){
            toast(ex.getMessage());
        }
    }

    List<SQLHelper.GodownInfo> godownsList;
    private void loadReport() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait . . .");
        pd.setCancelable(false);
        pd.show();
        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

            int godownNo = prefs.getInt("godownNo", 0);
            int uno = prefs.getInt("sno", 0);

            if(prefs.getString("usertype","").equals("ADMIN") || prefs.getString("usertype","").equals("MARG USER")){
                SQLHelper.GodownInfo selected = (SQLHelper.GodownInfo) spGodown.getSelectedItem();
                godownNo = (selected != null) ? selected.sno : 0;
                uno=0;
            }

            String fromDate = etDateFrom.getText().toString();
            String toDate = etDateTo.getText().toString();
            String expType = spExpenseType.getSelectedItem().toString();
            String status = spStatus.getSelectedItem().toString();

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
            System.out.println(json.toString());
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.GET_EXPENSE_REPORT,
                    json,
                    response -> {
                        try {
                            System.out.println(response);
                            JSONObject d = response.getJSONObject("d");

                            if (!d.getBoolean("valid")) {
                                toast("Error");
                                return;
                            }

                            JSONArray arr = d.getJSONArray("data");
                            if(arr.length()>0) {
                                List<JSONObject> list = new ArrayList<>();
                                for (int i = 0; i < arr.length(); i++) {
                                    list.add(arr.getJSONObject(i));
                                }

                                fabPdf.setVisibility(View.VISIBLE);
                                fabExcel.setVisibility(View.VISIBLE);
                                rvExpenses.setLayoutManager(new LinearLayoutManager(this));
                                rvExpenses.setAdapter(new ExpenseAdapter(list));
                            }
                            else{
                                toast("No data found");
                                rvExpenses.setAdapter(null);
                                fabPdf.setVisibility(View.GONE);
                                fabExcel.setVisibility(View.GONE);
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


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

        List<JSONObject> list;

        ExpenseAdapter(List<JSONObject> list) {
            this.list = list;
        }
        public List<JSONObject> getData(){
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
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.helper_expense_items, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int i) {
            try {
                JSONObject obj = list.get(i);

                h.chkSelect.setVisibility(View.GONE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)h.llInner.getLayoutParams();
                lp.weight = 10f;
                h.llInner.setLayoutParams(lp);

                String gName = godownsList.stream()
                        .filter(g -> {
                            try {
                                return g.getSno() == obj.getInt("godownno");
                            } catch (JSONException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .map(g -> g.getName())
                        .findFirst()
                        .orElse("");
                h.godownName.setText(gName);
                h.name.setText(obj.getString("ExpenseName"));
                h.amount.setText("₹ " + obj.getDouble("ExpenseAmount"));
                h.date.setText(obj.getString("DateOfExpense"));
                h.expBy.setText("Exp By - "+obj.getString("ExpenseBY"));

                if(i%2==0)
                    h.llExpense.setBackgroundColor(getResources().getColor(R.color.white));
                else
                    h.llExpense.setBackgroundColor(getResources().getColor(R.color.light_grey));


                String billPath = obj.getString("supportingBill");
                if(billPath.equalsIgnoreCase("None") || billPath.equalsIgnoreCase("-"))
                    h.itemView.findViewById(R.id.tvViewBill).setVisibility(View.GONE);
                else {
                    h.itemView.findViewById(R.id.tvViewBill).setVisibility(View.VISIBLE);
                    h.itemView.findViewById(R.id.tvViewBill).setOnClickListener(v -> {
                        openBillImage(billPath);
                    });
                }



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

}