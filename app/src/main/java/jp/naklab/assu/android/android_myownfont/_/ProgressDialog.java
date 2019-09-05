package jp.naklab.assu.android.android_myownfont._;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import jp.naklab.assu.android.android_myownfont.R;

public class ProgressDialog extends DialogFragment {
    private static final int DELAY_MILLISECOND = 450;
    private static final int SHOW_MIN_MILLISECOND = 300;

    private ProgressBar mProgressBar;
    private TextView mProgressMessage;
    private boolean mStartedShowing;
    private long mStartMillisecond;
    private long mStopMillisecond;

    private String mMessage;

    // required default constructor
    public ProgressDialog() {
        super();
    }

    public static ProgressDialog newInstance(String message) {
        ProgressDialog instance = new ProgressDialog();

        Bundle arguments = new Bundle();
        arguments.putString("message", message);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mMessage = getArguments().getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_progress, null));
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressBar = getDialog().findViewById(R.id.progress);
        mProgressMessage = getDialog().findViewById(R.id.progress_message);
        mProgressMessage.setText(mMessage);
    }

    @Override
    public void show(final FragmentManager manager, final String tag) {
        mStartMillisecond = System.currentTimeMillis();
        mStartedShowing = false;
        mStopMillisecond = Long.MAX_VALUE;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStopMillisecond > System.currentTimeMillis()) {
                    showDialogAfterDelay(manager, tag);
                }
            }
        }, DELAY_MILLISECOND);
    }

    private void showDialogAfterDelay(FragmentManager manager, String tag) {
        mStartedShowing = true;
        super.show(manager, tag);
    }

    public void cancel() {
        mStopMillisecond = System.currentTimeMillis();

        if (mStartedShowing) {
            if (mProgressBar != null) {
                cancelWhenShowing();
            } else {
                cancelWhenNotShowing();
            }
        }
    }

    private void cancelWhenShowing() {
        if (mStopMillisecond < mStartMillisecond + DELAY_MILLISECOND + SHOW_MIN_MILLISECOND) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissAllowingStateLoss();
                }
            }, SHOW_MIN_MILLISECOND);
        } else {
            dismissAllowingStateLoss();
        }
    }

    private void cancelWhenNotShowing() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissAllowingStateLoss();
            }
        }, SHOW_MIN_MILLISECOND);
    }

    public void setMessage(String message) {
        if (mProgressMessage == null) {
            mProgressMessage = getDialog().findViewById(R.id.progress_message);
        }
        mProgressMessage.setText(message);
    }
}