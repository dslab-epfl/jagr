package roc.recomgr.event;

import roc.recomgr.*;

public class FailureReportEvent extends Event {

    public static final String TYPE = "failurereportevent";

    private FailureReport report;
    
    public FailureReportEvent( FailureReport report ) {
	this.report = report;
    }

    public FailureReport getReport() {
	return report;
    }

    public String getType() {
	return TYPE;
    }

    public String toString() {
	return report.toString();
    }
}
