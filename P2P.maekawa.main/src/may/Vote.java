package may;

import java.io.Serializable;

/**
*
* @author Dan
*/
public class Vote implements Serializable
{
    public String i;
    public String voteFor;
    public int chair;

    //[THIS GUY , VOTES FOR THIS GUY, FOR THIS CHAIR ]
    public Vote(String i, String voteFor, int chair)
    {
        this.i = i;
        this.voteFor = voteFor;
        this.chair = chair;
    }


    public String getI()
    {
        return i;
    }
    
    public String getVoteFor()
    {
        return voteFor;
    }
    
    public int getChair()
    {
        return chair;
    }
}
