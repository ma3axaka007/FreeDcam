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

package freed.cam.apis.camera1.modules;

import android.location.Location;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoSource;
import android.os.Handler;

import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.VideoMediaProfile;
import freed.cam.apis.basecamera.modules.VideoMediaProfile.VideoMode;
import freed.cam.apis.camera1.CameraHolder;
import freed.cam.apis.camera1.parameters.ParametersHandler;
import freed.cam.apis.camera1.parameters.modes.VideoProfilesParameter;
import freed.utils.AppSettingsManager;
import freed.utils.DeviceUtils.Devices;


/**
 * Created by troop on 16.08.2014.
 */
public class VideoModule extends AbstractVideoModule
{
    private final String TAG = VideoModule.class.getSimpleName();
    private VideoMediaProfile currentProfile;
    private int MaXFPS = 30;

    public VideoModule(CameraWrapperInterface cameraUiWrapper, Handler mBackgroundHandler) {
        super(cameraUiWrapper,mBackgroundHandler);
    }



    protected MediaRecorder initRecorder()
    {
        recorder = new MediaRecorder();
        recorder.reset();
        recorder.setCamera(((CameraHolder) cameraUiWrapper.GetCameraHolder()).GetCamera());
        if (cameraUiWrapper.GetAppSettingsManager().getApiString(AppSettingsManager.SETTING_LOCATION).equals(KEYS.ON)){
            Location location = cameraUiWrapper.getActivityInterface().getLocationHandler().getCurrentLocation();
            if (location != null)
                recorder.setLocation((float) location.getLatitude(), (float) location.getLongitude());
        }

        recorder.setVideoSource(VideoSource.CAMERA);

        switch (currentProfile.Mode)
        {
            case Normal:
            case Highspeed:
                if(currentProfile.isAudioActive)
                    recorder.setAudioSource(AudioSource.CAMCORDER);
                break;
            case Timelapse:
                break;
        }
        recorder.setOutputFormat(OutputFormat.MPEG_4);
        recorder.setVideoFrameRate(MaXFPS);
        recorder.setVideoSize(currentProfile.videoFrameWidth, currentProfile.videoFrameHeight);
        recorder.setVideoEncodingBitRate(currentProfile.videoBitRate);
        try {
            recorder.setVideoEncoder(currentProfile.videoCodec);
        }
        catch (IllegalArgumentException ex)
        {
            recorder.reset();
            cameraUiWrapper.GetCameraHolder().SendUIMessage("VideoCodec not Supported");
        }

        switch (currentProfile.Mode)
        {
            case Normal:
            case Highspeed:
                if(currentProfile.isAudioActive)
                {
                    recorder.setAudioSamplingRate(currentProfile.audioSampleRate);
                    recorder.setAudioEncodingBitRate(currentProfile.audioBitRate);
                    recorder.setAudioChannels(currentProfile.audioChannels);
                    try {
                        recorder.setAudioEncoder(currentProfile.audioCodec);
                    }
                    catch (IllegalArgumentException ex)
                    {
                        recorder.reset();
                        cameraUiWrapper.GetCameraHolder().SendUIMessage("AudioCodec not Supported");
                    }
                }
                break;
            case Timelapse:
                float frame = 60;
                if(!appSettingsManager.getApiString(AppSettingsManager.TIMELAPSEFRAME).equals(""))
                    frame = Float.parseFloat(appSettingsManager.getApiString(AppSettingsManager.TIMELAPSEFRAME).replace(",", "."));
                else
                    appSettingsManager.setApiString(AppSettingsManager.TIMELAPSEFRAME, ""+frame);
                recorder.setCaptureRate(frame);
                break;
        }
        return recorder;
    }



    @Override
    public void InitModule()
    {
        super.InitModule();
        if (appSettingsManager.videoHDR.isSupported())
            if(appSettingsManager.videoHDR.equals("on"))
                cameraUiWrapper.GetParameterHandler().VideoHDR.SetValue("on", true);
        loadProfileSpecificParameters();
    }

    @Override
    public void DestroyModule() {
        if (isWorking)
            stopRecording();
        if (cameraUiWrapper.GetParameterHandler().VideoHDR != null && cameraUiWrapper.GetParameterHandler().VideoHDR.IsSupported())
            cameraUiWrapper.GetParameterHandler().VideoHDR.SetValue("off", true);
        super.DestroyModule();
    }

    private void loadProfileSpecificParameters()
    {

        VideoProfilesParameter videoProfilesG3Parameter = (VideoProfilesParameter) cameraUiWrapper.GetParameterHandler().VideoProfiles;
        currentProfile = videoProfilesG3Parameter.GetCameraProfile(appSettingsManager.getApiString(AppSettingsManager.VIDEOPROFILE));
        if (currentProfile.Mode == VideoMode.Highspeed)
        {
            if(appSettingsManager.getDevice() == Devices.Htc_M8 ||appSettingsManager.getDevice() == Devices.Htc_M9||appSettingsManager.getDevice() == Devices.HTC_OneA9||appSettingsManager.getDevice() == Devices.HTC_OneE8 ) {
                loadHtcHighspeed();
            }
            else if (((CameraHolder)cameraUiWrapper.GetCameraHolder()).DeviceFrameWork == CameraHolder.Frameworks.MTK)
            {
                loadMtkHighspeed();
            }
            else
            {
                loadDefaultHighspeed();
            }
        }
        else
        {
            if (currentProfile.videoFrameRate <=24) {
                if (((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-frame-rate-values") != null) {
                    for (String fpz : ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-frame-rate-values").split(",")) {
                        if (Integer.parseInt(fpz) == currentProfile.videoFrameRate) {
                            cameraUiWrapper.GetParameterHandler().PreviewFPS.SetValue(currentProfile.videoFrameRate + "", false);
                        }

                    }

                }
            }


            if (((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-fps-range") != null) {

                if (currentProfile.videoFrameRate <= 30) {
                    ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().set("preview-fps-range", String.valueOf(currentProfile.videoFrameRate * 1000) + "," + String.valueOf(currentProfile.videoFrameRate * 1000));
                    ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).SetParametersToCamera(((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters());
                }
            }
            if (currentProfile.ProfileName.contains(VideoProfilesParameter._2160p) ||currentProfile.ProfileName.contains(VideoProfilesParameter._2160pDCI))
            {

                if (cameraUiWrapper.GetParameterHandler().DigitalImageStabilization != null && cameraUiWrapper.GetParameterHandler().DigitalImageStabilization.IsSupported())
                    cameraUiWrapper.GetParameterHandler().DigitalImageStabilization.SetValue("disable", true);
                if (cameraUiWrapper.GetParameterHandler().VideoStabilization != null && cameraUiWrapper.GetParameterHandler().VideoStabilization.IsSupported())
                    cameraUiWrapper.GetParameterHandler().VideoStabilization.SetValue("false", true);

                if (appSettingsManager.IsCamera2FullSupported().equals(KEYS.FALSE))
                    cameraUiWrapper.GetParameterHandler().PreviewFormat.SetValue("nv12-venus",true);
            }
            else
                cameraUiWrapper.GetParameterHandler().PreviewFormat.SetValue("yuv420sp", true);

            if (cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo != null && cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.IsSupported())
            {
                cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.SetValue("off", true);
            }
        }


        String size = currentProfile.videoFrameWidth + "x" + currentProfile.videoFrameHeight;
        cameraUiWrapper.StopPreview();
        if (appSettingsManager.previewSize.isSupported())
            cameraUiWrapper.GetParameterHandler().PreviewSize.SetValue(size,false);
        //video size applies the parameters to the camera
        if (appSettingsManager.videoSize.isSupported())
            cameraUiWrapper.GetParameterHandler().VideoSize.SetValue(size, true);

        cameraUiWrapper.StartPreview();
    }

    private void loadDefaultHighspeed() {
        //turn off all blocking/postprocessing parameters wich avoid highframes
        if (appSettingsManager.memoryColorEnhancement.isSupported())
            cameraUiWrapper.GetParameterHandler().MemoryColorEnhancement.SetValue("disable", false);
        if (appSettingsManager.digitalImageStabilisationMode.isSupported())
            cameraUiWrapper.GetParameterHandler().DigitalImageStabilization.SetValue("disable", false);
        if (cameraUiWrapper.GetParameterHandler().VideoStabilization != null && cameraUiWrapper.GetParameterHandler().VideoStabilization.IsSupported())
            cameraUiWrapper.GetParameterHandler().VideoStabilization.SetValue("false", false);
        if (cameraUiWrapper.GetParameterHandler().Denoise != null && cameraUiWrapper.GetParameterHandler().Denoise.IsSupported())
            cameraUiWrapper.GetParameterHandler().Denoise.SetValue("denoise-off", false);
        //full camera2 devices dont use hardware preview format so set it only for legacy devices
        if (appSettingsManager.IsCamera2FullSupported().equals(KEYS.FALSE))
            cameraUiWrapper.GetParameterHandler().PreviewFormat.SetValue("nv12-venus", false);

        if (((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-frame-rate-values") != null) {
            for (String fpz : ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-frame-rate-values").split(",")) {
                if (Integer.parseInt(fpz) == currentProfile.videoFrameRate) {
                    cameraUiWrapper.GetParameterHandler().PreviewFPS.SetValue(currentProfile.videoFrameRate + "", false);
                }

            }

        }


        if (((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-fps-range") != null) {

            if (currentProfile.videoFrameRate < 30) {
                ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().set("preview-fps-range", String.valueOf(currentProfile.videoFrameRate * 1000) + "," + String.valueOf(currentProfile.videoFrameRate * 1000));
                ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).SetParametersToCamera(((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters());
            }
        }
        //set the profile defined frames per seconds
        if (cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo != null && cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.IsSupported()) {
            cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.SetValue(currentProfile.videoFrameRate + "", false);
        }

        if (cameraUiWrapper.GetParameterHandler().HTCVideoModeHSR != null && cameraUiWrapper.GetParameterHandler().HTCVideoModeHSR.IsSupported()) {
            cameraUiWrapper.GetParameterHandler().HTCVideoModeHSR.SetValue(currentProfile.videoFrameRate + "", false);
        }
        if (((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-fps-range") != null) {

            if (((ParametersHandler) cameraUiWrapper.GetParameterHandler()).getParameters().get("preview-fps-range").split(",")[1].equals(String.valueOf(currentProfile.videoFrameRate * 1000))) {
                MaXFPS = currentProfile.videoFrameRate;
            }
        }
    }

    private void loadMtkHighspeed() {
        if(cameraUiWrapper.GetParameterHandler().PreviewFPS.GetValues().toString().contains(currentProfile.videoFrameRate+""))
        {
            cameraUiWrapper.GetParameterHandler().PreviewFPS.SetValue(currentProfile.videoFrameRate+"",false);

            if (cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo != null && cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.IsSupported()) {
                cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.SetValue(currentProfile.videoFrameRate + "", false);
                cameraUiWrapper.GetParameterHandler().PreviewFPS.SetValue(currentProfile.videoFrameRate+"",true);
            }

        }
    }

    private void loadHtcHighspeed() {
        if (currentProfile.ProfileName.equals("1080pHFR"))
        {
            cameraUiWrapper.GetParameterHandler().HTCVideoMode.SetValue("2",true);
            cameraUiWrapper.GetParameterHandler().HTCVideoModeHSR.SetValue("60",true);
            cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.SetValue("60", true);
        }
        else if (currentProfile.ProfileName.equals("720pHFR"))
        {
            if (currentProfile.videoFrameRate < 120 && currentProfile.videoFrameRate >30 )
            {
                cameraUiWrapper.GetParameterHandler().HTCVideoMode.SetValue("2",false);
                cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.SetValue("off", false);
            }
            else {
                cameraUiWrapper.GetParameterHandler().HTCVideoMode.SetValue("1",true);
                cameraUiWrapper.GetParameterHandler().VideoHighFramerateVideo.SetValue("120", true);}
        }
    }


}