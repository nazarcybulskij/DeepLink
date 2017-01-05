package com.inverita.nazarko.deeplink;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteApi;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final String DEEP_LINK_URL_VIDEO = "http://teleportal.ua/video/12";
    private static final String DEEP_LINK_URL_NEWS = "http://teleportal.ua/news";


    // [START define_variables]
    private GoogleApiClient mGoogleApiClient;
    // [END define_variables]



    // [START on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Validate that the developer has set the app code.
        validateAppCode();

        TextView link =  ((TextView) findViewById(R.id.link_view_send));

        // Create a deep link and display it in the UI
        final Uri deepLinkvideo = buildDeepLink(Uri.parse(DEEP_LINK_URL_VIDEO), 0, false);
        link.setText(link.getText() + "\n"+deepLinkvideo.toString());


        // Create a deep link and display it in the UI
        final Uri deepLinknews = buildDeepLink(Uri.parse(DEEP_LINK_URL_NEWS), 0, false);
        link.setText(link.getText() + "\n"+deepLinknews.toString());


        // [END_EXCLUDE]

        // [START build_api_client]
        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();
        // [END build_api_client]

        // [START get_deep_link]
        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    // Handle the deep link. For example, open the linked
                                    // content, or apply promotional credit to the user's
                                    // account.
                                    // [START_EXCLUDE]
                                    // Display deep link in the UI
                                    ((TextView) findViewById(R.id.link_view_receive)).setText(deepLink);
//                                    if (deepLink.endsWith("news")){
//                                        startActivity(new Intent(MainActivity.this,Main2Activity.class));
//                                    }
                                    // [END_EXCLUDE]
                                } else {
                                    Log.d(TAG, "getInvitation: no deep link found.");
                                }
                            }
                        });
        // [END get_deep_link]

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("link"))
            {
                // extract the extra-data in the Notification
                String link = intent.getStringExtra("link");


//                //((TextView) findViewById(R.id.link_view_receive)).setText(msg);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(link));;
                startActivity(browserIntent);
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                //Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));

                startActivity(browserIntent);

            }
        }
    }

    // [END on_create]

    /**
     * Build a Firebase Dynamic Link.
     * https://firebase.google.com/docs/dynamic-links/android#create-a-dynamic-link
     *
     * @param deepLink the deep link your app will open. This link must be a valid URL and use the
     *                 HTTP or HTTPS scheme.
     * @param minVersion the {@code versionCode} of the minimum version of your app that can open
     *                   the deep link. If the installed app is an older version, the user is taken
     *                   to the Play store to upgrade the app. Pass 0 if you do not
     *                   require a minimum version.
     * @param isAd true if the dynamic link is used in an advertisement, false otherwise.
     * @return a {@link Uri} representing a properly formed deep link.
     */
    @VisibleForTesting
    public Uri buildDeepLink(@NonNull Uri deepLink, int minVersion, boolean isAd) {
        // Get the unique appcode for this app.
        String appCode = getString(R.string.app_code);

        // Get this app's package name.
        String packageName = getApplicationContext().getPackageName();

        // Build the link with all required parameters
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(appCode + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", deepLink.toString())
                .appendQueryParameter("apn", packageName);


        // If the deep link is used in an advertisement, this value must be set to 1.
        if (isAd) {
            builder.appendQueryParameter("ad", "1");
        }

        // Minimum version is optional.
        if (minVersion > 0) {
            builder.appendQueryParameter("amv", Integer.toString(minVersion));
        }


        // Return the completed deep link.
        return builder.build();
    }



    private void validateAppCode() {
        String appCode = getString(R.string.app_code);
        if (appCode.contains("YOUR_APP_CODE")) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid Configuration")
                    .setMessage("Please set your app code in res/values/strings.xml")
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
