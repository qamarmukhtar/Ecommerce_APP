package com.techqamar.omkar_project;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;


public class Utils {


    public static byte[] fileToBytes(File input) {
        FileInputStream objFileIS = null;
        try {
            objFileIS = new FileInputStream(input);

            ByteArrayOutputStream objByteArrayOS = new ByteArrayOutputStream();
            byte[] byteBufferString = new byte[1024];

            for (int readNum; (readNum = objFileIS.read(byteBufferString)) != -1; ) {
                objByteArrayOS.write(byteBufferString, 0, readNum);
            }

            return objByteArrayOS.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void showConfirmationDialog(Context activity, String title, String msg, String yesBtnTxt, String noBtnTxt, final ConfirmationDialogActions action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(msg);

        String positiveText = yesBtnTxt;
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action.onPositiveAction();
                    }
                });

        String negativeText = noBtnTxt;
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action.onNegativeAction();
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }
    public interface ConfirmationDialogActions {
        void onPositiveAction();

        void onNegativeAction();
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass,Context activity) {
        if(activity==null){
            return true;
        }
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static void setVolleyRetryPolicy(Request<?> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(40000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
