package lk.disontech.campusassist.fragment.fragment.writer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.WriterEarningsAdapter;
import lk.disontech.campusassist.model.WriterEarningItem;

public class WriterEarningsFragment extends Fragment {

    private MaterialButton btnFromDate;
    private MaterialButton btnToDate;
    private MaterialButton btnClearDates;
    private TextView tvTotalEarnings;
    private TextView tvCompletedCount;
    private TextView tvNoEarnings;
    private ProgressBar progressEarnings;
    private RecyclerView recyclerEarnings;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private WriterEarningsAdapter earningsAdapter;

    private final List<WriterEarningItem> allPaidItems = new ArrayList<>();
    private final List<WriterEarningItem> filteredItems = new ArrayList<>();

    private Long fromDateMillis = null;
    private Long toDateMillis = null;

    private final NumberFormat amountFormat = NumberFormat.getNumberInstance(Locale.US);
    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat receiptDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_writer_earnings, container, false);

        amountFormat.setMinimumFractionDigits(2);
        amountFormat.setMaximumFractionDigits(2);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        btnFromDate = view.findViewById(R.id.btnFromDate);
        btnToDate = view.findViewById(R.id.btnToDate);
        btnClearDates = view.findViewById(R.id.btnClearDates);
        tvTotalEarnings = view.findViewById(R.id.tvTotalEarnings);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvNoEarnings = view.findViewById(R.id.tvNoEarnings);
        progressEarnings = view.findViewById(R.id.progressEarnings);
        recyclerEarnings = view.findViewById(R.id.recyclerEarnings);

        earningsAdapter = new WriterEarningsAdapter();
        recyclerEarnings.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerEarnings.setAdapter(earningsAdapter);

        btnFromDate.setOnClickListener(v -> openDatePicker(true));
        btnToDate.setOnClickListener(v -> openDatePicker(false));
        btnClearDates.setOnClickListener(v -> {
            fromDateMillis = null;
            toDateMillis = null;
            btnFromDate.setText("From Date");
            btnToDate.setText("To Date");
            applyFiltersAndRender();
        });

        loadPaidEarnings();

        return view;
    }

    private void loadPaidEarnings() {
        if (firebaseAuth.getCurrentUser() == null) {
            showLoading(false);
            tvNoEarnings.setVisibility(View.VISIBLE);
            tvNoEarnings.setText("Please login to view earnings.");
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();
        showLoading(true);

        firebaseFirestore.collection("Assignments")
                .whereEqualTo("assignedWriterId", writerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allPaidItems.clear();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        WriterEarningItem item = toPaidEarningItem(documentSnapshot);
                        if (item != null) {
                            allPaidItems.add(item);
                        }
                    }
                    applyFiltersAndRender();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    tvNoEarnings.setVisibility(View.VISIBLE);
                    tvNoEarnings.setText("Failed to load earnings.");
                    Toast.makeText(getContext(), "Failed to load earnings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Nullable
    private WriterEarningItem toPaidEarningItem(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            return null;
        }

        String assignmentStatus = safe(documentSnapshot.getString("status"));
        if (!"Completed".equalsIgnoreCase(assignmentStatus)) {
            return null;
        }

        if (!isPaymentConfirmed(documentSnapshot)) {
            return null;
        }

        double amount = firstNonNullDouble(
                asDouble(documentSnapshot.get("paymentAmount")),
                asDouble(getMapValue(documentSnapshot.get("paymentReceipt"), "paidAmount")),
                asDouble(getMapValue(documentSnapshot.get("paymentVerification"), "amount")),
                0d
        );

        long paidAtMillis = extractPaidAtMillis(documentSnapshot);
        String title = safe(documentSnapshot.getString("title"));
        String studentName = safe(documentSnapshot.getString("studentName"));
        String paymentStatus = resolvePaymentStatus(documentSnapshot);

        return new WriterEarningItem(
                documentSnapshot.getId(),
                title.isEmpty() ? "Untitled Assignment" : title,
                studentName.isEmpty() ? "Unknown Student" : studentName,
                amount,
                paymentStatus,
                paidAtMillis
        );
    }

    private void applyFiltersAndRender() {
        filteredItems.clear();

        for (WriterEarningItem item : allPaidItems) {
            if (matchesDateFilter(item.getPaidAtMillis())) {
                filteredItems.add(item);
            }
        }

        double total = 0d;
        for (WriterEarningItem item : filteredItems) {
            total += item.getAmount();
        }

        tvTotalEarnings.setText("LKR " + amountFormat.format(total));
        tvCompletedCount.setText("Completed Paid Assignments: " + filteredItems.size());

        earningsAdapter.submitList(filteredItems);
        tvNoEarnings.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesDateFilter(long paidAtMillis) {
        if (fromDateMillis == null && toDateMillis == null) {
            return true;
        }
        if (paidAtMillis <= 0L) {
            return false;
        }
        if (fromDateMillis != null && paidAtMillis < fromDateMillis) {
            return false;
        }
        if (toDateMillis != null && paidAtMillis > toDateMillis) {
            return false;
        }
        return true;
    }

    private void openDatePicker(boolean isFrom) {
        Calendar calendar = Calendar.getInstance();
        Long existing = isFrom ? fromDateMillis : toDateMillis;
        if (existing != null && existing > 0L) {
            calendar.setTimeInMillis(existing);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (isFrom) {
                        picked.set(Calendar.HOUR_OF_DAY, 0);
                        picked.set(Calendar.MINUTE, 0);
                        picked.set(Calendar.SECOND, 0);
                        picked.set(Calendar.MILLISECOND, 0);
                        fromDateMillis = picked.getTimeInMillis();
                        btnFromDate.setText(dateOnlyFormat.format(new Date(fromDateMillis)));
                    } else {
                        picked.set(Calendar.HOUR_OF_DAY, 23);
                        picked.set(Calendar.MINUTE, 59);
                        picked.set(Calendar.SECOND, 59);
                        picked.set(Calendar.MILLISECOND, 999);
                        toDateMillis = picked.getTimeInMillis();
                        btnToDate.setText(dateOnlyFormat.format(new Date(toDateMillis)));
                    }

                    applyFiltersAndRender();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private boolean isPaymentConfirmed(DocumentSnapshot documentSnapshot) {
        String topLevelPaymentStatus = safe(documentSnapshot.getString("paymentStatus"));
        String receiptPaymentStatus = safeMapString(documentSnapshot.get("paymentReceipt"), "paymentStatus");
        String verifyStatus = safeMapString(documentSnapshot.get("paymentVerification"), "status");
        Double verifyCode = asDouble(getMapValue(documentSnapshot.get("paymentVerification"), "statusCode"));

        boolean paidLabel = "Paid".equalsIgnoreCase(topLevelPaymentStatus)
                || "Paid".equalsIgnoreCase(receiptPaymentStatus)
                || "RECEIVED".equalsIgnoreCase(verifyStatus)
                || (verifyCode != null && verifyCode.intValue() == 2);

        return paidLabel && extractPaidAtMillis(documentSnapshot) > 0L;
    }

    private String resolvePaymentStatus(DocumentSnapshot documentSnapshot) {
        String topLevelPaymentStatus = safe(documentSnapshot.getString("paymentStatus"));
        if (!topLevelPaymentStatus.isEmpty()) {
            return topLevelPaymentStatus;
        }

        String receiptPaymentStatus = safeMapString(documentSnapshot.get("paymentReceipt"), "paymentStatus");
        if (!receiptPaymentStatus.isEmpty()) {
            return receiptPaymentStatus;
        }

        String verifyStatus = safeMapString(documentSnapshot.get("paymentVerification"), "status");
        if (!verifyStatus.isEmpty()) {
            return verifyStatus;
        }

        return "Paid";
    }

    private long extractPaidAtMillis(DocumentSnapshot documentSnapshot) {
        Long topLevelCompletedAt = documentSnapshot.getLong("paymentCompletedAt");
        if (topLevelCompletedAt != null && topLevelCompletedAt > 0L) {
            return topLevelCompletedAt;
        }

        Long topLevelVerifiedAt = documentSnapshot.getLong("paymentVerifiedAt");
        if (topLevelVerifiedAt != null && topLevelVerifiedAt > 0L) {
            return topLevelVerifiedAt;
        }

        Long receiptTimestamp = asLong(getMapValue(documentSnapshot.get("paymentReceipt"), "paymentDateTimestamp"));
        if (receiptTimestamp != null && receiptTimestamp > 0L) {
            return receiptTimestamp;
        }

        String receiptDate = safeMapString(documentSnapshot.get("paymentReceipt"), "paymentDateTime");
        if (!receiptDate.isEmpty()) {
            try {
                Date parsed = receiptDateFormat.parse(receiptDate);
                if (parsed != null) {
                    return parsed.getTime();
                }
            } catch (ParseException ignored) {
            }
        }

        return 0L;
    }

    private void showLoading(boolean loading) {
        if (progressEarnings != null) {
            progressEarnings.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Nullable
    private Object getMapValue(Object mapObject, String key) {
        if (!(mapObject instanceof Map) || key == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) mapObject;
        return map.get(key);
    }

    private String safeMapString(Object mapObject, String key) {
        Object value = getMapValue(mapObject, key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @Nullable
    private Double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    private Long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private double firstNonNullDouble(Double... values) {
        if (values == null) {
            return 0d;
        }
        for (Double value : values) {
            if (value != null) {
                return value;
            }
        }
        return 0d;
    }
}

