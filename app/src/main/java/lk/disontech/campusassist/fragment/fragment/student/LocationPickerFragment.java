package lk.disontech.campusassist.fragment.fragment.student;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.disontech.campusassist.R;

public class LocationPickerFragment extends Fragment implements OnMapReadyCallback {

    public static final String RESULT_KEY = "location_picker_result";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ADDRESS = "address";

    private GoogleMap googleMap;
    private Marker selectedMarker;
    private LatLng selectedLatLng;
    private String selectedAddress = "";

    private TextInputEditText etSearchLocation;
    private TextView tvPickedAddress;
    private MaterialButton btnSearchLocation;
    private MaterialButton btnUseCurrentLocation;
    private MaterialButton btnConfirmLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    moveToCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        etSearchLocation = view.findViewById(R.id.etSearchLocation);
        tvPickedAddress = view.findViewById(R.id.tvPickedAddress);
        btnSearchLocation = view.findViewById(R.id.btnSearchLocation);
        btnUseCurrentLocation = view.findViewById(R.id.btnUseCurrentLocation);
        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);

        topAppBar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnSearchLocation.setOnClickListener(v -> searchAddress());
        btnUseCurrentLocation.setOnClickListener(v -> requestCurrentLocation());
        btnConfirmLocation.setOnClickListener(v -> confirmSelection());

        setupMap();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        geocodeExecutor.shutdownNow();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();
            getChildFragmentManager().executePendingTransactions();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng defaultLocation = new LatLng(6.9271, 79.8612);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        googleMap.setOnMapClickListener(latLng -> setSelectedLocation(latLng, true));
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                setSelectedLocation(marker.getPosition(), false);
            }
        });
    }

    private void setSelectedLocation(@NonNull LatLng latLng, boolean moveCamera) {
        selectedLatLng = latLng;

        if (selectedMarker == null) {
            selectedMarker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        } else {
            selectedMarker.setPosition(latLng);
        }

        if (moveCamera) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }

        reverseGeocode(latLng);
    }

    private void searchAddress() {
        String query = etSearchLocation.getText() != null ? etSearchLocation.getText().toString().trim() : "";
        if (TextUtils.isEmpty(query)) {
            etSearchLocation.setError("Enter a location to search");
            return;
        }

        geocodeExecutor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(query, 1);
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    if (results == null || results.isEmpty()) {
                        Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Address address = results.get(0);
                    setSelectedLocation(new LatLng(address.getLatitude(), address.getLongitude()), true);
                });
            } catch (IOException e) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void moveToCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (location == null) {
                        Toast.makeText(requireContext(), "Unable to fetch current location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    setSelectedLocation(current, true);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reverseGeocode(@NonNull LatLng latLng) {
        geocodeExecutor.execute(() -> {
            String addressText = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (results != null && !results.isEmpty()) {
                    Address address = results.get(0);
                    if (address.getAddressLine(0) != null) {
                        addressText = address.getAddressLine(0);
                    }
                }
            } catch (IOException ignored) {
            }

            if (!isAdded()) {
                return;
            }
            String finalAddressText = addressText;
            requireActivity().runOnUiThread(() -> {
                selectedAddress = finalAddressText;
                tvPickedAddress.setText(finalAddressText);
            });
        });
    }

    private void confirmSelection() {
        if (selectedLatLng == null) {
            Toast.makeText(requireContext(), "Select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle result = new Bundle();
        result.putDouble(KEY_LATITUDE, selectedLatLng.latitude);
        result.putDouble(KEY_LONGITUDE, selectedLatLng.longitude);
        result.putString(KEY_ADDRESS, selectedAddress);

        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}

