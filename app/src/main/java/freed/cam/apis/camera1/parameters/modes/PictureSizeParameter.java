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

package freed.cam.apis.camera1.parameters.modes;

import android.hardware.Camera.Parameters;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.camera1.parameters.ParametersHandler;

/**
 * Created by troop on 18.08.2014.
 */
public class PictureSizeParameter extends BaseModeParameter
{
    final String TAG = PictureSizeParameter.class.getSimpleName();
    public PictureSizeParameter(Parameters  parameters, CameraWrapperInterface parameterChanged) {
        super(parameters, parameterChanged, "", "");
        this.cameraUiWrapper = parameterChanged;
        isSupported = true;
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCam)
    {
        parameters.set("picture-size" , valueToSet);

        ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).SetParametersToCamera(parameters);
        onValueHasChanged(valueToSet);
    }

    @Override
    public String GetValue() {
        return cameraUiWrapper.GetAppSettingsManager().pictureSize.get();
    }

    @Override
    public String[] GetValues() {
        return cameraUiWrapper.GetAppSettingsManager().pictureSize.getValues();
    }
}
