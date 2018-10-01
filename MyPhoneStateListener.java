package com.example.lucianolimina.signalmap;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.widget.TextView;

public class MyPhoneStateListener extends PhoneStateListener {

    private int signalPower;
    private TextView signalPowertText;


    public void setSignalPowertText(TextView signalPowertText) {
        this.signalPowertText = signalPowertText;
    }

    public int getSignalPower() {
        return signalPower;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        signalPower = signalStrength.getGsmSignalStrength();
        signalPowertText.setText("" + signalPower);



    }
}
