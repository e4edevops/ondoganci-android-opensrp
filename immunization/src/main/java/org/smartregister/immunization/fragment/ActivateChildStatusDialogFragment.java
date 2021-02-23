package org.smartregister.immunization.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.smartregister.immunization.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 04/04/2018.
 */
@SuppressLint("ValidFragment")
public class ActivateChildStatusDialogFragment extends DialogFragment {

    private String thirdPersonPronoun = "";
    private String currentStatus = "";
    private DialogInterface.OnDismissListener onDismissListener;
    private DialogInterface.OnClickListener onClickListener;
    private int dialogTheme;

    public static ActivateChildStatusDialogFragment newInstance(String thirdPersonPronoun, String currentStatus, int theme) {
        ActivateChildStatusDialogFragment activateChildStatusDialogFragment = new ActivateChildStatusDialogFragment();
        activateChildStatusDialogFragment.setThirdPersonPronoun(thirdPersonPronoun);
        activateChildStatusDialogFragment.setCurrentStatus(currentStatus);
        activateChildStatusDialogFragment.dialogTheme = theme;

        return activateChildStatusDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dialogTheme != 0) {
            setStyle(STYLE_NO_TITLE, dialogTheme);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String dialogTitle = String.format(
                getActivity().getString(R.string.activate_child_status_dialog_title),
                currentStatus,
                thirdPersonPronoun);

        View inflatedView = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_fragment_activate_child_status, container, false);
        inflatedView.setFilterTouchesWhenObscured(true);
        ((TextView) inflatedView.findViewById(R.id.tv_dialog_activate_child_title))
                .setText(dialogTitle);

        Button negativeButton = inflatedView.findViewById(android.R.id.button2);
        Button positiveButton = inflatedView.findViewById(android.R.id.button1);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(getDialog(), AlertDialog.BUTTON_NEGATIVE);
                }
                dismiss();
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(getDialog(), AlertDialog.BUTTON_POSITIVE);
                }
                dismiss();
            }
        });

        return inflatedView;
    }

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public DialogInterface.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String getThirdPersonPronoun() {
        return thirdPersonPronoun;
    }

    public void setThirdPersonPronoun(String thirdPersonPronoun) {
        this.thirdPersonPronoun = thirdPersonPronoun;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}

