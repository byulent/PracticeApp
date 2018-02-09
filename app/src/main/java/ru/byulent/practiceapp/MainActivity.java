package ru.byulent.practiceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.widget.AdapterView.*;

public class MainActivity extends AppCompatActivity {

    private static final int GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1;
    private static final String LOG_TAG = "PracticeApp";
    private static final int[] ranges = {Calendar.DAY_OF_MONTH, Calendar.WEEK_OF_YEAR, Calendar.MONTH};
    ArrayList<Map<String, String>> datapoints;
    SimpleAdapter listAdapter;
    //Data types.
    DataType[] types = {
            //Aggregable types.
            //Open fitness data.
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_WEIGHT,
            DataType.TYPE_ACTIVITY_SEGMENT,
            DataType.TYPE_BASAL_METABOLIC_RATE,
            DataType.TYPE_BODY_FAT_PERCENTAGE,
            DataType.TYPE_CALORIES_EXPENDED,
            DataType.TYPE_DISTANCE_DELTA,
            DataType.TYPE_HEART_RATE_BPM,
//            DataType.TYPE_HEIGHT,
            DataType.TYPE_HYDRATION,
            DataType.TYPE_NUTRITION,
            DataType.TYPE_POWER_SAMPLE,
            DataType.TYPE_SPEED,
            //Non-aggregable types.
            DataType.TYPE_ACTIVITY_SAMPLES,
            DataType.TYPE_HEIGHT
    };
//Aggregates for data types.
    DataType [] aggregates = {
            DataType.AGGREGATE_STEP_COUNT_DELTA,
            DataType.AGGREGATE_WEIGHT_SUMMARY,
            DataType.AGGREGATE_ACTIVITY_SUMMARY,
            DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY,
        DataType.AGGREGATE_BODY_FAT_PERCENTAGE_SUMMARY,
        DataType.AGGREGATE_CALORIES_EXPENDED,
        DataType.AGGREGATE_DISTANCE_DELTA,
        DataType.AGGREGATE_HEART_RATE_SUMMARY,
//        DataType.AGGREGATE_HEIGHT_SUMMARY,
        DataType.AGGREGATE_HYDRATION,
        DataType.AGGREGATE_NUTRITION_SUMMARY,
        DataType.AGGREGATE_POWER_SUMMARY,
        DataType.AGGREGATE_SPEED_SUMMARY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datapoints = new ArrayList<>();
        listAdapter = new SimpleAdapter(this, datapoints, android.R.layout.simple_list_item_2, new String[]{"field", "value"}, new int[]{android.R.id.text1, android.R.id.text2}) {
        };
        ListView list = findViewById(R.id.list);
        list.setAdapter(listAdapter);
        FitnessOptions.Builder builder = FitnessOptions.builder();
        for (DataType type : types) {
            builder.addDataType(type, FitnessOptions.ACCESS_READ);
        }
        for (DataType aggregate: aggregates) {
            builder.addDataType(aggregate, FitnessOptions.ACCESS_READ);
        }
        FitnessOptions fitnessOptions = builder.build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)){
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSION_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions
            );
        }
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.time_ranges, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(), String.valueOf(i), Toast.LENGTH_SHORT).show();
                datapoints.clear();
                loadData(ranges[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadData(int timeRange) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(timeRange, -1);
        long startTime = cal.getTimeInMillis();

        long days = TimeUnit.DAYS.convert(endTime-startTime, TimeUnit.MILLISECONDS);

        int aggs = aggregates.length;
        DataReadRequest.Builder builder = new DataReadRequest.Builder();
        //Aggregating data.
        for (int i = 0; i < aggs; i++){
//            if (i==8) builder.aggregate(DataType.TYPE_HEIGHT, DataType.AGGREGATE_HEIGHT_SUMMARY);
            builder.aggregate(types[i], aggregates[i]);
        }
        for (int i = aggs; i < types.length; i++) {
            builder.read(types[i]);
        }
        DataReadRequest readRequest = builder
                .bucketByTime((int)days,TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        final ProgressDialog progressDialog = ProgressDialog.show(this,"Loading","Loading data...",true,false);
        Task<DataReadResponse> response = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(LOG_TAG, "onSuccess()");
                        printData(dataReadResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Log.d(LOG_TAG, "onComplete()");
                        progressDialog.dismiss();
                    }
                });

//        List<DataSet> dataSets = response.getResult().getDataSets();
    }

    private void printData(DataReadResponse dataReadResponse) {
        if(dataReadResponse.getBuckets().size() > 0){
            for(Bucket bucket: dataReadResponse.getBuckets()){
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        for (DataPoint dataPoint: dataSet.getDataPoints()){
            for (Field field : dataPoint.getDataType().getFields()){
                HashMap<String, String> m = new HashMap<>();
                m.put("field", dataPoint.getDataType().getName() + ": " + field.getName());
                m.put("value", dataPoint.getValue(field).toString());
                datapoints.add(m);
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == GOOGLE_FIT_PERMISSION_REQUEST_CODE){
                loadData(ranges[0]);
            }
        }
    }
}
