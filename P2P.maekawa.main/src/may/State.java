package may;

import java.io.Serializable;

/**
*
* @author Dan
*/
public class State implements Serializable
{
    String ip;
    Integer status;
    Integer chair;
    
    //I WANT/RELEASE CHAIR
    public State(String ip, int status, int chair)
    {
        this.ip = ip; //node's IP
        this.status = status; // WANTED 0, RELEASED 1
        this.chair = chair;
    }

    public String getIP() {
        return ip;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public Integer getChair() {
        return chair;
    }
    
    public void setIP(String ip) {
        this.ip = ip;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void setChair(int chair) {
        this.chair = chair;
    }
}
