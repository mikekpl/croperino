package com.mikelau.croperino;

import android.content.DialogInterface;

public class AlertInterface {

    public interface WithNeutral {
        void PositiveMethod(DialogInterface dialog, int id);
        void NeutralMethod(DialogInterface dialog, int id);
        void NegativeMethod(DialogInterface dialog, int id);
    }
}
