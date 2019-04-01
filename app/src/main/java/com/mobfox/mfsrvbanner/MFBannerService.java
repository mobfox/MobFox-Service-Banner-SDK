package com.mobfox.mfsrvbanner;

import android.app.job.JobParameters;
import android.app.job.JobService;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.github.mobfox.from_service.MobFoxAdRequest;

import org.json.JSONObject;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MFBannerService extends JobService{

    public static final String AD_LOADED      = "onAdLoaded";
    public static final String AD_FAILED_LOAD = "onAdFailLoad";

    @Override
    public boolean onStartJob(final JobParameters params) {

        // read the inventory hash and user-agent from app:
        String invh = params.getExtras().getString("invh");
        String ua   = params.getExtras().getString("ua");

        // create ad request:
        MobFoxAdRequest mfAdRequest = MobFoxAdRequest.getInstance();

        // request the ad:
        mfAdRequest.requestBanner(this, ua, invh,
                320, 50, new MobFoxAdRequest.Listener() {

                    @Override
                    public void onAdLoaded(JSONObject adResponse) {

                        // if got response - broadcast to app:
                        Intent loadedIntent = new Intent(AD_LOADED);
                        loadedIntent.putExtra("ad", adResponse.toString());
                        sendBroadcast(loadedIntent);

                        jobFinished(params,true);
                    }

                    @Override
                    public void onAdFailLoad(String err) {

                        // if failed - broadcast error to app:
                        Intent failedIntent = new Intent(AD_FAILED_LOAD);
                        failedIntent.putExtra("error", err);
                        sendBroadcast(failedIntent);

                        jobFinished(params,true);
                    }
                });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
