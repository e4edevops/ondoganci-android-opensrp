package org.smartregister.immunization.fragment.mock;

import android.os.Bundle;
import androidx.core.app.FragmentActivity;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.smartregister.domain.Alert;
import org.smartregister.domain.Photo;
import org.smartregister.immunization.R;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceRecordTest;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.ServiceWrapperTest;
import org.smartregister.immunization.fragment.UndoServiceDialogFragment;
import org.smartregister.immunization.listener.ServiceActionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by real on 05/11/17.
 */

public class UndoServiceDialogFragmentTestActivity extends FragmentActivity implements ServiceActionListener {

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme); //we need this here
        super.onCreate(bundle);
        LinearLayout linearLayout;
        linearLayout = new LinearLayout(this);
        setContentView(linearLayout);
        startFragment();
    }

    public void startFragment() {
        DateTime datetime = new DateTime();
        Alert alert = Mockito.mock(Alert.class);

        List<ServiceRecord> issuedServices = new ArrayList<ServiceRecord>();
        ServiceRecord serviceRecord = new ServiceRecord(0l, ServiceRecordTest.BASEENTITYID,
                ServiceRecordTest.PROGRAMCLIENTID, 0l, ServiceRecordTest.VALUE, new Date(), ServiceRecordTest.ANMID,
                ServiceRecordTest.LOCATIONID, ServiceRecordTest.SYNCED, ServiceRecordTest.EVENTID,
                ServiceRecordTest.FORMSUBMISSIONID, 0l, new Date());

        issuedServices.add(serviceRecord);
        ServiceWrapper tag = new ServiceWrapper();
        //        tag.setId(ServiceWrapperTest.ID);
        tag.setDbKey(0l);
        tag.setStatus(ServiceWrapperTest.STATUS);
        tag.setVaccineDate(datetime);
        tag.setAlert(alert);
        tag.setDefaultName(ServiceWrapperTest.DEFAULTNAME);
        tag.setPreviousVaccine(ServiceWrapperTest.ID);
        tag.setColor(ServiceWrapperTest.COLOR);
        tag.setDob(datetime);
        ServiceType serviceType = new ServiceType();
        serviceType.setUnits("units");
        serviceType.setType("type");
        serviceType.setId(0l);
        tag.setServiceType(serviceType);
        serviceType.setName(ServiceWrapperTest.NAME);
        tag.setValue(ServiceWrapperTest.VALUE);
        tag.setPatientName(ServiceWrapperTest.PATIENTNAME);
        tag.setUpdatedVaccineDate(datetime, true);
        tag.setPatientNumber(ServiceWrapperTest.NUMBER);
        Photo photo = Mockito.mock(Photo.class);
        tag.setPhoto(photo);
        tag.setGender(ServiceWrapperTest.GENDER);
        tag.setSynced(true);
        UndoServiceDialogFragment fragment = UndoServiceDialogFragment.newInstance(tag);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onGiveToday(ServiceWrapper tag, View view) {

    }

    @Override
    public void onGiveEarlier(ServiceWrapper tag, View view) {

    }

    @Override
    public void onUndoService(ServiceWrapper tag, View view) {

    }
}
