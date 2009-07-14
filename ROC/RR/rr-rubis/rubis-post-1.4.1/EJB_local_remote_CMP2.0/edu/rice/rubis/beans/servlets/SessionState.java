/*
 *  SessionState object keeps all of the RUBiS' Session States
 *
 *                                   Mar/10/2004 S.Kawamoto
 */

package edu.rice.rubis.beans.servlets;


public class SessionState implements java.io.Serializable {
    /* 
      states keeps RR-RUBiS's session state
        states[0]: user id
	states[1]: item id
	states[2]: user id for comment
	states[3]: return url from login page
    */
    private String[] states = new String[4];

    // constructor 
    SessionState(){
	for(int i=0;i<4;i++){
	    states[i]=null;
	}
    }

    // getter and setter for userId
	
    public void setUserId(Integer uid){
	states[0] = SessionFaultInjector.getUserId(uid);
	//states[0] = uid.toString();
	// print new userid for debug
	Debug.println(" [setUserId] new userId: "+states[0]);
    }
    
    public Integer getUserId(){
	if (states[0] == null){
	    return null;
	} else {
	    return new Integer(states[0]);
	}
    }

    // getter and setter for itemId

    public void setItemId(String item){
	states[1] = item;
	Debug.println(" [setItemId] new itemId: "+states[1]);
    }

    public String getItemId(){
	return states[1];
    }

    // getter and setter for to
	
    public void setTo(String t){
	states[2] = t;
	Debug.println(" [setTo] new to: "+states[2]);
    }

    public String getTo(){
	return states[2];
    }

    // getter and setter for url

    public void setURL(String url){
	states[3] = url;
	Debug.println(" [setURL] new url: "+states[3]);
    }

    public String getURL(){
	return states[3];
    }

    public String[] getStates(){
	// debug 
	printStates();
	return states;
    }

    public void setStates(String[] s){
	states = s;
	// debug 
	printStates();
    }

    private void printStates(){
	Debug.println("[getStates] states:"+
		      " uid "+states[0]+
		      " itemId "+states[1]+
		      " to "+states[2]+
		      " url "+states[3]);
    }

    public String toString()
    {
	return "[uid=" + states[0] + ", itemId=" + states[1] +
	    ", to=" + states[2] + ", url=" + states[3] + "]";
    }
}
