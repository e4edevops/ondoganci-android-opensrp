package org.smartregister.ondoganci.util;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.dao.ChildDao;

public class VaccineUtils {
    public static void refreshImmunizationSchedules(String caseId) {
        boolean prematureBaby = ChildDao.isPrematureBaby(caseId);
        String conditionalVaccine = null;

        if (prematureBaby) {
            conditionalVaccine = AppConstants.ConditionalVaccines.PRETERM_VACCINES;
        }

        if (!StringUtils.equalsIgnoreCase(conditionalVaccine, ImmunizationLibrary.getInstance().getCurrentConditionalVaccine())) {
            VaccineSchedule.setVaccineSchedules(null);
            ImmunizationLibrary.getInstance().setCurrentConditionalVaccine(conditionalVaccine);
            OndoganciApplication.getInstance().initOfflineSchedules();
        } else if (conditionalVaccine == null && ImmunizationLibrary.getInstance().getCurrentConditionalVaccine() == null) {
            OndoganciApplication.getInstance().initOfflineSchedules();
        }
    }
}
