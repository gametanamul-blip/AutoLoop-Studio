package com.autoloop.studio.editor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * AutoLoop Studio
 * Core Video Editing Project Model
 *
 * This class manages:
 * - Video clips
 * - Audio tracks
 * - Text layers
 * - Image overlays
 * - Timeline positions
 * - Project settings
 * - Clip operations
 */

public class EditorProject {

    // ==========================================
    // PROJECT INFORMATION
    // ==========================================

    private final String projectId;

    private String projectName;

    private long createdAt;
    private long modifiedAt;

    private int width;
    private int height;

    private int frameRate;

    private long durationUs;

    private boolean saved;

    // ==========================================
    // PROJECT COLLECTIONS
    // ==========================================

    private final List<VideoClip> videoClips;

    private final List<AudioTrack> audioTracks;

    private final List<TextLayer> textLayers;

    private final List<OverlayLayer> overlayLayers;

    // ==========================================
    // PROJECT CONSTRUCTOR
    // ==========================================

    public EditorProject() {

        projectId = UUID.randomUUID().toString();

        projectName = "Untitled Project";

        createdAt = System.currentTimeMillis();

        modifiedAt = createdAt;

        width = 1920;

        height = 1080;

        frameRate = 30;

        durationUs = 0;

        saved = false;

        videoClips = new ArrayList<>();

        audioTracks = new ArrayList<>();

        textLayers = new ArrayList<>();

        overlayLayers = new ArrayList<>();
    }

    // ==========================================
    // PROJECT ID
    // ==========================================

    public String getProjectId() {

        return projectId;
    }

    // ==========================================
    // PROJECT NAME
    // ==========================================

    public String getProjectName() {

        return projectName;
    }

    public void setProjectName(String name) {

        if (name == null || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Project name cannot be empty"
            );
        }

        projectName = name.trim();

        markModified();
    }

    // ==========================================
    // PROJECT TIME
    // ==========================================

    public long getCreatedAt() {

        return createdAt;
    }

    public long getModifiedAt() {

        return modifiedAt;
    }

    public void markModified() {

        modifiedAt = System.currentTimeMillis();

        saved = false;
    }

    // ==========================================
    // PROJECT SAVE STATUS
    // ==========================================

    public boolean isSaved() {

        return saved;
    }

    public void setSaved(boolean value) {

        saved = value;
    }

    // ==========================================
    // VIDEO RESOLUTION
    // ==========================================

    public int getWidth() {

        return width;
    }

    public int getHeight() {

        return height;
    }

    public void setResolution(int newWidth, int newHeight) {

        if (newWidth <= 0 || newHeight <= 0) {

            throw new IllegalArgumentException(
                    "Invalid video resolution"
            );
        }

        width = newWidth;

        height = newHeight;

        markModified();
    }

    // ==========================================
    // FRAME RATE
    // ==========================================

    public int getFrameRate() {

        return frameRate;
    }

    public void setFrameRate(int fps) {

        if (fps < 1 || fps > 240) {

            throw new IllegalArgumentException(
                    "Unsupported frame rate"
            );
        }

        frameRate = fps;

        markModified();
    }

    // ==========================================
    // PROJECT DURATION
    // ==========================================

    public long getDurationUs() {

        return durationUs;
    }

    public long getDurationMs() {

        return durationUs / 1000L;
    }

    public void setDurationUs(long duration) {

        if (duration < 0) {

            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }

        durationUs = duration;

        markModified();
    }

    // ==========================================
    // VIDEO CLIPS
    // ==========================================

    public List<VideoClip> getVideoClips() {

        return Collections.unmodifiableList(
                videoClips
        );
    }

    public int getVideoClipCount() {

        return videoClips.size();
    }

    public void addVideoClip(VideoClip clip) {

        if (clip == null) {

            throw new IllegalArgumentException(
                    "Video clip cannot be null"
            );
        }

        videoClips.add(clip);

        updateProjectDuration();

        markModified();
    }

    public boolean removeVideoClip(String clipId) {

        boolean removed = false;

        for (int i = videoClips.size() - 1; i >= 0; i--) {

            if (videoClips.get(i).getId().equals(clipId)) {

                videoClips.remove(i);

                removed = true;
            }
        }

        if (removed) {

            updateProjectDuration();

            markModified();
        }

        return removed;
    }

    public VideoClip findVideoClip(String clipId) {

        for (VideoClip clip : videoClips) {

            if (clip.getId().equals(clipId)) {

                return clip;
            }
        }

        return null;
    }

    public void moveVideoClip(int from, int to) {

        if (from < 0 || from >= videoClips.size()) {

            throw new IndexOutOfBoundsException(
                    "Invalid source position"
            );
        }

        if (to < 0 || to >= videoClips.size()) {

            throw new IndexOutOfBoundsException(
                    "Invalid destination position"
            );
        }

        VideoClip clip = videoClips.remove(from);

        videoClips.add(to, clip);

        markModified();
    }

    // ==========================================
    // AUDIO TRACKS
    // ==========================================

    public List<AudioTrack> getAudioTracks() {

        return Collections.unmodifiableList(
                audioTracks
        );
    }

    public void addAudioTrack(AudioTrack track) {

        if (track == null) {

            throw new IllegalArgumentException(
                    "Audio track cannot be null"
            );
        }

        audioTracks.add(track);

        markModified();
    }

    public boolean removeAudioTrack(String trackId) {

        for (int i = audioTracks.size() - 1; i >= 0; i--) {

            if (audioTracks.get(i).getId().equals(trackId)) {

                audioTracks.remove(i);

                markModified();

                return true;
            }
        }

        return false;
    }

    // ==========================================
    // TEXT LAYERS
    // ==========================================

    public List<TextLayer> getTextLayers() {

        return Collections.unmodifiableList(
                textLayers
        );
    }

    public void addTextLayer(TextLayer layer) {

        if (layer == null) {

            throw new IllegalArgumentException(
                    "Text layer cannot be null"
            );
        }

        textLayers.add(layer);

        markModified();
    }

    public boolean removeTextLayer(String layerId) {

        for (int i = textLayers.size() - 1; i >= 0; i--) {

            if (textLayers.get(i).getId().equals(layerId)) {

                textLayers.remove(i);

                markModified();

                return true;
            }
        }

        return false;
    }

    // ==========================================
    // IMAGE OVERLAYS
    // ==========================================

    public List<OverlayLayer> getOverlayLayers() {

        return Collections.unmodifiableList(
                overlayLayers
        );
    }

    public void addOverlayLayer(OverlayLayer layer) {

        if (layer == null) {

            throw new IllegalArgumentException(
                    "Overlay cannot be null"
            );
        }

        overlayLayers.add(layer);

        markModified();
    }

    public boolean removeOverlayLayer(String layerId) {

        for (int i = overlayLayers.size() - 1; i >= 0; i--) {

            if (overlayLayers.get(i).getId().equals(layerId)) {

                overlayLayers.remove(i);

                markModified();

                return true;
            }
        }

        return false;
    }

    // ==========================================
    // TIMELINE DURATION
    // ==========================================

    public void updateProjectDuration() {

        long maximumDuration = 0;

        for (VideoClip clip : videoClips) {

            long end = clip.getTimelineEndUs();

            if (end > maximumDuration) {

                maximumDuration = end;
            }
        }

        for (AudioTrack track : audioTracks) {

            long end = track.getTimelineEndUs();

            if (end > maximumDuration) {

                maximumDuration = end;
            }
        }

        for (TextLayer layer : textLayers) {

            long end = layer.getTimelineEndUs();

            if (end > maximumDuration) {

                maximumDuration = end;
            }
        }

        durationUs = maximumDuration;
    }

    // ==========================================
    // SORT TIMELINE
    // ==========================================

    public void sortVideoTimeline() {

        Collections.sort(
                videoClips,
                Comparator.comparingLong(
                        VideoClip::getTimelineStartUs
                )
        );

        markModified();
    }

    public void sortAudioTimeline() {

        Collections.sort(
                audioTracks,
                Comparator.comparingLong(
                        AudioTrack::getTimelineStartUs
                )
        );

        markModified();
    }

    // ==========================================
    // CLEAR PROJECT
    // ==========================================

    public void clearProject() {

        videoClips.clear();

        audioTracks.clear();

        textLayers.clear();

        overlayLayers.clear();

        durationUs = 0;

        markModified();
    }

    // ==========================================
    // VIDEO CLIP MODEL
    // ==========================================

    public static class VideoClip {

        private final String id;

        private String uri;

        private long sourceStartUs;

        private long sourceEndUs;

        private long timelineStartUs;

        private float volume;

        private float speed;

        private int rotation;

        private boolean muted;

        private boolean reversed;

        private boolean enabled;

        public VideoClip(String uri) {

            if (uri == null || uri.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Video URI is required"
                );
            }

            id = UUID.randomUUID().toString();

            this.uri = uri;

            sourceStartUs = 0;

            sourceEndUs = -1;

            timelineStartUs = 0;

            volume = 1.0f;

            speed = 1.0f;

            rotation = 0;

            muted = false;

            reversed = false;

            enabled = true;
        }

        public String getId() {

            return id;
        }

        public String getUri() {

            return uri;
        }

        public void setUri(String uri) {

            this.uri = uri;
        }

        public long getSourceStartUs() {

            return sourceStartUs;
        }

        public void setSourceStartUs(long value) {

            if (value < 0) {

                throw new IllegalArgumentException(
                        "Start time cannot be negative"
                );
            }

            sourceStartUs = value;
        }

        public long getSourceEndUs() {

            return sourceEndUs;
        }

        public void setSourceEndUs(long value) {

            if (value != -1 && value <= sourceStartUs) {

                throw new IllegalArgumentException(
                        "End time must be after start time"
                );
            }

            sourceEndUs = value;
        }

        public long getTimelineStartUs() {

            return timelineStartUs;
        }

        public void setTimelineStartUs(long value) {

            if (value < 0) {

                throw new IllegalArgumentException(
                        "Timeline position cannot be negative"
                );
            }

            timelineStartUs = value;
        }

        public float getVolume() {

            return volume;
        }

        public void setVolume(float value) {

            volume = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public float getSpeed() {

            return speed;
        }

        public void setSpeed(float value) {

            if (value <= 0 || value > 16f) {

                throw new IllegalArgumentException(
                        "Invalid playback speed"
                );
            }

            speed = value;
        }

        public int getRotation() {

            return rotation;
        }

        public void setRotation(int value) {

            rotation = ((value % 360) + 360) % 360;
        }

        public boolean isMuted() {

            return muted;
        }

        public void setMuted(boolean value) {

            muted = value;
        }

        public boolean isReversed() {

            return reversed;
        }

        public void setReversed(boolean value) {

            reversed = value;
        }

        public boolean isEnabled() {

            return enabled;
        }

        public void setEnabled(boolean value) {

            enabled = value;
        }

        public long getTrimmedDurationUs() {

            if (sourceEndUs == -1) {

                return 0;
            }

            long sourceDuration =
                    sourceEndUs - sourceStartUs;

            return (long) (sourceDuration / speed);
        }

        public long getTimelineEndUs() {

            return timelineStartUs + getTrimmedDurationUs();
        }
    }

    // ==========================================
    // AUDIO TRACK MODEL
    // ==========================================

    public static class AudioTrack {

        private final String id;

        private String uri;

        private long sourceStartUs;

        private long sourceEndUs;

        private long timelineStartUs;

        private float volume;

        private boolean loop;

        private boolean muted;

        private boolean fadeIn;

        private boolean fadeOut;

        public AudioTrack(String uri) {

            if (uri == null || uri.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Audio URI is required"
                );
            }

            id = UUID.randomUUID().toString();

            this.uri = uri;

            sourceStartUs = 0;

            sourceEndUs = -1;

            timelineStartUs = 0;

            volume = 1.0f;

            loop = false;

            muted = false;

            fadeIn = false;

            fadeOut = false;
        }

        public String getId() {

            return id;
        }

        public String getUri() {

            return uri;
        }

        public void setUri(String uri) {

            this.uri = uri;
        }

        public long getSourceStartUs() {

            return sourceStartUs;
        }

        public void setSourceStartUs(long value) {

            sourceStartUs = Math.max(0, value);
        }

        public long getSourceEndUs() {

            return sourceEndUs;
        }

        public void setSourceEndUs(long value) {

            if (value != -1 && value <= sourceStartUs) {

                throw new IllegalArgumentException(
                        "Invalid audio trim range"
                );
            }

            sourceEndUs = value;
        }

        public long getTimelineStartUs() {

            return timelineStartUs;
        }

        public void setTimelineStartUs(long value) {

            timelineStartUs = Math.max(0, value);
        }

        public float getVolume() {

            return volume;
        }

        public void setVolume(float value) {

            volume = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public boolean isLoopEnabled() {

            return loop;
        }

        public void setLoop(boolean value) {

            loop = value;
        }

        public boolean isMuted() {

            return muted;
        }

        public void setMuted(boolean value) {

            muted = value;
        }

        public boolean isFadeInEnabled() {

            return fadeIn;
        }

        public void setFadeIn(boolean value) {

            fadeIn = value;
        }

        public boolean isFadeOutEnabled() {

            return fadeOut;
        }

        public void setFadeOut(boolean value) {

            fadeOut = value;
        }

        public long getTimelineEndUs() {

            if (sourceEndUs == -1) {

                return timelineStartUs;
            }

            return timelineStartUs +
                    (sourceEndUs - sourceStartUs);
        }
    }

    // ==========================================
    // TEXT LAYER MODEL
    // ==========================================

    public static class TextLayer {

        private final String id;

        private String text;

        private long startUs;

        private long endUs;

        private int color;

        private float textSize;

        private float positionX;

        private float positionY;

        private boolean visible;

        public TextLayer(String text) {

            id = UUID.randomUUID().toString();

            this.text = text;

            startUs = 0;

            endUs = 5000000;

            color = 0xFFFFFFFF;

            textSize = 48f;

            positionX = 0.5f;

            positionY = 0.5f;

            visible = true;
        }

        public String getId() {

            return id;
        }

        public String getText() {

            return text;
        }

        public void setText(String value) {

            text = value;
        }

        public long getStartUs() {

            return startUs;
        }

        public void setStartUs(long value) {

            startUs = Math.max(0, value);
        }

        public long getEndUs() {

            return endUs;
        }

        public void setEndUs(long value) {

            if (value <= startUs) {

                throw new IllegalArgumentException(
                        "Invalid text duration"
                );
            }

            endUs = value;
        }

        public int getColor() {

            return color;
        }

        public void setColor(int value) {

            color = value;
        }

        public float getTextSize() {

            return textSize;
        }

        public void setTextSize(float value) {

            textSize = Math.max(8f, value);
        }

        public float getPositionX() {

            return positionX;
        }

        public void setPositionX(float value) {

            positionX = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public float getPositionY() {

            return positionY;
        }

        public void setPositionY(float value) {

            positionY = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public boolean isVisible() {

            return visible;
        }

        public void setVisible(boolean value) {

            visible = value;
        }

        public long getTimelineEndUs() {

            return endUs;
        }
    }

    // ==========================================
    // OVERLAY LAYER MODEL
    // ==========================================

    public static class OverlayLayer {

        private final String id;

        private String uri;

        private long startUs;

        private long endUs;

        private float scale;

        private float positionX;

        private float positionY;

        private float opacity;

        private boolean visible;

        public OverlayLayer(String uri) {

            id = UUID.randomUUID().toString();

            this.uri = uri;

            startUs = 0;

            endUs = 5000000;

            scale = 1.0f;

            positionX = 0.5f;

            positionY = 0.5f;

            opacity = 1.0f;

            visible = true;
        }

        public String getId() {

            return id;
        }

        public String getUri() {

            return uri;
        }

        public void setUri(String value) {

            uri = value;
        }

        public long getStartUs() {

            return startUs;
        }

        public void setStartUs(long value) {

            startUs = Math.max(0, value);
        }

        public long getEndUs() {

            return endUs;
        }

        public void setEndUs(long value) {

            if (value <= startUs) {

                throw new IllegalArgumentException(
                        "Invalid overlay duration"
                );
            }

            endUs = value;
        }

        public float getScale() {

            return scale;
        }

        public void setScale(float value) {

            scale = Math.max(0.1f, Math.min(5f, value));
        }

        public float getPositionX() {

            return positionX;
        }

        public void setPositionX(float value) {

            positionX = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public float getPositionY() {

            return positionY;
        }

        public void setPositionY(float value) {

            positionY = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public float getOpacity() {

            return opacity;
        }

        public void setOpacity(float value) {

            opacity = Math.max(
                    0f,
                    Math.min(1f, value)
            );
        }

        public boolean isVisible() {

            return visible;
        }

        public void setVisible(boolean value) {

            visible = value;
        }

        public long getTimelineEndUs() {

            return endUs;
        }
    }
}