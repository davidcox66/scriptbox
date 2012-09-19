package org.scriptbox.util.common.os.proc;

public class ProcessStatus {
    public String user;
    public int pid;
    public int ppid;
    public int cpu;
    public String startTime;
    public String tty;
    public String time;
    public String command; 
    
    public String toString() {
        return "ProcessStatus{ user=" + user + ", pid=" + pid + ", ppid=" + ppid + ", cpu=" + cpu + ", startTime=" + startTime + ", tty=" + tty + ", time=" + time + ", command=" + command + " }";
    }
}
