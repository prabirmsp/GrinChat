package com.example.prabir.grinchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int CHOOSE_PHOTO_REQUEST = 2;
    public static final int CHOOSE_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 10*1024*1024; // 10 MB

    protected Uri mMediaUri;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    // Take Picture
    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: // Take pic
                            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                            if (mMediaUri == null) {
                                // error
                                Toast.makeText(MainActivity.this, R.string.error_access_memory, Toast.LENGTH_LONG).show();
                            }
                            else {
                                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                            }
                            break;
                        case 1: // Take vid
                            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                            if (mMediaUri == null) {
                                // error
                                Toast.makeText(MainActivity.this, R.string.error_access_memory, Toast.LENGTH_LONG).show();
                            }
                            else {
                                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                                startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                            }
                            break;
                        case 2: // Choose pic
                            Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            choosePhotoIntent.setType("image/*");
                            startActivityForResult(choosePhotoIntent, CHOOSE_PHOTO_REQUEST);
                            break;
                        case 3: // Choose vid
                            Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            chooseVideoIntent.setType("video/*");
                            Toast.makeText(MainActivity.this, R.string.video_size_warning, Toast.LENGTH_LONG).show();
                            startActivityForResult(chooseVideoIntent, CHOOSE_VIDEO_REQUEST);
                            break;

                    }
                }

                private Uri getOutputMediaFileUri(int mediaType) {
                    if (isExternalStorageAvaliable()) {
                        // get the URI

                        String appName = getString(R.string.app_name);
                        //get ext storage directory
                        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);
                        if (! mediaStorageDir.exists()) {
                            if (! mediaStorageDir.mkdir()) {
                                Log.e(TAG, "Failed to create directory.");
                                return null;
                            }
                        }

                        // Create the file
                        File mediaFile;
                        Date now = new Date();
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
                        String path = mediaStorageDir.getPath() + File.separator;
                        if (mediaType == MEDIA_TYPE_IMAGE) {
                            mediaFile = new File( path + "IMG_" + timestamp + ".jpg");
                            Log.d(TAG, Uri.fromFile(mediaFile).toString());
                        }
                        else if (mediaType == MEDIA_TYPE_VIDEO) {
                            mediaFile = new File( path + "VID_" + timestamp + ".mp4");
                            Log.d(TAG, Uri.fromFile(mediaFile).toString());
                        }
                        else return null;

                        // Return the file's URI
                        return Uri.fromFile(mediaFile);
                    }
                    else return null;

                }

                private boolean isExternalStorageAvaliable() {
                    String state = Environment.getExternalStorageState();

                    if (state.equals(Environment.MEDIA_MOUNTED)){
                        return true;
                    }
                    else return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }
        else {
            Log.i(TAG, currentUser.getUsername());
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == CHOOSE_PHOTO_REQUEST || requestCode == CHOOSE_VIDEO_REQUEST) {
                if (data == null) {
                    // Error
                    toastGenericError();
                }
                else {
                    mMediaUri = data.getData();
                }

                if (requestCode == CHOOSE_VIDEO_REQUEST) {
                    // check for under 10 MB
                    int fileSize = 0;
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    }
                    catch (FileNotFoundException e) {
                        toastGenericError();
                        return;
                    }
                    catch (IOException e) {
                        toastGenericError();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) { /* Intentionally blank */ }
                    }
                    if (fileSize >= FILE_SIZE_LIMIT) {
                        Toast.makeText(this, R.string.file_too_large, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                // add to gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Intent recipientIntent = new Intent(this, RecipientsActivity.class);
            recipientIntent.setData(mMediaUri);

            String fileType = "";
            if (requestCode == CHOOSE_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else if (requestCode == CHOOSE_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST) {
                fileType = ParseConstants.TYPE_VIDEO;
            }

            recipientIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
            startActivity(recipientIntent);



        }
        else if (requestCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.signupErrorTitle, Toast.LENGTH_LONG).show();
        }
    }

    private void toastGenericError() {
        Toast.makeText(MainActivity.this, R.string.signupErrorTitle, Toast.LENGTH_LONG).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.menu_editFriends:
                Intent intent = new Intent(this, EditFriendsListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

}
