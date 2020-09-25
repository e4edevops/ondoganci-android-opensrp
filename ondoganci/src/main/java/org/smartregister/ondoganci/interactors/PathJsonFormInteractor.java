package org.smartregister.ondoganci.interactors;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.child.widgets.ChildDatePickerFactory;
import org.smartregister.child.widgets.ChildEditTextFactory;

/**
 * Created by keyman on 11/04/2017.
 */
public class PathJsonFormInteractor extends JsonFormInteractor {

    private static final JsonFormInteractor PATH_INTERACTOR_INSTANCE = new PathJsonFormInteractor();

    private PathJsonFormInteractor() {
        super();
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.EDIT_TEXT, new ChildEditTextFactory());
        map.put(JsonFormConstants.DATE_PICKER, new ChildDatePickerFactory());
    }

    public static JsonFormInteractor getPathInteractorInstance() {
        return PATH_INTERACTOR_INSTANCE;
    }
}
