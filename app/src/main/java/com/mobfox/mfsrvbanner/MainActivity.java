package com.mobfox.mfsrvbanner;


import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobfox.sdk.banner.Banner;
import com.mobfox.sdk.logging.MobFoxReport;
import com.mobfox.sdk.service_banner.BannerWithExternalResponse;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    JobScheduler jobScheduler;
    public final static int JOB_ID = 111;

    private ArrayList<String> mArrMobFoxAdResponses = new ArrayList<>();

    //====================================================================

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnGO);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mArrMobFoxAdResponses.size()>0)
                {
                    ((TextView)findViewById(R.id.lblQueueStatus)).setText("");
                    HandleMobFoxBanner(mArrMobFoxAdResponses.get(0));
                    mArrMobFoxAdResponses.remove(0);
                    UpdateQueueStatus();
                }
            }
        });

        // Start the service to collect banner ads
        startMobFoxBannerService();

        UpdateQueueStatus();
    }

    //====================================================================

    private void UpdateQueueStatus()
    {
        String txt = "Queue has "+mArrMobFoxAdResponses.size()+" ads";
        ((TextView)findViewById(R.id.lblQueueStatus)).setText(txt);

        ((Button)findViewById(R.id.btnGO)).setEnabled(mArrMobFoxAdResponses.size()>0);
    }

    private void UpdateBannerStatus(String txt)
    {
        ((TextView)findViewById(R.id.lblBannerStatus)).setText(txt);
    }

    //====================================================================

    private void HandleMobFoxBanner(String adResponse)
    {
        BannerWithExternalResponse banner  = new BannerWithExternalResponse(adResponse,
                MainActivity.this,
                320,
                50,
                "fe96717d9875b9da4339ea5367eff1ec", new Banner.Listener()
        {
            @Override
            public void onBannerLoadFailed(Banner banner, String s) {

            }

            @Override
            public void onBannerLoaded(Banner banner) {

            }

            @Override
            public void onBannerClosed(Banner banner) {

            }

            @Override
            public void onBannerFinished() {

            }

            @Override
            public void onBannerClicked(Banner banner) {

            }
        });

        RelativeLayout view = findViewById(R.id.bannerContainer);
        view.addView(banner);
        banner.load();

        UpdateBannerStatus("Displaying banner...");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startMobFoxBannerService()
    {
        // define broadcast receiver (handle broadcasts from service):
        BroadcastReceiver receiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

                if ((intent!=null) && (intent.getAction()!=null))
                {
                    if (intent.getAction().equals(MFBannerService.AD_FAILED_LOAD)) {

                        String error = intent.getStringExtra("error");
                        if (error!=null)
                        {
                            UpdateBannerStatus(error);
                        }
                    }

                    if (intent.getAction().equals(MFBannerService.AD_LOADED)) {

                        String adResponse = intent.getStringExtra("ad");
                        if (adResponse!=null)
                        {
                            mArrMobFoxAdResponses.add(adResponse);
                            UpdateQueueStatus();
                        }
                    }
                }
            }
        };

        // register receiver:
        IntentFilter intentFilter;
        intentFilter = new IntentFilter(MFBannerService.AD_FAILED_LOAD);
        this.registerReceiver(receiver, intentFilter);
        intentFilter = new IntentFilter(MFBannerService.AD_LOADED);
        this.registerReceiver(receiver, intentFilter);

        // start the service:
        jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName jobService = new ComponentName(getPackageName(), MFBannerService.class.getName());

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("invh","fe96717d9875b9da4339ea5367eff1ec");
        bundle.putString("ua"  , MobFoxReport.getUserAgent(MainActivity.this));

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, jobService)
                .setMinimumLatency(1000)
                .setOverrideDeadline(5000)
                .setExtras(bundle).build();

        jobScheduler.schedule(jobInfo);
    }
}
