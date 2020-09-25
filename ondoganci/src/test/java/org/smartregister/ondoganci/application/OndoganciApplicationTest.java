package org.smartregister.ondoganci.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.ondoganci.BaseUnitTest;
import org.smartregister.ondoganci.TestOndoganciApplication;

/**
 * Created by ndegwamartin on 2019-12-13.
 */
public class OndoganciApplicationTest extends BaseUnitTest {
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCommonFtsObjectFunctionsCorrectly() {

        OndoganciApplication ondoganciApplication = new TestOndoganciApplication();
        Assert.assertNotNull(ondoganciApplication);

        CommonFtsObject commonFtsObject = ondoganciApplication.createCommonFtsObject(RuntimeEnvironment.application);
        Assert.assertNotNull(commonFtsObject);

        String[] ftsObjectTables = commonFtsObject.getTables();
        Assert.assertNotNull(ftsObjectTables);
        Assert.assertEquals(2, ftsObjectTables.length);

        String scheduleName = commonFtsObject.getAlertScheduleName("bcg");
        Assert.assertNotNull(scheduleName);

        scheduleName = commonFtsObject.getAlertScheduleName("penta1");
        Assert.assertNotNull(scheduleName);

        scheduleName = commonFtsObject.getAlertScheduleName("mr1");
        Assert.assertNotNull(scheduleName);

        scheduleName = commonFtsObject.getAlertScheduleName("SomeNonExistentVaccine");
        Assert.assertNull(scheduleName);
    }
}
