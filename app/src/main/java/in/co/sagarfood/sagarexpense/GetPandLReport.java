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
import android.widget.EditText;
import android.widget.ImageView;
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

public class GetPandLReport extends AppCompatActivity {
    Spinner spGodown;
    EditText etDateFrom, etDateTo;
    RecyclerView rvIncomes;
    FloatingActionButton fabPdf;
    FloatingActionButton fabExcel;
    ArrayAdapter<SQLHelper.GodownInfo> godownAdapter;
    List<SQLHelper.GodownInfo> godownsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_pand_lreport);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        spGodown = findViewById(R.id.spPLGodown);
        etDateFrom = findViewById(R.id.etPLFromDate);
        etDateTo = findViewById(R.id.etPLToDate);
        rvIncomes = findViewById(R.id.rvPL);

        fabPdf = findViewById(R.id.fabDownloadPLPdf);
        fabExcel = findViewById(R.id.fabDownloadPLExcel);

        godownsList = new ArrayList<>();

        setupDatePicker(etDateFrom);
        setupDatePicker(etDateTo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDateFrom.setText(sdf.format(new Date()));
        etDateTo.setText(sdf.format(new Date()));

        loadGodowns();


        fabPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, "PandLReport_" + System.currentTimeMillis() + ".pdf");
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
                intent.putExtra(Intent.EXTRA_TITLE, "PandLReport_" + System.currentTimeMillis() + ".csv");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 102);
                //generateExcelReport();
            }
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
            writer.write("Date,Income,Expense,Godown,Status\n");
            List<JSONObject> list = ((GetPandLReport.PLAdapter) rvIncomes.getAdapter()).getData();
            double ttlInc=0, ttlExp = 0;
            for (JSONObject e : list) {
                String gName ="-";
                if(e.getInt("godownNo")>0)
                    gName = godownsList.stream()
                            .filter(g -> {
                                try {
                                    return g.getSno() == e.getInt("godownNo");
                                } catch (JSONException ex) {
                                    throw new RuntimeException(ex);
                                }
                            })
                            .map(g -> g.getName())
                            .findFirst()
                            .orElse("");
                String status = "Loss";
                double inc = e.getDouble("incomeAmount");
                double exp = e.getDouble("expenseAmount");
                if(inc>exp)
                    status = "Profit";
                else if(inc<exp)
                    status = "Loss";
                else
                    status = "Equal";
                writer.write(
                    e.getString("Date") + "," +
                        e.getDouble("incomeAmount") + "," +
                        e.getString("expenseAmount") + "," +
                        gName + "," +
                        status + "\n"
                    );
                ttlInc+=e.getDouble("incomeAmount");
                ttlExp+=e.getDouble("expenseAmount");
            }
            writer.write(","+ttlInc+","+ttlExp+",,"+((ttlInc>ttlExp)?"Profit":((ttlInc<ttlExp)?"Loss":"Equal")));

            writer.flush();
            Toast.makeText(this, "Excel (CSV) saved to Downloads", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "CSV write failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void exportTablePdfToUri(Uri uri) {
        try {
            RecyclerView.Adapter adapter = rvIncomes.getAdapter();
            if (!(adapter instanceof GetPandLReport.PLAdapter)) return;

            List<JSONObject> list = ((GetPandLReport.PLAdapter) adapter).getData();

            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) throw new Exception("Stream is null");

            PdfWriter writer = new PdfWriter(os);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("P and L Report").setBold().setFontSize(12));
            document.setFontSize(10);
            Table table = new Table(5);

            table.addHeaderCell("Date");
            table.addHeaderCell("Income");
            table.addHeaderCell("Expense");
            table.addHeaderCell("Godown Name");
            table.addHeaderCell("Status");
            double ttlInc = 0, ttlExp = 0;
            for (JSONObject e : list) {
                String gName = "";
                if(e.getInt("godownNo")>0)
                    gName = godownsList.stream()
                            .filter(g -> {
                                try {
                                    return g.getSno() == e.getInt("godownNo");
                                } catch (JSONException ex) {
                                    throw new RuntimeException(ex);
                                }
                            })
                            .map(g -> g.getName())
                            .findFirst()
                            .orElse("");

                String status = "Loss";
                double inc = e.getDouble("incomeAmount");
                double exp = e.getDouble("expenseAmount");
                if(inc>exp)
                    status = "Profit";
                else if(inc<exp)
                    status = "Loss";
                else
                    status = "Equal";

                table.addCell(e.getString("Date"));
                table.addCell(String.valueOf(e.getDouble("incomeAmount")));
                table.addCell(e.getString("expenseAmount"));
                table.addCell(gName);
                table.addCell(status);
                ttlInc+=e.getDouble("incomeAmount");
                ttlExp+=e.getDouble("expenseAmount");
            }
            table.addCell("");
            table.addCell(String.valueOf(ttlInc));
            table.addCell(String.valueOf(ttlExp));
            table.addCell("");
            table.addCell((ttlInc>ttlExp)?"Profit":((ttlInc<ttlExp)?"Loss":"Equal"));

            document.add(table);
            document.close();
            os.close();

            Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF export failed", Toast.LENGTH_SHORT).show();
        }
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
                            System.out.println(response);
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
                            loadPLReport();
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
    public void GetPLReport(View v){
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
            loadPLReport();
        }
        catch (Exception ex){
            toast(ex.getMessage());
        }
    }

    private void loadPLReport() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait . . .");
        pd.setCancelable(false);
        pd.show();
        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

            SQLHelper.GodownInfo selected = (SQLHelper.GodownInfo) spGodown.getSelectedItem();
            int godownNo = (selected != null) ? selected.sno : 0;
            int uid = 0;
            String fromDate = etDateFrom.getText().toString();
            String toDate = etDateTo.getText().toString();

            JSONObject json = new JSONObject();
            try {
                json.put("uid", uid);
                json.put("godownNo", godownNo);
                json.put("dateFrom", fromDate);
                json.put("dateTo", toDate);
            } catch (Exception e) {
                toast(e.getMessage());
            }
            System.out.println(json.toString());
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    APIHelper.GET_INCOME_AND_EXPENSES,
                    json,
                    response -> {
                        try {
                            pd.dismiss();
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
                                rvIncomes.setLayoutManager(new LinearLayoutManager(this));
                                rvIncomes.setAdapter(new GetPandLReport.PLAdapter(list));
                            }
                            else{
                                toast("No data found");
                                rvIncomes.setAdapter(null);
                                fabPdf.setVisibility(View.GONE);
                                fabExcel.setVisibility(View.GONE);
                            }

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


    class PLAdapter extends RecyclerView.Adapter<GetPandLReport.PLAdapter.VH> {

        List<JSONObject> list;

        PLAdapter(List<JSONObject> list) {
            this.list = list;
        }
        public List<JSONObject> getData(){
            return list;
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivMarker;
            TextView date, incAmt, expAmt, godown;
            LinearLayout llRow;

            VH(View v) {
                super(v);
                ivMarker = v.findViewById(R.id.ivPLMarker);
                date = v.findViewById(R.id.tvPLDate);
                incAmt = v.findViewById(R.id.tvPLInc);
                expAmt = v.findViewById(R.id.tvPLExp);
                godown = v.findViewById(R.id.tvPLGName);
                llRow = v.findViewById(R.id.llPLRow);
            }
        }

        @Override
        public GetPandLReport.PLAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.helper_pandl_list, parent, false);
            return new GetPandLReport.PLAdapter.VH(v);
        }

        @Override
        public void onBindViewHolder(GetPandLReport.PLAdapter.VH h, int i) {
            try {
                JSONObject obj = list.get(i);

                String gName = "-";
                if(obj.getInt("godownNo")>0)
                    gName = godownsList.stream()
                            .filter(g -> {
                                try {
                                    return g.getSno() == obj.getInt("godownNo");
                                } catch (JSONException ex) {
                                    throw new RuntimeException(ex);
                                }
                            })
                            .map(g -> g.getName())
                            .findFirst()
                            .orElse("");
                h.date.setText(obj.getString("Date"));
                h.incAmt.setText("₹ " + obj.getDouble("incomeAmount"));
                h.expAmt.setText("₹ " + obj.getDouble("expenseAmount"));
                h.godown.setText(gName);

                double incAmt = obj.getDouble("incomeAmount");
                double expAmt = obj.getDouble("expenseAmount");
                if(incAmt>=expAmt)
                    h.ivMarker.setImageDrawable(getResources().getDrawable(R.drawable.ico_up));
                else
                    h.ivMarker.setImageDrawable(getResources().getDrawable(R.drawable.ico_down));

                if(i%2==0) {
                    h.llRow.setBackgroundColor(getResources().getColor(R.color.light_grey));
                }
                else {
                    h.llRow.setBackgroundColor(getResources().getColor(R.color.white));
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


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}