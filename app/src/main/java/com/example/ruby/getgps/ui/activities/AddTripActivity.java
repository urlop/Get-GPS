package com.example.ruby.getgps.ui.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.TimeHelper;
import com.example.ruby.getgps.utils.UIHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class AddTripActivity extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    @Bind(R.id.tv_date_picker)
    TextView tv_date_picker;
    DatePickerDialog.OnDateSetListener date;
    @Bind(R.id.upload_trip)
    ImageView upload_trip;
    @Bind(R.id.et_miles)
    EditText et_miles;
    @Bind(R.id.bt_trip_work)
    Button bt_trip_work;
    @Bind(R.id.bt_trip_charity)
    Button bt_trip_charity;
    @Bind(R.id.bt_trip_medical)
    Button bt_trip_medical;
    @Bind(R.id.bt_trip_personal)
    Button bt_trip_personal;
    @Bind(R.id.et_from)
    EditText et_from;
    @Bind(R.id.et_to)
    EditText et_to;
    @Bind(R.id.et_add_note)
    EditText et_add_note;
    @Bind(R.id.ll_business_line)
    LinearLayout ll_businnes_line;
    @Bind(R.id.business_line_separator)
    View business_line_separator;
    @Bind(R.id.scrollview)
    ScrollView scrollView;
    @Bind(R.id.iv_placeholder)
    ImageView iv_placeholder;
    @Bind(R.id.iv_placeholder_icon)
    ImageView iv_placeholder_icon;
    @Bind(R.id.tv_cancel_pic)
    TextView tv_cancel_pic;
    @Bind(R.id.v_progress)
    ProgressBar v_progress;
    @Bind(R.id.tv_business_line)
    TextView tv_business_line;

    private boolean tripWork = true, tripPersonal = false, tripCharity = false, tripMedical = false;
    private float miles = 0f;
    private Dialog dialog;
    private TextView tv_gallery_pic, tv_take_pic, tv_last_pic_taken;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int RESULT_OK = -1;
    private static Uri pictureFileUri;
    private static File pictureFile;
    private Bitmap cameraPhoto, galleryPhoto;
    private ContentResolver contentResolver;
    private File fileToUpload;
    private String businessLine = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);
        contentResolver = getContentResolver();
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setUpDateField();
        scrollView.requestFocus();
        toggleTripClassButtons();
        milesEditTextListener();
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(this);
        dialog.setTitle(getResources().getString(R.string.dialog_pick_photo));
        dialog.setContentView(R.layout.dialog_upload_picture);
        dialog.setCancelable(true);
        tv_gallery_pic = (TextView) dialog.findViewById(R.id.tv_gallery_pic);
        tv_take_pic = (TextView) dialog.findViewById(R.id.tv_take_pic);
        tv_last_pic_taken = (TextView) dialog.findViewById(R.id.tv_last_pic_taken);
        tv_gallery_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType(Constants.IMAGE_FILE);
                startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GALLERY);
                dialog.dismiss();
            }
        });
        tv_take_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                dialog.dismiss();
            }
        });
        tv_last_pic_taken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String picturePath = getLastPhotoTaken();
                galleryPhoto = BitmapFactory.decodeFile(picturePath);
                if (galleryPhoto != null) {
                    galleryPhoto = rotatePicture(galleryPhoto, getGalleryPhotoOrientation(picturePath));
                    picturePath = getRealPathFromURI(getImageUri(getApplicationContext(), galleryPhoto));
                    File galleryFile = new File(picturePath);
                    placePicture(galleryFile);
                    dialog.dismiss();
                }
            }
        });

    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        try {
            pictureFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(pictureFile));
            pictureFileUri = Uri.fromFile(pictureFile);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void milesEditTextListener() {
        et_miles.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!et_miles.getText().toString().isEmpty()) {
                        miles = Float.parseFloat(et_miles.getText().toString());
                        et_miles.setText(et_miles.getText() + " mi");
                    }
                } else {
                    et_miles.setText("");
                    miles = 0;
                }
            }
        });
        et_miles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && !s.toString().contains("mi")) {
                    miles = Float.parseFloat(s.toString());
                }
            }
        });
    }

    private void setUpDateField() {
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (!myCalendar.after(Calendar.getInstance())) {
                    updateDatePickerField();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_invalid_date), Toast.LENGTH_SHORT).show();
                    myCalendar.setTime(Calendar.getInstance().getTime());
                    updateDatePickerField();
                }
            }

        };
        myCalendar.setTime(Calendar.getInstance().getTime());
        updateDatePickerField();
    }

    private void updateDatePickerField() {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tv_date_picker.setText(sdf.format(myCalendar.getTime()));
    }

    @OnClick(R.id.ll_date_picker)
    protected void onDatepicker() {
        new DatePickerDialog(AddTripActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @OnClick(R.id.upload_trip)
    protected void uploadTrip() {

        if (attemptUploadTrip()) {
            //HashMap<String,  MultipartBody.Part> map = new HashMap<>();
            Call<Trip> call;

            if (fileToUpload != null) {

                String fileName = TimeHelper.dateToString(Calendar.getInstance());

                // create RequestBody instance from file
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("image/png"), fileToUpload);

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part filePart =
                        MultipartBody.Part.createFormData("trip[photo_attributes][image]", fileName, requestFile);

                //map.put("trip[photo_attributes][image]\";" + "file\"; filename=\"" + fileName, body);


                call = RequestManager.getMultipartServices(this).uploadTripMultipart2(
                        filePart, miles, et_from.getText().toString(),
                        et_to.getText().toString(), et_add_note.getText().toString(),
                        tv_date_picker.getText().toString(), getPurpose(),businessLine);

                // The request can also be constructed manually
//                call = rawRequest(miles, et_from.getText().toString(),
//                        et_to.getText().toString(), et_add_note.getText().toString(),
//                        et_date_picker.getText().toString(), getPurpose(), fileToUpload);

            } else {
                call = RequestManager.getMultipartServices(this).uploadTripWithoutPhoto(
                        miles, et_from.getText().toString(),
                        et_to.getText().toString(), et_add_note.getText().toString(),
                        tv_date_picker.getText().toString(), getPurpose(),businessLine);
            }
            call.enqueue(new CustomCallback<Trip>(this, call) {
                @Override
                public void onResponse(Call<Trip> call, Response<Trip> response) {
                    super.onResponse(call, response);
                    UIHelper.showProgress(AddTripActivity.this, v_progress, null, false);
                    if (response.isSuccessful()) {
                        Toast.makeText(AddTripActivity.this, R.string.ws_ok_send, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra(Constants.EDIT_PROFILE_EXTRA, true);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(AddTripActivity.this, R.string.ws_error_send, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public Call<Trip> rawRequest(float miles, String from, String to, String notes, String date, String purpose, File photo) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("trip[miles]", String.valueOf(miles))
                .addFormDataPart("trip[from]", from)
                .addFormDataPart("trip[to]", to)
                .addFormDataPart("trip[notes]", notes)
                .addFormDataPart("trip[date]", date)
                .addFormDataPart("trip[purpose]", purpose)
                .addFormDataPart("trip[photo_attributes][image]", TimeHelper.dateToString(Calendar.getInstance()),
                        RequestBody.create(MediaType.parse("image/jpeg"), photo))
                .build();

        return RequestManager.getMultipartServices(this).rawUploadTrip(body);
    }

    private boolean attemptUploadTrip() {
        boolean tripMiles, tripDate;
        if (miles == 0f) {
            et_miles.setError(getResources().getString(R.string.et_error_add_trip));
            tripMiles = false;
        } else {
            tripMiles = true;
        }
        if (tv_date_picker.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_error_add_trip), Toast.LENGTH_SHORT).show();
            tripDate = false;
        } else {
            tripDate = true;
        }
        return tripMiles && tripDate;
    }

    private String getPurpose() {
        if (tripWork) {
            return getResources().getString(R.string.str_work);
        } else if (tripCharity) {
            return getResources().getString(R.string.str_charity);
        } else if (tripPersonal) {
            return getResources().getString(R.string.str_personal);
        } else {
            return getResources().getString(R.string.str_medical);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void toggleTripClassButtons() {
        bt_trip_work.setEnabled(!tripWork);
        bt_trip_work.setTextColor(tripWork ? ContextCompat.getColor(bt_trip_work.getContext(), android.R.color.white) : ContextCompat.getColor(bt_trip_personal.getContext(), android.R.color.darker_gray));

        bt_trip_personal.setEnabled(!tripPersonal);
        bt_trip_personal.setTextColor(tripPersonal ? ContextCompat.getColor(bt_trip_personal.getContext(), android.R.color.white) : ContextCompat.getColor(bt_trip_personal.getContext(), android.R.color.darker_gray));

        bt_trip_charity.setEnabled(!tripCharity);
        bt_trip_charity.setTextColor(tripCharity ? ContextCompat.getColor(bt_trip_work.getContext(), android.R.color.white) : ContextCompat.getColor(bt_trip_personal.getContext(), android.R.color.darker_gray));

        bt_trip_medical.setEnabled(!tripMedical);
        bt_trip_medical.setTextColor(tripMedical ? ContextCompat.getColor(bt_trip_work.getContext(), android.R.color.white) : ContextCompat.getColor(bt_trip_personal.getContext(), android.R.color.darker_gray));

        ll_businnes_line.setVisibility(tripWork ? View.VISIBLE : View.GONE);
        business_line_separator.setVisibility(tripWork ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.bt_trip_work)
    protected void tripWork() {
        tripWork = true;
        tripCharity = false;
        tripPersonal = false;
        tripMedical = false;
        toggleTripClassButtons();
    }

    @OnClick(R.id.bt_trip_personal)
    protected void tripPersonal() {
        tripWork = false;
        tripCharity = false;
        tripPersonal = true;
        tripMedical = false;
        toggleTripClassButtons();

    }

    @OnClick(R.id.bt_trip_charity)
    protected void tripCharity() {
        tripWork = false;
        tripCharity = true;
        tripPersonal = false;
        tripMedical = false;
        toggleTripClassButtons();
    }

    @OnClick(R.id.bt_trip_medical)
    protected void tripMedical() {
        tripWork = false;
        tripCharity = false;
        tripPersonal = false;
        tripMedical = true;
        toggleTripClassButtons();
    }

    @OnClick(R.id.iv_placeholder)
    protected void getImageFromUser() {
        dialog.show();
    }


    private void placePicture(File file) {
        if (file.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            iv_placeholder.setImageBitmap(myBitmap);
            iv_placeholder_icon.setVisibility(View.GONE);
            deleteFileFromMediaStore(contentResolver, file);
            tv_cancel_pic.setVisibility(View.VISIBLE);
            fileToUpload = file;
        }
    }

    @OnClick(R.id.tv_cancel_pic)
    protected void deleteSelectedPicture() {
        galleryPhoto = null;
        cameraPhoto = null;
        iv_placeholder.setImageBitmap(null);
        iv_placeholder_icon.setVisibility(View.VISIBLE);
        tv_cancel_pic.setVisibility(View.GONE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri selectedImage = pictureFileUri;
            contentResolver.notifyChange(selectedImage, null);
            try {
                cameraPhoto = android.provider.MediaStore.Images.Media
                        .getBitmap(contentResolver, selectedImage);
                cameraPhoto = rotatePicture(cameraPhoto, getGalleryPhotoOrientation(pictureFile.getAbsolutePath()));
                OutputStream outputStream;
                outputStream = new FileOutputStream(pictureFile);
                cameraPhoto.compress(Bitmap.CompressFormat.JPEG, 95, outputStream); // saving the Bitmap to a file compressed as a JPEG with 95% compression rate
                outputStream.flush();
                outputStream.close();
                MediaStore.Images.Media.insertImage(contentResolver, pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
                placePicture(pictureFile);
                cameraPhoto = null;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.photo_failed_to_load), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String picturePath = getRealPathFromURI(selectedImageUri);
            try {
                galleryPhoto = BitmapFactory.decodeFile(picturePath);
                if (galleryPhoto != null) {
                    galleryPhoto = rotatePicture(galleryPhoto, getGalleryPhotoOrientation(picturePath));
                    picturePath = getRealPathFromURI(getImageUri(getApplicationContext(), galleryPhoto));
                    File galleryFile = new File(picturePath != null ? picturePath : null);
                    placePicture(galleryFile);
                }
            } catch (OutOfMemoryError e) {
                Toast.makeText(this, getResources().getString(R.string.photo_failed_to_load), Toast.LENGTH_SHORT)
                        .show();
                Timber.e(e, "method=onActivityResult error=OutOfMemoryError");
            }
        }
        if (requestCode == Constants.TRIP_WORK_SOURCE && resultCode == RESULT_OK) {
            businessLine = data.getStringExtra(Constants.TRIP_WORK_SOURCE_STRING_EXTRA);
            tv_business_line.setText(businessLine);
        }
    }


    public int getGalleryPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private Bitmap rotatePicture(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        int reduceWidth = (int) (bitmap.getWidth() * 0.5);
        int reduceHeight = (int) (bitmap.getHeight() * 0.5);
        if (bitmap.getWidth() > 3000 || bitmap.getHeight() > 3000) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() - reduceWidth, bitmap.getHeight() - reduceHeight, true);
            return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, getResources().getString(R.string.image_gallery_title), null);
        return Uri.parse(path);
    }

    private String getLastPhotoTaken() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = contentResolver
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(1);
        }
        return null;
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        int indexColumn;
        if (cursor != null) {
            cursor.moveToFirst();
            indexColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(indexColumn);
        }
        return null;
    }

    private static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }

    @OnClick(R.id.ll_business_line)
    protected void businessLines() {
        displayWorkSourcesActivity();
    }

    private void displayWorkSourcesActivity() {
        if (TripTabFragment.getInstance().getMainActivity().getUser() != null &&
                !TripTabFragment.getInstance().getMainActivity().getUser().getIncomeSources().isEmpty()) {
            Intent intent = new Intent(AddTripActivity.this, MyWorkSourcesActivity.class);
            intent.putExtra(Constants.TRIP_WORK_SOURCE_USER_EXTRA, TripTabFragment.getInstance().getMainActivity().getUser());
            intent.putExtra(Constants.TRIP_WORK_SOURCE_ORIGIN_EXTRA, Constants.ORIGIN_ADD_TRIP_ACTIVITY);
            startActivityForResult(intent, Constants.TRIP_WORK_SOURCE);
        }
    }
}
