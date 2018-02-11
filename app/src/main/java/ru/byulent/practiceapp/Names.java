package ru.byulent.practiceapp;

import android.content.Context;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.HashMap;

/**
 * Created by Бюлент on 10.02.2018.
 */

public class Names {
    private static HashMap<String, Integer> datatypes;
    private static HashMap<Field, Integer> fields;
    private static HashMap<Object, Integer> measures;

    public static void setNames() {
        datatypes = new HashMap<>();
        fields = new HashMap<>();
        measures = new HashMap<>();
        //todo: add all existing datatypes
        datatypes.put("com.google.activity.summary", R.string.activity_summary);
        datatypes.put("com.google.heart_rate.summary", R.string.heart_rate_summary);
        datatypes.put("com.google.step_count.delta", R.string.step_count_delta);
        datatypes.put("com.google.calories.expended", R.string.calories_expended);
        datatypes.put("com.google.distance.delta", R.string.distance_delta);
        datatypes.put("com.google.weight.summary", R.string.weight_summary);
        datatypes.put("com.google.calories.bmr.summary", R.string.calories_bmr_summary);
        datatypes.put("com.google.location.bounding_box", R.string.location_bounding_box);
        //todo: add all existing fields
        fields.put(Field.FIELD_MAX, R.string.max);
        fields.put(Field.FIELD_MIN, R.string.min);
        fields.put(Field.FIELD_AVERAGE, R.string.average);
        fields.put(Field.FIELD_STEPS, R.string.steps);
        measures.put(DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY, R.string.kcal);
        measures.put(DataType.AGGREGATE_WEIGHT_SUMMARY, R.string.kg);
        measures.put(Field.FIELD_DURATION, R.string.s);
        measures.put(Field.FIELD_DISTANCE, R.string.m);
        measures.put(Field.FIELD_CALORIES, R.string.kcal);
        measures.put(DataType.AGGREGATE_LOCATION_BOUNDING_BOX, R.string.degree);
    }

    public static String getDataTypeString (String dataTypeName, Context context) {
        if (datatypes.get(dataTypeName) != null){
            return context.getString(datatypes.get(dataTypeName));
        }
        return dataTypeName;
    }

    public static String getFieldName (Field field, Context context) {
        if (fields.get(field) != null){
            return context.getString(fields.get(field));
        }
        return field.getName();
    }

    public static String getMeasure (Object object, Context context) {
        if (measures.get(object) != null){
            return context.getString(measures.get(object));
        }
        return null;
    }
}
