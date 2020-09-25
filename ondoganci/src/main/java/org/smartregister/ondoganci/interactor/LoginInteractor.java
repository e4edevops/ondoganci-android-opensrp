package org.smartregister.ondoganci.interactor;

import org.smartregister.child.job.ArchiveClientsJob;
import org.smartregister.growthmonitoring.job.HeadIntentServiceJob;
import org.smartregister.growthmonitoring.job.HeightIntentServiceJob;
import org.smartregister.growthmonitoring.job.WeightIntentServiceJob;
import org.smartregister.growthmonitoring.job.ZScoreRefreshIntentServiceJob;
import org.smartregister.immunization.job.RecurringServiceJob;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.reporting.job.RecurringIndicatorGeneratingJob;
import org.smartregister.ondoganci.BuildConfig;
import org.smartregister.ondoganci.job.AppVaccineUpdateJob;
import org.smartregister.view.contract.BaseLoginContract;

import java.util.concurrent.TimeUnit;

public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void scheduleJobsPeriodically() {

        VaccineServiceJob
                .scheduleJob(VaccineServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                        getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        RecurringServiceJob
                .scheduleJob(RecurringServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                        getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        WeightIntentServiceJob
                .scheduleJob(WeightIntentServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                        getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        HeightIntentServiceJob
                .scheduleJob(HeightIntentServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                        getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        HeadIntentServiceJob
                .scheduleJob(HeadIntentServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                        getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        ZScoreRefreshIntentServiceJob.scheduleJob(ZScoreRefreshIntentServiceJob.TAG,
                TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES),
                getFlexValue(BuildConfig
                        .DATA_SYNC_DURATION_MINUTES));

        PullUniqueIdsServiceJob
                .scheduleJob(PullUniqueIdsServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.PULL_UNIQUE_IDS_MINUTES),
                        getFlexValue(BuildConfig.PULL_UNIQUE_IDS_MINUTES));

        ImageUploadServiceJob
                .scheduleJob(ImageUploadServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.IMAGE_UPLOAD_MINUTES),
                        getFlexValue(BuildConfig.IMAGE_UPLOAD_MINUTES));

        RecurringIndicatorGeneratingJob.scheduleJob(RecurringIndicatorGeneratingJob.TAG,
                TimeUnit.HOURS.toMinutes(6), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        ArchiveClientsJob.scheduleDaily();

        // Schedule daily jobs
        AppVaccineUpdateJob.scheduleEverydayAt(AppVaccineUpdateJob.TAG, 1, 20);
    }

    @Override
    protected void scheduleJobsImmediately() {
        //        schedule jobs
        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
        PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG); //need these asap!
        ZScoreRefreshIntentServiceJob.scheduleJobImmediately(ZScoreRefreshIntentServiceJob.TAG);
        ImageUploadServiceJob.scheduleJobImmediately(ImageUploadServiceJob.TAG);
        ArchiveClientsJob.runAtOnce();
    }
}
