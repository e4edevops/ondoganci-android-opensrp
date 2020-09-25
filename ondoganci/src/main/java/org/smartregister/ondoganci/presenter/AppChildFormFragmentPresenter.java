package org.smartregister.ondoganci.presenter;

import android.view.View;
import android.widget.AdapterView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.child.presenter.ChildFormFragmentPresenter;
import org.smartregister.ondoganci.R;
import org.smartregister.ondoganci.activity.ChildFormActivity;
import org.smartregister.ondoganci.fragment.AppChildFormFragment;
import org.smartregister.ondoganci.util.AppConstants;

import java.util.Objects;

import timber.log.Timber;

public class AppChildFormFragmentPresenter extends ChildFormFragmentPresenter {

    private AppChildFormFragment formFragment;

    public AppChildFormFragmentPresenter(JsonFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
        this.formFragment = (AppChildFormFragment) formFragment;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        String key = (String) parent.getTag(R.id.key);
        try {

            if (key.equals(AppConstants.REACTION_VACCINE)) {
                MaterialSpinner spinnerReactionVaccine = (MaterialSpinner) ((ChildFormActivity) Objects.requireNonNull(formFragment.getActivity())).getFormDataView(JsonFormConstants.STEP1 + ":" + AppConstants.REACTION_VACCINE);
                int selectedItemPos = spinnerReactionVaccine.getSelectedItemPosition();
                AppChildFormFragment.OnReactionVaccineSelected onReactionVaccineSelected = formFragment.getOnReactionVaccineSelected();
                if (selectedItemPos > 0) {
                    selectedItemPos = selectedItemPos - 1;
                    String reactionVaccine = (String) spinnerReactionVaccine.getAdapter().getItem(selectedItemPos);
                    if (StringUtils.isNotBlank(reactionVaccine) && (reactionVaccine.length() > 10)) {
                        String reactionVaccineDate = reactionVaccine.substring(reactionVaccine.length() - 11, reactionVaccine.length() - 1);
                        if (onReactionVaccineSelected != null) {
                            onReactionVaccineSelected.updateDatePicker(reactionVaccineDate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

    }

}
