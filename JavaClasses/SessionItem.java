package com.example.timer;

public class SessionItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_SESSION = 1;

    private int type;

    // For header type
    private String header;

    // For session type
    private StudySession session;

    // Constructor for header
    public SessionItem(String header) {
        this.type = TYPE_HEADER;
        this.header = header;
    }

    // Constructor for session
    public SessionItem(StudySession session) {
        this.type = TYPE_SESSION;
        this.session = session;
    }

    public int getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public StudySession getSession() {
        return session;
    }
}
