package lk.disontech.campusassist.fragment.fragment.student;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import java.util.concurrent.Executor;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import lk.disontech.campusassist.R;
import lk.disontech.campusassist.adapter.BidWriterAdapter;
import lk.disontech.campusassist.model.BidModel;
import lk.disontech.campusassist.model.NotificationModel;
import lk.disontech.campusassist.model.User;
import lk.disontech.campusassist.repository.BidRepository;
import lk.disontech.campusassist.repository.NotificationRepository;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.Address;
import lk.payhere.androidsdk.model.Customer;
import lk.payhere.androidsdk.model.InitRequest;

public class AssignmentDetailsFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvTitle, tvSubject, tvDeadline, tvStudent, tvDescription, tvAttachmentName, tvView;
    private TextView tvPaymentAmount, tvDeliveryAddress;
    private TextInputEditText etTitle, etDescription, etDeadline;
    private MaterialAutoCompleteTextView actvSubject;
    private TextView etAttachmentName;
    private MaterialButton btnChangeFile;
    private LinearLayout attachmentRow;
    private LinearLayout detailsViewLayout, editModeLayout;
    private LinearLayout attachmentEditRow;
    private MaterialCardView studentBidsSection;
    private MaterialCardView writerWorkSection;
    private RecyclerView recyclerBids;
    private TextView tvNoBids, tvWriterWorkStatus, tvSubmissionFileName, tvEditDeleteRestrictionMessage;
    private MaterialButton btnEdit, btnDelete, btnSave, btnCancel;
    private MaterialButton btnBidForAssignment, btnStartWork, btnPickSubmissionFile, btnSubmitCompletedWork;
    private MaterialButton btnPayNow;
    private MaterialButton btnOpenDeliveryMap;
    private MaterialButton btnCancelBid;
    private ProgressBar progressAssignmentDetails;
    private TextInputEditText etSubmissionNotes;
    private LinearLayout completeWorkSection;
    private MaterialCardView paymentSuccessBanner;
    private MaterialCardView submittedWorkCard;
    private MaterialCardView deliveryMapCard;
    private TextView tvSubmittedFileNameStudent;
    private MaterialButton btnViewSubmittedFile;
    private MaterialButton btnViewReceipt;
    private MaterialButton btnDownloadReceipt;
    private TextView tvWriterNoteStudent;
    private TextView tvMapPreviewHint;
    private TextView tvWriterBidMessage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private BidRepository bidRepository;
    private NotificationRepository notificationRepository;
    private BidWriterAdapter bidWriterAdapter;
    private final List<BidModel> currentBidList = new ArrayList<>();
    
    private String fileUrl = "";
    private String assignmentId = "";
    private String studentId = "";
    private String currentFileName = "";
    private String assignmentStatus = "";
    private String writerBidStatus = "";
    private String currentWriterBidId = "";
    private String currentWriterBidStatus = "";
    private String writerWorkStatus = "";
    private String newFileUrl = "";
    private String newFileName = "";
    private Uri selectedFileUri = null;
    private Uri submissionFileUri = null;
    private String submissionFileName = "";
    private String assignedWriterId = "";
    private String assignedWriterName = "";
    private String submissionFileUrlForStudent = "";
    private Double paymentAmount = null;
    private String deliveryAddressText = "";
    private Double deliveryLatitude = null;
    private Double deliveryLongitude = null;

    // Edit-mode fields for payment and location
    private TextInputEditText etEditPaymentAmount;
    private MaterialButton btnEditSelectLocation;
    private TextView tvEditSelectedLocation;
    private Double editSelectedLatitude = null;
    private Double editSelectedLongitude = null;
    private String editSelectedAddress = "";

    private GoogleMap deliveryPreviewMap;
    private Marker deliveryLocationMarker;
    private boolean isEditMode = false;
    private boolean isWriterView = false;
    private boolean fromMyWork = false;
    private ProgressDialog progressDialog;
    private final OkHttpClient httpClient = new OkHttpClient();
    private String currentOrderId = "";
    private String receiptNumber = "";
    private String receiptOrderId = "";
    private String receiptTransactionId = "";
    private String receiptWriterName = "";
    private String receiptPaymentStatus = "";
    private String receiptPaymentDateTime = "";
    private String receiptCurrency = "LKR";
    private Double receiptPaidAmount = null;
    private boolean payNowAuthInProgress = false;
    private static final String PAYHERE_SANDBOX_MERCHANT_ID = "1226330";
    // Raw merchant secret exactly as shown in the PayHere sandbox dashboard
    private static final String PAYHERE_SANDBOX_MERCHANT_SECRET = "MTg4NjMxOTM2NTIwNzQ4NjMyNTAzNDkxNTAxNzc3Mzk2MDE2MTk4OA==";

    // -----------------------------------------------------------------------
    // IMPORTANT: These are NOT the same as the Merchant ID / Merchant Secret.
    // You must create a "Business App" in your PayHere Sandbox portal:
    //   https://sandbox.payhere.lk/merchant/  →  Business Apps  →  Create App
    // Then paste the generated App ID and App Secret below.
    // Using the Merchant ID here causes a 401 Unauthorized error.
    // -----------------------------------------------------------------------
    private static final String PAYHERE_BUSINESS_APP_CLIENT_ID = "4OVybzZnikK4JEVNu2WAJJ3D5";        // <-- replace
    private static final String PAYHERE_BUSINESS_APP_CLIENT_SECRET = "4TttECjc5jQ4qAYIiuqOzk4Dx5u6V1QwS8m4N02gByqc"; // <-- replace

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    newFileName = getFileName(uri);
                    etAttachmentName.setText("Selected: " + newFileName);
                }
            });

    private final ActivityResultLauncher<String> submissionFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    submissionFileUri = uri;
                    submissionFileName = getFileName(uri);
                    tvSubmissionFileName.setText(submissionFileName == null || submissionFileName.isEmpty()
                            ? "No file selected" : submissionFileName);
                }
            });

    private final ActivityResultLauncher<Intent> payHereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                int statusCode = (data != null)
                        ? data.getIntExtra(PHConstants.INTENT_EXTRA_STATUS, -1)
                        : -1;
                String payMessage = (data != null)
                        ? data.getStringExtra(PHConstants.INTENT_EXTRA_MESSAGE)
                        : null;

                Log.d("PAYHERE", "resultCode=" + result.getResultCode()
                        + ", statusCode=" + statusCode
                        + ", message=" + (payMessage == null ? "" : payMessage)
                        + ", orderId=" + currentOrderId);

                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Do not trust popup success alone; verify with Merchant API first.
                    if (statusCode == 2 || statusCode == -1) {
                        verifyPaymentAndSave();
                    } else if (statusCode == 1) {
                        Toast.makeText(getContext(),
                                "Payment is pending confirmation." + (payMessage != null ? " " + payMessage : ""),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Payment was not completed." + (payMessage != null ? " " + payMessage : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Some devices/SDK paths return RESULT_CANCELED even when payment reaches gateway.
                    if (!TextUtils.isEmpty(currentOrderId)) {
                        Toast.makeText(getContext(),
                                "Payment window closed. Checking payment status...",
                                Toast.LENGTH_LONG).show();
                        verifyPaymentAfterCloseFallback();
                    } else {
                        if (!TextUtils.isEmpty(payMessage)) {
                            Toast.makeText(getContext(),
                                    "Payment closed: " + payMessage,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Payment was cancelled or closed before completion.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private void verifyPaymentAfterCloseFallback() {
        btnPayNow.setEnabled(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                verifyPaymentAndSave();
            }
        }, 1500L);
    }

    private final ActivityResultLauncher<Intent> receiptDownloadLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri uri = result.getData().getData();
                if (uri == null) {
                    Toast.makeText(getContext(), "Unable to create receipt file", Toast.LENGTH_SHORT).show();
                    return;
                }

                try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
                    if (outputStream == null) {
                        Toast.makeText(getContext(), "Unable to write receipt file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    outputStream.write(buildReceiptPdfBytes());
                    outputStream.flush();
                    Toast.makeText(getContext(), "Receipt downloaded", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Failed to save receipt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    private void viewReceiptPdf() {
        if (!hasReceiptData()) {
            Toast.makeText(getContext(), "Receipt is not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        ensureReceiptDisplayFieldsReady(this::openReceiptPdfFromCurrentData);
    }

    private void openReceiptPdfFromCurrentData() {
        if (!isAdded()) {
            return;
        }

        try {
            // Use a unique cache file to avoid external viewer reusing stale content by URI/path.
            File receiptFile = new File(requireContext().getCacheDir(), buildReceiptCacheFileName());
            try (FileOutputStream fileOutputStream = new FileOutputStream(receiptFile)) {
                fileOutputStream.write(buildReceiptPdfBytes());
                fileOutputStream.flush();
            }

            Uri pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    receiptFile
            );

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(pdfUri, "application/pdf");
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (viewIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(viewIntent);
            } else {
                Toast.makeText(getContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open receipt PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ensureReceiptDisplayFieldsReady(Runnable onReady) {
        if (onReady == null) {
            return;
        }

        String writerForDisplay = normalizeDisplayCandidate(receiptWriterName);
        if (!TextUtils.isEmpty(writerForDisplay) && !"Writer".equalsIgnoreCase(writerForDisplay)) {
            onReady.run();
            return;
        }

        String writerId = firstNonEmpty(assignedWriterId);
        if (TextUtils.isEmpty(writerId)) {
            onReady.run();
            return;
        }

        resolveWriterNameForReceipt(writerId, resolvedWriterName -> {
            String normalized = normalizeDisplayCandidate(resolvedWriterName);
            if (!TextUtils.isEmpty(normalized) && !"Writer".equalsIgnoreCase(normalized)) {
                receiptWriterName = normalized;
            }
            if (isAdded()) {
                onReady.run();
            }
        });
    }

    private void downloadReceipt() {
        if (!hasReceiptData()) {
            Toast.makeText(getContext(), "Receipt is not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Receipt_" + receiptNumber + ".pdf");
        receiptDownloadLauncher.launch(intent);
    }

    private byte[] buildReceiptPdfBytes() throws Exception {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        renderProfessionalReceipt(page.getCanvas(), pageInfo.getPageWidth(), pageInfo.getPageHeight(), getReceiptPdfData());

        pdfDocument.finishPage(page);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdfDocument.writeTo(byteArrayOutputStream);
        pdfDocument.close();
        return byteArrayOutputStream.toByteArray();
    }

    private ReceiptPdfData getReceiptPdfData() {
        String currency = TextUtils.isEmpty(receiptCurrency) ? "LKR" : receiptCurrency;
        Double amountValue = receiptPaidAmount != null ? receiptPaidAmount : paymentAmount;
        String amountText = formatCurrencyAmount(currency, amountValue);
        String receiptNumberText = safeDisplay(firstNonEmpty(
                normalizeDisplayCandidate(receiptNumber),
                normalizeDisplayCandidate(receiptOrderId),
                TextUtils.isEmpty(assignmentId) ? "" : "RCP-" + assignmentId
        ));
        String receiptDateText = safeDisplay(firstNonEmpty(
                normalizeDisplayCandidate(receiptPaymentDateTime),
                formatTimestamp(System.currentTimeMillis())
        ));
        String writerText = safeDisplay(firstNonEmpty(
                normalizeDisplayCandidate(receiptWriterName),
                normalizeDisplayCandidate(assignedWriterName),
                "Writer"
        ));

        return new ReceiptPdfData(
                "CampusAssist",
                "Payment Receipt",
                receiptNumberText,
                receiptDateText,
                getStudentNameText(),
                writerText,
                getAssignmentTitleText(),
                safeDisplay(assignmentId),
                "PayHere",
                "Paid",
                amountText,
                TextUtils.isEmpty(deliveryAddressText) ? "N/A" : deliveryAddressText.trim(),
                amountText,
                amountText,
                "Thank you for using CampusAssist",
                "Support: support@campusassist.app"
        );
    }

    private void renderProfessionalReceipt(Canvas canvas, int pageWidth, int pageHeight, ReceiptPdfData data) {
        final float margin = 40f;
        final float contentWidth = pageWidth - (2f * margin);
        float y = margin;

        Paint headerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerBgPaint.setColor(Color.parseColor("#0F2D6B"));

        Paint titleWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titleWhitePaint.setColor(Color.WHITE);
        titleWhitePaint.setTextSize(19f);
        titleWhitePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint subtitleWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitleWhitePaint.setColor(Color.parseColor("#DCE8FF"));
        subtitleWhitePaint.setTextSize(12f);

        Paint sectionTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionTitlePaint.setColor(Color.parseColor("#0F2D6B"));
        sectionTitlePaint.setTextSize(12f);
        sectionTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#1F2937"));
        labelPaint.setTextSize(11f);
        labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(Color.parseColor("#111827"));
        valuePaint.setTextSize(11f);

        Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.parseColor("#D1D5DB"));
        dividerPaint.setStrokeWidth(1.5f);

        Paint totalBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        totalBgPaint.setColor(Color.parseColor("#E9F1FF"));

        Paint totalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        totalTextPaint.setColor(Color.parseColor("#0B1E4A"));
        totalTextPaint.setTextSize(14f);
        totalTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float headerHeight = 108f;
        RectF headerRect = new RectF(margin, y, pageWidth - margin, y + headerHeight);
        canvas.drawRoundRect(headerRect, 8f, 8f, headerBgPaint);

        float headerTextX = margin + 18f;
        float appNameY = y + 35f;
        canvas.drawText(data.companyName, headerTextX, appNameY, titleWhitePaint);
        canvas.drawText(data.title, headerTextX, appNameY + 22f, subtitleWhitePaint);
        canvas.drawText("Receipt #: " + data.receiptNumber, headerTextX, appNameY + 44f, subtitleWhitePaint);
        canvas.drawText("Date: " + data.paymentDateTime, headerTextX, appNameY + 62f, subtitleWhitePaint);

        Bitmap logo = loadReceiptLogo();
        if (logo != null) {
            float targetW = 56f;
            float targetH = 56f;
            RectF logoRect = new RectF(pageWidth - margin - targetW - 16f, y + 18f, pageWidth - margin - 16f, y + 18f + targetH);
            canvas.drawBitmap(logo, null, logoRect, null);
        }

        y = y + headerHeight + 20f;

        y = drawSectionHeader(canvas, "Customer & Assignment Info", margin, y, sectionTitlePaint, dividerPaint, pageWidth);
        y = drawKeyValueRow(canvas, "Student Name", data.studentName, margin, y, contentWidth, labelPaint, valuePaint);
        y = drawKeyValueRow(canvas, "Writer Name", data.writerName, margin, y, contentWidth, labelPaint, valuePaint);
        y = drawKeyValueRow(canvas, "Assignment Title", data.assignmentTitle, margin, y, contentWidth, labelPaint, valuePaint);
        y = drawKeyValueRow(canvas, "Assignment ID", data.assignmentId, margin, y, contentWidth, labelPaint, valuePaint);

        y += 8f;
        y = drawSectionHeader(canvas, "Payment Details", margin, y, sectionTitlePaint, dividerPaint, pageWidth);
        y = drawKeyValueRow(canvas, "Payment Method", data.paymentMethod, margin, y, contentWidth, labelPaint, valuePaint);
        y = drawKeyValueRow(canvas, "Payment Status", data.paymentStatus, margin, y, contentWidth, labelPaint, valuePaint);
        y = drawKeyValueRow(canvas, "Amount Paid", data.amountPaid, margin, y, contentWidth, labelPaint, totalTextPaint);

        if (!TextUtils.isEmpty(data.address) && !"N/A".equalsIgnoreCase(data.address)) {
            y += 8f;
            y = drawSectionHeader(canvas, "Billing / Delivery Address", margin, y, sectionTitlePaint, dividerPaint, pageWidth);
            y = drawWrappedText(canvas, data.address, margin, y, contentWidth, valuePaint, 17f) + 4f;
        }

        y += 10f;
        y = drawSectionHeader(canvas, "Summary", margin, y, sectionTitlePaint, dividerPaint, pageWidth);
        y = drawKeyValueRow(canvas, "Subtotal", data.subtotal, margin, y, contentWidth, labelPaint, valuePaint);

        RectF totalRect = new RectF(margin, y + 2f, pageWidth - margin, y + 38f);
        canvas.drawRoundRect(totalRect, 6f, 6f, totalBgPaint);
        canvas.drawText("Total Amount", margin + 12f, y + 25f, totalTextPaint);
        float totalWidth = totalTextPaint.measureText(data.totalAmount);
        canvas.drawText(data.totalAmount, pageWidth - margin - 12f - totalWidth, y + 25f, totalTextPaint);
        y += 46f;

        float footerTop = Math.max(y + 8f, pageHeight - margin - 70f);
        canvas.drawLine(margin, footerTop, pageWidth - margin, footerTop, dividerPaint);
        canvas.drawText(data.thankYouMessage, margin, footerTop + 22f, valuePaint);
        canvas.drawText(data.supportInfo, margin, footerTop + 40f, valuePaint);
    }

    private Bitmap loadReceiptLogo() {
        int customLogoResId = requireContext().getResources().getIdentifier(
                "receipt_logo",
                "drawable",
                requireContext().getPackageName()
        );
        int logoResId = customLogoResId != 0 ? customLogoResId : requireContext().getApplicationInfo().icon;
        try {
            return BitmapFactory.decodeResource(getResources(), logoResId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private float drawSectionHeader(Canvas canvas,
                                    String title,
                                    float x,
                                    float y,
                                    Paint titlePaint,
                                    Paint dividerPaint,
                                    float pageWidth) {
        canvas.drawText(title, x, y + 14f, titlePaint);
        canvas.drawLine(x, y + 20f, pageWidth - x, y + 20f, dividerPaint);
        return y + 34f;
    }

    private float drawKeyValueRow(Canvas canvas,
                                  String label,
                                  String value,
                                  float x,
                                  float y,
                                  float maxWidth,
                                  Paint labelPaint,
                                  Paint valuePaint) {
        float labelWidth = 128f;
        canvas.drawText(label + ":", x, y + 13f, labelPaint);
        float valueY = drawWrappedText(canvas,
                TextUtils.isEmpty(value) ? "N/A" : value,
                x + labelWidth,
                y + 13f,
                maxWidth - labelWidth,
                valuePaint,
                17f);
        return valueY + 6f;
    }

    private float drawWrappedText(Canvas canvas,
                                  String text,
                                  float x,
                                  float baselineY,
                                  float maxWidth,
                                  Paint paint,
                                  float lineHeight) {
        String safeText = TextUtils.isEmpty(text) ? "N/A" : text;
        String[] words = safeText.split("\\s+");
        StringBuilder lineBuilder = new StringBuilder();
        float y = baselineY;

        for (String word : words) {
            String candidate = lineBuilder.length() == 0 ? word : lineBuilder + " " + word;
            if (paint.measureText(candidate) <= maxWidth) {
                lineBuilder.setLength(0);
                lineBuilder.append(candidate);
            } else {
                if (lineBuilder.length() > 0) {
                    canvas.drawText(lineBuilder.toString(), x, y, paint);
                    y += lineHeight;
                }
                lineBuilder.setLength(0);
                lineBuilder.append(word);
            }
        }

        if (lineBuilder.length() > 0) {
            canvas.drawText(lineBuilder.toString(), x, y, paint);
        }

        return y;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignment_details, container, false);

        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("assignments");
        firebaseAuth = FirebaseAuth.getInstance();
        bidRepository = new BidRepository();
        notificationRepository = new NotificationRepository();

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Updating Assignment");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Initialize view mode elements
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvDeadline = view.findViewById(R.id.tvDeadline);
        tvStudent = view.findViewById(R.id.tvStudent);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName);
        tvView = view.findViewById(R.id.tvView);
        tvPaymentAmount = view.findViewById(R.id.tvPaymentAmount);
        tvDeliveryAddress = view.findViewById(R.id.tvDeliveryAddress);
        btnOpenDeliveryMap = view.findViewById(R.id.btnOpenDeliveryMap);
        attachmentRow = view.findViewById(R.id.attachmentRow);
        detailsViewLayout = view.findViewById(R.id.detailsViewLayout);

        // Initialize edit mode elements
        etTitle = view.findViewById(R.id.etTitle);
        actvSubject = view.findViewById(R.id.actvSubject);
        etDeadline = view.findViewById(R.id.etDeadline);
        etDescription = view.findViewById(R.id.etDescription);
        etAttachmentName = view.findViewById(R.id.etAttachmentName);
        btnChangeFile = view.findViewById(R.id.btnChangeFile);
        attachmentEditRow = view.findViewById(R.id.attachmentEditRow);
        editModeLayout = view.findViewById(R.id.editModeLayout);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnBidForAssignment = view.findViewById(R.id.btnBidForAssignment);
        btnCancelBid = view.findViewById(R.id.btnCancelBid);
        studentBidsSection = view.findViewById(R.id.studentBidsSection);
        writerWorkSection = view.findViewById(R.id.writerWorkSection);
        recyclerBids = view.findViewById(R.id.recyclerBids);
        tvNoBids = view.findViewById(R.id.tvNoBids);
        tvEditDeleteRestrictionMessage = view.findViewById(R.id.tvEditDeleteRestrictionMessage);
        tvWriterWorkStatus = view.findViewById(R.id.tvWriterWorkStatus);
        tvSubmissionFileName = view.findViewById(R.id.tvSubmissionFileName);
        btnStartWork = view.findViewById(R.id.btnStartWork);
        btnPickSubmissionFile = view.findViewById(R.id.btnPickSubmissionFile);
        btnSubmitCompletedWork = view.findViewById(R.id.btnSubmitCompletedWork);
        etSubmissionNotes = view.findViewById(R.id.etSubmissionNotes);
        completeWorkSection = view.findViewById(R.id.completeWorkSection);
        btnPayNow = view.findViewById(R.id.btnPayNow);
        progressAssignmentDetails = view.findViewById(R.id.progressAssignmentDetails);
        paymentSuccessBanner = view.findViewById(R.id.paymentSuccessBanner);
        submittedWorkCard = view.findViewById(R.id.submittedWorkCard);
        deliveryMapCard = view.findViewById(R.id.deliveryMapCard);

        // Edit-mode payment and location fields
        etEditPaymentAmount = view.findViewById(R.id.etEditPaymentAmount);
        btnEditSelectLocation = view.findViewById(R.id.btnEditSelectLocation);
        tvEditSelectedLocation = view.findViewById(R.id.tvEditSelectedLocation);
        tvSubmittedFileNameStudent = view.findViewById(R.id.tvSubmittedFileNameStudent);
        btnViewSubmittedFile = view.findViewById(R.id.btnViewSubmittedFile);
        btnViewReceipt = view.findViewById(R.id.btnViewReceipt);
        btnDownloadReceipt = view.findViewById(R.id.btnDownloadReceipt);
        tvWriterNoteStudent = view.findViewById(R.id.tvWriterNoteStudent);
        tvMapPreviewHint = view.findViewById(R.id.tvMapPreviewHint);
        tvWriterBidMessage = view.findViewById(R.id.tvWriterBidMessage);

        bidWriterAdapter = new BidWriterAdapter(new BidWriterAdapter.BidActionListener() {
            @Override
            public void onViewProfile(BidModel bidModel) {
                openWriterProfile(bidModel);
            }

            @Override
            public void onAcceptBid(BidModel bidModel) {
                confirmAcceptBid(bidModel);
            }

            @Override
            public void onRejectBid(BidModel bidModel) {
                confirmRejectBid(bidModel);
            }
        });
        recyclerBids.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBids.setAdapter(bidWriterAdapter);
        recyclerBids.setNestedScrollingEnabled(false);

        // Setup subject dropdown
        setupSubjectDropdown();

        // Get data from Bundle arguments passed from MyAssignmentsFragment
        Bundle args = getArguments();
        if (args != null) {
            isWriterView = args.getBoolean("isWriterView", false);
            String title = args.getString("title", "");
            String subject = args.getString("subject", "");
            assignmentStatus = args.getString("status", "");
            writerBidStatus = args.getString("writerBidStatus", "");
            writerWorkStatus = args.getString("writerWorkStatus", "");
            fromMyWork = args.getBoolean("fromMyWork", false);
            String deadline = args.getString("deadline", "");
            String description = args.getString("description", "");
            String studentName = args.getString("studentName", args.getString("student", ""));
            String fileName = args.getString("fileName", "");
            fileUrl = args.getString("fileUrl", "");
            assignmentId = args.getString("assignmentId", "");
            studentId = args.getString("studentId", "");
            assignedWriterName = args.getString("assignedWriterName", args.getString("writerName", ""));
            deliveryAddressText = args.getString("deliveryAddress", "");

            if (args.containsKey("paymentAmount")) {
                paymentAmount = args.getDouble("paymentAmount");
            }
            if (args.containsKey("deliveryLatitude")) {
                deliveryLatitude = args.getDouble("deliveryLatitude");
            }
            if (args.containsKey("deliveryLongitude")) {
                deliveryLongitude = args.getDouble("deliveryLongitude");
            }

            currentFileName = fileName;
            newFileName = fileName;

            // Set the UI with actual data
            tvTitle.setText(title);
            tvSubject.setText(subject);
            tvDeadline.setText(deadline);
            tvDescription.setText(description);
            tvStudent.setText(studentName);

            // Set edit fields with same data
            etTitle.setText(title);
            actvSubject.setText(subject, false);
            etDeadline.setText(deadline);
            etDescription.setText(description);
            renderPaymentAndLocation();

            // Handle attachment display
            if (fileName != null && !fileName.isEmpty()) {
                tvAttachmentName.setText(fileName);
                attachmentRow.setVisibility(View.VISIBLE);
                etAttachmentName.setText(fileName);
            } else {
                attachmentRow.setVisibility(View.GONE);
                etAttachmentName.setText("No file selected");
            }
        } else {
            // Show placeholder if no data provided
            tvTitle.setText("Assignment Details");
            tvDescription.setText("No assignment data available");
            attachmentRow.setVisibility(View.GONE);
            renderPaymentAndLocation();
        }

        // Fallback to role from Dashboard intent when not explicitly passed in arguments.
        if (!isWriterView && getActivity() != null && getActivity().getIntent() != null) {
            String role = getActivity().getIntent().getStringExtra("role");
            isWriterView = role != null && role.equalsIgnoreCase("writer");
        }

        applyRoleBasedUi();
        fetchAssignmentDetails();

        // Handle attachment row click
        attachmentRow.setOnClickListener(v -> openAttachment());

        // Handle view button click
        tvView.setOnClickListener(v -> openAttachment());

        // Handle deadline date picker
        etDeadline.setOnClickListener(v -> openDatePicker());

        // Handle file change button
        btnChangeFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        // Handle edit button click
        btnEdit.setOnClickListener(v -> enableEditMode());

        // Handle delete button click
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        // Handle save button click
        btnSave.setOnClickListener(v -> saveChanges());

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> disableEditMode());

        // Handle edit-mode location picker
        btnEditSelectLocation.setOnClickListener(v -> openLocationPickerForEdit());

        // Listen for location picker result (used by both PostNew and Edit modes)
        getParentFragmentManager().setFragmentResultListener(
                LocationPickerFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    if (result.containsKey(LocationPickerFragment.KEY_LATITUDE)
                            && result.containsKey(LocationPickerFragment.KEY_LONGITUDE)) {
                        editSelectedLatitude = result.getDouble(LocationPickerFragment.KEY_LATITUDE);
                        editSelectedLongitude = result.getDouble(LocationPickerFragment.KEY_LONGITUDE);
                        editSelectedAddress = result.getString(LocationPickerFragment.KEY_ADDRESS, "");
                        if (TextUtils.isEmpty(editSelectedAddress)) {
                            editSelectedAddress = "Lat: " + editSelectedLatitude + ", Lng: " + editSelectedLongitude;
                        }
                        if (tvEditSelectedLocation != null) {
                            tvEditSelectedLocation.setText(editSelectedAddress);
                        }
                    }
                }
        );

        // Restore edit mode layout if returning from location picker
        if (isEditMode) {
            detailsViewLayout.setVisibility(View.GONE);
            editModeLayout.setVisibility(View.VISIBLE);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
            setupSubjectDropdown();
            prefillEditFields();
        }

        // Handle writer bid action
        btnBidForAssignment.setOnClickListener(v -> placeBidForAssignment());
        if (btnCancelBid != null) {
            btnCancelBid.setOnClickListener(v -> showCancelBidConfirmation());
        }
        btnStartWork.setOnClickListener(v -> startWork());
        btnPickSubmissionFile.setOnClickListener(v -> submissionFilePickerLauncher.launch("*/*"));
        btnSubmitCompletedWork.setOnClickListener(v -> submitCompletedWork());
        btnPayNow.setOnClickListener(v -> onPayNowClicked());
        btnOpenDeliveryMap.setOnClickListener(v -> openDeliveryLocationOnMap());
        if (btnViewReceipt != null) {
            btnViewReceipt.setOnClickListener(v -> viewReceiptPdf());
        }
        if (btnDownloadReceipt != null) {
            btnDownloadReceipt.setOnClickListener(v -> downloadReceipt());
        }

        return view;
    }

    private void applyRoleBasedUi() {
        if (studentBidsSection == null) {
            return;
        }

        if (isWriterView) {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            if (tvEditDeleteRestrictionMessage != null) {
                tvEditDeleteRestrictionMessage.setVisibility(View.GONE);
            }
            studentBidsSection.setVisibility(View.GONE);
            btnPayNow.setVisibility(View.GONE);

            if (fromMyWork) {
                btnBidForAssignment.setVisibility(View.GONE);
                if (tvWriterBidMessage != null) {
                    tvWriterBidMessage.setVisibility(View.GONE);
                }
                if (btnCancelBid != null) {
                    btnCancelBid.setVisibility(View.GONE);
                }
                writerWorkSection.setVisibility(View.VISIBLE);
                setupWriterWorkSection();
            } else {
                writerWorkSection.setVisibility(View.GONE);
                if (assignmentId != null && !assignmentId.isEmpty()) {
                    loadWriterBidState();
                } else {
                    btnBidForAssignment.setVisibility(View.GONE);
                    if (tvWriterBidMessage != null) {
                        tvWriterBidMessage.setVisibility(View.GONE);
                    }
                    if (btnCancelBid != null) {
                        btnCancelBid.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            btnBidForAssignment.setVisibility(View.GONE);
            if (tvWriterBidMessage != null) {
                tvWriterBidMessage.setVisibility(View.GONE);
            }
            if (btnCancelBid != null) {
                btnCancelBid.setVisibility(View.GONE);
            }
            writerWorkSection.setVisibility(View.GONE);
            applyStudentEditDeleteState();
            studentBidsSection.setVisibility(View.VISIBLE);
            loadBidsForAssignment();

            // Show Pay Now button only when payment is pending
            boolean isPendingPayment = "Pending Payment".equalsIgnoreCase(assignmentStatus);
            boolean isCompleted = "Completed".equalsIgnoreCase(assignmentStatus);

            btnPayNow.setVisibility(isPendingPayment ? View.VISIBLE : View.GONE);

            // Show payment success banner and submitted work when Completed
            if (paymentSuccessBanner != null) {
                paymentSuccessBanner.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
            }
            if (submittedWorkCard != null) {
                submittedWorkCard.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
            }
            updateReceiptActionsVisibility(isCompleted);

            // Wire up the "View" button for submitted file
            if (btnViewSubmittedFile != null) {
                btnViewSubmittedFile.setOnClickListener(v -> openSubmittedFile());
            }
        }
    }

    private void fetchAssignmentDetails() {
        if (TextUtils.isEmpty(assignmentId)) {
            return;
        }

        setDetailsLoading(true);
        firebaseFirestore.collection("Assignments").document(assignmentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        setDetailsLoading(false);
                        return;
                    }

                    lk.disontech.campusassist.model.AssignmentModel assignment =
                            documentSnapshot.toObject(lk.disontech.campusassist.model.AssignmentModel.class);
                    if (assignment != null) {
                        bindAssignmentFromBackend(assignment);
                        bindReceiptFromDocument(documentSnapshot);
                        applyRoleBasedUi();
                    }
                    setDetailsLoading(false);
                })
                .addOnFailureListener(e -> {
                    setDetailsLoading(false);
                    Toast.makeText(getContext(), "Failed to load assignment details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindAssignmentFromBackend(lk.disontech.campusassist.model.AssignmentModel assignment) {
        assignmentStatus = safe(assignment.getStatus());
        assignmentId = safe(assignment.getAssignmentId());
        studentId = safe(assignment.getStudentId());
        fileUrl = safe(assignment.getFileUrl());
        assignedWriterId = safe(assignment.getAssignedWriterId());
        assignedWriterName = safe(assignment.getAssignedWriterName());
        submissionFileUrlForStudent = safe(assignment.getSubmissionFileUrl());
        paymentAmount = assignment.getPaymentAmount();
        deliveryAddressText = safe(assignment.getDeliveryAddress());
        deliveryLatitude = assignment.getDeliveryLatitude();
        deliveryLongitude = assignment.getDeliveryLongitude();

        String title = safe(assignment.getTitle());
        String subject = safe(assignment.getSubject());
        String deadline = safe(assignment.getDeadline());
        String description = safe(assignment.getDescription());
        String studentName = safe(assignment.getStudentName());
        String fileName = safe(assignment.getFileName());

        tvTitle.setText(title);
        tvSubject.setText(subject);
        tvDeadline.setText(deadline);
        tvDescription.setText(description);
        tvStudent.setText(studentName);

        etTitle.setText(title);
        actvSubject.setText(subject, false);
        etDeadline.setText(deadline);
        etDescription.setText(description);
        renderPaymentAndLocation();

        currentFileName = fileName;
        if (!TextUtils.isEmpty(fileName)) {
            tvAttachmentName.setText(fileName);
            etAttachmentName.setText(fileName);
            attachmentRow.setVisibility(View.VISIBLE);
        } else {
            attachmentRow.setVisibility(View.GONE);
            etAttachmentName.setText("No file selected");
        }

        // Writer's work submission fields (writer-side inputs)
        String writerSubmissionFileName = safe(assignment.getSubmissionFileName());
        String writerSubmissionNotes = safe(assignment.getSubmissionNotes());
        submissionFileName = writerSubmissionFileName;
        tvSubmissionFileName.setText(TextUtils.isEmpty(writerSubmissionFileName)
                ? "No file selected" : writerSubmissionFileName);
        etSubmissionNotes.setText(writerSubmissionNotes);

        // Student-facing submitted work section
        String subFileName = safe(assignment.getSubmissionFileName());
        String subNotes = safe(assignment.getSubmissionNotes());
        if (tvSubmittedFileNameStudent != null) {
            tvSubmittedFileNameStudent.setText(
                    TextUtils.isEmpty(subFileName) ? "No file available" : subFileName);
        }
        if (tvWriterNoteStudent != null) {
            tvWriterNoteStudent.setText(
                    TextUtils.isEmpty(subNotes) ? "No notes from writer." : subNotes);
        }
    }

    private void renderPaymentAndLocation() {
        if (tvPaymentAmount != null) {
            if (paymentAmount != null && paymentAmount > 0) {
                tvPaymentAmount.setText(String.format(Locale.US, "LKR %,.2f", paymentAmount));
            } else {
                tvPaymentAmount.setText("Not set");
            }
        }

        if (tvDeliveryAddress != null) {
            tvDeliveryAddress.setText(TextUtils.isEmpty(deliveryAddressText) ? "Not provided" : deliveryAddressText);
        }

        if (btnOpenDeliveryMap != null) {
            boolean hasCoordinates = deliveryLatitude != null && deliveryLongitude != null;
            boolean hasAddress = !TextUtils.isEmpty(deliveryAddressText);
            btnOpenDeliveryMap.setVisibility((hasCoordinates || hasAddress) ? View.VISIBLE : View.GONE);
        }

        boolean hasCoordinates = deliveryLatitude != null && deliveryLongitude != null;
        if (deliveryMapCard != null) {
            deliveryMapCard.setVisibility(hasCoordinates ? View.VISIBLE : View.GONE);
        }
        if (hasCoordinates) {
            ensureDeliveryMapPreview();
            updateDeliveryMapPreview();
            if (tvMapPreviewHint != null) {
                tvMapPreviewHint.setText(TextUtils.isEmpty(deliveryAddressText)
                        ? "Delivery location preview"
                        : deliveryAddressText);
            }
        }
    }

    private void ensureDeliveryMapPreview() {
        if (deliveryLatitude == null || deliveryLongitude == null || !isAdded()) {
            return;
        }

        FragmentManager childManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) childManager.findFragmentById(R.id.deliveryMapContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            childManager.beginTransaction()
                    .replace(R.id.deliveryMapContainer, mapFragment, "delivery_preview_map")
                    .commitNowAllowingStateLoss();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        deliveryPreviewMap = googleMap;
        deliveryPreviewMap.getUiSettings().setMapToolbarEnabled(false);
        deliveryPreviewMap.getUiSettings().setAllGesturesEnabled(false);
        updateDeliveryMapPreview();
    }

    private void updateDeliveryMapPreview() {
        if (deliveryPreviewMap == null || deliveryLatitude == null || deliveryLongitude == null) {
            return;
        }

        LatLng deliveryLatLng = new LatLng(deliveryLatitude, deliveryLongitude);
        if (deliveryLocationMarker == null) {
            deliveryLocationMarker = deliveryPreviewMap.addMarker(
                    new MarkerOptions().position(deliveryLatLng).title("Delivery Location")
            );
        } else {
            deliveryLocationMarker.setPosition(deliveryLatLng);
        }
        deliveryPreviewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLatLng, 15f));
    }

    private void setDetailsLoading(boolean isLoading) {
        if (progressAssignmentDetails != null) {
            progressAssignmentDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnEdit != null) {
            btnEdit.setEnabled(!isLoading);
        }
        if (btnDelete != null) {
            btnDelete.setEnabled(!isLoading);
        }
        if (btnBidForAssignment != null) {
            btnBidForAssignment.setEnabled(!isLoading);
        }
        if (btnStartWork != null) {
            btnStartWork.setEnabled(!isLoading);
        }
        if (btnSubmitCompletedWork != null) {
            btnSubmitCompletedWork.setEnabled(!isLoading);
        }
        if (btnPayNow != null) {
            btnPayNow.setEnabled(!isLoading);
        }
    }

    private void applyStudentEditDeleteState() {
        if (btnEdit == null || btnDelete == null) {
            return;
        }

        boolean canEditDelete = isStudentEditDeleteAllowed();
        btnEdit.setVisibility(canEditDelete ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(canEditDelete ? View.VISIBLE : View.GONE);

        if (tvEditDeleteRestrictionMessage != null) {
            if (canEditDelete) {
                tvEditDeleteRestrictionMessage.setVisibility(View.GONE);
            } else {
                String statusLabel = TextUtils.isEmpty(assignmentStatus) ? "This" : assignmentStatus;
                tvEditDeleteRestrictionMessage.setText(statusLabel + " assignments cannot be edited or deleted.");
                tvEditDeleteRestrictionMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isStudentEditDeleteAllowed() {
        return !isWriterView && !TextUtils.isEmpty(assignmentStatus) && assignmentStatus.equalsIgnoreCase("Open");
    }

    private void setupWriterWorkSection() {
        String statusToUse = !TextUtils.isEmpty(writerWorkStatus) ? writerWorkStatus : mapWriterStatusFromCurrentData();
        boolean isInProgress = "In Progress".equalsIgnoreCase(statusToUse);
        boolean isPendingPayment = "Pending Payment".equalsIgnoreCase(statusToUse);
        boolean isCompleted = "Completed".equalsIgnoreCase(statusToUse);

        tvWriterWorkStatus.setText("Status: " + statusToUse);
        btnStartWork.setVisibility("Approved".equalsIgnoreCase(statusToUse) ? View.VISIBLE : View.GONE);

        // Keep submitted details visible after work is submitted.
        if (isInProgress || isPendingPayment || isCompleted) {
            completeWorkSection.setVisibility(View.VISIBLE);

            if (isInProgress) {
                btnPickSubmissionFile.setVisibility(View.VISIBLE);
                btnSubmitCompletedWork.setVisibility(View.VISIBLE);
                etSubmissionNotes.setEnabled(true);
                tvSubmissionFileName.setOnClickListener(null);
                tvSubmissionFileName.setClickable(false);
            } else {
                // Post-submission states are read-only for writer.
                btnPickSubmissionFile.setVisibility(View.GONE);
                btnSubmitCompletedWork.setVisibility(View.GONE);
                etSubmissionNotes.setEnabled(false);

                if (!TextUtils.isEmpty(submissionFileUrlForStudent)) {
                    tvSubmissionFileName.setOnClickListener(v -> openSubmittedFile());
                    tvSubmissionFileName.setClickable(true);
                } else {
                    tvSubmissionFileName.setOnClickListener(null);
                    tvSubmissionFileName.setClickable(false);
                }
            }
        } else {
            completeWorkSection.setVisibility(View.GONE);
            submissionFileUri = null;
            submissionFileName = "";
            tvSubmissionFileName.setText("No file selected");
            etSubmissionNotes.setText("");
            tvSubmissionFileName.setOnClickListener(null);
            tvSubmissionFileName.setClickable(false);
        }
    }

    private String mapWriterStatusFromCurrentData() {
        if (writerBidStatus != null && (writerBidStatus.equalsIgnoreCase("Rejected")
                || writerBidStatus.equalsIgnoreCase("Cancelled"))) {
            return "Rejected";
        }
        if (writerBidStatus == null || writerBidStatus.isEmpty() || writerBidStatus.equalsIgnoreCase("Pending")) {
            return "Pending Confirmation";
        }
        if (assignmentStatus.equalsIgnoreCase("Assigned")) {
            return "Approved";
        }
        if (assignmentStatus.equalsIgnoreCase("In Progress")) {
            return "In Progress";
        }
        if (assignmentStatus.equalsIgnoreCase("Completed")) {
            return "Completed";
        }
        if (assignmentStatus.equalsIgnoreCase("Pending Payment")) {
            return "Pending Payment";
        }
        return "Approved";
    }

    private void startWork() {
        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Starting Work");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        firebaseFirestore.collection("Assignments").document(assignmentId)
                .update("status", "In Progress", "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    assignmentStatus = "In Progress";
                    writerWorkStatus = "In Progress";
                    setupWriterWorkSection();
                    sendNotification(
                            studentId,
                            "student",
                            "Assignment In Progress",
                            "Work has started on your assignment " + getAssignmentTitleText() + ".",
                            assignmentId,
                            getAssignmentTitleText(),
                            "In Progress",
                            ""
                    );
                    Toast.makeText(getContext(), "Work started", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to start work: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void submitCompletedWork() {
        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }
        if (submissionFileUri == null) {
            Toast.makeText(getContext(), "Please choose a file", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = etSubmissionNotes.getText() == null ? "" : etSubmissionNotes.getText().toString().trim();

        progressDialog.setTitle("Submitting Work");
        progressDialog.setMessage("Uploading file...");
        progressDialog.show();

        String extension = getFileExtension(submissionFileUri);
        String storedName = "Submission_" + UUID.randomUUID() + extension;
        StorageReference submissionRef = firebaseStorage.getReference()
                .child("submissions")
                .child(assignmentId)
                .child(storedName);

        submissionRef.putFile(submissionFileUri)
                .addOnSuccessListener(taskSnapshot -> submissionRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("status", "Pending Payment");
                            updates.put("submissionFileUrl", uri.toString());
                            updates.put("submissionFileName", submissionFileName);
                            updates.put("submissionNotes", notes);
                            updates.put("submissionWriterId", firebaseAuth.getCurrentUser() != null
                                    ? firebaseAuth.getCurrentUser().getUid() : "");
                            updates.put("submissionAt", System.currentTimeMillis());
                            updates.put("updatedAt", System.currentTimeMillis());

                            firebaseFirestore.collection("Assignments").document(assignmentId)
                                    .update(updates)
                                    .addOnSuccessListener(unused -> {
                                        progressDialog.dismiss();
                                        submissionFileUrlForStudent = uri.toString();
                                        assignmentStatus = "Pending Payment";
                                        writerWorkStatus = "Pending Payment";
                                        setupWriterWorkSection();
                                        sendNotification(
                                                studentId,
                                                "student",
                                                "Work Submitted",
                                                "Work for your assignment " + getAssignmentTitleText() + " was submitted and is now Pending Payment.",
                                                assignmentId,
                                                getAssignmentTitleText(),
                                                "Pending Payment",
                                                ""
                                        );
                                        Toast.makeText(getContext(), "Work submitted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Failed to save submission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to get file url: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void placeBidForAssignment() {
        if (assignmentId == null || assignmentId.isEmpty()) {
            Toast.makeText(getContext(), "Assignment ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();
        btnBidForAssignment.setEnabled(false);

        firebaseFirestore.collection("Users").document(writerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        btnBidForAssignment.setEnabled(true);
                        Toast.makeText(getContext(), "Writer profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String firstName = documentSnapshot.getString("firstName") != null
                            ? documentSnapshot.getString("firstName").trim() : "";
                    String lastName = documentSnapshot.getString("lastName") != null
                            ? documentSnapshot.getString("lastName").trim() : "";
                    String writerName = (firstName + " " + lastName).trim();
                    if (writerName.isEmpty()) {
                        String email = documentSnapshot.getString("email");
                        writerName = email != null && !email.trim().isEmpty() ? email : "Writer";
                    }
                    final String writerDisplayName = writerName;

                    BidModel bidModel = BidModel.builder()
                            .assignmentId(assignmentId)
                            .studentId(studentId)
                            .writerId(writerId)
                            .writerName(writerDisplayName)
                            .writerEmail(documentSnapshot.getString("email") != null ? documentSnapshot.getString("email") : "")
                            .writerMobile(documentSnapshot.getString("mobile") != null ? documentSnapshot.getString("mobile") : "")
                            .createdAt(System.currentTimeMillis())
                            .status("Pending")
                            .build();

                    bidRepository.createBid(bidModel, new BidRepository.OnBidActionCallback() {
                        @Override
                        public void onSuccess() {
                            currentWriterBidId = safe(bidModel.getBidId());
                            currentWriterBidStatus = "Pending";
                            writerBidStatus = "Pending";
                            updateWriterBidUi(true);
                            sendNotification(
                                    studentId,
                                    "student",
                                    "New Bid Received",
                                    writerDisplayName + " has bid on your assignment " + getAssignmentTitleText() + ".",
                                    assignmentId,
                                    getAssignmentTitleText(),
                                    assignmentStatus,
                                    ""
                            );
                            sendNotification(
                                    writerId,
                                    "writer",
                                    "Bid Submitted",
                                    "You successfully bid on " + getAssignmentTitleText() + " posted by " + getStudentNameText() + ".",
                                    assignmentId,
                                    getAssignmentTitleText(),
                                    assignmentStatus,
                                    "Pending Confirmation"
                            );
                            Toast.makeText(getContext(), "Bid submitted successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            btnBidForAssignment.setEnabled(true);
                            if (!TextUtils.isEmpty(errorMessage)
                                    && errorMessage.toLowerCase(Locale.US).contains("already placed")) {
                                loadWriterBidState();
                            }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    btnBidForAssignment.setEnabled(true);
                    Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadWriterBidState() {
        if (!isWriterView || fromMyWork) {
            return;
        }

        if (firebaseAuth.getCurrentUser() == null || TextUtils.isEmpty(assignmentId)) {
            currentWriterBidId = "";
            currentWriterBidStatus = "";
            updateWriterBidUi(false);
            return;
        }

        String writerId = firebaseAuth.getCurrentUser().getUid();
        if (btnBidForAssignment != null) {
            btnBidForAssignment.setEnabled(false);
        }

        bidRepository.getLatestBidByAssignmentAndWriter(assignmentId, writerId, new BidRepository.OnBidLoadedCallback() {
            @Override
            public void onBidLoaded(BidModel bidModel) {
                if (!isAdded()) {
                    return;
                }

                boolean hasActiveBid = false;
                currentWriterBidId = "";
                currentWriterBidStatus = "";

                if (bidModel != null) {
                    currentWriterBidId = safe(bidModel.getBidId());
                    currentWriterBidStatus = safe(bidModel.getStatus());
                    writerBidStatus = currentWriterBidStatus;
                    hasActiveBid = isWriterBidActive(currentWriterBidStatus);
                }

                updateWriterBidUi(hasActiveBid);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) {
                    return;
                }
                updateWriterBidUi(false);
                Toast.makeText(getContext(), "Failed to load bid state: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWriterBidUi(boolean hasActiveBid) {
        if (btnBidForAssignment != null) {
            btnBidForAssignment.setVisibility(hasActiveBid ? View.GONE : View.VISIBLE);
            btnBidForAssignment.setEnabled(!hasActiveBid);
            btnBidForAssignment.setText("Bid for Assignment");
        }
        if (tvWriterBidMessage != null) {
            tvWriterBidMessage.setVisibility(hasActiveBid ? View.VISIBLE : View.GONE);
            tvWriterBidMessage.setText("You have already bid for this assignment.");
        }
        if (btnCancelBid != null) {
            btnCancelBid.setVisibility(hasActiveBid ? View.VISIBLE : View.GONE);
            btnCancelBid.setEnabled(hasActiveBid);
        }
    }

    private void showCancelBidConfirmation() {
        if (TextUtils.isEmpty(currentWriterBidId)) {
            Toast.makeText(getContext(), "No active bid to cancel", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Bid")
                .setMessage("Do you want to cancel your bid for this assignment?")
                .setPositiveButton("Cancel Bid", (dialog, which) -> cancelCurrentWriterBid())
                .setNegativeButton("Keep", null)
                .show();
    }

    private void cancelCurrentWriterBid() {
        if (TextUtils.isEmpty(currentWriterBidId)) {
            Toast.makeText(getContext(), "No active bid to cancel", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnCancelBid != null) {
            btnCancelBid.setEnabled(false);
        }

        bidRepository.cancelBid(currentWriterBidId, new BidRepository.OnBidActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                currentWriterBidStatus = "Cancelled";
                writerBidStatus = "Cancelled";
                currentWriterBidId = "";
                updateWriterBidUi(false);
                Toast.makeText(getContext(), "Bid cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) {
                    return;
                }
                if (btnCancelBid != null) {
                    btnCancelBid.setEnabled(true);
                }
                Toast.makeText(getContext(), "Failed to cancel bid: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWriterBidActive(String status) {
        if (TextUtils.isEmpty(status)) {
            return true;
        }
        return !(status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Rejected"));
    }

    private void loadBidsForAssignment() {
        setDetailsLoading(true);
        if (assignmentId == null || assignmentId.isEmpty()) {
            tvNoBids.setVisibility(View.VISIBLE);
            recyclerBids.setVisibility(View.GONE);
            tvNoBids.setText("No bids available for this assignment");
            setDetailsLoading(false);
            return;
        }

        bidRepository.getBidsByAssignment(assignmentId, new BidRepository.OnBidsLoadedCallback() {
            @Override
            public void onBidsLoaded(java.util.List<BidModel> bids) {
                if (bids == null || bids.isEmpty()) {
                    tvNoBids.setVisibility(View.VISIBLE);
                    recyclerBids.setVisibility(View.GONE);
                    setDetailsLoading(false);
                    return;
                }

                enrichBidsForStudentView(bids);
            }

            @Override
            public void onError(String errorMessage) {
                tvNoBids.setVisibility(View.VISIBLE);
                recyclerBids.setVisibility(View.GONE);
                setDetailsLoading(false);
                Toast.makeText(getContext(), "Error loading bids: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrichBidsForStudentView(java.util.List<BidModel> bids) {
        if (bids == null || bids.isEmpty()) {
            tvNoBids.setVisibility(View.VISIBLE);
            recyclerBids.setVisibility(View.GONE);
            currentBidList.clear();
            return;
        }

        currentBidList.clear();
        currentBidList.addAll(bids);

        final int[] remaining = {bids.size()};
        for (BidModel bid : bids) {
            populateBidMetadata(bid, () -> {
                remaining[0]--;
                if (remaining[0] <= 0) {
                    tvNoBids.setVisibility(View.GONE);
                    recyclerBids.setVisibility(View.VISIBLE);
                    bidWriterAdapter.submitList(bids);
                    setDetailsLoading(false);
                }
            });
        }
    }

    private void populateBidMetadata(BidModel bid, Runnable onComplete) {
        if (bid == null || bid.getWriterId() == null || bid.getWriterId().trim().isEmpty()) {
            onComplete.run();
            return;
        }

        final int[] pendingTasks = {2};
        Runnable finishTask = () -> {
            pendingTasks[0]--;
            if (pendingTasks[0] <= 0) {
                onComplete.run();
            }
        };

        firebaseFirestore.collection("Users").document(bid.getWriterId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Double rating = documentSnapshot.getDouble("rating");
                    if (rating != null) {
                        bid.setRating(rating);
                    }

                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String fullName = ((firstName != null ? firstName.trim() : "") + " "
                            + (lastName != null ? lastName.trim() : "")).trim();
                    if (!fullName.isEmpty()) {
                        bid.setWriterName(fullName);
                    }
                    finishTask.run();
                })
                .addOnFailureListener(e -> finishTask.run());

        firebaseFirestore.collection("Assignments")
                .whereEqualTo("assignedWriterId", bid.getWriterId())
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bid.setCompletedProjectsCount(queryDocumentSnapshots.size());
                    finishTask.run();
                })
                .addOnFailureListener(e -> finishTask.run());
    }

    private void openWriterProfile(BidModel bidModel) {
        if (bidModel == null || bidModel.getWriterId() == null || bidModel.getWriterId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Writer profile not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("writerId", bidModel.getWriterId());

        WriterProfileFragment fragment = new WriterProfileFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void confirmAcceptBid(BidModel bidModel) {
        if (bidModel == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Accept Bid")
                .setMessage("Accept this writer for the assignment? Other pending bids will be rejected.")
                .setPositiveButton("Accept", (dialog, which) -> acceptBid(bidModel))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void acceptBid(BidModel bidModel) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid bid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bidModel.getWriterId() == null || bidModel.getWriterId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid writer", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Accepting Bid");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        bidRepository.acceptBid(
                assignmentId,
                bidModel.getBidId(),
                bidModel.getWriterId().trim(),
                bidModel.getWriterName() != null ? bidModel.getWriterName() : "Writer",
                new BidRepository.OnBidActionCallback() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        sendNotification(
                                bidModel.getWriterId(),
                                "writer",
                                "Bid Accepted",
                                "Your bid for " + getAssignmentTitleText() + " was accepted by " + getStudentNameText() + ".",
                                assignmentId,
                                getAssignmentTitleText(),
                                "Assigned",
                                "Approved"
                        );

                        for (BidModel otherBid : currentBidList) {
                            if (otherBid == null || otherBid.getBidId() == null || otherBid.getWriterId() == null) {
                                continue;
                            }
                            if (otherBid.getBidId().equals(bidModel.getBidId())) {
                                continue;
                            }
                            String otherStatus = otherBid.getStatus();
                            if (otherStatus == null || otherStatus.equalsIgnoreCase("Pending")) {
                                sendNotification(
                                        otherBid.getWriterId(),
                                        "writer",
                                        "Bid Rejected",
                                        "Your bid for " + getAssignmentTitleText() + " was rejected by " + getStudentNameText() + ".",
                                        assignmentId,
                                        getAssignmentTitleText(),
                                        "Assigned",
                                        "Rejected"
                                );
                            }
                        }

                        Toast.makeText(getContext(), "Bid accepted successfully", Toast.LENGTH_SHORT).show();
                        loadBidsForAssignment();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void confirmRejectBid(BidModel bidModel) {
        if (bidModel == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Bid")
                .setMessage("Reject this writer's bid?")
                .setPositiveButton("Reject", (dialog, which) -> rejectBid(bidModel))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void rejectBid(BidModel bidModel) {
        if (bidModel.getBidId() == null || bidModel.getBidId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Invalid bid", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Rejecting Bid");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        bidRepository.updateBidStatus(bidModel.getBidId(), "Rejected", new BidRepository.OnBidActionCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                sendNotification(
                        bidModel.getWriterId(),
                        "writer",
                        "Bid Rejected",
                        "Your bid for " + getAssignmentTitleText() + " was rejected by " + getStudentNameText() + ".",
                        assignmentId,
                        getAssignmentTitleText(),
                        assignmentStatus,
                        "Rejected"
                );
                Toast.makeText(getContext(), "Bid rejected", Toast.LENGTH_SHORT).show();
                loadBidsForAssignment();
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSubjectDropdown() {
        String[] subjects = new String[]{
                "Mathematics", "Science", "English", "History", "ICT", "Business"
        };
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                subjects
        );
        actvSubject.setAdapter(subjectAdapter);
        actvSubject.setThreshold(0); // Show dropdown even with no input
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, day);

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    etDeadline.setText(sdf.format(picked.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void openLocationPickerForEdit() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardContainer, new LocationPickerFragment())
                .addToBackStack(null)
                .commit();
    }

    private void prefillEditFields() {
        // Pre-fill payment amount
        if (etEditPaymentAmount != null) {
            if (paymentAmount != null && paymentAmount > 0) {
                etEditPaymentAmount.setText(String.format(Locale.US, "%.2f", paymentAmount));
            } else {
                etEditPaymentAmount.setText("");
            }
        }
        // Pre-fill delivery address
        if (!TextUtils.isEmpty(editSelectedAddress)) {
            // Already updated by location picker result – don't overwrite
        } else {
            editSelectedLatitude = deliveryLatitude;
            editSelectedLongitude = deliveryLongitude;
            editSelectedAddress = deliveryAddressText != null ? deliveryAddressText : "";
        }
        if (tvEditSelectedLocation != null) {
            tvEditSelectedLocation.setText(
                    TextUtils.isEmpty(editSelectedAddress) ? "No address selected" : editSelectedAddress);
        }
    }

    private void enableEditMode() {
        if (!isStudentEditDeleteAllowed()) {
            Toast.makeText(getContext(), "Only open assignments can be edited", Toast.LENGTH_SHORT).show();
            return;
        }

        isEditMode = true;
        // Reset edit-location state from current saved values
        editSelectedLatitude = deliveryLatitude;
        editSelectedLongitude = deliveryLongitude;
        editSelectedAddress = deliveryAddressText != null ? deliveryAddressText : "";

        detailsViewLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        selectedFileUri = null;
        newFileUrl = "";
        prefillEditFields();
        // Re-setup dropdown to ensure items are visible
        setupSubjectDropdown();
    }

    private void disableEditMode() {
        if (!isEditMode) return; // Prevent unintended calls
        isEditMode = false;
        detailsViewLayout.setVisibility(View.VISIBLE);
        editModeLayout.setVisibility(View.GONE);
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
        selectedFileUri = null;
        newFileName = currentFileName;
        etAttachmentName.setText(currentFileName != null && !currentFileName.isEmpty() ? currentFileName : "No file selected");
        // Reset edit-location state
        editSelectedLatitude = null;
        editSelectedLongitude = null;
        editSelectedAddress = "";
    }

    private void saveChanges() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String subject = actvSubject.getText() != null ? actvSubject.getText().toString().trim() : "";
        String deadline = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(subject)) {
            actvSubject.setError("Subject is required");
            return;
        }
        if (TextUtils.isEmpty(deadline)) {
            etDeadline.setError("Deadline is required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            return;
        }

        // Validate payment amount
        String paymentAmountText = etEditPaymentAmount.getText() != null
                ? etEditPaymentAmount.getText().toString().trim() : "";
        double newPaymentAmount;
        if (TextUtils.isEmpty(paymentAmountText)) {
            etEditPaymentAmount.setError("Payment amount is required");
            etEditPaymentAmount.requestFocus();
            return;
        }
        try {
            newPaymentAmount = Double.parseDouble(paymentAmountText);
        } catch (NumberFormatException e) {
            etEditPaymentAmount.setError("Enter a valid numeric amount");
            etEditPaymentAmount.requestFocus();
            return;
        }
        if (newPaymentAmount <= 0) {
            etEditPaymentAmount.setError("Amount must be greater than 0");
            etEditPaymentAmount.requestFocus();
            return;
        }
        etEditPaymentAmount.setError(null);

        // Delivery address – keep existing if not changed
        Double newDeliveryLatitude = editSelectedLatitude != null ? editSelectedLatitude : deliveryLatitude;
        Double newDeliveryLongitude = editSelectedLongitude != null ? editSelectedLongitude : deliveryLongitude;
        String newDeliveryAddress = !TextUtils.isEmpty(editSelectedAddress) ? editSelectedAddress : deliveryAddressText;

        // If file is selected, upload it first
        if (selectedFileUri != null) {
            uploadFileAndUpdateAssignment(title, subject, deadline, description,
                    newPaymentAmount, newDeliveryAddress, newDeliveryLatitude, newDeliveryLongitude);
        } else {
            // Update without file
            updateAssignmentInFirestore(title, subject, deadline, description, fileUrl, currentFileName,
                    newPaymentAmount, newDeliveryAddress, newDeliveryLatitude, newDeliveryLongitude);
        }
    }

    private void uploadFileAndUpdateAssignment(String title, String subject, String deadline, String description,
                                               double newPaymentAmount, String newDeliveryAddress,
                                               Double newDeliveryLatitude, Double newDeliveryLongitude) {
        progressDialog.show();

        String fileExtension = getFileExtension(selectedFileUri);
        String fileName = "Assignment_" + UUID.randomUUID() + fileExtension;

        StorageReference fileReference = storageReference.child(studentId).child(fileName);

        fileReference.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        newFileUrl = uri.toString();
                        updateAssignmentInFirestore(title, subject, deadline, description, newFileUrl, newFileName,
                                newPaymentAmount, newDeliveryAddress, newDeliveryLatitude, newDeliveryLongitude);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error getting file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    long progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading... " + progress + "%");
                });
    }

    private void updateAssignmentInFirestore(String title, String subject, String deadline, String description,
                                            String fileUrl, String fileName,
                                            double newPaymentAmount, String newDeliveryAddress,
                                            Double newDeliveryLatitude, Double newDeliveryLongitude) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("subject", subject);
        updates.put("deadline", deadline);
        updates.put("description", description);
        updates.put("fileUrl", fileUrl);
        updates.put("fileName", fileName);
        updates.put("paymentAmount", newPaymentAmount);
        updates.put("deliveryAddress", newDeliveryAddress != null ? newDeliveryAddress : "");
        if (newDeliveryLatitude != null) updates.put("deliveryLatitude", newDeliveryLatitude);
        if (newDeliveryLongitude != null) updates.put("deliveryLongitude", newDeliveryLongitude);
        updates.put("updatedAt", System.currentTimeMillis());

        // Update in Firestore
        firebaseFirestore.collection("Assignments").document(assignmentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Assignment updated successfully! ✅", Toast.LENGTH_SHORT).show();

                    // Update view mode UI
                    tvTitle.setText(title);
                    tvSubject.setText(subject);
                    tvDeadline.setText(deadline);
                    tvDescription.setText(description);

                    // Update in-memory payment & location values
                    paymentAmount = newPaymentAmount;
                    deliveryAddressText = newDeliveryAddress != null ? newDeliveryAddress : "";
                    if (newDeliveryLatitude != null) AssignmentDetailsFragment.this.deliveryLatitude = newDeliveryLatitude;
                    if (newDeliveryLongitude != null) AssignmentDetailsFragment.this.deliveryLongitude = newDeliveryLongitude;
                    renderPaymentAndLocation();

                    // Update file display
                    if (fileName != null && !fileName.isEmpty()) {
                        tvAttachmentName.setText(fileName);
                        attachmentRow.setVisibility(View.VISIBLE);
                    } else {
                        attachmentRow.setVisibility(View.GONE);
                    }

                    currentFileName = fileName;
                    AssignmentDetailsFragment.this.fileUrl = fileUrl;

                    // Disable edit mode
                    disableEditMode();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error updating assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation() {
        if (!isStudentEditDeleteAllowed()) {
            Toast.makeText(getContext(), "Only open assignments can be deleted", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAssignment())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteAssignment() {
        firebaseFirestore.collection("Assignments").document(assignmentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Assignment deleted successfully! ✅", Toast.LENGTH_SHORT).show();
                    // Navigate back to my assignments
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error deleting assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openAttachment() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(getContext(), "No file attached to this assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Open the file URL in a browser or default app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(fileUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver cursor = requireContext().getContentResolver();
            try {
                String[] projection = {"_display_name"};
                android.database.Cursor queryCursor = cursor.query(uri, projection, null, null, null);
                if (queryCursor != null && queryCursor.moveToFirst()) {
                    result = queryCursor.getString(queryCursor.getColumnIndexOrThrow("_display_name"));
                    queryCursor.close();
                }
            } catch (Exception e) {
                result = "document";
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        android.webkit.MimeTypeMap mimeTypeMap = android.webkit.MimeTypeMap.getSingleton();
        return "." + mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void onPayNowClicked() {
        if (!"Pending Payment".equalsIgnoreCase(assignmentStatus)) {
            Toast.makeText(getContext(), "Payment is not required for this assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Assignment reference is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (paymentAmount == null || paymentAmount <= 0d) {
            Toast.makeText(getContext(), "Invalid payment amount for this assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(deliveryAddressText)) {
            Toast.makeText(getContext(), "Assignment delivery address is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        startPayNowAuthenticationGate();
    }

    private void startPayNowAuthenticationGate() {
        if (payNowAuthInProgress) {
            return;
        }

        payNowAuthInProgress = true;
        btnPayNow.setEnabled(false);

        int canAuthenticate = BiometricManager.from(requireContext())
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPromptForPayment();
            return;
        }

        showPasswordFallbackDialog();
    }

    private void showBiometricPromptForPayment() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                if (!isAdded()) {
                    return;
                }
                proceedToPayHereAfterAuth();
            }

            @Override
            public void onAuthenticationFailed() {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Biometric authentication failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                if (!isAdded()) {
                    return;
                }
                payNowAuthInProgress = false;
                btnPayNow.setEnabled(true);
                Toast.makeText(getContext(), "Authentication cancelled.", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm Payment")
                .setSubtitle("Use biometric authentication to continue")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void showPasswordFallbackDialog() {
        if (!isAdded()) {
            payNowAuthInProgress = false;
            return;
        }

        final TextInputEditText passwordInput = new TextInputEditText(requireContext());
        passwordInput.setHint("Enter your login password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Password")
                .setMessage("Biometric authentication is unavailable. Enter your account password to continue payment.")
                .setView(passwordInput)
                .setCancelable(false)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", (d, which) -> {
                    payNowAuthInProgress = false;
                    btnPayNow.setEnabled(true);
                })
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = passwordInput.getText() == null ? "" : passwordInput.getText().toString();
            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Password is required");
                return;
            }
            dialog.dismiss();
            reauthenticateForPayment(password);
        }));

        dialog.show();
    }

    private void reauthenticateForPayment(String password) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            payNowAuthInProgress = false;
            btnPayNow.setEnabled(true);
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            payNowAuthInProgress = false;
            btnPayNow.setEnabled(true);
            Toast.makeText(getContext(), "Password confirmation is not available for this account.", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) {
                        return;
                    }
                    proceedToPayHereAfterAuth();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }
                    payNowAuthInProgress = false;
                    btnPayNow.setEnabled(true);
                    Toast.makeText(getContext(), "Incorrect password. Payment not started.", Toast.LENGTH_SHORT).show();
                });
    }

    private void proceedToPayHereAfterAuth() {
        payNowAuthInProgress = false;
        btnPayNow.setEnabled(false);
        fetchCurrentUserPaymentData(new PaymentUserDataCallback() {
            @Override
            public void onLoaded(PaymentUserData userData) {
                btnPayNow.setEnabled(true);
                launchPayHerePayment(userData);
            }

            @Override
            public void onError(String message) {
                btnPayNow.setEnabled(true);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCurrentUserPaymentData(PaymentUserDataCallback callback) {
        if (firebaseAuth.getCurrentUser() == null) {
            callback.onError("Please login first");
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        String authEmail = safe(firebaseAuth.getCurrentUser().getEmail());
        String authDisplayName = safe(firebaseAuth.getCurrentUser().getDisplayName());
        String authPhone = safe(firebaseAuth.getCurrentUser().getPhoneNumber());

        firebaseFirestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String firstName = safe(documentSnapshot.getString("firstName"));
                    String lastName = safe(documentSnapshot.getString("lastName"));
                    String dbEmail = safe(documentSnapshot.getString("email"));
                    String dbPhone = safe(documentSnapshot.getString("mobile"));

                    String fullName = (firstName + " " + lastName).trim();
                    if (TextUtils.isEmpty(fullName)) {
                        fullName = authDisplayName;
                    }
                    if (TextUtils.isEmpty(fullName)) {
                        fullName = "Student";
                    }

                    String resolvedEmail = !TextUtils.isEmpty(dbEmail) ? dbEmail : authEmail;
                    if (TextUtils.isEmpty(resolvedEmail)) {
                        callback.onError("Email not found. Please update your profile.");
                        return;
                    }

                    String resolvedPhone = !TextUtils.isEmpty(dbPhone) ? dbPhone : authPhone;
                    if (TextUtils.isEmpty(resolvedPhone)) {
                        callback.onError("Phone number not found. Please update your profile.");
                        return;
                    }

                    String[] nameParts = fullName.split("\\s+", 2);
                    String resolvedFirstName = nameParts.length > 0 ? nameParts[0].trim() : "Student";
                    String resolvedLastName = nameParts.length > 1 ? nameParts[1].trim() : "User";

                    PaymentUserData userData = new PaymentUserData(
                            resolvedFirstName,
                            resolvedLastName,
                            resolvedEmail,
                            resolvedPhone
                    );
                    callback.onLoaded(userData);
                })
                .addOnFailureListener(e -> callback.onError("Failed to load user details: " + e.getMessage()));
    }

    private void launchPayHerePayment(PaymentUserData userData) {
        try {
            String preflightError = validatePayHerePreflight(userData);
            if (!TextUtils.isEmpty(preflightError)) {
                Toast.makeText(getContext(), preflightError, Toast.LENGTH_LONG).show();
                Log.e("PAYHERE", "Preflight failed: " + preflightError);
                return;
            }

            String normalizedPhone = normalizePhoneForPayHere(userData.phone);
            String normalizedEmail = userData.email == null ? "" : userData.email.trim();
            String safeFirstName = userData.firstName == null ? "Student" : userData.firstName.trim();
            String safeLastName = userData.lastName == null ? "User" : userData.lastName.trim();
            if (TextUtils.isEmpty(safeFirstName)) {
                safeFirstName = "Student";
            }
            if (TextUtils.isEmpty(safeLastName)) {
                safeLastName = "User";
            }

            currentOrderId = buildCompactOrderId();
            InitRequest payment = new InitRequest();
            payment.setSandBox(true);
            payment.setMerchantId(PAYHERE_SANDBOX_MERCHANT_ID);
            payment.setMerchantSecret(PAYHERE_SANDBOX_MERCHANT_SECRET);
            payment.setNotifyUrl("https://example.com/payhere/notify");
            payment.setOrderId(currentOrderId);
            payment.setItemsDescription("Assignment payment - " + getAssignmentTitleText());
            payment.setAmount(roundToTwoDecimals(paymentAmount));
            payment.setCurrency("LKR");

            Customer customer = new Customer();
            customer.setFirstName(safeFirstName);
            customer.setLastName(safeLastName);
            customer.setEmail(normalizedEmail);
            customer.setPhone(normalizedPhone);

            String city = resolveCityFromAddress(deliveryAddressText);
            String country = resolveCountryFromAddress(deliveryAddressText);

            Address billingAddress = new Address();
            billingAddress = customer.getAddress();
            if (billingAddress != null) {
                billingAddress.setAddress(safeAddressForPayHere(deliveryAddressText));
                billingAddress.setCity(city);
                billingAddress.setCountry(country);
            }

            Address deliveryAddress = new Address();
            deliveryAddress.setAddress(safeAddressForPayHere(deliveryAddressText));
            deliveryAddress.setCity(city);
            deliveryAddress.setCountry(country);
            customer.setDeliveryAddress(deliveryAddress);

            payment.setCustomer(customer);

            Log.d("PAYHERE", "Launching payment with orderId=" + currentOrderId
                    + ", amount=" + paymentAmount
                    + ", phone=" + normalizedPhone);

            Intent payHereIntent = new Intent(requireContext(), PHMainActivity.class);
            payHereIntent.putExtra(PHConstants.INTENT_EXTRA_DATA, payment);
            payHereLauncher.launch(payHereIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String resolveCityFromAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "Colombo";
        }

        String[] parts = address.split(",");
        if (parts.length >= 2 && !TextUtils.isEmpty(parts[1].trim())) {
            return parts[1].trim();
        }
        if (!TextUtils.isEmpty(parts[0].trim())) {
            return parts[0].trim();
        }
        return "Colombo";
    }

    private String resolveCountryFromAddress(String address) {
        if (!TextUtils.isEmpty(address) && address.toLowerCase(Locale.US).contains("sri lanka")) {
            return "Sri Lanka";
        }
        return "Sri Lanka";
    }

    private String buildCompactOrderId() {
        String cleanedAssignmentId = TextUtils.isEmpty(assignmentId)
                ? "ASSIGN"
                : assignmentId.replaceAll("[^A-Za-z0-9]", "");
        if (cleanedAssignmentId.length() > 12) {
            cleanedAssignmentId = cleanedAssignmentId.substring(0, 12);
        }
        return "ASSIGN-" + cleanedAssignmentId + "-" + (System.currentTimeMillis() / 1000L);
    }

    private String normalizePhoneForPayHere(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 9 && digits.startsWith("7")) {
            digits = "0" + digits;
        }
        if (digits.startsWith("94") && digits.length() == 11) {
            digits = "0" + digits.substring(2);
        }
        if (digits.length() != 10) {
            return "";
        }
        if (!digits.startsWith("0")) {
            return "";
        }
        return digits;
    }

    private String safeAddressForPayHere(String address) {
        String safeAddress = TextUtils.isEmpty(address) ? "Colombo" : address.trim();
        if (safeAddress.length() > 120) {
            safeAddress = safeAddress.substring(0, 120);
        }
        return safeAddress;
    }

    private double roundToTwoDecimals(Double amount) {
        if (amount == null) {
            return 0d;
        }
        return Math.round(amount * 100.0d) / 100.0d;
    }

    private String validatePayHerePreflight(PaymentUserData userData) {
        if (TextUtils.isEmpty(PAYHERE_SANDBOX_MERCHANT_ID) || TextUtils.isEmpty(PAYHERE_SANDBOX_MERCHANT_SECRET)) {
            return "Merchant credentials are missing.";
        }
        if (userData == null) {
            return "Unable to load payment profile.";
        }
        if (paymentAmount == null || paymentAmount <= 0d) {
            return "Invalid payment amount.";
        }
        if (TextUtils.isEmpty(normalizePhoneForPayHere(userData.phone))) {
            return "Please update your mobile as a valid Sri Lankan number (e.g., 0771234567).";
        }
        String email = userData.email == null ? "" : userData.email.trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please update a valid email in your profile.";
        }
        if (TextUtils.isEmpty(getAssignmentTitleText().trim())) {
            return "Assignment title is missing for payment.";
        }
        return "";
    }

    private interface PaymentUserDataCallback {
        void onLoaded(PaymentUserData userData);

        void onError(String message);
    }

    private static class PaymentUserData {
        final String firstName;
        final String lastName;
        final String email;
        final String phone;

        PaymentUserData(String firstName, String lastName, String email, String phone) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
        }
    }

    // Merchant API verification flow (same pattern as your friend's implementation)
    private void verifyPaymentAndSave() {
        btnPayNow.setEnabled(false);

        if (TextUtils.isEmpty(PAYHERE_BUSINESS_APP_CLIENT_ID)
                || TextUtils.isEmpty(PAYHERE_BUSINESS_APP_CLIENT_SECRET)
                || PAYHERE_BUSINESS_APP_CLIENT_ID.startsWith("YOUR_")
                || PAYHERE_BUSINESS_APP_CLIENT_SECRET.startsWith("YOUR_")) {
            handleError("Business App credentials are not configured.\n"
                    + "Go to PayHere Sandbox portal → Business Apps → Create App,\n"
                    + "then update PAYHERE_BUSINESS_APP_CLIENT_ID and PAYHERE_BUSINESS_APP_CLIENT_SECRET.");
            return;
        }

        if (TextUtils.isEmpty(currentOrderId)) {
            handleError("Payment reference is missing. Please try again.");
            return;
        }

        Toast.makeText(requireContext(), "Payment submitted. Verifying payment...", Toast.LENGTH_LONG).show();

        String authString = PAYHERE_BUSINESS_APP_CLIENT_ID + ":" + PAYHERE_BUSINESS_APP_CLIENT_SECRET;
        String authBase64 = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        RequestBody tokenBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request tokenRequest = new Request.Builder()
                .url("https://sandbox.payhere.lk/merchant/v1/oauth/token")
                .header("Authorization", "Basic " + authBase64)
                .post(tokenBody)
                .build();

        httpClient.newCall(tokenRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleError("Authorization failed. Connection error.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonToken = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(jsonToken);
                        String accessToken = obj.optString("access_token", "");
                        if (TextUtils.isEmpty(accessToken)) {
                            handleError("Error parsing auth token.");
                            return;
                        }

                        // STEP 2: Call Search API with generated token
                        searchPayment(accessToken);

                    } catch (Exception e) {
                        handleError("Error parsing auth token.");
                    }
                } else {
                    Log.e("VERIFY", "Token Error (" + response.code() + "): " + jsonToken);
                    handleError("Merchant authentication failed (HTTP " + response.code() + ").\n"
                            + "Check that PAYHERE_BUSINESS_APP_CLIENT_ID and "
                            + "PAYHERE_BUSINESS_APP_CLIENT_SECRET match your Business App in PayHere portal.");
                }
            }
        });
    }

    private void searchPayment(String accessToken) {
        HttpUrl url = HttpUrl.parse("https://sandbox.payhere.lk/merchant/v1/payment/search")
                .newBuilder()
                .addQueryParameter("order_id", currentOrderId)
                .build();

        Request searchRequest = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        httpClient.newCall(searchRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleError("Verification search failed.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body() != null ? response.body().string() : "";
                Log.d("VERIFY", "Search Result: " + jsonData);

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("status", 0) == 1) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");
                            if (dataArray != null && dataArray.length() > 0) {
                                JSONObject lastTransaction = dataArray.getJSONObject(0);

                                int statusCode = lastTransaction.optInt("status_code", -1);
                                String statusStr = lastTransaction.optString("status", "");

                                if (statusCode == 2 || statusStr.equalsIgnoreCase("RECEIVED")) {
                                    String txnReference = firstNonEmpty(
                                            lastTransaction.optString("payment_id", ""),
                                            lastTransaction.optString("payment_no", ""),
                                            lastTransaction.optString("reference", ""),
                                            lastTransaction.optString("transaction_id", ""),
                                            lastTransaction.optString("id", "")
                                    );
                                    String paidAt = firstNonEmpty(
                                            lastTransaction.optString("captured_at", ""),
                                            lastTransaction.optString("paid_at", ""),
                                            lastTransaction.optString("updated_at", ""),
                                            lastTransaction.optString("created_at", "")
                                    );
                                    double verifiedAmount = optDoubleOrDefault(
                                            lastTransaction,
                                            "amount",
                                            paymentAmount != null ? paymentAmount : 0d
                                    );
                                    String currency = firstNonEmpty(lastTransaction.optString("currency", ""), "LKR");
                                    VerifiedPaymentData verifiedPaymentData = new VerifiedPaymentData(
                                            statusCode,
                                            statusStr,
                                            txnReference,
                                            currentOrderId,
                                            verifiedAmount,
                                            currency,
                                            paidAt,
                                            jsonData
                                    );

                                    requireActivity().runOnUiThread(() -> {
                                        handlePaymentSuccess(verifiedPaymentData);
                                        btnPayNow.setEnabled(true);
                                    });
                                    return;
                                }
                            }
                        }
                        handleError("Payment verification failed. Payment is still pending.");
                    } catch (Exception e) {
                        Log.e("VERIFY", "Parsing Error: " + e.getMessage());
                        handleError("Verification parsing error.");
                    }
                } else {
                    handleError("Search API Server Error: " + response.code());
                }
            }
        });
    }

    private void handleError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            btnPayNow.setEnabled(true);
        });
    }

    private void handlePaymentSuccess(VerifiedPaymentData verifiedPaymentData) {
        if (TextUtils.isEmpty(assignmentId)) {
            Toast.makeText(getContext(), "Invalid assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Completing Payment");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String writerId = !TextUtils.isEmpty(assignedWriterId) ? assignedWriterId : "";
        resolveWriterNameForReceipt(writerId, writerName -> {
            long verifiedAt = System.currentTimeMillis();
            Map<String, Object> receiptMap = buildReceiptMap(verifiedPaymentData, writerName, verifiedAt);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "Completed");
            updates.put("paymentStatus", "Paid");
            updates.put("paymentCompletedAt", verifiedAt);
            updates.put("paymentVerifiedAt", verifiedAt);
            updates.put("paymentVerification", verifiedPaymentData.toMap());
            updates.put("paymentReceipt", receiptMap);
            updates.put("updatedAt", verifiedAt);

            firebaseFirestore.collection("Assignments").document(assignmentId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                    // Reload the assignment from Firestore to get latest submission details
                    firebaseFirestore.collection("Assignments").document(assignmentId).get()
                            .addOnSuccessListener(doc -> {
                                progressDialog.dismiss();

                                if (doc.exists()) {
                                    bindReceiptFromDocument(doc);
                                    lk.disontech.campusassist.model.AssignmentModel assignment =
                                            doc.toObject(lk.disontech.campusassist.model.AssignmentModel.class);
                                    if (assignment != null) {
                                        submissionFileUrlForStudent = safe(assignment.getSubmissionFileUrl());
                                        String subFileName = safe(assignment.getSubmissionFileName());
                                        String subNotes = safe(assignment.getSubmissionNotes());
                                        String writerIdForNotif = safe(assignment.getAssignedWriterId());

                                        // Update student-facing submitted work UI
                                        if (tvSubmittedFileNameStudent != null) {
                                            tvSubmittedFileNameStudent.setText(
                                                    TextUtils.isEmpty(subFileName) ? "No file available" : subFileName);
                                        }
                                        if (tvWriterNoteStudent != null) {
                                            tvWriterNoteStudent.setText(
                                                    TextUtils.isEmpty(subNotes) ? "No notes from writer." : subNotes);
                                        }

                                        // Send notification to student
                                        sendNotification(
                                                studentId,
                                                "student",
                                                "Payment Completed",
                                                "Payment was completed for " + getAssignmentTitleText()
                                                        + ", and the assignment is now completed.",
                                                assignmentId,
                                                getAssignmentTitleText(),
                                                "Completed",
                                                ""
                                        );

                                        // Send notification to writer
                                        if (!TextUtils.isEmpty(writerIdForNotif)) {
                                            sendNotification(
                                                    writerIdForNotif,
                                                    "writer",
                                                    "Payment Completed",
                                                    "Payment was completed for " + getAssignmentTitleText()
                                                            + ", and the assignment is now completed.",
                                                    assignmentId,
                                                    getAssignmentTitleText(),
                                                    "Completed",
                                                    "Completed"
                                            );
                                        } else if (!TextUtils.isEmpty(assignedWriterId)) {
                                            sendNotification(
                                                    assignedWriterId,
                                                    "writer",
                                                    "Payment Completed",
                                                    "Payment was completed for " + getAssignmentTitleText()
                                                            + ", and the assignment is now completed.",
                                                    assignmentId,
                                                    getAssignmentTitleText(),
                                                    "Completed",
                                                    "Completed"
                                            );
                                        }
                                    }
                                }

                                // Update local state and refresh UI
                                assignmentStatus = "Completed";
                                btnPayNow.setVisibility(View.GONE);
                                if (paymentSuccessBanner != null) {
                                    paymentSuccessBanner.setVisibility(View.VISIBLE);
                                }
                                if (submittedWorkCard != null) {
                                    submittedWorkCard.setVisibility(View.VISIBLE);
                                }
                                updateReceiptActionsVisibility(true);
                                applyStudentEditDeleteState();

                                // Show success dialog
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Payment Successful")
                                        .setMessage("Payment was verified with PayHere and marked as paid. Your official receipt is now available.")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                // Status update succeeded; still update UI
                                assignmentStatus = "Completed";
                                btnPayNow.setVisibility(View.GONE);
                                if (paymentSuccessBanner != null) {
                                    paymentSuccessBanner.setVisibility(View.VISIBLE);
                                }
                                if (submittedWorkCard != null) {
                                    submittedWorkCard.setVisibility(View.VISIBLE);
                                }
                                updateReceiptActionsVisibility(true);

                                // Notifications with cached IDs
                                sendNotification(studentId, "student",
                                        "Payment Completed",
                                        "Payment was completed for " + getAssignmentTitleText()
                                                + ", and the assignment is now completed.",
                                        assignmentId, getAssignmentTitleText(), "Completed", "");

                                if (!TextUtils.isEmpty(assignedWriterId)) {
                                    sendNotification(assignedWriterId, "writer",
                                            "Payment Completed",
                                            "Payment was completed for " + getAssignmentTitleText()
                                                    + ", and the assignment is now completed.",
                                            assignmentId, getAssignmentTitleText(), "Completed", "Completed");
                                }

                                Toast.makeText(getContext(), "Payment completed successfully.", Toast.LENGTH_LONG).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(),
                                "Payment verified but failed to update assignment: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void openSubmittedFile() {
        if (TextUtils.isEmpty(submissionFileUrlForStudent)) {
            Toast.makeText(getContext(), "No submitted file available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(submissionFileUrlForStudent));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindReceiptFromDocument(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            clearReceiptData();
            return;
        }

        Map<String, Object> receipt = asObjectMap(documentSnapshot.get("paymentReceipt"));
        Map<String, Object> verification = asObjectMap(documentSnapshot.get("paymentVerification"));

        if (receipt == null && verification == null) {
            clearReceiptData();
            return;
        }

        String assignmentIdForReceipt = firstNonEmpty(assignmentId, documentSnapshot.getId());
        String fallbackReceiptNumber = TextUtils.isEmpty(assignmentIdForReceipt) ? "" : "RCP-" + assignmentIdForReceipt;
        receiptNumber = firstNonEmpty(
                firstNonEmptyMapString(receipt, "receiptNumber", "receiptNo", "receipt_id", "invoiceNumber", "invoiceNo"),
                fallbackReceiptNumber
        );
        receiptOrderId = firstNonEmpty(
                firstNonEmptyMapString(receipt, "orderId", "orderID", "order_id"),
                firstNonEmptyMapString(verification, "orderId", "orderID", "order_id"),
                currentOrderId
        );
        receiptTransactionId = firstNonEmpty(
                firstNonEmptyMapString(receipt, "transactionId", "transactionReference", "paymentId", "payment_id", "reference", "transaction_id"),
                firstNonEmptyMapString(verification, "transactionReference", "transactionId", "paymentId", "payment_id", "reference", "transaction_id"),
                receiptOrderId
        );
        receiptWriterName = firstNonEmpty(
                firstNonEmptyMapString(receipt, "writerName", "assignedWriterName", "writer"),
                safe(documentSnapshot.getString("assignedWriterName")),
                assignedWriterName,
                "Writer"
        );
        receiptPaymentStatus = firstNonEmpty(
                firstNonEmptyMapString(receipt, "paymentStatus", "status"),
                firstNonEmptyMapString(verification, "status"),
                "Paid"
        );
        receiptPaymentDateTime = firstNonEmpty(
                firstNonEmptyMapString(receipt, "paymentDateTime", "paidAt", "capturedAt"),
                firstNonEmptyMapString(verification, "paidAt", "capturedAt"),
                formatTimestamp(firstNonNullLong(
                        firstLongFromMap(receipt, "paymentDateTimestamp", "paidAtTimestamp", "paymentTimestamp"),
                        getDocumentLong(documentSnapshot, "paymentCompletedAt"),
                        getDocumentLong(documentSnapshot, "paymentVerifiedAt")
                )),
                formatTimestamp(System.currentTimeMillis())
        );
        receiptCurrency = firstNonEmpty(
                firstNonEmptyMapString(receipt, "currency"),
                firstNonEmptyMapString(verification, "currency"),
                "LKR"
        );
        if (TextUtils.isEmpty(receiptCurrency)) {
            receiptCurrency = "LKR";
        }
        receiptPaidAmount = firstNonNullDouble(
                firstDoubleFromMap(receipt, "paidAmount", "amountPaid", "amount", "totalAmount", "subtotal"),
                firstDoubleFromMap(verification, "amount", "paidAmount"),
                paymentAmount
        );
    }

    private Map<String, Object> buildReceiptMap(VerifiedPaymentData verifiedPaymentData, String writerName, long verifiedAt) {
        Map<String, Object> receipt = new HashMap<>();
        String generatedReceiptNumber = "RCP-" + assignmentId + "-" + verifiedAt;

        receipt.put("receiptNumber", generatedReceiptNumber);
        receipt.put("assignmentId", assignmentId);
        receipt.put("orderId", verifiedPaymentData.orderId);
        receipt.put("transactionId", verifiedPaymentData.transactionReference);
        receipt.put("studentName", getStudentNameText());
        receipt.put("writerName", TextUtils.isEmpty(writerName) ? "Writer" : writerName);
        receipt.put("assignmentTitle", getAssignmentTitleText());
        receipt.put("paidAmount", verifiedPaymentData.amount);
        receipt.put("currency", TextUtils.isEmpty(verifiedPaymentData.currency) ? "LKR" : verifiedPaymentData.currency);
        receipt.put("paymentDateTime", formatTimestamp(verifiedAt));
        receipt.put("paymentDateTimestamp", verifiedAt);
        receipt.put("paymentStatus", "Paid");

        receiptNumber = generatedReceiptNumber;
        receiptOrderId = verifiedPaymentData.orderId;
        receiptTransactionId = verifiedPaymentData.transactionReference;
        receiptWriterName = TextUtils.isEmpty(writerName) ? "Writer" : writerName;
        receiptPaymentStatus = "Paid";
        receiptPaymentDateTime = formatTimestamp(verifiedAt);
        receiptCurrency = TextUtils.isEmpty(verifiedPaymentData.currency) ? "LKR" : verifiedPaymentData.currency;
        receiptPaidAmount = verifiedPaymentData.amount;

        return receipt;
    }

    private void resolveWriterNameForReceipt(String writerId, WriterNameCallback callback) {
        if (callback == null) {
            return;
        }
        if (TextUtils.isEmpty(writerId)) {
            callback.onResolved("Writer");
            return;
        }

        firebaseFirestore.collection("Users").document(writerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String firstName = safe(documentSnapshot.getString("firstName"));
                    String lastName = safe(documentSnapshot.getString("lastName"));
                    String fullName = (firstName + " " + lastName).trim();
                    if (TextUtils.isEmpty(fullName)) {
                        fullName = safe(documentSnapshot.getString("email"));
                    }
                    callback.onResolved(TextUtils.isEmpty(fullName) ? "Writer" : fullName);
                })
                .addOnFailureListener(e -> callback.onResolved("Writer"));
    }

    private void updateReceiptActionsVisibility(boolean isCompleted) {
        int visibility = (isCompleted && hasReceiptData()) ? View.VISIBLE : View.GONE;
        if (btnViewReceipt != null) {
            btnViewReceipt.setVisibility(visibility);
        }
        if (btnDownloadReceipt != null) {
            btnDownloadReceipt.setVisibility(visibility);
        }
    }

    private boolean hasReceiptData() {
        return !TextUtils.isEmpty(normalizeDisplayCandidate(receiptNumber))
                || !TextUtils.isEmpty(normalizeDisplayCandidate(receiptOrderId))
                || receiptPaidAmount != null;
    }

    private String safeDisplay(String value) {
        if (TextUtils.isEmpty(value)) {
            return "N/A";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()
                || "null".equalsIgnoreCase(trimmed)
                || "n/a".equalsIgnoreCase(trimmed)
                || "na".equalsIgnoreCase(trimmed)
                || "undefined".equalsIgnoreCase(trimmed)) {
            return "N/A";
        }
        return trimmed;
    }

    private void clearReceiptData() {
        receiptNumber = "";
        receiptOrderId = "";
        receiptTransactionId = "";
        receiptWriterName = "";
        receiptPaymentStatus = "";
        receiptPaymentDateTime = "";
        receiptCurrency = "LKR";
        receiptPaidAmount = null;
    }

    private String safeObject(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String normalizeDisplayCandidate(String value) {
        String display = safeDisplay(value);
        return "N/A".equals(display) ? "" : display;
    }

    @Nullable
    private Long getDocumentLong(DocumentSnapshot documentSnapshot, String field) {
        if (documentSnapshot == null || TextUtils.isEmpty(field)) {
            return null;
        }
        return documentSnapshot.getLong(field);
    }

    @Nullable
    private Long firstLongFromMap(@Nullable Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Long value = objectToLong(map.get(key));
            if (value != null && value > 0L) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    private Long firstNonNullLong(Long... values) {
        if (values == null) {
            return null;
        }
        for (Long value : values) {
            if (value != null && value > 0L) {
                return value;
            }
        }
        return null;
    }

    private String buildReceiptCacheFileName() {
        String base = TextUtils.isEmpty(receiptNumber) ? "receipt" : receiptNumber.replaceAll("[^A-Za-z0-9_-]", "_");
        return "Receipt_" + base + "_" + System.currentTimeMillis() + ".pdf";
    }

    private String formatCurrencyAmount(String currency, Double amount) {
        if (amount == null) {
            return "N/A";
        }
        String safeCurrency = TextUtils.isEmpty(currency) ? "LKR" : currency;
        return String.format(Locale.US, "%s %,.2f", safeCurrency, amount);
    }

    private String firstNonEmptyMapString(@Nullable Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            String value = safeObject(map.get(key));
            if (!"N/A".equals(safeDisplay(value))) {
                return value.trim();
            }
        }
        return "";
    }

    @Nullable
    private Map<String, Object> asObjectMap(Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapValue = (Map<String, Object>) value;
            return mapValue;
        }
        return null;
    }

    @Nullable
    private Double firstDoubleFromMap(@Nullable Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Double value = objectToDouble(map.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    private Double firstNonNullDouble(Double... values) {
        if (values == null) {
            return null;
        }
        for (Double value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Double objectToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    private Long objectToLong(Object value) {
        if (value instanceof Number) {
            long longValue = ((Number) value).longValue();
            return longValue > 0L ? longValue : null;
        }
        if (value instanceof String) {
            try {
                long longValue = Long.parseLong(((String) value).trim());
                return longValue > 0L ? longValue : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(timestamp));
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return "";
    }

    private double optDoubleOrDefault(JSONObject jsonObject, String key, double fallback) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return fallback;
        }
        try {
            if (jsonObject.has(key)) {
                return jsonObject.optDouble(key, fallback);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private void openDeliveryLocationOnMap() {
        if (deliveryLatitude == null || deliveryLongitude == null) {
            if (TextUtils.isEmpty(deliveryAddressText)) {
                Toast.makeText(getContext(), "Delivery location not available", Toast.LENGTH_SHORT).show();
                return;
            }

            String addressQuery = Uri.encode(deliveryAddressText);
            Intent addressIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + addressQuery));

            if (addressIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(addressIntent);
            } else {
                Toast.makeText(getContext(), "No map app found", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String coordQuery = deliveryLatitude + "," + deliveryLongitude;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(coordQuery)));

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(getContext(), "No map app found", Toast.LENGTH_SHORT).show();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private interface WriterNameCallback {
        void onResolved(String writerName);
    }

    private static class VerifiedPaymentData {
        final int statusCode;
        final String status;
        final String transactionReference;
        final String orderId;
        final double amount;
        final String currency;
        final String paidAt;
        final String rawResponse;

        VerifiedPaymentData(int statusCode,
                            String status,
                            String transactionReference,
                            String orderId,
                            double amount,
                            String currency,
                            String paidAt,
                            String rawResponse) {
            this.statusCode = statusCode;
            this.status = status;
            this.transactionReference = transactionReference;
            this.orderId = orderId;
            this.amount = amount;
            this.currency = currency;
            this.paidAt = paidAt;
            this.rawResponse = rawResponse;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("statusCode", statusCode);
            map.put("status", status);
            map.put("transactionReference", transactionReference);
            map.put("orderId", orderId);
            map.put("amount", amount);
            map.put("currency", currency);
            map.put("paidAt", paidAt);
            map.put("rawResponse", rawResponse);
            return map;
        }
    }

    private static class ReceiptPdfData {
        final String companyName;
        final String title;
        final String receiptNumber;
        final String paymentDateTime;
        final String studentName;
        final String writerName;
        final String assignmentTitle;
        final String assignmentId;
        final String paymentMethod;
        final String paymentStatus;
        final String amountPaid;
        final String address;
        final String subtotal;
        final String totalAmount;
        final String thankYouMessage;
        final String supportInfo;

        ReceiptPdfData(String companyName,
                       String title,
                       String receiptNumber,
                       String paymentDateTime,
                       String studentName,
                       String writerName,
                       String assignmentTitle,
                       String assignmentId,
                       String paymentMethod,
                       String paymentStatus,
                       String amountPaid,
                       String address,
                       String subtotal,
                       String totalAmount,
                       String thankYouMessage,
                       String supportInfo) {
            this.companyName = companyName;
            this.title = title;
            this.receiptNumber = receiptNumber;
            this.paymentDateTime = paymentDateTime;
            this.studentName = studentName;
            this.writerName = writerName;
            this.assignmentTitle = assignmentTitle;
            this.assignmentId = assignmentId;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.amountPaid = amountPaid;
            this.address = address;
            this.subtotal = subtotal;
            this.totalAmount = totalAmount;
            this.thankYouMessage = thankYouMessage;
            this.supportInfo = supportInfo;
        }
    }


    private void sendNotification(String recipientUserId,
                                  String recipientRole,
                                  String title,
                                  String message,
                                  String assignmentId,
                                  String assignmentTitle,
                                  String assignmentStatus,
                                  String writerWorkStatus) {
        if (recipientUserId == null || recipientUserId.trim().isEmpty()) {
            return;
        }

        NotificationModel notificationModel = NotificationModel.builder()
                .recipientUserId(recipientUserId)
                .recipientRole(recipientRole)
                .title(title)
                .message(message)
                .assignmentId(assignmentId)
                .assignmentTitle(assignmentTitle)
                .assignmentStatus(assignmentStatus)
                .writerWorkStatus(writerWorkStatus)
                .createdAt(System.currentTimeMillis())
                .read(false)
                .build();

        notificationRepository.createNotification(notificationModel, new NotificationRepository.OnNotificationActionCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private String getAssignmentTitleText() {
        return tvTitle != null && tvTitle.getText() != null && !tvTitle.getText().toString().trim().isEmpty()
                ? tvTitle.getText().toString().trim()
                : "this assignment";
    }

    private String getStudentNameText() {
        return tvStudent != null && tvStudent.getText() != null && !tvStudent.getText().toString().trim().isEmpty()
                ? tvStudent.getText().toString().trim()
                : "the student";
    }
}

