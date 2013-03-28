package org.scriptbox.panopticon.apache;

public class ApacheStatus {

    public long totalAccesses;
    public long totalKbytes;
    public double cpuLoad;
    public int uptime;
    public double requestsPerSecond;
    public double bytesPerSecond;
    public double bytesPerRequest;
    public int busyWorkers;
    public int idleWorkers;

    public int waitingForConnection;
    public int startingUp;
    public int readingRequest;
    public int sendingReply;
    public int keepAlive;
    public int closingConnection;
    public int logging;
    public int gracefullyFinishing;
    public int idleCleanup;
    public int openSlot;
    public int unknown;

    public String toString() {
        return "ApacheStatus{ " +
            "totalAccesses=" + totalAccesses +
            ", totalKbytes=" + totalKbytes +
            ", cpuLoad=" + cpuLoad +
            ", uptime=" + uptime +
            ", requestsPerSecond=" + requestsPerSecond +
            ", bytesPerSecond=" + bytesPerSecond +
            ", bytesPerRequest=" + bytesPerRequest +
            ", busyWorkers=" + busyWorkers +
            ", idleWorkers=" + idleWorkers +
            ", waitingForConnection=" + waitingForConnection +
            ", startingUp=" + startingUp +
            ", readingRequest=" + readingRequest +
            ", sendingReply=" + sendingReply +
            ", keepAlive=" + keepAlive +
            ", closingConnection=" + closingConnection +
            ", logging=" + logging +
            ", gracefullyFinishing=" + gracefullyFinishing +
            ", idleCleanup=" + idleCleanup +
            ", openSlot=" + openSlot +
            ", unknown=" + unknown + " }";
    }        
}