package com.autoloop.studio;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.net.Uri;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
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
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.media3.transformer.*;
import androidx.media3.transformer.ProgressHolder;
import androidx.media3.effect.Presentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private final int BG = 0xFF101116;
    private final int CARD = 0xFF1D2029;
    private final int WHITE = 0xFFFFFFFF;
    private final int GRAY = 0xFFADB3C2;
    private final int ACCENT = 0xFF8B5CF6;

    Button videoButton, musicButton, exportButton, cancelButton;

    TextView videoText, musicText, statusText;
    TextView percentText, elapsedText, remainingText;

    EditText durationInput;
    Spinner qualitySpinner;

    ImageView thumbnailView;
    PlayerView playerView;

    ProgressBar exportProgress;

    Uri videoUri, musicUri;

    long videoDuration = 0;
    long exportStartTime = 0;

    Transformer transformer;
    ExoPlayer player;

    Handler handler = new Handler(Looper.getMainLooper());

    boolean exporting = false;

    ActivityResultLauncher<String[]> videoPicker;
    ActivityResultLauncher<String[]> musicPicker;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {

            if (!exporting || transformer == null) {
                return;
            }

            ProgressHolder progressHolder = new ProgressHolder();

            int state = transformer.getProgress(progressHolder);

            if (state == Transformer.PROGRESS_STATE_AVAILABLE) {

                int percent = progressHolder.progress;

                exportProgress.setProgress(percent);
                percentText.setText(percent + "%");

                long elapsed =
                        (SystemClock.elapsedRealtime() - exportStartTime) / 1000;

                elapsedText.setText(
                        "Elapsed: " + formatTime(elapsed)
                );

                if (percent > 0) {

                    long remaining =
                            (elapsed * (100 - percent)) / percent;

                    remainingText.setText(
                            "Remaining: " + formatTime(remaining)
                    );

                } else {

                    remainingText.setText("Remaining: Calculating...");
                }

            }

            handler.postDelayed(this, 500);
        }
    };

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

        TextView brand = createText(
                "AUTOLOOP STUDIO", 13, ACCENT, true
        );
        main.addView(brand);

        TextView title = createText(
                "Create Something\nAmazing.", 29, WHITE, true
        );
        title.setPadding(0, 14, 0, 5);
        main.addView(title);

        TextView subtitle = createText(
                "Your video loop & export studio",
                14, GRAY, false
        );
        main.addView(subtitle);

        addSpace(main, 25);

        addSectionTitle(main, "01  VIDEO SOURCE");

        LinearLayout videoCard = createCard();
        main.addView(videoCard);

        videoCard.addView(createText(
                "🎬  Video Preview", 17, WHITE, true
        ));

        thumbnailView = new ImageView(this);
        thumbnailView.setBackground(createBackground(
                0xFF292D38, 12
        ));
        thumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams imageParams =
                new LinearLayout.LayoutParams(-1, 190);

        imageParams.topMargin = 12;
        imageParams.bottomMargin = 12;

        videoCard.addView(thumbnailView, imageParams);

        playerView = new PlayerView(this);
        playerView.setLayoutParams(
                new LinearLayout.LayoutParams(-1, 200)
        );
        playerView.setUseController(true);
        playerView.setVisibility(View.GONE);

        videoCard.addView(playerView);

        videoText = createText(
                "No video selected", 13, GRAY, false
        );

        videoText.setPadding(0, 10, 0, 12);
        videoCard.addView(videoText);

        videoButton = createButton("＋  Choose Video");
        videoCard.addView(videoButton);

        addSpace(main, 20);

        addSectionTitle(main, "02  BACKGROUND AUDIO");

        LinearLayout musicCard = createCard();
        main.addView(musicCard);

        musicCard.addView(createText(
                "🎵  Background Music", 17, WHITE, true
        ));

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

        settingsCard.addView(createText(
                "Video Duration", 15, WHITE, true
        ));

        durationInput = new EditText(this);

        durationInput.setSingleLine(true);
        durationInput.setHint("HH:MM:SS  (01:13:00)");
        durationInput.setHintTextColor(0xFF777D8B);
        durationInput.setTextColor(WHITE);
        durationInput.setTextSize(15);

        durationInput.setInputType(
                InputType.TYPE_CLASS_DATETIME |
                InputType.TYPE_DATETIME_VARIATION_TIME
        );

        durationInput.setPadding(15, 12, 15, 12);

        durationInput.setBackground(
                createBackground(0xFF292D38, 12)
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(-1, -2);

        inputParams.topMargin = 10;
        inputParams.bottomMargin = 22;

        settingsCard.addView(durationInput, inputParams);

        settingsCard.addView(createText(
                "Export Resolution", 15, WHITE, true
        ));

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

        settingsCard.addView(qualitySpinner);

        addSpace(main, 25);

        exportButton = createButton("▶  START EXPORT");
        exportButton.setTextSize(17);
        exportButton.setPadding(10, 17, 10, 17);

        main.addView(exportButton);

        addSpace(main, 15);

        LinearLayout progressCard = createCard();
        main.addView(progressCard);

        progressCard.addView(createText(
                "EXPORT PROGRESS", 13, ACCENT, true
        ));

        percentText = createText(
                "0%", 30, WHITE, true
        );

        percentText.setGravity(Gravity.CENTER);
        percentText.setPadding(0, 15, 0, 10);

        progressCard.addView(percentText);

        exportProgress = new ProgressBar(
                this, null,
                android.R.attr.progressBarStyleHorizontal
        );

        exportProgress.setMax(100);
        exportProgress.setProgress(0);

        progressCard.addView(exportProgress);

        elapsedText = createText(
                "Elapsed: 00:00", 13, GRAY, false
        );

        elapsedText.setPadding(0, 15, 0, 5);
        progressCard.addView(elapsedText);

        remainingText = createText(
                "Remaining: --:--", 13, GRAY, false
        );

        progressCard.addView(remainingText);

        cancelButton = createButton("✕  CANCEL EXPORT");
        cancelButton.setVisibility(View.GONE);

        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(-1, -2);

        cancelParams.topMargin = 15;

        progressCard.addView(cancelButton, cancelParams);

        addSpace(main, 15);

        LinearLayout statusCard = createCard();
        main.addView(statusCard);

        statusCard.addView(createText(
                "EXPORT STATUS", 12, ACCENT, true
        ));

        statusText = createText(
                "● Ready to export", 14, WHITE, false
        );

        statusText.setPadding(0, 9, 0, 0);
        statusCard.addView(statusText);

        TextView footer = createText(
                "AUTOLOOP STUDIO • VERSION 3.0",
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

        cancelButton.setOnClickListener(v -> cancelExport());
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

    private String formatTime(long seconds) {

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format(
                    Locale.US, "%02d:%02d:%02d",
                    hours, minutes, secs
            );
        }

        return String.format(
                Locale.US, "%02d:%02d",
                minutes, secs
        );
    }

    private void setupPickers() {

        videoPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {

                    if (uri == null) return;

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

                        Bitmap thumbnail =
                                retriever.getFrameAtTime(
                                        1000000,
                                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                );

                        retriever.release();

                        if (thumbnail != null) {
                            thumbnailView.setImageBitmap(thumbnail);
                        }

                        videoText.setText(
                                "✓ Video selected • " +
                                formatTime(videoDuration / 1000)
                        );

                        playerView.setVisibility(View.VISIBLE);

                        thumbnailView.setVisibility(View.GONE);

                        if (player != null) {
                            player.release();
                        }

                        player = new ExoPlayer.Builder(this).build();

                        playerView.setPlayer(player);

                        player.setMediaItem(
                                MediaItem.fromUri(videoUri)
                        );

                        player.prepare();

                        player.setPlayWhenReady(false);

                        statusText.setText("● Video ready");

                    } catch (Exception e) {

                        statusText.setText(
                                "Could not load video: " + e.getMessage()
                        );
                    }
                }
        );

        musicPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {

                    if (uri == null) return;

                    musicUri = uri;

                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {}

                    musicText.setText("✓ Background music selected");
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

        if (exporting) return;

        if (videoUri == null || videoDuration <= 0) {

            statusText.setText(
                    "● Please select a valid video"
            );

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
            cancelButton.setVisibility(View.VISIBLE);

            exportProgress.setProgress(0);
            percentText.setText("0%");

            elapsedText.setText("Elapsed: 00:00");
            remainingText.setText("Remaining: Calculating...");

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

                                exporting = false;

                                handler.removeCallbacks(progressRunnable);

                                exportProgress.setProgress(100);
                                percentText.setText("100%");

                                statusText.setText(
                                        "✓ Export completed!\n" +
                                        output.getAbsolutePath()
                                );

                                remainingText.setText(
                                        "Remaining: Completed"
                                );

                                exportButton.setEnabled(true);
                                cancelButton.setVisibility(View.GONE);
                            });
                        }

                        @Override
                        public void onError(
                                Composition composition,
                                ExportResult result,
                                ExportException exception
                        ) {

                            runOnUiThread(() -> {

                                exporting = false;

                                handler.removeCallbacks(progressRunnable);

                                statusText.setText(
                                        "Export failed: " +
                                        exception.getMessage()
                                );

                                exportButton.setEnabled(true);
                                cancelButton.setVisibility(View.GONE);
                            });
                        }
                    })
                    .build();

            exporting = true;

            exportStartTime = SystemClock.elapsedRealtime();

            statusText.setText("● Export started...");

            transformer.start(
                    composition,
                    output.getAbsolutePath()
            );

            handler.post(progressRunnable);

        } catch (Exception e) {

            exporting = false;

            statusText.setText(
                    "Error: " + e.getMessage()
            );

            exportButton.setEnabled(true);
            cancelButton.setVisibility(View.GONE);
        }
    }

    private void cancelExport() {

        if (transformer != null && exporting) {

            transformer.cancel();

            exporting = false;

            handler.removeCallbacks(progressRunnable);

            statusText.setText("● Export cancelled");

            exportButton.setEnabled(true);
            cancelButton.setVisibility(View.GONE);

            remainingText.setText("Remaining: Cancelled");
        }
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(progressRunnable);

        if (player != null) {
            player.release();
            player = null;
        }

        super.onDestroy();
    }
}