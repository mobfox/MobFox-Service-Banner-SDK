package com.mobfox.mfsrvbanner;

import android.app.job.JobParameters;
import android.app.job.JobService;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.github.mobfox.from_service.MobFoxAdRequest;

import org.json.JSONObject;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TestService extends JobService{

    public static final String AD_LOADED      = "onAdLoaded";
    public static final String AD_FAILED_LOAD = "onAdFailLoad";

    @Override
    public boolean onStartJob(final JobParameters params) {
        /*
         * True - if your service needs to process
         * the work (on a separate thread).
         * False - if there's no more work to be done for this job.
         */

        Log.i("Calldorado","dbg: ### service-onStartJob ###");

        String invh = params.getExtras().getString("invh");
        String ua   = params.getExtras().getString("ua");

        MobFoxAdRequest mfadr = MobFoxAdRequest.getInstance();
        mfadr.requestBanner(this, ua, invh,
                320, 50, new MobFoxAdRequest.Listener() {
                    @Override
                    public void onAdLoaded(JSONObject adResponse) {

                        Log.i("Calldorado","dbg: ### service-onAdLoaded ###");

                        Intent loadedIntent = new Intent(AD_LOADED);
                        loadedIntent.putExtra("ad", adResponse.toString());
                        sendBroadcast(loadedIntent);

                        jobFinished(params,true);
                    }

                    @Override
                    public void onAdFailLoad(String err) {

                        Log.i("Calldorado","dbg: ### service-onAdFailLoad: "+err);

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

        Log.i("Calldorado","dbg: ### service-onStopJob ###");

        return false;
    }
}
