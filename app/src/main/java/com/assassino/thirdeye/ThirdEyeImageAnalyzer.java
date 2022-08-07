package com.assassino.thirdeye;

import android.media.Image;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

class ThirdEyeImageAnalyzer implements ImageAnalysis.Analyzer {
    private final FirebaseFunctions functions = FirebaseFunctions.getInstance();
    private final MainActivity mainActivity;

    ThirdEyeImageAnalyzer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (this.mainActivity.isOnlineAllowed()) {
            Toast.makeText(mainActivity, R.string.toast_usingCloud, Toast.LENGTH_SHORT).show();
            analyzeOnline(imageProxy);
        } else {
            analyzeOffline(imageProxy);
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private void analyzeOnline(ImageProxy imageProxy) {
        //Create json request to cloud vision
        JsonObject request = new JsonObject();
        JsonObject image = new JsonObject();
        image.add("content", new JsonPrimitive(BitmapUtils.convertBitmapToHex(BitmapUtils.scaleDownBitmap(BitmapUtils.getBitmap(imageProxy), 640))));
        request.add("image", image);
        JsonObject feature = new JsonObject();
        feature.add("maxResults", new JsonPrimitive(5));
        feature.add("type", new JsonPrimitive("LABEL_DETECTION"));
        JsonArray features = new JsonArray();
        features.add(feature);
        request.add("features", features);

        //Send request
        functions.getHttpsCallable("annotateImage")
            .call(request.toString())
            .continueWith(task -> JsonParser.parseString(new Gson().toJson(task.getResult().getData())))
            .addOnFailureListener(e -> this.mainActivity.updateUIWithResult(false, e.getMessage()))
            .addOnSuccessListener(result -> {
                StringBuilder sb = new StringBuilder();
                for (JsonElement label : result.getAsJsonArray().get(0).getAsJsonObject().get("labelAnnotations").getAsJsonArray()) {
                    JsonObject labelObj = label.getAsJsonObject();
                    sb.append(labelObj.get("description").getAsString())
                        .append(", ");
                }
                this.mainActivity.updateUIWithResult(true, sb.toString());
            })
            .addOnCompleteListener(task -> {
                imageProxy.close();
                this.mainActivity.resetAnalyzer();
            });
    }

    @androidx.camera.core.ExperimentalGetImage
    private void analyzeOffline(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
            labeler.process(image)
                .addOnFailureListener(e -> this.mainActivity.updateUIWithResult(false, e.getMessage()))
                .addOnSuccessListener(labels -> {
                    StringBuilder sb = new StringBuilder();
                    for (ImageLabel label : labels) {
                        sb.append(label.getText())
                            .append(", ");
                    }
                    this.mainActivity.updateUIWithResult(true, sb.toString());
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                    this.mainActivity.resetAnalyzer();
                });
        }
    }
}