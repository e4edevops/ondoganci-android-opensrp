package org.smartregister.ondoganci.repository;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.ondoganci.BaseRobolectricTest;
import org.smartregister.ondoganci.application.OndoganciApplication;
import org.smartregister.ondoganci.shadow.ShadowSQLiteDatabase;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 06-03-2020.
 */
@Config(shadows = {ShadowSQLiteDatabase.class})
public class OndoganciRepositoryTest extends BaseRobolectricTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private OndoganciRepository ondoganciRepository;

    @Mock
    private SQLiteDatabase sqLiteDatabase;

    @Before
    public void setUp() throws Exception {
        ondoganciRepository = Mockito.spy((OndoganciRepository) OndoganciApplication.getInstance().getRepository());

        Mockito.doReturn(sqLiteDatabase).when(ondoganciRepository).getReadableDatabase();
        Mockito.doReturn(sqLiteDatabase).when(ondoganciRepository).getReadableDatabase(Mockito.anyString());
        Mockito.doReturn(sqLiteDatabase).when(ondoganciRepository).getWritableDatabase();
        Mockito.doReturn(sqLiteDatabase).when(ondoganciRepository).getWritableDatabase(Mockito.anyString());

        ReflectionHelpers.setField(OndoganciApplication.getInstance(), "repository", ondoganciRepository);
    }

    // TODO: FIX THIS
    @Test
    public void onCreateShouldCreate32tables() {
        Mockito.doNothing().when(ondoganciRepository).onUpgrade(Mockito.any(SQLiteDatabase.class), Mockito.anyInt(), Mockito.anyInt());
        SQLiteDatabase database = Mockito.mock(SQLiteDatabase.class);
        ondoganciRepository.onCreate(database);

        // TODO: Investigate this counter
        Mockito.verify(database, Mockito.times(32)).execSQL(Mockito.contains("CREATE TABLE"));
    }
}