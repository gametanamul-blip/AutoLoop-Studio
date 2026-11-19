package com.autoloop.studio;

import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
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
    Spinner durationSpinner, qualitySpinner;

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
        durationLabel.setText("Select Video Duration");
        layout.addView(durationLabel);

        durationSpinner = new Spinner(this);
        durationSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"1 Hour", "2 Hours"}
        ));
        layout.addView(durationSpinner);

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

        videoPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        videoUri = uri;
                        videoText.setText("Video selected");

                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                            MediaMetadataRetriever retriever =
                                    new MediaMetadataRetriever();

                            retriever.setDataSource(this, uri);

                            String duration = retriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_DURATION
                            );

                            videoDuration = Long.parseLong(duration);
                            retriever.release();

                        } catch (Exception e) {
                            videoDuration = 0;
                            statusText.setText(
                                    "Could not read video duration"
                            );
                        }
                    }
                }
        );

        musicPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        musicUri = uri;
                        musicText.setText("Music selected");

                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {}
                    }
                }
        );

        videoButton.setOnClickListener(v ->
                videoPicker.launch(new String[]{"video/*"})
        );

        musicButton.setOnClickListener(v ->
                musicPicker.launch(new String[]{"audio/*"})
        );

        exportButton.setOnClickListener(v -> startExport());
    }

    private void startExport() {

        if (videoUri == null || videoDuration <= 0) {
            statusText.setText("Please select a valid video first");
            return;
        }

        try {
            exportButton.setEnabled(false);
            statusText.setText("Preparing video...");

            long targetDuration =
                    durationSpinner.getSelectedItemPosition() == 0
                    ? 3600000L : 7200000L;

            int height =
                    qualitySpinner.getSelectedItemPosition() == 0
                    ? 720 : 1080;

            int count = (int) (targetDuration / videoDuration);
            long remaining = targetDuration % videoDuration;

            if (remaining > 0) count++;

            if (count > 500) {
                statusText.setText(
                        "Video is too short for this export"
                );
                exportButton.setEnabled(true);
                return;
            }

            ArrayList<EditedMediaItem> videoItems =
                    new ArrayList<>();

            for (int i = 0; i < count; i++) {

                MediaItem.Builder mediaBuilder =
                        new MediaItem.Builder().setUri(videoUri);

                if (i == count - 1 && remaining > 0) {
                    mediaBuilder.setClippingConfiguration(
                            new MediaItem.ClippingConfiguration.Builder()
                                    .setEndPositionMs(remaining)
                                    .build()
                    );
                }

                MediaItem mediaItem = mediaBuilder.build();

                Presentation presentation =
                        Presentation.createForHeight(height);

                Effects effects = new Effects(
                        Collections.emptyList(),
                        Collections.singletonList(presentation)
                );

                EditedMediaItem editedItem =
                        new EditedMediaItem.Builder(mediaItem)
                                .setRemoveAudio(musicUri != null)
                                .setEffects(effects)
                                .build();

                videoItems.add(editedItem);
            }

            EditedMediaItemSequence videoSequence =
                    musicUri == null
                    ? EditedMediaItemSequence.withAudioAndVideoFrom(videoItems)
                    : EditedMediaItemSequence.withVideoFrom(videoItems);

            Composition composition;

            if (musicUri != null) {

                EditedMediaItem musicItem =
                        new EditedMediaItem.Builder(
                                MediaItem.fromUri(musicUri)
                        ).build();

                EditedMediaItemSequence musicSequence =
                        EditedMediaItemSequence.withAudioFrom(
                                Collections.singletonList(musicItem)
                        ).buildUpon()
                                .setIsLooping(true)
                                .build();

                composition = new Composition.Builder(
                        videoSequence, musicSequence
                ).build();

            } else {
                composition = new Composition.Builder(
                        videoSequence
                ).build();
            }

            File folder = getExternalFilesDir(
                    Environment.DIRECTORY_MOVIES
            );

            if (folder == null) {
                throw new Exception("Storage unavailable");
            }

            File output = new File(
                    folder,
                    "AutoLoop_" + System.currentTimeMillis() + ".mp4"
            );

            transformer = new Transformer.Builder(this)
                    .addListener(new Transformer.Listener() {

                        @Override
                        public void onCompleted(
                                Composition composition,
                                ExportResult result
                        ) {
                            runOnUiThread(() -> {
                                statusText.setText(
                                        "Export completed!\nSaved in:\n"
                                                + output.getAbsolutePath()
                                );
                                exportButton.setEnabled(true);
                            });
                        }

                        @Override
                        public void onError(
                                Composition composition,
                                ExportResult result,
                                ExportException exception
                        ) {
                            runOnUiThread(() -> {
                                statusText.setText(
                                        "Export failed: "
                                                + exception.getMessage()
                                );
                                exportButton.setEnabled(true);
                            });
                        }
                    })
                    .build();

            statusText.setText("Export started. Please wait...");

            transformer.start(
                    composition,
                    output.getAbsolutePath()
            );

        } catch (Exception e) {
            statusText.setText("Error: " + e.getMessage());
            exportButton.setEnabled(true);
        }
    }
}