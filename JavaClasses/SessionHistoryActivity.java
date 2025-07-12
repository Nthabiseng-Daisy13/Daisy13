package com.example.timer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);

        TextView textToday = findViewById(R.id.textToday);
        TextView textWeek = findViewById(R.id.textWeek);
        TextView textOverall = findViewById(R.id.textOverall);

        // Get today's date and range for the past 7 days
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String today = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        String endDate = sdf.format(calendar.getTime()); // today
        calendar.add(Calendar.DAY_OF_YEAR, -6); // 7-day range including today
        String startDate = sdf.format(calendar.getTime());

        // Fetch totals in background
        new Thread(() -> {
            SessionDao dao = SessionDatabase.getInstance(getApplicationContext()).sessionDao();

            int todayTotal = dao.getTotalForDay(today) != null ? dao.getTotalForDay(today) : 0;
            int weekTotal = dao.getTotalForRange(startDate, endDate) != null ? dao.getTotalForRange(startDate, endDate) : 0;
            int overallTotal = dao.getTotalOverall() != null ? dao.getTotalOverall() : 0;


            runOnUiThread(() -> {
                textToday.setText("Today: " + todayTotal + " min");
                textWeek.setText("Last 7 days: " + weekTotal + " min");
                textOverall.setText("Total: " + overallTotal + " min");
            });
        }).start();

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Session History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_timer);
        }

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerViewSessions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        new Thread(() -> {
            List<StudySession> sessions = SessionDatabase
                    .getInstance(getApplicationContext())
                    .sessionDao()
                    .getAllSessions();

            runOnUiThread(() -> {
                if (sessions.isEmpty()) {
                    Toast.makeText(this, "No sessions found", Toast.LENGTH_SHORT).show();
                } else {
                    List<SessionItem> items = groupSessionsByDay(sessions);
                    adapter = new SessionAdapter(items);

                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }


    // Handle back/home navigation
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private List<SessionItem> groupSessionsByDay(List<StudySession> sessions) {
        List<SessionItem> items = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String todayStr = sdf.format(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = sdf.format(cal.getTime());

        String currentHeader = "";

        for (StudySession session : sessions) {
            String sessionDate = session.getDate();

            String header;
            if (sessionDate.equals(todayStr)) {
                header = "Today";
            } else if (sessionDate.equals(yesterdayStr)) {
                header = "Yesterday";
            } else {
                header = sessionDate; // Or format nicely, e.g. "Jun 10, 2025"
            }

            if (!header.equals(currentHeader)) {
                items.add(new SessionItem(header)); // Add header item
                currentHeader = header;
            }

            items.add(new SessionItem(session)); // Add session item
        }

        return items;
    }

}
