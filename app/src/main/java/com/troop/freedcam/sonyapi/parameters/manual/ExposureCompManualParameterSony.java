package com.troop.freedcam.sonyapi.parameters.manual;

import com.troop.freedcam.sonyapi.parameters.ParameterHandlerSony;
import com.troop.freedcam.sonyapi.sonystuff.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by troop on 03.01.2015.
 */
public class ExposureCompManualParameterSony extends BaseManualParameterSony
{
    int min = -1;
    int max = -1;
    public ExposureCompManualParameterSony(String VALUE_TO_GET, String VALUES_TO_GET, String VALUE_TO_SET, ParameterHandlerSony parameterHandlerSony) {
        super(VALUE_TO_GET, VALUES_TO_GET, VALUE_TO_SET, parameterHandlerSony);
    }

    @Override
    public int GetMaxValue()
    {
        if (max == -1)
            getMinMaxValues();
        return max;
    }

    @Override
    public int GetMinValue()
    {
        if (min == -1)
            getMinMaxValues();
        return min;
    }

    @Override
    public void SetValue(final int valueToSet)
    {
        this.val = valueToSet;
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                //String val = valueToSet +"";
                JSONArray array = null;
                try {
                    array = new JSONArray().put(0, valueToSet);
                    JSONObject object =  ParameterHandler.mRemoteApi.setParameterToCamera(VALUE_TO_SET, array);
                    currentValueChanged(valueToSet);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getMinMaxValues()
    {
        if (min == -1 && max == -1)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        JSONObject object =  ParameterHandler.mRemoteApi.getParameterFromCamera(VALUES_TO_GET);
                        JSONArray array = object.getJSONArray("result");
                        min = array.getInt(2);
                        max = array.getInt(1);
                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            }).start();
            while (max == -1)
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int GetValue()
    {
        val = -1;
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    JSONObject object = mRemoteApi.getParameterFromCamera(VALUE_TO_GET);
                    JSONArray array = object.getJSONArray("result");
                    val = array.getInt(0);


                } catch (IOException e) {
                    e.printStackTrace();
                    val = 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                    val = 0;
                }
            }
        }).start();
        while (val == -1)
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return val;
    }

    @Override
    public void onMaxValueChanged(int max) {
        this.max = max;
    }

    @Override
    public void onMinValueChanged(int min) {
        this.min = min;
    }

    public String[] getStringValues()
    {
        return null;
    }
}
