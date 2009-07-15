package org.jboss.test.web.util;

/** A class that is placed into the WEB-INF/lib directory in a report.jar
to test access of JavaBeans from web libs.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
*/
public class ReportBean
{
   private String reportPath;

   public ReportBean()
   {
   }

   public String getReportPath()
   {
      return reportPath;
   }
   public void setReportPath(String path)
   {
      this.reportPath = path;
   }
}
