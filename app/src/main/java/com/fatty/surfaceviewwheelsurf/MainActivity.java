package com.fatty.surfaceviewwheelsurf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private WheelSurf mWheelSurf;
    private ImageView btnStart;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWheelSurf = (WheelSurf) findViewById(R.id.id_wheelsurf);
        btnStart = (ImageView) findViewById(R.id.iv_btn_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mWheelSurf.stillTurning()){
                    //永远中ipad
                    mWheelSurf.wheelSurfStart(1);
                    btnStart.setImageResource(R.drawable.stop);
                } else {
                    if (!mWheelSurf.isEnding()) {
                        mWheelSurf.wheelSurfStop();
                        btnStart.setImageResource(R.drawable.start);
                    }
                }
            }
        });
    }
}
