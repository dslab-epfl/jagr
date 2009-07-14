package tracing;

import java.util.*;

/**
 * Parses simple SQL statements to extract the tables names. 
 * Currently supports SELECT and UPDATE. 
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: SqlParser.java,v 1.2 2002/11/18 09:59:11 mikechen Exp $
 */ 


public class SqlParser {
    String query;
    List tables;
    boolean isUpdate = false;
    boolean isSelect = false;

    public SqlParser(String query) {
	this.query = query;
    }

    public void parse() throws SqlParserException {
	query = query.trim().toLowerCase();
	if (query.startsWith("update")) {
	    String tableStr = query.substring("update".length(), query.indexOf("set")).trim();
	    parseTableStr(tableStr);
	    isUpdate = true;
	}
	else if (query.startsWith("select")) {
	    int end = query.indexOf("where");
	    if (end == -1)
		end = query.length();
	    String tableStr = query.substring(query.indexOf("from")+4, end).trim();
	    parseTableStr(tableStr);
	    isSelect = true;
	}
	else throw new SqlParserException("query type not handled : " + query);
    }

    public static boolean isSqlQuery(String str) {
	str = str.toLowerCase();
	return (str.startsWith("select ") || str.startsWith("update "));
    }
    
    public boolean isUpdate() {
	return isUpdate;
    }

    public boolean isSelect() {
	return isSelect;
    }


    public List getTables() throws SqlParserException {
	if (tables == null)
	    parse();
	return tables;
    }

    void parseTableStr(String tableStr) {
	System.out.println("tableStr:" + tableStr);
	StringTokenizer st = new StringTokenizer(tableStr, ",");
	tables = new ArrayList();
	while (st.hasMoreTokens()) {
	    String table = st.nextToken().trim();
	    if (table.indexOf(" ") != -1)
		tables.add(table.substring(0, table.indexOf(" ")));
	    else 
		tables.add(table);
	}
	
    }
    
    public static void main(String[] args) throws Exception {
	SqlParser parser 
	    //= new SqlParser("UPDATE inventory SET qty = 9 WHERE itemid = 'EST-1'");
	    = new SqlParser("UPDATE inventory SET qty = 9 WHERE itemid = 4");
	//= new SqlParser("select names, job from emp, management where emp.id = management.id");
	
	parser.parse();
	System.out.println("tables     = " + parser.getTables());
	//System.out.println("fields     = " + parser.getFields());
	//System.out.println("conditions = " + parser.getConditions());
	
	parser = new SqlParser("select names, job from emp, management where emp.id = management.id");
	parser.parse();
	System.out.println("tables     = " + parser.getTables());
	
    }
}
