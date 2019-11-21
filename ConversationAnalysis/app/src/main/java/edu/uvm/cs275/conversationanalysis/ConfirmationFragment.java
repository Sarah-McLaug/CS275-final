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
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_confirmation)
                .setPositiveButton(R.string.confirm, yesPressed)
                .setNegativeButton(R.string.deny, null)
                .create();
    }

    private DialogInterface.OnClickListener yesPressed = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            sendResult(Activity.RESULT_OK, true);
        }
    };

    private void sendResult(int resultCode, Boolean bool) {
        //if (getTargetFragment() == null) {
        //    return;
        //}

        Intent intent = new Intent();
        intent.putExtra(EXTRA_BOOL, true);

        onActivityResult(getTargetRequestCode(), resultCode, intent);
        //getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
