package com.assassino.thirdeye;

import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;
import android.widget.RadioGroup;
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
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

class ThirdEyeImageAnalyzer implements ImageAnalysis.Analyzer {
    private final FirebaseFunctions functions = FirebaseFunctions.getInstance();
    private final MainActivity mainActivity;

    ThirdEyeImageAnalyzer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        RadioGroup radioGrpAnalysisMode = mainActivity.findViewById(R.id.radioGrpAnalysisMode);

        if (radioGrpAnalysisMode.getCheckedRadioButtonId() == R.id.radioLabelScene) {
            if (this.mainActivity.isOnlineAllowed()) {
                Toast.makeText(mainActivity, R.string.toast_usingCloud, Toast.LENGTH_SHORT).show();
                labelSceneOnline(imageProxy);
            } else {
                labelSceneOffline(imageProxy);
            }
        } else if (radioGrpAnalysisMode.getCheckedRadioButtonId() == R.id.radioReadText) {
            recognizeTextOffline(imageProxy);
        }

    }

    @androidx.camera.core.ExperimentalGetImage
    private void labelSceneOnline(@NonNull ImageProxy imageProxy) {
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
    private void labelSceneOffline(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            Log.d("THIRD_EYE", String.valueOf(imageProxy.getImageInfo().getRotationDegrees()));

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

    @androidx.camera.core.ExperimentalGetImage
    private void recognizeTextOffline(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            Log.d("THIRD_EYE", String.valueOf(imageProxy.getImageInfo().getRotationDegrees()));

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                .addOnFailureListener(e -> this.mainActivity.updateUIWithResult(false, e.getMessage()))
                .addOnSuccessListener(result -> {
                    StringBuilder sb = new StringBuilder();

                    for (Text.TextBlock block : result.getTextBlocks()) {
                        sb.append(block.getText()).append("\n\n");
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