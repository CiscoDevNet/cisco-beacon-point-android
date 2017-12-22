package com.android.ble.sample.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.android.ble.sample.R;
import com.android.ble.sample.listeners.DialogListener;


/**
 * Created by Entappiainc on 08-03-2016.
 */
public class AddVenueDialog extends DialogFragment implements View.OnClickListener{

    Button cancelButton, qrCodeButton;
    DialogListener dialogListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_venue, container);

        Window window = getDialog().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        window.setGravity(Gravity.BOTTOM);
        getDialog().setTitle(null);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Transparent);

        dialogListener = (DialogListener) getActivity();

        cancelButton   = (Button)view.findViewById(R.id.cancelButton);
        qrCodeButton   = (Button)view.findViewById(R.id.qrCodeButton);

        cancelButton.setOnClickListener(this);
        qrCodeButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.cancelButton:
                dismiss();
                break;

            case R.id.qrCodeButton:
                dialogListener.performDialogAction("qr_code_event", true);
                dismiss();
                break;
        }
    }


}
