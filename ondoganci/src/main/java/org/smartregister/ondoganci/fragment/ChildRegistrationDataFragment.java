package org.smartregister.ondoganci.fragment;

import android.os.Bundle;

import org.smartregister.child.fragment.BaseChildRegistrationDataFragment;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.util.AppConstants;
import org.smartregister.util.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ndegwamartin on 2019-05-30.
 */
public class ChildRegistrationDataFragment extends BaseChildRegistrationDataFragment {

    @Override
    public String getRegistrationForm() {
        return AppConstants.JSON_FORM.CHILD_ENROLLMENT;
    }

}
