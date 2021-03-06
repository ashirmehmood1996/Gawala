package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.R;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ManageVacationsActivity extends AppCompatActivity implements View.OnClickListener {
    /*library usage https://github.com/Applandeo/Material-Calendar-View*/
    private CalendarView calendarView;
    private String myId;
    private List<Calendar> daysOffList;
    private List<EventDay> eventDayArrayList;
    private Button editDaysOffbutton, resetcalendarButton;
    private AlertDialog mAlertDialog;
    private ValueEventListener mDaysOffListener;
    private DatabaseReference offDaysNodeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_off);
        getSupportActionBar().setTitle("Manage Vacations");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initFields();
        attachListeners();
        setDaysOffListener();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initFields() {
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        daysOffList = new ArrayList<>();
        eventDayArrayList = new ArrayList<>();
        editDaysOffbutton = findViewById(R.id.bt_days_off_edit_days);
        resetcalendarButton = findViewById(R.id.bt_days_off_reset_calendar);
        calendarView = findViewById(R.id.cv_days_off);/*        calendarView.setClickable(false); calendars = new ArrayList<>(); calendars.add(Calendar.getInstance()); events.add(new EventDay(Calendar.getInstance(), R.drawable.flag_vanuatu)); calendarView.setEvents(events); events.add(new EventDay(Calendar.getInstance(), R.drawable.flag_afghanistan));*/
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                showCalenderDatepickerDialog();
            }
        });
        offDaysNodeRef = rootRef.child("days_off").child(myId).child("days");
        mDaysOffListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    daysOffList.clear();
                    eventDayArrayList.clear();
                    for (DataSnapshot dayOffTimeSnap : dataSnapshot.getChildren()) {
                        long dayOffTimeInMilli = Long.parseLong(dayOffTimeSnap.getKey());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(dayOffTimeInMilli);
                        daysOffList.add(calendar);
                        eventDayArrayList.add(new EventDay(calendar, R.drawable.ic_do_not_disturb_red_24dp));
                    }
                    calendarView.setSelectedDates(daysOffList);
                    calendarView.setEvents(eventDayArrayList);
                } else
                    Toast.makeText(getApplicationContext(), "no data found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        initializeDialog();
    }

    private void attachListeners() {
        editDaysOffbutton.setOnClickListener(this);
        resetcalendarButton.setOnClickListener(this);
    }

    private void setDaysOffListener() {
        offDaysNodeRef.addValueEventListener(mDaysOffListener);
    }

    @Override
    protected void onDestroy() {
        offDaysNodeRef.removeEventListener(mDaysOffListener);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_days_off_edit_days:
                showCalenderDatepickerDialog();
                break;
            case R.id.bt_days_off_reset_calendar:
                clearAllDaysOff();
                break;
        }
    }

    private void showCalenderDatepickerDialog() {

        OnSelectDateListener listener = calendars -> {
            daysOffList = calendars;
            calendarView.setSelectedDates(calendars);
//                Drawable drawable = CalendarUtils.getDrawableText(ManageVacationsActivity.this, "No Delivery", Typeface.DEFAULT, R.color.colorAccent, 10);
            List<EventDay> eevents = new ArrayList<>();
            for (Calendar calendar : calendars) {
                eevents.add(new EventDay(calendar, R.drawable.ic_do_not_disturb_red_24dp));
            }

            setDaysOffToFirebase(calendars);
            calendarView.setEvents(eevents);
            Toast.makeText(ManageVacationsActivity.this, String.format("%d number of dates selected ", calendars.size()), Toast.LENGTH_SHORT).show();
        };
        new DatePickerBuilder(this, listener)
                .setDate(Calendar.getInstance()) // Initial date as Calendar object
                .setHeaderColor(R.color.colorPrimary) // Color of the dialog header
                .setHeaderLabelColor(R.color.colorAccent) // Color of the header label
                .setPickerType(CalendarView.RANGE_PICKER)// the  number of days we want to select simultaneously
//                .setMinimumDate(Calendar.getInstance()) // Minimum available date
//                .setMaximumDate(Calendar.getInstance()) // Maximum available date
//                .setDisabledDays(List<Calendar>) /// List of disabled days
//                .setPreviousButtonSrc(R.drawable.drawable) // Custom drawable of the previous arrow
//                .setForwardButtonSrc(R.drawable.drawable) // Custom drawable of the forward arrow
//                .setPreviousPageChangeListener(new OnCalendarPageChangeListener(){}) // Listener called when scroll to the previous page
//                .setForwardPageChangeListener(new OnCalendarPageChangeListener(){}) // Listener called when scroll to the next page
//                .setAbbreviationsBarColor(R.color.color) // Color of bar with day symbols
//                .setAbbreviationsLabelsColor(R.color.color) // Color of symbol labels
//                .setAbbreviationsBarVisibility(int) // Visibility of abbreviations bar
//        .setPagesColor(R.color.sampleLighter) // Color of the calendar background
//                .setSelectionColor(R.color.color) // Color of the selection circle
//                .setSelectionLabelColor(R.color.color) // Color of the label in the circle
//                .setDaysLabelsColor(R.color.color) // Color of days numbers
//                .setAnotherMonthsDaysLabelsColor(R.color.color) // Color of visible days numbers from previous and next month page
//                .setDisabledDaysLabelsColor(R.color.color) // Color of disabled days numbers
//                .setHighlightedDaysLabelsColor(R.color.color) // Color of highlighted days numbers
//                .setTodayColor(R.color.color) // Color of the present day background
//                .setTodayLabelColor(R.color.color) // Color of the today number
//                .setDialogButtonsColor(R.color.color); // Color of "Cancel" and "OK" buttons
//        .setMaximumDaysRange(int) // Maximum number of selectable days in range mode
//        .setNavigationVisibility(int) // Navigation buttons visibility
                .setSelectedDays(daysOffList)
                .build().show();

    }

    /**
     * this mehtid will set the selected days of for this costomer on all delivery items and will lead to complete no delivery situation
     *
     * @param calendars
     */
    private void setDaysOffToFirebase(List<Calendar> calendars) {
        mAlertDialog.show();
        HashMap<String, Object> daysMap = new HashMap<>();
//
        for (Calendar calendar : calendars) {
            String timeStr = calendar.getTimeInMillis() + "";
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            daysMap.put(timeStr, true);
        }
        rootRef.child("days_off").child(myId).child("days").setValue(daysMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successfully updated the off delivery days", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        mAlertDialog.cancel();
                    }
                });

    }

    private void clearAllDaysOff() {
        mAlertDialog.show();
        rootRef.child("days_off").child(myId).child("days").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (ManageVacationsActivity.this != null) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Successfully updated the off delivery days", Toast.LENGTH_SHORT).show();
                        daysOffList.clear();
                        eventDayArrayList.clear();
                        calendarView.setSelectedDates(daysOffList);
                        calendarView.setEvents(eventDayArrayList);

                    } else {
                        Toast.makeText(getApplicationContext(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    mAlertDialog.cancel();
                }
            }
        });
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(true).create();
    }
}