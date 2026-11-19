package com.autoloop.studio;

import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.InputType;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.transformer.*;
import androidx.media3.effect.Presentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    Button videoButton, musicButton, exportButton;
    TextView videoText, musicText, statusText;
    EditText durationInput;
    Spinner qualitySpinner;

    Uri videoUri, musicUri;
    long videoDuration = 0;

    Transformer transformer;

    ActivityResultLauncher<String[]> videoPicker;
    ActivityResultLauncher<String[]> musicPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(0xFFFFFFFF);

        TextView title = new TextView(this);
        title.setText("AutoLoop Studio");
        title.setTextSize(28);
        title.setTextColor(0xFF000000);
        layout.addView(title);

        videoButton = new Button(this);
        videoButton.setText("Select Video");
        layout.addView(videoButton);

        videoText = new TextView(this);
        videoText.setText("No video selected");
        layout.addView(videoText);

        musicButton = new Button(this);
        musicButton.setText("Select Background Music");
        layout.addView(musicButton);

        musicText = new TextView(this);
        musicText.setText("No music selected");
        layout.addView(musicText);

        TextView durationLabel = new TextView(this);
        durationLabel.setText("Enter Video Duration (HH:MM:SS)");
        layout.addView(durationLabel);

        durationInput = new EditText(this);
        durationInput.setHint("Example: 01:13:00");
        durationInput.setSingleLine(true);
        durationInput.setInputType(InputType.TYPE_CLASS_DATETIME);
        layout.addView(durationInput);

        TextView qualityLabel = new TextView(this);
        qualityLabel.setText("Select Quality");
        layout.addView(qualityLabel);

        qualitySpinner = new Spinner(this);
        qualitySpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"720p", "1080p"}
        ));
        layout.addView(qualitySpinner);

        exportButton = new Button(this);
        exportButton.setText("START EXPORT");
        layout.addView(exportButton);

        statusText = new TextView(this);
        statusText.setText("Ready");
        layout.addView(statusText);

        setContentView(layout);