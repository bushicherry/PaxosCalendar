package java;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

public class meetingInfo {
    // name of the meeting
    private String name;
    // day and time
    public GregorianCalendar day;
    public GregorianCalendar start;
    public GregorianCalendar end;
    // users
    public Vector<String> users;
    //indicator 1 if insert, 0 if delete

    // constructor for deletion
    public meetingInfo(String n){
        name = n;
        users = null;
        day = null;
        start = null;
        end = null;
    }
    // constructor
    public meetingInfo(String n, GregorianCalendar d, GregorianCalendar s, GregorianCalendar e, Vector<String> u){
        name = n;
        day = new GregorianCalendar(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DATE));
        start = new GregorianCalendar(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DATE),s.get(Calendar.HOUR_OF_DAY), s.get(Calendar.MINUTE));
        end = new GregorianCalendar(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DATE),e.get(Calendar.HOUR_OF_DAY), e.get(Calendar.MINUTE));
        users = new Vector<>(u);
    }

    public meetingInfo(meetingInfo MI){
        if(MI.users != null) {
            name = MI.name;
            day = new GregorianCalendar(MI.day.get(Calendar.YEAR), MI.day.get(Calendar.MONTH), MI.day.get(Calendar.DATE));
            start = new GregorianCalendar(MI.day.get(Calendar.YEAR), MI.day.get(Calendar.MONTH), MI.day.get(Calendar.DATE), MI.start.get(Calendar.HOUR_OF_DAY), MI.start.get(Calendar.MINUTE));
            end = new GregorianCalendar(MI.day.get(Calendar.YEAR), MI.day.get(Calendar.MONTH), MI.day.get(Calendar.DATE), MI.end.get(Calendar.HOUR_OF_DAY), MI.end.get(Calendar.MINUTE));
            users = new Vector<>(MI.users);
        } else {
            name = MI.name;
            users = null;
            day = null;
            start = null;
            end = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof meetingInfo) {
            meetingInfo m = (meetingInfo)o;
            return name.equals(m.name); //Given that name of each meeting is unique
        }
        return false;
    }

}
