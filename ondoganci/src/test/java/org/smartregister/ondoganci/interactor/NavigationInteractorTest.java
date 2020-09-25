package org.smartregister.ondoganci.interactor;

import net.sqlcipher.Cursor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.util.AppConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OndoganciApplication.class)
public class NavigationInteractorTest {

    @Mock
    private OndoganciApplication ondoganciApplication;

    @Mock
    private Context context;

    @Mock
    private CommonRepository commonRepository;

    private NavigationInteractor navigationInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        navigationInteractor = new NavigationInteractor();
    }

    @Test
    public void getCountForChildRegisterCountIfResultIsOne() throws Exception {
        PowerMockito.mockStatic(OndoganciApplication.class);
        PowerMockito.when(ondoganciApplication.getContext()).thenReturn(context);
        PowerMockito.when(context.commonrepository(AppConstants.TABLE_NAME.ALL_CLIENTS)).thenReturn(commonRepository);
        Cursor cursor = Mockito.mock(Cursor.class);
        PowerMockito.when(cursor.moveToFirst()).thenReturn(true);
        PowerMockito.when(cursor.getInt(0)).thenReturn(1);
        PowerMockito.when(commonRepository.rawCustomQueryForAdapter("select count(*) from ec_client inner join client_register_type on ec_client.id=client_register_type.base_entity_id  where ec_client.date_removed is null AND register_type IN ('child')  AND ( dod is NULL OR dod = '' ) ;")).thenReturn(cursor);
        PowerMockito.when(OndoganciApplication.getInstance()).thenReturn(ondoganciApplication);
        int result = Whitebox.invokeMethod(navigationInteractor, "getCount", AppConstants.RegisterType.CHILD);
        Assert.assertEquals(1, result);
    }

}