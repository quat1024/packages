package agency.highlysuspect.packages.net;

import java.util.ArrayList;

public enum PSneakingStatus
{
    IS_SNEAKING((byte)0),
    NOT_SNEAKING((byte)1);
    // Java doesn't have byte literals, yay.


    private final static ArrayList<PSneakingStatus> statusValues = new ArrayList<>();
    public final   byte                             netValue;

    // This is mostly just a convenience wrapper, because calling getOrdinal and casting all the time is unreadable.
    PSneakingStatus(byte netValue)
    {
        this.netValue = netValue;
    }

    // Can potentially get malformed packets, either due to error check failure or bad actors.
    // This is 10^-5 behavior, but it'd crash the server and be really annoying to troubleshoot.
    static PSneakingStatus safeGetStatusFromByte(byte netValue)
    {
        if(netValue < PSneakingStatus.values().length)
        {
            return PSneakingStatus.values()[netValue];
        }
        return NOT_SNEAKING;
    }
}
