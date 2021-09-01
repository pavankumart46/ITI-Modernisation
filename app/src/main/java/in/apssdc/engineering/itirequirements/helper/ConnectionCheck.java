package in.apssdc.engineering.itirequirements.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import in.apssdc.engineering.itirequirements.R;


public class ConnectionCheck {

    private static Context c;

    public static boolean checkConnection(Context context) {
        c = context;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Log.d(context.getString(R.string.conn_info_log), context.getString(R.string.succ));
            return true;
        } else {
            showConnectionDisabledAlert();
            return false;
        }
    }

    public static void showConnectionDisabledAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
        alertDialogBuilder.setMessage(R.string.enable_internet)
                .setCancelable(false)
                .setPositiveButton(R.string.net_alert_poistive_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
