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

package freed.cam.apis.basecamera.parameters;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.FocusRect;
import freed.cam.apis.basecamera.parameters.manual.ManualParameterInterface;
import freed.cam.apis.basecamera.parameters.modes.GuideList;
import freed.cam.apis.basecamera.parameters.modes.Horizont;
import freed.cam.apis.basecamera.parameters.modes.IntervalDurationParameter;
import freed.cam.apis.basecamera.parameters.modes.IntervalShutterSleepParameter;
import freed.cam.apis.basecamera.parameters.modes.LocationParameter;
import freed.cam.apis.basecamera.parameters.modes.ModeParameterInterface;
import freed.cam.apis.basecamera.parameters.modes.NightOverlayParameter;
import freed.cam.apis.basecamera.parameters.modes.SDModeParameter;
import freed.cam.apis.camera1.parameters.device.I_Device;
import freed.utils.AppSettingsManager;

/**
 * Created by troop on 09.12.2014.
 */

/**
 * This class holds all availible parameters supported by the camera
 * Parameter can be null when unsupported.
 * Bevor accessing it, check if is not null or IsSupported
 */
public abstract class AbstractParameterHandler
{
    final String TAG = AbstractParameterHandler.class.getSimpleName();
    /**
     * Holds the UI/Main Thread
     */
    protected Handler uiHandler;

    protected AppSettingsManager appSettingsManager;

    protected CameraWrapperInterface cameraUiWrapper;

    public ManualParameterInterface ManualBrightness;
    public ManualParameterInterface ManualSharpness;
    public ManualParameterInterface ManualContrast;
    public ManualParameterInterface ManualSaturation;
    public ManualParameterInterface ManualExposure;
    public ManualParameterInterface ManualConvergence;
    public ManualParameterInterface ManualFocus;
    public ManualParameterInterface ManualShutter;
    public ManualParameterInterface ManualFNumber;
    public ManualParameterInterface Burst;
    public ManualParameterInterface CCT;
    public ManualParameterInterface FX;
    public ManualParameterInterface ManualIso;
    public ManualParameterInterface Zoom;
    public ManualParameterInterface ProgramShift;
    public ManualParameterInterface PreviewZoom;


    public ModeParameterInterface ColorMode;
    public ModeParameterInterface ExposureMode;
    public ModeParameterInterface AE_PriorityMode;
    public ModeParameterInterface FlashMode;
    public ModeParameterInterface IsoMode;
    public ModeParameterInterface AntiBandingMode;
    public ModeParameterInterface WhiteBalanceMode;
    public ModeParameterInterface PictureSize;
    public ModeParameterInterface PictureFormat;
    public ModeParameterInterface HDRMode;
    public ModeParameterInterface JpegQuality;
    //defcomg was here
    public ModeParameterInterface GuideList;
    //done
    public ModeParameterInterface ImagePostProcessing;
    public ModeParameterInterface PreviewSize;
    public ModeParameterInterface PreviewFPS;
    public ModeParameterInterface PreviewFormat;
    public ModeParameterInterface SceneMode;
    public ModeParameterInterface FocusMode;
    public ModeParameterInterface RedEye;
    public ModeParameterInterface LensShade;
    public ModeParameterInterface ZSL;
    public ModeParameterInterface SceneDetect;
    public ModeParameterInterface Denoise;
    public ModeParameterInterface DigitalImageStabilization;
    public ModeParameterInterface VideoStabilization;
    public ModeParameterInterface MemoryColorEnhancement;
    public ModeParameterInterface NightMode;
    public ModeParameterInterface NonZslManualMode;
    public ModeParameterInterface AE_Bracket;
    public ModeParameterInterface Histogram;
    public ModeParameterInterface ExposureLock;
    public ModeParameterInterface CDS_Mode;

    public ModeParameterInterface HTCVideoMode;
    public ModeParameterInterface HTCVideoModeHSR;
    public ModeParameterInterface VideoProfiles;
    public ModeParameterInterface VideoSize;
    public ModeParameterInterface VideoHDR;
    public ModeParameterInterface VideoHighFramerateVideo;
    public ModeParameterInterface LensFilter;
    public ModeParameterInterface Horizont;

    //yet only seen on m9
    public ModeParameterInterface TnrMode;

    //SonyApi
    public ModeParameterInterface ContShootMode;
    public ModeParameterInterface ContShootModeSpeed;
    public ModeParameterInterface ObjectTracking;
    public ModeParameterInterface PostViewSize;
    public ModeParameterInterface Focuspeak;
    public ModeParameterInterface Module;
    public ModeParameterInterface ZoomSetting;
    public ModeParameterInterface NightOverlay;
    private boolean isDngActive;
    public boolean IsDngActive(){ return isDngActive; }
    public void SetDngActive(boolean active) {
        isDngActive = active;}



    //camera2 modes
    public ModeParameterInterface EdgeMode;
    public ModeParameterInterface ColorCorrectionMode;
    public ModeParameterInterface HotPixelMode;
    public ModeParameterInterface ToneMapMode;
    public ModeParameterInterface ControlMode;

    public ModeParameterInterface oismode;

    public ModeParameterInterface SdSaveLocation;

    public ModeParameterInterface locationParameter;

    public boolean IntervalCapture;
    public boolean IntervalCaptureFocusSet;

    public ModeParameterInterface IntervalDuration;
    public ModeParameterInterface IntervalShutterSleep;

    public ModeParameterInterface opcode;
    public ModeParameterInterface bayerformat;
    public ModeParameterInterface matrixChooser;
    public ModeParameterInterface scalePreview;

    public AbstractParameterHandler(CameraWrapperInterface cameraUiWrapper) {
        this.cameraUiWrapper = cameraUiWrapper;
        uiHandler = new Handler(Looper.getMainLooper());
        this.appSettingsManager = cameraUiWrapper.GetAppSettingsManager();

        GuideList = new GuideList();
        locationParameter = new LocationParameter(cameraUiWrapper);
        IntervalDuration = new IntervalDurationParameter(cameraUiWrapper);
        IntervalShutterSleep = new IntervalShutterSleepParameter(cameraUiWrapper);
        Horizont = new Horizont();
        SdSaveLocation = new SDModeParameter(appSettingsManager);
        NightOverlay = new NightOverlayParameter(cameraUiWrapper);

    }

    public abstract I_Device getDevice();

    public abstract void SetFocusAREA(FocusRect focusAreas);

    public abstract void SetMeterAREA(FocusRect meteringAreas);

    public abstract void SetPictureOrientation(int or);

    public abstract float[] getFocusDistances();

    public void SetAppSettingsToParameters()
    {
        setMode(locationParameter, AppSettingsManager.SETTING_LOCATION);
        setAppSettingsToCamera(ColorMode,appSettingsManager.colorMode);
        //setMode(ColorMode, AppSettingsManager.COLORMODE);
        setAppSettingsToCamera(ExposureMode,appSettingsManager.exposureMode);
        //setMode(ExposureMode, AppSettingsManager.EXPOSUREMODE);
        setAppSettingsToCamera(FlashMode,appSettingsManager.flashMode);
        //setMode(FlashMode, AppSettingsManager.FLASHMODE);
        setAppSettingsToCamera(IsoMode,appSettingsManager.isoMode);
        //setMode(IsoMode, AppSettingsManager.ISOMODE);

        setAppSettingsToCamera(AntiBandingMode,appSettingsManager.antiBandingMode);
        //setMode(AntiBandingMode, AppSettingsManager.ANTIBANDINGMODE);
        setAppSettingsToCamera(WhiteBalanceMode,appSettingsManager.whiteBalanceMode);
        //setMode(WhiteBalanceMode, AppSettingsManager.WHITEBALANCEMODE);
        setAppSettingsToCamera(PictureSize,appSettingsManager.pictureSize);
        //setMode(PictureSize, AppSettingsManager.PICTURESIZE);
        setAppSettingsToCamera(PictureFormat,appSettingsManager.pictureFormat);
        //setMode(PictureFormat, AppSettingsManager.PICTUREFORMAT);
        setAppSettingsToCamera(bayerformat,appSettingsManager.rawPictureFormat);
        //setMode(bayerformat,AppSettingsManager.BAYERFORMAT);
        setAppSettingsToCamera(oismode,appSettingsManager.opticalImageStabilisation);
        //setMode(oismode, AppSettingsManager.SETTING_OIS);

        //setMode(JpegQuality, AppSettingsManager.JPEGQUALITY);
        setAppSettingsToCamera(JpegQuality,appSettingsManager.jpegQuality);
        setMode(GuideList, AppSettingsManager.GUIDE);
        setAppSettingsToCamera(ImagePostProcessing,appSettingsManager.imagePostProcessing);
        //setMode(ImagePostProcessing, AppSettingsManager.IMAGEPOSTPROCESSINGMODE);
        setAppSettingsToCamera(SceneMode,appSettingsManager.sceneMode);
        //setMode(SceneMode, AppSettingsManager.SCENEMODE);
        setAppSettingsToCamera(FocusMode,appSettingsManager.focusMode);
        //setMode(FocusMode, AppSettingsManager.FOCUSMODE);
        setAppSettingsToCamera(RedEye,appSettingsManager.redEyeMode);
        //setMode(RedEye,AppSettingsManager.REDEYEMODE);
        setAppSettingsToCamera(LensShade,appSettingsManager.lenshade);
        //setMode(LensShade,AppSettingsManager.LENSHADEMODE);
        setAppSettingsToCamera(ZSL,appSettingsManager.zeroshutterlag);
        //setMode(ZSL, AppSettingsManager.ZSLMODE);
        //setMode(SceneDetect, AppSettingsManager.SCENEDETECTMODE);
        setAppSettingsToCamera(SceneDetect,appSettingsManager.sceneDetectMode);
        setAppSettingsToCamera(Denoise,appSettingsManager.denoiseMode);
        //setMode(Denoise, AppSettingsManager.DENOISETMODE);
        //setMode(DigitalImageStabilization, AppSettingsManager.DIGITALIMAGESTABMODE);
        setAppSettingsToCamera(DigitalImageStabilization,appSettingsManager.digitalImageStabilisationMode);
        //setMode(MemoryColorEnhancement, AppSettingsManager.MEMORYCOLORENHANCEMENTMODE);
        setAppSettingsToCamera(MemoryColorEnhancement,appSettingsManager.memoryColorEnhancement);
        setMode(NightMode, AppSettingsManager.NIGHTMODE);
        setMode(NonZslManualMode, AppSettingsManager.NONZSLMANUALMODE);

        //setMode(Histogram, AppSettingsManager.HISTOGRAM);
        setMode(VideoProfiles, AppSettingsManager.VIDEOPROFILE);
        setAppSettingsToCamera(VideoHDR, appSettingsManager.videoHDR);
        setAppSettingsToCamera(VideoSize, appSettingsManager.videoSize);
        setMode(VideoStabilization,AppSettingsManager.VIDEOSTABILIZATION);
        setMode(VideoHighFramerateVideo,AppSettingsManager.HIGHFRAMERATEVIDEO);
        //setMode(WhiteBalanceMode,AppSettingsManager.WHITEBALANCEMODE);
        setAppSettingsToCamera(WhiteBalanceMode,appSettingsManager.whiteBalanceMode);
        setMode(ColorCorrectionMode, AppSettingsManager.SETTING_COLORCORRECTION);
        setAppSettingsToCamera(EdgeMode, appSettingsManager.edgeMode);
        setAppSettingsToCamera(EdgeMode,appSettingsManager.edgeMode);
        setAppSettingsToCamera(HotPixelMode, appSettingsManager.hotpixelMode);
        setMode(ToneMapMode, AppSettingsManager.SETTING_TONEMAP);
        setAppSettingsToCamera(ControlMode, appSettingsManager.controlMode);
        setMode(IntervalDuration,AppSettingsManager.SETTING_INTERVAL_DURATION);
        setMode(IntervalShutterSleep, AppSettingsManager.SETTING_INTERVAL);
        setMode(Horizont, AppSettingsManager.SETTING_HORIZONT);

        setAppSettingsToCamera(HDRMode, appSettingsManager.hdrMode);

        setMode(matrixChooser, AppSettingsManager.CUSTOMMATRIX);

        //setManualMode(ManualContrast, AppSettingsManager.MCONTRAST);
        //setManualMode(ManualConvergence,AppSettingsManager.MCONVERGENCE);
        //setManualMode(ManualExposure, AppSettingsManager.MEXPOSURE);
        //setManualMode(ManualFocus, AppSettingsManager.MF);
        //setManualMode(ManualSharpness,AppSettingsManager.MSHARPNESS);
        //setManualMode(ManualShutter, AppSettingsManager.MSHUTTERSPEED);
        //setManualMode(ManualBrightness, AppSettingsManager.MBRIGHTNESS);
        //setManualMode(ManualIso, AppSettingsManager.MISO);
        //setManualMode(ManualSaturation, AppSettingsManager.MSATURATION);
        //setManualMode(CCT,AppSettingsManager.MWB);


    }

    protected void SetParameters()
    {}

    protected void setMode(ModeParameterInterface parameter, String settings_key)
    {
        if (parameter != null && parameter.IsSupported() && settings_key != null && !settings_key.equals(""))
        {
            Log.d(TAG, parameter.getClass().getSimpleName() + " load settings: " + settings_key);
            if (appSettingsManager.getApiString(settings_key).equals("") || appSettingsManager.getApiString(settings_key) == null)
            {
                String tmp = parameter.GetValue();
                Log.d(TAG, settings_key + " is empty, set default from camera : " +tmp);
                appSettingsManager.setApiString(settings_key, tmp);
            }
            else
            {
                String tmp = appSettingsManager.getApiString(settings_key);
                Log.d(TAG, "Found AppSetting: "+settings_key+" set to: " + tmp);
                parameter.SetValue(tmp, false);
            }
        }
    }

    protected void setAppSettingsToCamera(ModeParameterInterface parameter, AppSettingsManager.SettingMode settingMode)
    {
        if (settingMode.isSupported() && parameter != null && parameter.GetValues() != null)
        {
            String toset = settingMode.get();
            Log.d(TAG,"set to :" + toset);
            if (toset.equals("") || toset.equals("none"))
                settingMode.set(parameter.GetValue());
            else
                parameter.SetValue(toset,true);
        }
    }

    protected void setManualMode(ManualParameterInterface parameter, String settings_key)
    {
        if (parameter != null && parameter.IsSupported() && settings_key != null && !settings_key.equals(""))
        {
            Log.d(TAG, parameter.getClass().getSimpleName() + " load settings: " + settings_key);
            if (appSettingsManager.getApiString(settings_key).equals("") || appSettingsManager.getApiString(settings_key).equals(null))
            {
                String tmp = parameter.GetValue()+"";
                Log.d(TAG, settings_key + " is empty, set default from camera : " +tmp);
                appSettingsManager.setApiString(settings_key, tmp);
            }
            else
            {
                try {
                    int tmp = Integer.parseInt(appSettingsManager.getApiString(settings_key));
                    Log.d(TAG, "Found AppSetting: "+settings_key+" set to: " + tmp);
                    parameter.SetValue(tmp);
                }
                catch (NumberFormatException ex)
                {
                    ex.printStackTrace();
                }

            }
        }
    }
}
