package com.example.btmm;

import static java.lang.Math.abs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.example.btmm.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Page1 extends Fragment implements OnChartValueSelectedListener, View.OnClickListener {

    private static final int PERMISSION_STORAGE = 0;
    private static LineChart chart;
    private TextView rawDataText;
    private SwitchMaterial graphSwitch;
    private String mode;

    public Page1(){
        //required empty public constructor.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page1,container,false);
        rawDataText = (TextView) rootView.findViewById(R.id.raw);
        graphSwitch = (SwitchMaterial) rootView.findViewById(R.id.toggleGraph);
        rootView.findViewById(R.id.clearGraph).setOnClickListener(this);
        rootView.findViewById(R.id.saveGraph).setOnClickListener(this);
        rootView.findViewById(R.id.saveData).setOnClickListener(this);

        ChipGroup modeSelector = rootView.findViewById(R.id.modeSelector);
        modeSelector.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {

                Chip chip = chipGroup.findViewById(i);
                if (chip != null)
                    if (chart != null) {
                        clearData();
                        mode = String.valueOf(chip.getText());
                        Toast.makeText(getContext(), "Switched to " + mode, Toast.LENGTH_SHORT).show();
                    }
            }
        });
        Chip volt = rootView.findViewById(R.id.voltage);
        volt.setChecked(true);
        mode = "Voltage";
        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getActivity().setContentView(R.layout.fragment_page1);

        //getActivity().setTitle("RealtimeLineChartActivity");

        chart = rootView.findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        // enable description text
        chart.getDescription().setEnabled(true);
        chart.getDescription().setTextColor(Color.WHITE);
        chart.getDescription().setText("");
        chart.getDescription().setTextSize(100);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);
        if (getContext() != null) {
            if ((getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                chart.setBackgroundColor(Color.DKGRAY);
            }
        }
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        //leftAxis.setAxisMaximum(50f);
        //leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        return rootView;
    }

    protected void clearData() {
        LineData data = chart.getData();
        chart.clearValues();
        chart.notifyDataSetChanged();
        chart.getDescription().setText("");

        ILineDataSet set = createSet();
        data.addDataSet(set);
        data.notifyDataChanged();
    }
    protected void newData(float newVal) {
        if (graphSwitch.isChecked()) {
            addEntry(newVal);
        }
        updateRaw(newVal);
    }
    protected void updateRaw(float newVal) {
        //val.setText(String.valueOf(Math.round(newVal*1000.0)/1000.0));
        String raw;
        switch (mode) {
            case "Voltage":
                raw = String.valueOf(newVal) + " V";
                break;
            case "Current":
                raw = String.valueOf(newVal) + " A";
                break;
            case "Resistance":
                raw = String.valueOf(newVal) + " Ω";
                break;
            default:
                raw = "Error";
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();

        }
        rawDataText.setText(raw);
    }

    protected static void addEntry(float val) {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), val), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(50);
            //chart.setVisibleYRange(50, 0,AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getXMax());
            //chart.moveViewTo(data.getXMax(), data.getYMax(), AxisDependency.LEFT);

            // this automatically refreshes the chart (calls invalidate())
            //chart.moveViewTo(data.getXMax()-50, 55f, AxisDependency.LEFT);
        }
    }

    public void decodeData(byte[] data) {
        int index=0;
        switch (mode) {
            case "Voltage":
                index = 0;
                break;
            case "Current":
                index = 2;
                break;
            case "Resistance":
                index = 4;
                break;
            default:
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
//        Log.w("BeforeDecodeValue", String.valueOf(data[0]) + " " + String.valueOf(data[1]));
        /*
        byte[] data = new byte[36];
        //... populate byte array...

        ByteBuffer buffer = ByteBuffer.wrap(data);

        int first = buffer.getInt();
        float second = buffer.getFloat();
         */

        //int asInt = (data[index] & 0xFF) | ((data[index+1] & 0xFF) << 8);
        //float num = Float.intBitsToFloat(asInt);

        //float num = Float.intBitsToFloat(data[index] ^ data[index+1]<<8);

        //DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        //float f = dis.readFloat();

        //int combined = (data[index+1] << 8) | data[index];
        //float num = Float.intBitsToFloat(combined);

        //int low=data[index] & 0xff;
        //int high=(data[index+1] & 0xff)<< 8;
        //float num = (low | high);

        int low=data[index] & 0xff;
        int high=data[index+1] << 8;
        float num = (high | low);
        if (!mode.equals("Resistance")) {
            num = num/1000;
        }
//        Log.w("AfterDecode",String.valueOf(num));
        if (!String.valueOf(num).equals("NaN")) {
            newData(abs(num));
        }
        //newData(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat());
    }

    private static LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Multimeter reading");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry(0);
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnable inside the loop.
                    getActivity().runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    //@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getActivity().getMenuInflater().inflate(R.menu.realtime, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.viewGithub: {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/RealtimeLineChartActivity.java"));
//               startActivity(i);
//                break;
//            }
//            case R.id.actionAdd: {
//                addEntry(0);
//                break;
//            }
//            case R.id.actionClear: {
//                chart.clearValues();
//                Toast.makeText(getActivity().getApplicationContext(), "Chart cleared!", Toast.LENGTH_SHORT).show();
//                break;
//            }
//            case R.id.actionFeedMultiple: {
//                feedMultiple();
//                break;
//            }
//            case R.id.actionSave: {
//                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    saveToGallery(chart, "RealtimeLineChartActivity");
//                } else {
//                    requestStoragePermission(chart);
//                }
//                break;
//            }
//        }
//        return true;
//    }

    protected void saveToGallery(Chart chart, String name) {
        //if (chart.saveToPath(name + "_" + System.currentTimeMillis(), ""))
        if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), "ChartScreenshots","Screenshot of chart", Bitmap.CompressFormat.PNG, 100))
            Toast.makeText(getContext(), "Saving to gallery SUCCESSFUL!",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                    .show();
    }

    protected void requestStoragePermission(View view) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(view, "Write permission is required to save image to gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                        }
                    }).show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Permission Required!", Toast.LENGTH_SHORT)
                    .show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        chart.getDescription().setText(e.toString());
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.clearGraph:
                clearData();
                Toast.makeText(getActivity().getApplicationContext(), "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.saveGraph:
                graphSwitch.setChecked(false);
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery(chart, "graph");
                }
                else {
                    requestStoragePermission(chart);
                }
                break;
            case R.id.saveData:
                graphSwitch.setChecked(false);
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveData();
                }
                else {
                    requestStoragePermission(chart);
                }
                break;
            //case R.id.rawData:
                //Intent intent = new Intent(getActivity(), Activity_BTLE_Services.class);
                //intent.putExtra(Activity_BTLE_Services.EXTRA_NAME, name);
                //intent.putExtra(Activity_BTLE_Services.EXTRA_ADDRESS, address);
                //startActivityForResult(intent, 2);
            default:
                throw new RuntimeException("Button error");
        }
    }
    public void saveData() {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String pathname = folder.getPath() + "/data"+ "_" + System.currentTimeMillis()+".txt";
        try {
            LineData data = chart.getData();
            ILineDataSet set = data.getDataSetByIndex(0);
            if (data.getEntryCount() > 0) {
                File file = new File(pathname);
                Writer writer = new BufferedWriter(new FileWriter(file));
                for (int i = 0; i < set.getEntryCount(); i++) {
                    writer.write(String.valueOf(set.getEntryForIndex(i).getY()));
                    writer.write(System.getProperty( "line.separator" ));
                }
                writer.close();
                Toast.makeText(getActivity().getApplicationContext(), "Saved data file", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No data to save", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("File error: ", e.getMessage());
        }
    }
}