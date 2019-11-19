package edu.uvm.cs275.conversationanalysis;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmationFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_confirmation)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.deny, null)
                .create();
    }
}
