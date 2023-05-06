package com.ixxc.uiot;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ixxc.uiot.Adapter.BottomSheetAdapter;
import com.ixxc.uiot.Model.Attribute;
import com.ixxc.uiot.Model.Device;
import com.ixxc.uiot.Model.Map;
import com.mapbox.maps.CameraBoundsOptions;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.attribution.AttributionPlugin;
import com.mapbox.maps.plugin.logo.LogoPlugin;
import com.mapbox.maps.plugin.scalebar.ScaleBarPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment {
    HomeActivity parentActivity;
    private MapView mapView;
    RecyclerView rv_attributes;
    public String lastSelectedId = "";
    private boolean firstTime = true;

    public MapsFragment() { }

    public MapsFragment(HomeActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InitViews(view);
        InitEvents();

        mapView.setVisibility(View.INVISIBLE);

        new Thread(() -> {
            while (!Map.isReady) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            parentActivity.runOnUiThread(this::setMapView);

        }).start();
    }

    private void InitViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        rv_attributes = view.findViewById(R.id.rv_attributes);
    }

    private void InitEvents() { }

    private void setMapView() {
        Map mapData = Map.getMapObj();

        // Get the scale bar plugin instance and disable it
        ScaleBarPlugin scaleBarPlugin = mapView.getPlugin(Plugin.MAPBOX_SCALEBAR_PLUGIN_ID);
        assert scaleBarPlugin != null;
        scaleBarPlugin.setEnabled(true);

        // Get the logo plugin instance and disable it
        LogoPlugin logoPlugin = mapView.getPlugin(Plugin.MAPBOX_LOGO_PLUGIN_ID);
        assert logoPlugin != null;
        logoPlugin.setEnabled(false);

        //Get the attribution plugin instance and disable it
        AttributionPlugin attributionPlugin = mapView.getPlugin(Plugin.MAPBOX_ATTRIBUTION_PLUGIN_ID);
        assert attributionPlugin != null;
        attributionPlugin.setEnabled(false);

        MapboxMap mapboxMap = mapView.getMapboxMap();

        // Load style and map data
        mapboxMap.loadStyleJson(Objects.requireNonNull(new Gson().toJson(mapData)), style -> {

            style.removeStyleLayer("poi-level-1");
            style.removeStyleLayer("highway-name-major");

            // Get the annotation plugin instance
            AnnotationPlugin annoPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
            AnnotationConfig annoConfig = new AnnotationConfig("map_annotation");
            PointAnnotationManager pointAnnoManager = (PointAnnotationManager) annoPlugin.createAnnotationManager(AnnotationType.PointAnnotation, annoConfig);

            // Add click listener to the annotation manager
            pointAnnoManager.addClickListener(pointAnnotation -> {
                String id = Objects.requireNonNull(pointAnnotation.getData()).getAsJsonObject().get("id").getAsString();
                setBottomSheet(id);

                return true;
            });

            // Add device markers to the map
            ArrayList<PointAnnotationOptions> markerList = new ArrayList<>();

            for (Device device : Device.getDevicesList()) {
                if (device.getPoint() == null) continue;

                Bitmap bitmap = device.getIconPinBitmap(parentActivity);

                JsonObject o = new JsonObject();
                o.addProperty("id", device.id);
                PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                        .withPoint(device.getPoint())
                        .withData(o)
                        .withIconImage(bitmap);
                markerList.add(pointAnnotationOptions);
            }

            pointAnnoManager.create(markerList);
        });

        // Set camera position
        mapboxMap.setCamera(
                new CameraOptions.Builder()
                        .center(mapData.getCenter())
                        .zoom(mapData.getZoom())
                        .build()
        );

        // Set camera bounds
        mapboxMap.setBounds(
                new CameraBoundsOptions.Builder()
                        .minZoom(mapData.getMinZoom())
                        .maxZoom(mapData.getMaxZoom())
                        .bounds(mapData.getBounds())
                        .build()
        );

        // Set camera animation
        CameraAnimationsPlugin cameraAnimationsPlugin = mapView.getPlugin(Plugin.MAPBOX_CAMERA_PLUGIN_ID);
        assert cameraAnimationsPlugin != null;

        if (!isHidden()) onHiddenChanged(false);
    }

    private void setBottomSheet(@NonNull String deviceId) {
        Device device = Device.getDeviceById(deviceId);

        if (device != null) {
            Dialog dialog = new Dialog(parentActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottom_sheet);

            List<Attribute> attributes = device.getRequiredAttributes();
            BottomSheetAdapter adapter = new BottomSheetAdapter(attributes);
            LinearLayoutManager layoutManager =  new LinearLayoutManager(parentActivity);

            TextView tvAssetName = dialog.findViewById(R.id.tv_assetName);
            ImageView ivIcon = dialog.findViewById(R.id.iv_assetIcon);
            ImageView iv_go = dialog.findViewById(R.id.iv_go_1);
            Button btn_chart = dialog.findViewById(R.id.btn_chart);
            RecyclerView rv_attributes = dialog.findViewById(R.id.rv_attributes);

            iv_go.setOnClickListener(view -> {
                // TODO: Change start activity animation
                Intent intent = new Intent(parentActivity, DeviceInfoActivity.class);
                intent.putExtra("DEVICE_ID", deviceId);
                parentActivity.startActivity(intent);
                dialog.dismiss();
            });

            btn_chart.setOnClickListener(view -> {
                // TODO: Change start activity animation
                Intent intent = new Intent(parentActivity, ChartActivity.class);
                intent.putExtra("DEVICE_ID", deviceId);
                startActivity(intent);
                dialog.dismiss();
            });

            tvAssetName.setText(device.name);
            ivIcon.setImageResource(device.getIconRes());

            rv_attributes.setHasFixedSize(true);
            rv_attributes.setLayoutManager(layoutManager);
            rv_attributes.setAdapter(adapter);

            if (attributes.size() > 8) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(parentActivity, 480));
            } else {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
        }

        lastSelectedId = deviceId;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (firstTime && Map.isReady && !hidden) {
            firstTime = false;
            Utils.delayHandler.postDelayed(() -> mapView.setVisibility(View.VISIBLE), 200);
        }
        super.onHiddenChanged(hidden);
    }
}