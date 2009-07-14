package tracing;

import java.util.*;

/**
 * Generates unique IDs for requests.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: StateDependencyAnalyzer.java,v 1.1 2002/11/18 09:59:11 mikechen Exp $
 */ 


public class StateDependencyAnalyzer {
    Map writes = new HashMap();
    Map reads  = new HashMap();
    Map uses   = new HashMap();
    Map urls   = new HashMap();

    public void addObservation(RequestObservation obs) {
	String id   = obs.requestId;
	int seqNum  = obs.seqNum;
	String name = obs.name;
	
	if (seqNum == 0) {
	    int args = name.indexOf("?");
	    if (args == -1)
		urls.put(id, name);
	    else
		urls.put(id, name.substring(0, args));
	}
	try {
	    if (SqlParser.isSqlQuery(name)) {
		addQuery(id, name);
	    }
	}
	catch (Exception e) {
	    System.err.println(obs);
	    e.printStackTrace();
	}
	
    }

    void addQuery(String id, String sql) throws SqlParserException {
	SqlParser parser = new SqlParser(sql);
	parser.parse();
	if (parser.isUpdate()) {
	    insert(id, parser.getTables(), writes);
	    insert(id, parser.getTables(), uses);
	}
	if (parser.isSelect()) {
	    insert(id, parser.getTables(), reads);
	    insert(id, parser.getTables(), uses);
	}
    }

    void insert(String id, List tableList, Map map) {
	Iterator it = tableList.iterator();
	while (it.hasNext()) {
	    String tableName = (String)(it.next());
	    Set urlSet = (Set)map.get(tableName);
	    if (urlSet == null) {
		urlSet = new HashSet();
		map.put(tableName, urlSet);
	    }
	    String url = (String)urls.get(id);
	    urlSet.add(url);
	    System.out.println("adding url:" + url + " to table:" + tableName);
	}
	
    }

    public void printCurrentDependency() {
	System.out.println("======= vvvvvvvvvvvv =========");
	System.out.println("\nreads:\n"+ reads);
	System.out.println("\nwrites:\n"+ writes);
	System.out.println("\nuses:\n"+ reads);
	System.out.println("======= ^^^^^^^^^^^^ =========");
    }
    

}


