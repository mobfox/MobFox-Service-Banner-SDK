# MobFox-Service-Banner-SDK
MobFox Banner with service

<!-- toc -->

* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Usage](#usage)
* [Manifest](#manifest)
* [Code](#code)

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

### Code

You can copy the **MFBannerService.java** file and use it as a whole to your banner retrieval
service.

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


# Thank you for using MobFox-Service-Banner-SDK !
