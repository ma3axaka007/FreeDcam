/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.viewer.dngconvert;

import android.R.layout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ortiz.touch.TouchImageView;
import com.troop.freedcam.R;
import com.troop.freedcam.R.array;
import com.troop.freedcam.R.id;
import com.troop.freedcam.R.string;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.parameters.modes.MatrixChooserParameter;
import freed.dng.DngProfile;
import freed.dng.DngSupportedDevices;
import freed.jni.RawToDng;
import freed.jni.RawUtils;
import freed.utils.AppSettingsManager;
import freed.utils.DeviceUtils;
import freed.utils.StringUtils;
import freed.utils.StringUtils.FileEnding;

/**
 * Created by troop on 22.12.2015.
 */
public class DngConvertingFragment extends Fragment
{
    final String TAG = DngConvertingFragment.class.getSimpleName();
    private View view;
    private EditText editTextwidth;
    private EditText editTextheight;
    private EditText editTextblacklvl;
    private Spinner spinnerMatrixProfile;
    private Spinner spinnerColorPattern;
    private Spinner spinnerrawFormat;
    private Button buttonconvertToDng;
    private String[] filesToConvert;
    private DngProfile dngprofile;
    private Handler handler;
    private Button closeButton;
    private CheckBox fakeGPS;
    private AppSettingsManager appSettingsManager;
    private MatrixChooserParameter matrixChooserParameter;
    private TouchImageView imageView;
    private final double Altitude =561.0;
    private final double Latitude = 48.2503155;
    private final double Longitude = 11.65918818;
    private final String Provider = "gps";
    private final long gpsTime = 1477324747000l;

    public static final String EXTRA_FILESTOCONVERT = "extra_files_to_convert";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        appSettingsManager = new AppSettingsManager(PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()),getResources());
        if (appSettingsManager.getDevice() == null)
            appSettingsManager.SetDevice(new DeviceUtils().getDevice(getResources()));
        handler = new Handler();
        view = inflater.inflate(R.layout.dngconvertingfragment, container, false);
        editTextwidth = (EditText) view.findViewById(id.editText_width);
        editTextheight = (EditText) view.findViewById(id.editText_height);
        editTextblacklvl = (EditText) view.findViewById(id.editText_blacklevel);
        spinnerMatrixProfile = (Spinner) view.findViewById(id.spinner_MatrixProfile);
        matrixChooserParameter = new MatrixChooserParameter(appSettingsManager.getMatrixesMap());
        String[] items = matrixChooserParameter.GetValues();
        ArrayAdapter<String> matrixadapter = new ArrayAdapter<>(getContext(), layout.simple_spinner_item, items);
        //ArrayAdapter<CharSequence> matrixadapter = ArrayAdapter.createFromResource(getContext(),R.array.matrixes, android.R.layout.simple_spinner_item);
        matrixadapter.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        spinnerMatrixProfile.setAdapter(matrixadapter);


        buttonconvertToDng = (Button) view.findViewById(id.button_convertDng);
        buttonconvertToDng.setOnClickListener(convertToDngClick);

        spinnerColorPattern =(Spinner) view.findViewById(id.spinner_ColorPattern);
        ArrayAdapter<CharSequence> coloradapter = ArrayAdapter.createFromResource(getContext(),
                array.color_pattern, layout.simple_spinner_item);
        coloradapter.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        spinnerColorPattern.setAdapter(coloradapter);

        spinnerrawFormat = (Spinner) view.findViewById(id.spinner_rawFormat);
        ArrayAdapter<CharSequence> rawadapter = ArrayAdapter.createFromResource(getContext(),
                array.raw_format, layout.simple_spinner_item);
        rawadapter.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        spinnerrawFormat.setAdapter(rawadapter);
        closeButton = (Button) view.findViewById(id.button_goback_from_conv);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent returnIntent = new Intent();
                getActivity().setResult(Activity.RESULT_CANCELED, returnIntent);
                getActivity().finish();
            }
        });
        imageView = (TouchImageView)view.findViewById(id.dngconvert_imageview);
        fakeGPS = (CheckBox)view.findViewById(id.checkBox_fakeGPS);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        filesToConvert = getActivity().getIntent().getStringArrayExtra(EXTRA_FILESTOCONVERT);
        if (filesToConvert != null && filesToConvert.length > 0) {
            dngprofile = new DngSupportedDevices().getProfile(appSettingsManager.getDevice(), (int) new File(filesToConvert[0]).length(),matrixChooserParameter);
            if (dngprofile == null) {
                dngprofile = new DngSupportedDevices().GetEmptyProfile(matrixChooserParameter);
                Toast.makeText(getContext(), string.unknown_raw_add_manual_stuff, Toast.LENGTH_LONG).show();
            }
            editTextwidth.setText(dngprofile.widht + "");
            editTextheight.setText(dngprofile.height + "");
            editTextblacklvl.setText(dngprofile.blacklevel + "");

            if (dngprofile.bayerPattern.equals(DngProfile.BGGR))
                spinnerColorPattern.setSelection(0);
            else if (dngprofile.bayerPattern.equals(DngProfile.RGGB))
                spinnerColorPattern.setSelection(1);
            else if (dngprofile.bayerPattern.equals(DngProfile.GRBG))
                spinnerColorPattern.setSelection(2);
            else if (dngprofile.bayerPattern.equals(DngProfile.GBRG))
                spinnerColorPattern.setSelection(3);
            else if (dngprofile.bayerPattern.equals(DngProfile.RGBW))
                spinnerColorPattern.setSelection(4);

            spinnerMatrixProfile.setSelection(0);
            spinnerrawFormat.setSelection(dngprofile.rawType);
            if (dngprofile != null){
                spinnerMatrixProfile.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                dngprofile.matrixes = matrixChooserParameter.GetCustomMatrixNotOverWritten(MatrixChooserParameter.NEXUS6);
                                break;
                            case 1:
                                dngprofile.matrixes = matrixChooserParameter.GetCustomMatrixNotOverWritten(MatrixChooserParameter.G4);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spinnerColorPattern.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                dngprofile.bayerPattern = DngProfile.BGGR;
                                break;
                            case 1:
                                dngprofile.bayerPattern = DngProfile.RGGB;
                                break;
                            case 2:
                                dngprofile.bayerPattern = DngProfile.GRBG;
                                break;
                            case 3:
                                dngprofile.bayerPattern = DngProfile.GBRG;
                                break;
                            case 4:
                                dngprofile.bayerPattern = DngProfile.RGBW;
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                spinnerrawFormat.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        dngprofile.rawType = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }
        }
        else {
            Toast.makeText(getContext(), string.no_sel_raw, Toast.LENGTH_LONG).show();
        }
    }

    private final OnClickListener convertToDngClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (filesToConvert == null || filesToConvert.length == 0) {
                Toast.makeText(getContext(), string.no_sel_raw, Toast.LENGTH_LONG).show();
            }
            else {
                dngprofile.widht = Integer.parseInt(editTextwidth.getText().toString());
                dngprofile.height = Integer.parseInt(editTextheight.getText().toString());
                dngprofile.blacklevel = Integer.parseInt(editTextblacklvl.getText().toString());
                final ProgressDialog pr = ProgressDialog.show(getContext(), "Converting DNG", "");

                pr.setMax(filesToConvert.length);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int t = 0;
                        for (String s : filesToConvert) {
                            convertRawToDng(new File(s));
                            t++;
                            final int i = t;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    pr.setProgress(i);
                                }
                            });
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                pr.dismiss();
                            }
                        });
                    }
                }).start();

            }
        }
    };

    private void convertRawToDng(File file)
    {
        byte[] data = null;
        try {
            data = RawToDng.readFile(file);
            Log.d("Main", "Filesize: " + data.length + " File:" + file.getAbsolutePath());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String out =null;
        if (file.getName().endsWith(FileEnding.RAW))
            out = file.getAbsolutePath().replace(FileEnding.RAW, FileEnding.DNG);
        if (file.getName().endsWith(FileEnding.BAYER))
            out = file.getAbsolutePath().replace(FileEnding.BAYER, FileEnding.DNG);
        RawToDng dng = RawToDng.GetInstance();
        String intsd = StringUtils.GetInternalSDCARD();
        if (VERSION.SDK_INT <= VERSION_CODES.LOLLIPOP
                || file.getAbsolutePath().contains(intsd))
            dng.setBayerData(data, out);
        else
        {
            DocumentFile df = ((ActivityInterface)getActivity()).getFreeDcamDocumentFolder();
            DocumentFile wr = df.createFile("image/dng", file.getName().replace(FileEnding.JPG, FileEnding.DNG));
            ParcelFileDescriptor pfd = null;
            try {

                pfd = getContext().getContentResolver().openFileDescriptor(wr.getUri(), "rw");
            } catch (FileNotFoundException | IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            if (pfd != null) {
                dng.SetBayerDataFD(data, pfd, file.getName());
                try {
                    pfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pfd = null;
            }
        }
        dng.setExifData(100, 0, 0, 0, 0, "", "0", 0);
        if (fakeGPS.isChecked())
            dng.SetGpsData(Altitude, Latitude, Longitude,Provider, gpsTime);
        dng.WriteDngWithProfile(dngprofile);
        data = null;
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        getActivity().sendBroadcast(intent);
        if (filesToConvert.length == 1)
        {

            final Bitmap map = new RawUtils().UnPackRAW(out);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(map);
                }
            });
        }
    }


}
