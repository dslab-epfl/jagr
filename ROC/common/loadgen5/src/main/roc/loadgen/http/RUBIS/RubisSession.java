package roc.loadgen.http.rubis;

import roc.loadgen.http.HttpSession;
import java.util.Random;

public class RubisSession extends HttpSession
{
    private String currentUserNick;
    private int itemID;
    private int nextbid;
    
    protected void handleCommand(String command)
    {
	Random r = new Random();
	if(command.equalsIgnoreCase("$randomizeuser"))
	    {
		int usernum = r.nextInt() % 100;
		currentUserId = Integer.toString(usernum);
		currentUserNick = "user" + usernum;
		currentPassword = "password" + usernum;
	    }
	else if(command.equalsIgnoreCase("$pickitem"))
	    {
		itemID = r.nextInt() % 10000;
	    }
	else if(command.equalsIgnoreCase("$setbid"))
	    {
		nextbid = r.nextInt() % 5000;
	    }
	else
	    super.handleCommand(command);
    }

    protected String doVariableReplacement(String s) 
    {
	if(s.indexOf("$NICKNAME") != -1)
	    s = replaceString(s, "$NICKNAME", currentUserNick);
	if(s.indexOf("$ITEM") != -1)
	    s = replaceString(s, "$ITEM", Integer.toString(itemID));
	if(s.indexOf("$BID") != -1)
	    s = replaceString(s, "$BID", Integer.toString(nextbid));

	return (super.doVariableReplacement(s));
    }
}
