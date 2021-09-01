/*
 * Copyright 2014 Thomas Hoffmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.apssdc.engineering.itirequirements.map_measure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Stack;

import in.apssdc.engineering.itirequirements.BuildConfig;
import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.model.Images;
import in.apssdc.engineering.itirequirements.model.MAPSFILE;

abstract class Dialogs {

    /**
     * @param c the Context
     * @return the about dialog
     */
    public static Dialog getAbout(final Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.about);

        TextView tv = new TextView(c);
        int pad = (Util.dpToPx(c, 10));
        tv.setPadding(pad, pad, pad, pad);

        try {
            tv.setText(R.string.about_text);
            tv.append(c.getString(R.string.app_version,
                    c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (NameNotFoundException e1) {
            // should not happen as the app is definitely installed when
            // seeing the dialog
            e1.printStackTrace();
        }
        builder.setView(tv);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    /**
     * @param c     the Context
     * @param trace the current trace of points
     * @return the "save & share" dialog
     */
    public static Dialog getSaveNShare(final Activity c, final Stack<LatLng> trace, final String area) {
        final Dialog d = new Dialog(c);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_save);
        d.findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final File destination;
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                        c.getExternalFilesDir(null) != null) {
                    destination = c.getExternalFilesDir(null);
                } else {
                    destination = c.getDir("traces", Context.MODE_PRIVATE);
                }

                d.dismiss();
                if (destination == null) {
                    Toast.makeText(c,
                            c.getString(R.string.error, "Can not access external files directory"),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                AlertDialog.Builder b = new AlertDialog.Builder(c);
                b.setTitle(R.string.save);
                final View layout =
                        c.getLayoutInflater().inflate(R.layout.dialog_enter_filename, null);
                ((TextView) layout.findViewById(R.id.location)).setText(
                        c.getString(R.string.file_path, destination.getAbsolutePath() + "/"));
                b.setView(layout);
                b.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String fname = ((EditText) layout.findViewById(R.id.filename)).getText()
                                    .toString();
                            if (fname.length() < 1) {
                                fname = "MapsMeasure_" + System.currentTimeMillis();
                            }
                            final File f = new File(destination, fname + ".csv");
                            Util.saveToFile(f, trace);
                            final SharedPreferences sharedPreferences = d.getContext().getSharedPreferences("signindetails", Context.MODE_PRIVATE);
                            String Selected_college = "", Selected_district = "";
                            if (sharedPreferences.contains("SELECTED_DISTRICT")) {
                                Selected_district = sharedPreferences.getString("SELECTED_DISTRICT", "DEFAULT");
                            }
                            if (sharedPreferences.contains("SELECTED_COLLEGE")) {
                                Selected_college = sharedPreferences.getString("SELECTED_COLLEGE", "DEFAULT");
                            }
                            final StorageReference filesToUpload = FirebaseStorage.getInstance().getReference().child(Selected_district).child(Selected_college).child(fname + ".csv");
                            final String finalFname = fname;
                            filesToUpload.putFile(Uri.parse((String) "file://" + destination + "/" + fname + ".csv")).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    filesToUpload
                                            .getDownloadUrl()
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            MAPSFILE images = new MAPSFILE(uri.toString(),area, finalFname);
                                                            Toast.makeText(d.getContext(), uri.toString(), Toast.LENGTH_SHORT).show();
                                                            FirebaseDatabase.getInstance().getReference().
                                                                    child(sharedPreferences.getString("SELECTED_DISTRICT", "NULLLLL")).
                                                                    child(sharedPreferences.getString("SELECTED_COLLEGE", "NUKJL")).
                                                                    child("Area Description").
                                                                    child(String.valueOf(new Date().getTime())).
                                                                    setValue(images);
                                                            Toast.makeText(d.getContext(), "Successfully Uploaded Files", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                            );
                                }
                            });
                            d.dismiss();
                            Toast.makeText(c, R.string.file_saved, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            if (BuildConfig.DEBUG) Logger.log(e);
                            Toast.makeText(c, c.getString(R.string.error,
                                    e.getClass().getSimpleName() + "\n" + e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                b.create().show();
            }
        });
        d.findViewById(R.id.load).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                File[] files = c.getDir("traces", Context.MODE_PRIVATE).listFiles();

                if (files == null) {
                    Toast.makeText(c, c.getString(R.string.dir_read_error,
                            c.getDir("traces", Context.MODE_PRIVATE).getAbsolutePath()),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    File ext = c.getExternalFilesDir(null);
                    // even though we checked the external storage state, ext is still sometimes null, accoring to Play Store crash reports
                    if (ext != null && ext.listFiles() != null) {
                        File[] filesExtern = ext.listFiles();
                        File[] allFiles = new File[files.length + filesExtern.length];
                        System.arraycopy(files, 0, allFiles, 0, files.length);
                        System.arraycopy(filesExtern, 0, allFiles, files.length,
                                filesExtern.length);
                        files = allFiles;
                    }
                }

                if (files.length == 0) {
                    Toast.makeText(c, c.getString(R.string.no_files_found,
                            c.getDir("traces", Context.MODE_PRIVATE).getAbsolutePath()),
                            Toast.LENGTH_SHORT).show();
                } else if (files.length == 1) {
                    try {
                        Util.loadFromFile(Uri.fromFile(files[0]), (Map) c);
                        d.dismiss();
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) Logger.log(e);
                        e.printStackTrace();
                        Toast.makeText(c, c.getString(R.string.error,
                                e.getClass().getSimpleName() + "\n" + e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    d.dismiss();
                    AlertDialog.Builder b = new AlertDialog.Builder(c);
                    b.setTitle(R.string.select_file);
                    final DeleteAdapter da = new DeleteAdapter(files, (Map) c);
                    b.setAdapter(da, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Util.loadFromFile(Uri.fromFile(da.getFile(which)), (Map) c);
                                dialog.dismiss();
                            } catch (IOException e) {
                                if (BuildConfig.DEBUG) Logger.log(e);
                                e.printStackTrace();
                                Toast.makeText(c, c.getString(R.string.error,
                                        e.getClass().getSimpleName() + "\n" + e.getMessage()),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    b.create().show();
                }
            }
        });
        d.findViewById(R.id.share).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    final File f = new File(c.getCacheDir(), "MapsMeasure.csv");
                    Util.saveToFile(f, trace);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider
                            .getUriForFile(c, "de.j4velin.mapsmeasure.fileprovider", f));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setType("text/comma-separated-values");
                    d.dismiss();
                    c.startActivity(Intent.createChooser(shareIntent, null));
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) Logger.log(e);
                    e.printStackTrace();
                    Toast.makeText(c, c.getString(R.string.error,
                            e.getClass().getSimpleName() + "\n" + e.getMessage()),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        return d;
    }

    /**
     * @param m        the Map
     * @param distance the current distance
     * @param area     the current area
     * @return the units dialog
     */
    public static Dialog getUnits(final Map m, float distance, double area) {
        final Dialog d = new Dialog(m);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_unit);
        CheckBox metricCb = (CheckBox) d.findViewById(R.id.metric);
        metricCb.setChecked(Map.metric);
        metricCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map.metric = !Map.metric;
                m.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
                        .putBoolean("metric", isChecked).commit();
                m.updateValueText();
            }
        });
        ((TextView) d.findViewById(R.id.distance)).setText(
                Map.formatter_two_dec.format(Math.max(0, distance)) + " m\n" +
                        Map.formatter_two_dec.format(distance / 1000) + " km\n\n" +
                        Map.formatter_two_dec.format(Math.max(0, distance / 0.3048f)) + " ft\n" +
                        Map.formatter_two_dec.format(Math.max(0, distance / 0.9144)) + " yd\n" +
                        Map.formatter_two_dec.format(distance / 1609.344f) + " mi\n" +
                        Map.formatter_two_dec.format(distance / 1852f) + " nautical miles");

        ((TextView) d.findViewById(R.id.area)).setText(
                Map.formatter_two_dec.format(Math.max(0, area)) + " mÂ²\n" +
                        Map.formatter_two_dec.format(area / 10000) + " ha\n" +
                        Map.formatter_two_dec.format(area / 1000000) + " kmÂ²\n\n" +
                        Map.formatter_two_dec.format(Math.max(0, area / 0.09290304d)) + " ftÂ²\n" +
                        Map.formatter_two_dec.format(area / 4046.8726099d) + " ac (U.S. Survey)\n" +
                        Map.formatter_two_dec.format(area / 2589988.110336d) + " miÂ²");
        d.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                d.dismiss();
            }
        });
        return d;
    }

    /**
     * @param c the Context
     * @return a dialog informing the user about an issue with getting altitude
     * data from the Google API
     */
    public static Dialog getElevationErrorDialog(final Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(Util.checkInternetConnection(c) ? R.string.elevation_error :
                R.string.elevation_error_no_connection);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    /**
     * Shows the dialog to unlock the elevation feature
     *
     * @param c       the map activity
     * @param service the billing service
     */
    public static void showElevationAccessDialog(final Map c, final IInAppBillingService service) {
        final ProgressDialog pg = new ProgressDialog(c);
        pg.setMessage("Loading...");
        pg.show();
        final Handler h = new Handler();
        isGoogleAvailable(new Callback() {
            @Override
            public void result(final boolean available) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        pg.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        if (!available) {
                            builder.setMessage(R.string.no_google_connection);
                        } else {
                            builder.setMessage(R.string.buy_pro);
                            builder.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog,
                                                            int which) {
                                            try {
                                                Bundle buyIntentBundle =
                                                        service.getBuyIntent(3, c.getPackageName(),
                                                                Map.SKU, "inapp",
                                                                c.getPackageName());
                                                if (buyIntentBundle.getInt("RESPONSE_CODE") == 0) {
                                                    PendingIntent pendingIntent = buyIntentBundle
                                                            .getParcelable("BUY_INTENT");
                                                    c.startIntentSenderForResult(
                                                            pendingIntent.getIntentSender(), 42,
                                                            null, 0, 0, 0);
                                                }
                                            } catch (Exception e) {
                                                if (BuildConfig.DEBUG) Logger.log(e);
                                                Toast.makeText(c, e.getClass().getName() + ": " +
                                                        e.getMessage(), Toast.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();
                                        }
                                    });
                        }
                        builder.setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.create().show();
                    }
                });
            }
        });
    }

    /**
     * Workaround if the edittext search doesnt work
     *
     * @param map the map activity
     * @return a search dialog
     */
    public static Dialog getSearchDialog(final Map map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(map);
        final EditText search = new EditText(map);
        search.setHint(android.R.string.search_go);
        builder.setView(search);
        builder.setPositiveButton(android.R.string.search_go,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        new GeocoderTask(map).execute(search.getText().toString());
                        // hide softinput keyboard
                        InputMethodManager inputManager = (InputMethodManager) map
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(search.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                // hide softinput keyboard
                InputMethodManager inputManager =
                        (InputMethodManager) map.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(search.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private static void isGoogleAvailable(final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://maps.googleapis.com");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.connect();
                    callback.result(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (BuildConfig.DEBUG) Logger.log(e);
                    callback.result(false);
                } finally {
                    if (urlConnection != null) urlConnection.disconnect();
                }
            }
        }).start();
    }

    private interface Callback {
        void result(boolean available);
    }
}
