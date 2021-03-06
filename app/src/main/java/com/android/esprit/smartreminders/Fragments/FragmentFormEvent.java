package com.android.esprit.smartreminders.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.esprit.smartreminders.Entities.Action;
import com.android.esprit.smartreminders.Entities.Event;
import com.android.esprit.smartreminders.Entities.Time;
import com.android.esprit.smartreminders.Entities.TimeTask;
import com.android.esprit.smartreminders.Entities.User;
import com.android.esprit.smartreminders.Enums.DayOfTheWeek;
import com.android.esprit.smartreminders.Enums.StateOfTask;
import com.android.esprit.smartreminders.R;
import com.android.esprit.smartreminders.Services.CallBackWSConsumer;
import com.android.esprit.smartreminders.Services.CallBackWSConsumerGET;
import com.android.esprit.smartreminders.Services.WebServiceEvent;
import com.android.esprit.smartreminders.Services.WebServiceTimeTask;
import com.android.esprit.smartreminders.Test.LocalData;
import com.android.esprit.smartreminders.Test.Notification_reciever;
import com.android.esprit.smartreminders.activities.MainFrame;
import com.android.esprit.smartreminders.broadcastrecivers.AlarmReceiver;
import com.android.esprit.smartreminders.notifications.Message;
import com.android.esprit.smartreminders.notifications.NotificationScheduler;
import com.android.esprit.smartreminders.sessions.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FragmentFormEvent extends FragmentChild implements View.OnClickListener {

    String TAG = "RemindMe";
    LocalData localData;

    SwitchCompat reminderSwitch;
    TextView tvTime;
    TextView EndtvTime;
    RelativeLayout ll_set_time, ll_end_time;

    int hour = 0, min = 0;

    ClipboardManager myClipboard;

    private boolean updateMode;
    private Event event;
    private EditText title;
    private EditText description;
    private Button pushBSunday;
    private Button getPushBMonday;
    private Button pushBTuesday;
    private Button pushBWednsday;
    private Button pushBThursday;
    private Button pushBFriday;
    private Button pushBSaturday;
    private Button RemindMeOn;
    private Button AddPlan;
    private Set<DayOfTheWeek> SelectedDays;
    private String[] Days = new String[7];
    int number = 0;

    private NumberPicker np;

    private static final int Time_PICKER_ID = 0;

    TimePickerDialog.OnTimeSetListener StartListener, EndListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_form_event, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        defineBehaviour();
        initForm();
    }

    private void initViews() {
        updateMode = false;
        title = this.ParentActivity.findViewById(R.id.titleText);
        description = this.ParentActivity.findViewById(R.id.descriptionText);
        np = new NumberPicker(this.getContext());
        SelectedDays = new HashSet<>();
        pushBSunday = this.ParentActivity.findViewById(R.id.pushb_sunday);
        pushBSunday.setOnClickListener(this);
        getPushBMonday = this.ParentActivity.findViewById(R.id.pushb_monday);
        getPushBMonday.setOnClickListener(this);
        pushBTuesday = this.ParentActivity.findViewById(R.id.pushb_tuesday);
        pushBTuesday.setOnClickListener(this);
        pushBWednsday = this.ParentActivity.findViewById(R.id.pushb_wednesday);
        pushBWednsday.setOnClickListener(this);
        pushBThursday = this.ParentActivity.findViewById(R.id.pushb_thursday);
        pushBThursday.setOnClickListener(this);
        pushBFriday = this.ParentActivity.findViewById(R.id.pushb_friday);
        pushBFriday.setOnClickListener(this);
        pushBSaturday = this.ParentActivity.findViewById(R.id.pushb_saturday);
        pushBSaturday.setOnClickListener(this);
        RemindMeOn = this.ParentActivity.findViewById(R.id.RemindMeOn);
        RemindMeOn.setOnClickListener(this);
        AddPlan = this.ParentActivity.findViewById(R.id.AddPlan);
        AddPlan.setOnClickListener(this);

        localData = new LocalData(this.getParentActivity().getApplicationContext());
        localData.set_min(0);
        localData.set_hour(0);
        localData.set_EndHour(0);
        localData.set_EndMin(0);

        myClipboard = (ClipboardManager) this.getParentActivity().getSystemService(this.ParentActivity.CLIPBOARD_SERVICE);

        ll_set_time = this.ParentActivity.findViewById(R.id.ll_set_time);
        ll_end_time = this.ParentActivity.findViewById(R.id.ll_end_time);


        tvTime = this.ParentActivity.findViewById(R.id.tv_start_reminder_time_desc);
        EndtvTime = this.ParentActivity.findViewById(R.id.tv_end_reminder_time_desc);

        reminderSwitch = this.ParentActivity.findViewById(R.id.timerSwitch);

        hour = localData.get_hour();
        min = localData.get_min();

        tvTime.setText(getFormatedTime(hour, min));
        reminderSwitch.setChecked(localData.getReminderStatus());

        if (!localData.getReminderStatus())
            ll_set_time.setAlpha(0.4f);


    }

    private void defineBehaviour() {
        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                localData.setReminderStatus(isChecked);
                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: true");
                    ll_set_time.setAlpha(1f);
                } else {
                    Log.d(TAG, "onCheckedChanged: false");
                    NotificationScheduler.cancelReminder(getContext(), AlarmReceiver.class);
                    ll_set_time.setAlpha(0.4f);
                }
            }
        });

        ll_set_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localData.getReminderStatus())
                    showTimePickerDialog(localData.get_hour(), localData.get_min());
            }
        });

        ll_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localData.getReminderStatus())
                    showTimePickerDialogEnd(localData.get_hour(), localData.get_min());
            }
        });
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.pushb_sunday:
                if (SelectedDays.contains(DayOfTheWeek.Sunday)) {
                    pushBSunday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    pushBSunday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Sunday);
                } else {
                    pushBSunday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    pushBSunday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Sunday);
                }
                break;
            case R.id.pushb_monday:
                if (SelectedDays.contains(DayOfTheWeek.Monday)) {
                    getPushBMonday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    getPushBMonday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Monday);
                } else {
                    getPushBMonday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    getPushBMonday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Monday);
                }
                break;
            case R.id.pushb_friday:
                if (SelectedDays.contains(DayOfTheWeek.Friday)) {
                    pushBFriday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    pushBFriday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Friday);
                } else {
                    pushBFriday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    pushBFriday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Friday);
                }
                break;
            case R.id.pushb_saturday:
                if (SelectedDays.contains(DayOfTheWeek.Saturday)) {
                    pushBSaturday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    pushBSaturday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Saturday);
                } else {
                    pushBSaturday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    pushBSaturday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Saturday);
                }
                break;
            case R.id.pushb_thursday:
                if (SelectedDays.contains(DayOfTheWeek.Thursday)) {
                    pushBThursday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    pushBThursday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Thursday);
                } else {
                    pushBThursday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    pushBThursday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Thursday);
                }
                break;
            case R.id.pushb_tuesday:
                if (SelectedDays.contains(DayOfTheWeek.Tuesday)) {
                    pushBTuesday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    pushBTuesday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Tuesday);
                } else {
                    pushBTuesday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    pushBTuesday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Tuesday);
                }

                break;
            case R.id.pushb_wednesday:
                if (SelectedDays.contains(DayOfTheWeek.Wednesday)) {
                    pushBWednsday.setBackground(getResources().getDrawable(R.drawable.roundbutton));
                    pushBWednsday.setTextColor(getResources().getColor(R.color.white));
                    SelectedDays.remove(DayOfTheWeek.Wednesday);
                } else {
                    pushBWednsday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
                    pushBWednsday.setTextColor(getResources().getColor(R.color.black));
                    SelectedDays.add(DayOfTheWeek.Wednesday);
                }

                break;
          /*  case R.id.StartTimeBtn:
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), FragmentFormEvent.this::onTimeSet, StartHour, StartMin, DateFormat.is24HourFormat(getActivity()));
                timePickerDialog.show();
                System.out.println(" ####################### Start Min : "+StartMin);
                break;
            case R.id.EndTimeBtn:
                Calendar c2 = Calendar.getInstance();
                TimePickerDialog timePickerDialog2 = new TimePickerDialog(getActivity(), FragmentFormEvent.this::onTimeSet2, EndHour, EndMin, DateFormat.is24HourFormat(getActivity()));
                timePickerDialog2.show();
                System.out.println(" ######################## End Min : "+EndMin);
                break; */
            case R.id.RemindMeOn:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Remind Me Before");
                np.setMinValue(0);
                np.setMaxValue(59);
                np.setWrapSelectorWheel(true);
                np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                        number = 0;
                        number = i1;
                    }
                });
                if (np.getParent() == null) {
                    alertDialog.setView(np);
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Choose", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getActivity(), number + " minutes Before the event", Toast.LENGTH_SHORT).show();
                        }
                    });
                    if (!alertDialog.isShowing())
                        alertDialog.show();
                } else {
                    ((ViewGroup) np.getParent()).removeView(np);
                }
                break;
            case R.id.AddPlan:
                if (title.getText().toString().equals("")) {
                    title.setBackgroundResource(R.drawable.backtext);
                    Toast.makeText(getActivity(), "Title should not be empty!", Toast.LENGTH_SHORT).show();
                    break;
                } else title.setBackgroundResource(R.color.white);
                if (description.getText().toString().equals("")) {
                    description.setBackgroundResource(R.drawable.backtext);
                    Toast.makeText(getActivity(), "Description should not be empty!", Toast.LENGTH_SHORT).show();
                    break;
                } else description.setBackgroundResource(R.color.white);
                if (number == 0) {
                    Toast.makeText(getActivity(), "Don't forget the Remind Me On button!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (SelectedDays.isEmpty()) {
                    Toast.makeText(getActivity(), "Don't forget to pick the Day!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (localData.get_hour() == 0 && localData.get_min() == 0) {
                    Toast.makeText(getActivity(), "Don't forget to pick when the Event starts!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (localData.get_EndHour() == 0 && localData.get_EndMin() == 0) {
                    Toast.makeText(getActivity(), "Don't forget to pick when the Event ends!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if ((localData.get_min() - number) < 0) {
                    localData.set_hour(localData.get_hour() - 1);
                    localData.set_min(60 - number);
                } else {
                    localData.set_min(localData.get_min() - number);
                }
                Event event = new Event();
                event.setDay(SelectedDays);
                event.setEndTime(new Time(localData.get_EndHour(), localData.get_EndMin()));
                event.setStartTime(new Time(localData.get_hour(), localData.get_min()));
                System.out.println("Start Hour " + localData.get_hour() + " Start Min : " + localData.get_min());
                System.out.println("End Hour " + localData.get_EndHour() + " End Min : " + localData.get_EndMin());
                event.setTitle(title.getText().toString());
                event.setDescription(description.getText().toString());
                event.setOwner(Session.getSession(this.ParentActivity).getSessionUser());
                event.setReminder(number);
                event.setState(StateOfTask.PENDING);
                WebServiceEvent wse = new WebServiceEvent(this.ParentActivity, new CallBackWSConsumer<Event>() {
                    @Override
                    public void OnHostUnreachable() {
                        CharSequence text = getString(R.string.hostunreachable);
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(FragmentFormEvent.this.ParentActivity.getApplicationContext(), text, duration);
                        toast.show();
                    }
                });
                try {
                    updateDataBase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                for (DayOfTheWeek d : SelectedDays) {
                    if (d == DayOfTheWeek.DayOfWeekForID(day)) {
                        if (calendar.getTime().getHours() <= localData.get_hour() && calendar.getTime().getMinutes() <= localData.get_min()) {
                            System.out.println("I'M INSIDE THE 3rd SCOOP");
                            System.out.println("EVENT NAME : " + title + " EVENT HOUR : " + localData.get_hour() + ":" + localData.get_min());
                            NotificationScheduler.setReminder(getContext(), AlarmReceiver.class, localData.get_hour(), localData.get_min());
                            Toast.makeText(getActivity(), "Event added!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), MainFrame.class);
                            startActivity(intent);
                        }
                    } else {
                        NotificationScheduler.setInExactReminder(getContext(), AlarmReceiver.class, localData.get_hour(), localData.get_min());

                      //  Intent intent = new Intent(getContext(), MainFrame.class);
                     //   startActivity(intent);
                    }
                }
        }
    }


    public void setAlarm(int dayOfWeek, int AlarmHrsInInt, int AlarmMinsInInt, int amorpm) {
        Calendar alarmCalendar = Calendar.getInstance();
        alarmCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        alarmCalendar.set(Calendar.HOUR, AlarmHrsInInt);
        alarmCalendar.set(Calendar.MINUTE, AlarmMinsInInt);
        System.out.println("########## Reminder SET FOR : " + AlarmHrsInInt + " : " + AlarmMinsInInt);
        alarmCalendar.set(Calendar.SECOND, 0);
        alarmCalendar.set(Calendar.AM_PM, amorpm);

        Long alarmTime = alarmCalendar.getTimeInMillis();

        AlarmManager am = (AlarmManager) this.ParentActivity.getSystemService(this.ParentActivity.ALARM_SERVICE);
        Intent intent = new Intent(this.ParentActivity.getApplicationContext(), Notification_reciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.ParentActivity.getApplicationContext(), 1, intent, 0);
        am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        Toast.makeText(getActivity(), "Notification Activated", Toast.LENGTH_SHORT).show();
        am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000, pendingIntent);
    }

    private void showTimePickerDialog(int h, int m) {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.timepicker_header, null);

        TimePickerDialog builder = new TimePickerDialog(getContext(), R.style.DialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                        Log.d(TAG, "StartTimeSet: hour " + hour);
                        Log.d(TAG, "StartTimeSet: min " + min);
                        localData.set_hour(hour);
                        localData.set_min(min);
                        tvTime.setText(getFormatedTime(hour, min));
                        //   NotificationScheduler.setReminder(getContext(), AlarmReceiver.class, localData.get_hour(), localData.get_min());
                    }
                }, h, m, false);

        builder.setCustomTitle(view);
        builder.show();
    }

    private void showTimePickerDialogEnd(int h, int m) {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.timepicker_header, null);
        TimePickerDialog builder = new TimePickerDialog(getContext(), R.style.DialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                        Log.d(TAG, "EndTimeSet: hour " + hour);
                        Log.d(TAG, "EndTimeSet: min " + min);
                        localData.set_EndHour(hour);
                        localData.set_EndMin(min);
                        EndtvTime.setText(getFormatedTime(hour, min));
                        //   NotificationScheduler.setReminder(getContext(), AlarmReceiver.class, localData.get_hour(), localData.get_min());
                    }
                }, h, m, false);

        builder.setCustomTitle(view);
        builder.show();
    }

    public String getFormatedTime(int h, int m) {
        final String OLD_FORMAT = "HH:mm";
        final String NEW_FORMAT = "hh:mm a";

        String oldDateString = h + ":" + m;
        String newDateString = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, getCurrentLocale());
            Date d = sdf.parse(oldDateString);
            sdf.applyPattern(NEW_FORMAT);
            newDateString = sdf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newDateString;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }

    private void initForm() {
        this.event = (Event) ((MainFrame) getParentActivity()).getEditedObject();
        if (this.event != null) {
            updateMode = true;
            localData.set_hour(event.getStartTime().getHour());
            localData.set_min(event.getStartTime().getMinute());
            localData.set_EndHour(event.getEndTime().getHour());
            localData.set_EndMin(event.getEndTime().getMinute());
            this.SelectedDays = event.getDays();
            this.title.setText(event.getTitle());
            this.description.setText(event.getDescription());
            this.tvTime.setText(event.getStartTime().toString());
            this.EndtvTime.setText(event.getEndTime().toString());
            this.number = event.getReminder();
            updateDaysButtons();
            this.AddPlan.setText("UPDATE Event");
        }else
            this.AddPlan.setText("Add Event");
    }

    private void updateDataBase() {

        WebServiceEvent ws = new WebServiceEvent(FragmentFormEvent.this.ParentActivity, new CallBackWSConsumer<Event>() {
            @Override
            public void OnResultPresent() {
                String msg = "";
                if (updateMode)
                    msg = "Event Updated !";
                else
                    msg = "Event added!";
                Message.message(FragmentFormEvent.this.ParentActivity,msg);
                FragmentFormEvent.this.ParentActivity.goToUnStackedFragment(new EventsndMettingsFragment());
            }

            @Override
            public void OnHostUnreachable() {
                Message.message(FragmentFormEvent.this.ParentActivity.getApplicationContext(), getString(R.string.hostunreachable));
            }
        });
        Event t = new Event();
        t.setDay(SelectedDays);
        t.setStartTime(new Time(localData.get_hour(), localData.get_min()));
        t.setEndTime(new Time(localData.get_EndHour(), localData.get_EndMin()));
        t.setReminder(number);
        t.setDescription(description.getText().toString());
        t.setState(StateOfTask.IN_PROGRESS);
        System.out.println("YOUSEEEEER : "+Session.getSession(this.ParentActivity).getSessionUser());
        t.setOwner(Session.getSession(this.ParentActivity).getSessionUser());
        t.setTitle(title.getText().toString());
        if (updateMode) {
            System.out.println(t);
            t.setId(event.getId());
            t.setOwner(Session.getSession(this.ParentActivity).getSessionUser());
            ws.update(t); }
        else
            ws.insert(t);
    }

    private void updateDaysButtons() {
        if (event.getDays().contains(DayOfTheWeek.Monday)) {

            getPushBMonday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            getPushBMonday.setTextColor(getResources().getColor(R.color.black));

        }
        if (event.getDays().contains(DayOfTheWeek.Sunday)) {

            pushBSunday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            pushBSunday.setTextColor(getResources().getColor(R.color.black));
        }
        if (event.getDays().contains(DayOfTheWeek.Tuesday)) {

            pushBTuesday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            pushBTuesday.setTextColor(getResources().getColor(R.color.black));
        }
        if (event.getDays().contains(DayOfTheWeek.Thursday)) {

            pushBThursday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            pushBThursday.setTextColor(getResources().getColor(R.color.black));
        }
        if (event.getDays().contains(DayOfTheWeek.Friday)) {

            pushBFriday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            pushBFriday.setTextColor(getResources().getColor(R.color.black));
        }
        if (event.getDays().contains(DayOfTheWeek.Saturday)) {

            pushBSaturday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            pushBSaturday.setTextColor(getResources().getColor(R.color.black));
        }
        if (event.getDays().contains(DayOfTheWeek.Wednesday)) {

            pushBWednsday.setBackground(getResources().getDrawable(R.drawable.roundbutton_active));
            pushBWednsday.setTextColor(getResources().getColor(R.color.black));
        }


    }

}
