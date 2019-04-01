# MobFox-Service-Banner-SDK
MobFox Banner with service

<!-- toc -->

* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Usage](#usage)
* [Manifest](#manifest)
* [Background Service](#background-service)
* [Foreground Code](#foreground-code-service)

<!-- toc stop -->

# Prerequisites

* You will need a Mobfox account.


# Installation

Clone or download **MobFox-Service-Banner-SDK** from here, and extract it on your computer.
In it you will find a demo application for how the **MobFox-Service-Banner-SDK** can be used.

You can import this project to Android Studio, and use it as a reference
or base to make your alterations.

If you already have an existing project, or want to create a new one,
The **MobFox-Service-Banner-SDK** comes in the form of **MobFox-Android-Service-Banner-X.X.X.aar**. 
You should be able to locate it in the **/app/libs** folder. There is also a **JAR** file there if you prefare.

To add it to your project, follow the steps below:

1. make sure you have a **/app/libs** folder inside your project, and copy the AAR
or JAR file to it.

2. In your main (root or project) **gradle.build** file add this:

```java
allprojects {
    repositories {
        ...
        
        flatDir {
            dirs 'libs'
        }
    }
}
```

3. In your application **gradle.build** file add this:

```java
dependencies {
    ...

    implementation 'com.android.volley:volley:1.1.0'

    implementation(name:'MobFox-Android-Service-Banner-X.X.X', ext:'aar')
}
```


# Usage

### Manifest

- Add the following permissions to your '**AndroidManifest.xml**' file:


```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

- Also add this inside the '**Application**' tag:

```xml
<service android:name=".MFBannerService"
    android:permission="android.permission.BIND_JOB_SERVICE"
    />
```

Where **MFBannerService** is the name of the service your app will use to get the banner
request from MobFox.



### JobService

This SDK varies from the standard Banner SDK by the fact that it allows your app to get
Banner/s in the background using a JobService, so that you always have a banner ad ready
for display when needed.

To do that you need to add to your app a JobService to handle the background work.

You can copy the **MFBannerService.java** file and use it as your banner retrieval service:

```java
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
```



### Background Service

This SDK varies from the standard Banner SDK by the fact that it allows your app to get
Banner/s in the background using a JobService, so that you always have a banner ad ready
for display when needed.

To do that you need to add to your app a JobService to handle the background work.

You can copy the **MFBannerService.java** file and use it as your banner retrieval service:

```java
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
```


### Foreground Code

On your foreground code you need to initialize and start the service, get the ad data
retrived by it, and then use it at will to display the banner/s. 

You can use the **MainActivity.java** file as a reference for this.

1. Initialize and start the background service:

```java
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
private void startMobFoxBannerService()
{
    // define broadcast receiver (handle broadcasts from service):
    BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            if ((intent!=null) && (intent.getAction()!=null))
            {
                // handle error in service: 
                if (intent.getAction().equals(MFBannerService.AD_FAILED_LOAD)) {
                    String error = intent.getStringExtra("error");
                    if (error!=null)
                    {
                        // here you can display or otherwise handle the error:
                        // ...
                    }
                }
                // handle ad data retrived from service:
                if (intent.getAction().equals(MFBannerService.AD_LOADED)) {
                    String adResponse = intent.getStringExtra("ad");
                    if (adResponse!=null)
                    {
                        // here you can handle the ad data retrieved:
                        // in the demo app we save it to a queue of banner ad data
                        mArrMobFoxAdResponses.add(adResponse);
                        // ...
                    }
                }
            }
        }
    };

    // register receivers for broadcasts from service:
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
```


2. Use the retrieved banner ad data to display a banner (example from demo app): 

```java
private void HandleMobFoxBanner(String adResponse)
{
    BannerWithExternalResponse banner  = new BannerWithExternalResponse(adResponse,
                                            MainActivity.this,
                                            320,
                                            50,
                                            "fe96717d9875b9da4339ea5367eff1ec", 
                                            new Banner.Listener()
    {
        @Override
        public void onBannerError(Banner banner, Exception e) {
            UpdateBannerStatus("Banner error: "+e.getLocalizedMessage());
        }

        @Override
        public void onBannerLoaded(Banner banner) {
            UpdateBannerStatus("onBannerLoaded");
        }

        @Override
        public void onBannerClosed(Banner banner) {
            UpdateBannerStatus("onBannerClosed");
        }

        @Override
        public void onBannerFinished() {
            UpdateBannerStatus("onBannerFinished");
        }

        @Override
        public void onBannerClicked(Banner banner) {
            UpdateBannerStatus("onBannerClicked");
        }

        @Override
        public void onNoFill(Banner banner) {
            UpdateBannerStatus("onNoFill");
        }
    });

    RelativeLayout view = findViewById(R.id.bannerContainer);
    view.addView(banner);
    banner.load();
}
```



# Thank you for using MobFox-Service-Banner-SDK !
