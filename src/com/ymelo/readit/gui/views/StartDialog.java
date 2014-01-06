package com.ymelo.readit.gui.views;



import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ymelo.readit.R;

public class StartDialog extends DialogFragment {
    int mNum;

    /**
     * Create a new instance of StartDialog, providing "num"
     * as an argument.
     */
    public static DialogFragment newInstance(int num) {
        StartDialog f = new StartDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.start_dialog_fragment, container, false);
        View tv = v.findViewById(R.id.start_dialog_tv);
        ((TextView)tv).setText(getString(R.string.start_dialog));
        return v;
    }
}
