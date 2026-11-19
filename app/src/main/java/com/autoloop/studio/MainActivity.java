package com.autoloop.studio;

import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
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

    private final int BG = 0xFF101116;
    private final int CARD = 0xFF1D2029;
    private final int WHITE = 0xFFFFFFFF;
    private final int GRAY = 0xFFADB3C2;
    private final int ACCENT = 0xFF8B5CF6;

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

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BG);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(22, 28, 22, 30);

        scrollView.addView(main);
        setContentView(scrollView);

        TextView brand = new TextView(this);
        brand.setText("AUTOLOOP STUDIO");
        brand.setTextColor(ACCENT);
        brand.setTextSize(13);
        brand.setTypeface(null, Typeface.BOLD);
        main.addView(brand);

        TextView title = new TextView(this);
        title.setText("Create Something\nAmazing.");
        title.setTextColor(WHITE);
        title.setTextSize(29);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 14, 0, 5);
        main.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your video loop & export studio");
        subtitle.setTextColor(GRAY);
        subtitle.setTextSize(14);
        main.addView(subtitle);

        addSpace(main, 25);

        addSectionTitle(main, "01  VIDEO SOURCE");

        LinearLayout videoCard = createCard();
        main.addView(videoCard);

        TextView videoHeading = createText(
                "🎬  Select Your Video", 17, WHITE, true
        );
        videoCard.addView(videoHeading);

        videoText = createText(
                "No video selected", 13, GRAY, false
        );
        videoText.setPadding(0, 8, 0, 12);
        videoCard.addView(videoText);

        videoButton = createButton("＋  Choose Video");
        videoCard.addView(videoButton);

        addSpace(main, 20);

        addSectionTitle(main, "02  BACKGROUND AUDIO");

        LinearLayout musicCard = createCard();
        main.addView(musicCard);

        TextView musicHeading = createText(
                "🎵  Background Music", 17, WHITE, true
        );
        musicCard.addView(musicHeading);

        musicText = createText(
                "No music selected", 13, GRAY, false
        );
        musicText.setPadding(0, 8, 0, 12);
        musicCard.addView(musicText);

        musicButton = createButton("＋  Choose Music");
        musicCard.addView(musicButton);

        addSpace(main, 20);

        addSectionTitle(main, "03  EXPORT SETTINGS");

        LinearLayout settingsCard = createCard();
        main.addView(settingsCard);

        TextView durationLabel = createText(
                "Video Duration", 15, WHITE, true
        );
        settingsCard.addView(durationLabel);

        durationInput = new EditText(this);
        durationInput.setSingleLine(true);
        durationInput.setHint("HH:MM:SS  (Example: 01:13:00)");
        durationInput.setHintTextColor(0xFF777D8B);
        durationInput.setTextColor(WHITE);
        durationInput.setTextSize(15);
        durationInput.setInputType(
                InputType.TYPE_CLASS_DATETIME |
                InputType.TYPE_DATETIME_VARIATION_TIME
        );
        durationInput.setPadding(15, 12, 15, 12);
        durationInput.setBackground(createBackground(
                0xFF292D38, 12
        ));

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        -1, -2
                );
        inputParams.topMargin = 10;
        inputParams.bottomMargin = 22;

        settingsCard.addView(durationInput, inputParams);

        TextView qualityLabel = createText(
                "Export Resolution", 15, WHITE, true
        );
        settingsCard.addView(qualityLabel);

        qualitySpinner = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"720p HD", "1080p Full HD"}
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        qualitySpinner.setAdapter(adapter);

        LinearLayout.LayoutParams spinnerParams =
                new LinearLayout.LayoutParams(
                        -1, -2
                );
        spinnerParams.topMargin = 10;

        settingsCard.addView(qualitySpinner, spinnerParams);

        addSpace(main, 25);

        exportButton = createButton("▶  START EXPORT");
        exportButton.setTextSize(17);
        exportButton.setPadding(10, 17, 10, 17);
        exportButton.setBackground(
                createBackground(ACCENT, 16)
        );
        main.addView(exportButton);

        addSpace(main, 15);

        LinearLayout statusCard = createCard();
        main.addView(statusCard);

        TextView statusHeading = createText(
                "EXPORT STATUS", 12, ACCENT, true
        );
        statusCard.addView(statusHeading);

        statusText = createText(
                "●  Ready to export", 14, WHITE, false
        );
        statusText.setPadding(0, 9, 0, 0);
        statusCard.addView(statusText);

        TextView footer = createText(
                "AUTOLOOP STUDIO  •  VERSION 2.0",
                11, GRAY, false
        );
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 25, 0, 0);
        main.addView(footer);

        setupPickers();

        videoButton.setOnClickListener(v ->
                videoPicker.launch(new String[]{"video/*"})
        );

        musicButton.setOnClickListener(v ->
                musicPicker.launch(new String[]{"audio/*"})
        );

        exportButton.setOnClickListener(v -> startExport());
    }

    private LinearLayout createCard() {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(17, 17, 17, 17);
        card.setBackground(createBackground(CARD, 18));

        return card;
    }

    private GradientDrawable createBackground(
            int color, int radius
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private TextView createText(
            String value, int size, int color, boolean bold
    ) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);

        if (bold) {
            text.setTypeface(null, Typeface.BOLD);
        }

        return text;
    }

    private Button createButton(String value) {

        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackground(createBackground(ACCENT, 12));

        return button;
    }

    private void addSectionTitle(
            LinearLayout layout, String value
    ) {
        TextView heading = createText(
                value, 12, GRAY, true
        );

        heading.setPadding(0, 0, 0, 10);
        layout.addView(heading);
    }

    private void addSpace(
            LinearLayout layout, int height
    ) {
        View space = new View(this);
        layout.addView(space,
                new LinearLayout.LayoutParams(1, height));
    }

    private void setupPickers() {

        videoPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        videoUri = uri;

                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                            MediaMetadataRetriever retriever =
                                    new MediaMetadataRetriever();

                            retriever.setDataSource(this, uri);

                            String duration =
                                    retriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_DURATION
                            );

                            videoDuration = Long.parseLong(duration);
                            retriever.release();

                            videoText.setText("✓ Video selected");

                            statusText.setText(
                                    "● Video ready"
                            );

                        } catch (Exception e) {
                            videoDuration = 0;
                            statusText.setText(
                                    "Could not read video"
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

                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {}

                        musicText.setText("✓ Music selected");
                    }
                }
        );
    }

    private long getTargetDuration() {

        String input = durationInput.getText()
                .toString().trim();

        String[] parts = input.split(":");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Enter duration as HH:MM:SS"
            );
        }

        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);

        if (hours < 0 || minutes < 0 || minutes > 59
                || seconds < 0 || seconds > 59) {
            throw new IllegalArgumentException(
                    "Invalid duration"
            );
        }

        long total = hours * 3600 + minutes * 60 + seconds;

        if (total <= 0) {
            throw new IllegalArgumentException(
                    "Duration must be greater than zero"
            );
        }

        return total * 1000L;
    }

    private void startExport() {

        if (videoUri == null || videoDuration <= 0) {
            statusText.setText("● Please select a valid video");
            return;
        }

        try {

            long targetDuration = getTargetDuration();

            int height =
                    qualitySpinner.getSelectedItemPosition() == 0
                    ? 720 : 1080;

            long countLong =
                    (targetDuration + videoDuration - 1)
                    / videoDuration;

            if (countLong > 500) {
                statusText.setText(
                        "Video is too short for this export"
                );
                return;
            }

            int count = (int) countLong;

            exportButton.setEnabled(false);
            statusText.setText("● Preparing video...");

            ArrayList<EditedMediaItem> videoItems =
                    new ArrayList<>();

            for (int i = 0; i < count; i++) {

                long remaining =
                        targetDuration - ((long) i * videoDuration);

                MediaItem.Builder builder =
                        new MediaItem.Builder().setUri(videoUri);

                if (remaining < videoDuration) {
                    builder.setClippingConfiguration(
                            new MediaItem.ClippingConfiguration.Builder()
                                    .setEndPositionMs(remaining)
                                    .build()
                    );
                }

                Presentation presentation =
                        Presentation.createForHeight(height);

                Effects effects = new Effects(
                        Collections.emptyList(),
                        Collections.singletonList(presentation)
                );

                EditedMediaItem item =
                        new EditedMediaItem.Builder(builder.build())
                                .setRemoveAudio(musicUri != null)
                                .setEffects(effects)
                                .build();

                videoItems.add(item);
            }

            EditedMediaItemSequence videoSequence =
                    musicUri == null
                    ? EditedMediaItemSequence.withAudioAndVideoFrom(
                            videoItems
                    )
                    : EditedMediaItemSequence.withVideoFrom(
                            videoItems
                    );

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
                        videoSequence,
                        musicSequence
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
                                        "✓ Export completed!\n"
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

            statusText.setText("● Export started...");

            transformer.start(
                    composition,
                    output.getAbsolutePath()
            );

        } catch (Exception e) {

            statusText.setText(
                    "Error: " + e.getMessage()
            );

            exportButton.setEnabled(true);
        }
    }
}