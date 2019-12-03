package edu.uvm.cs275.conversationanalysis;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmationFragment extends DialogFragment {
    public static final String EXTRA_BOOL = "edu.uvm.cs275.bool";

    public InterfaceCommunicator mInterfaceCommunicator;

    public interface InterfaceCommunicator {
        public void sendResultCode(boolean flag);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mInterfaceCommunicator = (InterfaceCommunicator) getContext();
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_confirmation)
                .setPositiveButton(R.string.confirm, yesPressed)
                .setNegativeButton(R.string.deny, null)
                .create();
    }

    private DialogInterface.OnClickListener yesPressed = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mInterfaceCommunicator.sendResultCode(true);
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        mInterfaceCommunicator = null;
    }
}
