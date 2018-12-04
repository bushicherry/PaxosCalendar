package Paxos;

import java.lang.management.PlatformLoggingMXBean;
import java.net.DatagramSocket;
import java.util.*;

public class LogAndDic {

//    // define variable
//    private Log PLi;
//    private Dic Vi;
//    private final Object lock = new Object();
//
//
//
//    public int get_process(){
//        return PLi.Index;
//    }
//
//    public int get_usernum(){
//        return PLi.Ti.length;
//    }
//
//    public Vector<String> get_user(String meetingname){
//        Vector<String> users = new Vector<>();
//        for(meetingInfo m: Vi.Cld){
//            if(m.name.equals(meetingname)){
//                users.addAll(m.users);
//            }
//        }
//        return users;
//    }
//
//    // initialization
//    public LogAndDic(int n, int i){
//        PLi = new Log(n,i);
//        Vi = new Dic();
//    }
//
//    // insert an event
//    public void Insert( meetingInfo m ){
//        PLi.Insert_E(m);
//        Vi.Insert_Dic(m);
//    }
//
//    public boolean check_user(String mtname, String username){
//        // true means user in the meeting, no means not in the meeting
//        for(meetingInfo m: Vi.Cld){
//            if(m.name.equals(mtname)){
//                if(m.users.contains(username)){
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    // Delete an event
//    public void Delete( String s ){
//        meetingInfo m = new meetingInfo(s);
//        Vi.Delete_Dic(m);
//        PLi.Insert_E(m);
//    }
//
//    // check collision. True means collision happens
//    public meetingInfo check_collision( meetingInfo m){
//        //loop users in m
//        for(String user: m.users){
//            for(meetingInfo meeting: this.Vi.Cld){
//                if(meeting.users.contains(user)){
//                    if(!Algorithm.ifFine(m, meeting)){
//                        return helper4(m, meeting);
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//
//    // for send and receive
//    public boolean has_rec(int[][] T, eRecord eR, int k){
//        return T[k-1][eR.P_ind-1] >= eR.tm;
//    }
//
//    public sendPac PacReady(int k){
//        // k is the index of which Pi
//        Vector<eRecord> my_NP = new Vector<>();
//        for(eRecord m: PLi.log_info){
//            if(!has_rec(PLi.Ti, m, k)){
//                my_NP.add(m);
//            }
//        }
//        return new sendPac("", PLi.Ti, my_NP, PLi.Index);
//    }
//
//
//    //for recovery
//    public sendPac get_log_pac(){
//        sendPac pac = new sendPac("", PLi.Ti, PLi.log_info, PLi.CurTmstmp);
//        return pac;
//    }
//
//    public sendPac get_dic_pac(){
//        Vector<eRecord> tmp1 = new Vector<>();
//        for(meetingInfo e: Vi.Cld){
//            tmp1.add(new eRecord(e, 1,1));
//        }
//        sendPac pac = new sendPac("", PLi.Ti, tmp1, PLi.Index);
//        return pac;
//    }
//
//    public void read_DicAlog_pac(sendPac log_pac, sendPac dic_pac ){
//
//        // reload log
//        if(log_pac != null) {
//            PLi.log_info.addAll(log_pac.NP);
//            PLi.Ti = log_pac.Ti.clone();
//            PLi.CurTmstmp = log_pac.index;
//            PLi.Index = dic_pac.index;
//        }
//        // reload dic
//        if(dic_pac != null) {
//            for(eRecord er: dic_pac.NP){
//                Vi.Cld.add(new meetingInfo(er.op));
//            }
//        }
//
//    }
//
//
//    // deal with rec
//    private boolean helper2(eRecord eR){
//        for(int s = 1; s < PLi.Ti.length; s++){
//            if (!has_rec(PLi.Ti, eR, s)){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean helper3(Vector<eRecord> NE, String meetingname){
//        for(eRecord eR: NE){
//            if(eR.op.name.equals(meetingname)){
//                if(eR.op.users == null){
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    // decide the hexi... order
//    private meetingInfo helper4(meetingInfo m1, meetingInfo m2){
//        if(m1.name.compareTo(m2.name) < 0){
//            return m2;
//        } else return m1;
//    }
//
//
//    public void dealWithReceive(sendPac pac, String myname, HashMap<String, int[] > myhash, DatagramSocket socket){
//        // prepare NE
//        if(pac.NP.size() > 0) {
//            Vector<eRecord> NE = new Vector<>();
//            for (eRecord fR : pac.NP) {
//                if (!has_rec(PLi.Ti, fR, PLi.Index)) {
//                    // which means It's in NE
//                    NE.add(fR);
//                }
//            }
//
//            synchronized (lock) {
//                // update Vi
//                for (eRecord dR : NE) {
//                    if (!Vi.Cld.contains(dR.op)) {
//                        if (dR.op.users == null) {
//                            System.out.println("Error: can't cancel because it doesn't exists");
//                        } else {
//                            if(!helper3(NE, dR.op.name)) {// no delete in NE
//                                // collision meeting coll)m
//                                meetingInfo coll_m = check_collision(dR.op);
//                                //insert
//                                Vi.Insert_Dic((dR.op));
//                                //check collision
//                                if(coll_m != null){
//                                    Algorithm.Cancel(this, coll_m.name, myname, myhash, socket);
//                                }
//                            }
//                        }
//                    } else {
//                        if(dR.op.users == null){
//                            Vi.Delete_Dic(dR.op);
//                        }
//                    }
//
//                    // update log
//                    if(!PLi.log_info.contains(dR)){
//                        PLi.log_info.add(dR);
//                    }
//                }
//                PLi.log_info.removeIf(item -> !helper2(item));
//            }
//
//
//        }
//        // update Ti
//        for (int r = 0; r < PLi.Ti.length; r++){
//            PLi.Ti[PLi.Index-1][r] = Math.max(PLi.Ti[PLi.Index-1][r], pac.Ti[pac.index-1][r]);
//        }
//        for (int r = 0; r < PLi.Ti.length; r++){
//            for(int s = 0; s < PLi.Ti.length; s++){
//                PLi.Ti[r][s] = Math.max(PLi.Ti[r][s], pac.Ti[r][s]);
//            }
//        }
//    }
//
//    // view dictionary
//    public void View_dic(){
//        Vi.printDic();
//    }
//
//    // view log
//    public void View_log(){
//        PLi.printLog();
//
//    }
//
//    // priority queue
//    private PriorityQueue<meetingInfo> get_pQ(){
//        PriorityQueue<meetingInfo> priQ = new PriorityQueue<>(new Comparator<meetingInfo>() {
//            @Override
//            public int compare(meetingInfo t1, meetingInfo t2) {
//                if(t1.day.compareTo(t2.day) == 0){
//
//                    if(t1.start.compareTo(t2.start) == 0){
//
//                        if(t1.name.compareTo(t2.name) == 0){
//                            return 0;
//                        } else return t1.name.compareTo(t2.name);
//
//                    } else return t1.start.compareTo(t2.start);
//
//                } else return t1.day.compareTo(t2.day);
//            }
//        });
//        return priQ;
//    }
//
//
//
//    public void myView(String name){
//        int ind = 0;
//
//        PriorityQueue<meetingInfo> priQ = get_pQ();
//        priQ.addAll(Vi.Cld);
//        PriorityQueue<meetingInfo> pq_copy = new PriorityQueue<>(priQ);
//
//
//        while(!pq_copy.isEmpty()){
//            meetingInfo m = pq_copy.poll();
//            if(m.users.contains(name)){
//                print_all(m);
//                ind = 1;
//            }
//        }
//        if ( ind == 0 ){
//            System.out.println("You have no schedule");
//        }
//    }
//
//    // define record
//    public static class eRecord {
//        meetingInfo op; // operation type
//        int tm; // time stamp
//        int P_ind; // Process index, Pi, that i
//
//
//        public eRecord(meetingInfo a, int b, int c){
//            op = new meetingInfo(a);
//            tm = b;
//            P_ind = c;
//        }
//        @Override
//        public boolean equals(Object o) {
//            if (o instanceof eRecord) {
//                int a = ((eRecord) o).tm;
//                int b = ((eRecord) o).P_ind;
//                return  a == tm && b == P_ind;
//            }
//            return false;
//        }
//
//    }
//
//    public static class sendPac {
//        String msg;
//        int[][] Ti;
//        Vector<eRecord> NP;
//        int index;
//        public sendPac(String m, int[][] t, Vector<eRecord> N, int i){
//            msg = m;
//            Ti = new int[t.length][t.length];
//            for(int i1 = 0; i1 < t.length; i1++){
//                for(int j = 0; j < t.length; j++){
//                    Ti[i1][j] = t[i1][j];
//                }
//            }
//            NP = new Vector<>();
//            for(eRecord eR: N){
//                NP.add(new eRecord(eR.op, eR.tm, eR.P_ind));
//            }
//            index = i;
//        }
//    }
//
//    // print out stuff
//    private String formatTransfer(int k){
//        String s;
//        if(k<10){
//            s = "0" + String.valueOf(k);
//        } else {
//            s = String.valueOf(k);
//        }
//        return s;
//    }
//
//    private void print_date(GregorianCalendar g){
//        System.out.print(formatTransfer(g.get(Calendar.MONTH)+1));
//        System.out.print("/" + formatTransfer(g.get(Calendar.DATE)) + "/");
//        System.out.print(g.get(Calendar.YEAR) + " ");
//    }
//
//    private void print_time(GregorianCalendar g){
//        System.out.print(formatTransfer(g.get(Calendar.HOUR_OF_DAY)) + ":");
//        System.out.print(formatTransfer(g.get(Calendar.MINUTE)) + " ") ;
//    }
//
//    private void print_all(meetingInfo m){
//        if(m.users != null) {
//            System.out.print(m.name + " ");
//            print_date(m.day);
//            print_time(m.start);
//            print_time(m.end);
//            for (int i = 0; i < m.users.size(); i++) {
//                if (i < m.users.size() - 1) {
//                    System.out.print(m.users.get(i) + ",");
//                } else {
//                    System.out.print(m.users.get(i));
//                }
//
//            }
//            System.out.println();
//        } else {
//            System.out.println(m.name);
//        }
//    }
//
//
//    public class Log   {
//        // log information and current time stamp
//        private Vector< eRecord > log_info;
//        private int CurTmstmp;
//        // Index indicates the index in all users
//        private int Index;
//        // matrix in algorithm
//        int[][] Ti;
//        private static final long serialVersionUID = 1L;
//
//        // initialization n is how many users, and
//        //  i is the index for itself
//        private Log(int n, int i) {
//            log_info = new Vector<>();
//            CurTmstmp = 0;
//            Index = i;
//            Ti = new int[n][n];
//        }
//
//        // When insert event
//        private void Insert_E( meetingInfo e ) {
//            synchronized (lock) {
//                CurTmstmp++;
//                Ti[Index-1][Index-1] = CurTmstmp;
//                // create event record
//                log_info.add(new eRecord(e, CurTmstmp, Index));
//            }
//        }
//
//
//        private void printLog(){
//            synchronized (lock) {
//
//
//                if (log_info.size() == 0) {
//                    System.out.println("No Log now");
//                    return;
//                }
//                for (eRecord item : log_info) {
//                    if (item.op.users == null) {
//                        System.out.print("delete ");
//                    } else {
//                        System.out.print("Create ");
//                    }
//                    print_all(item.op);
//                }
//            }
//
//        }
//
//    }
//
//    private class Dic {
//        // calender, a vector of vector
//        // each vector consists of < <Event name>, <Date>, <Start>, <End>, <User1>...<User n> >
//        private Vector< meetingInfo > Cld;
//        private static final long serialVersionUID = 1L;
//
//        // insert x to dictionary
//
//        private Dic(){
//            Cld = new Vector<>();
//        }
//
//        private void Insert_Dic(meetingInfo e){
//            synchronized (lock) {
//                Cld.add(e);
//            }
//        }
//
//        // delete x
//        private void Delete_Dic(meetingInfo e){
//            synchronized (lock) {
//                Cld.remove(e);
//            }
//        }
//
//        private void printDic(){
//            if(Cld.size()==0){
//                System.out.println("No Dic now");
//                return;
//            }
//            synchronized (lock) {
//                PriorityQueue<meetingInfo> priQ = get_pQ();
//                priQ.addAll(Cld);
//                PriorityQueue<meetingInfo> pq_copy = new PriorityQueue<>(priQ);
//                while(!pq_copy.isEmpty()){
//                    print_all(pq_copy.poll());
//                }
//
//            }
//        }
//
//    }
}