#!/bin/awk -f
#
# extract user and servlet from the trace file
#
#  usage: extract_outline.awk  trace_file
#  
#  Mar/26/2004 S.Kawamoto

# for CVS
# $Id: extract_outline.awk,v 1.4 2004/05/05 21:08:33 greg Exp $

BEGIN{
  print "<html><body>";
  error = 1;
}


/computeURL/{
  i = match($12,"<");
  if ( i != 0 ) {
    servlet = substr($12,0,i-1);
  } else {
    servlet = $12;
  }

  printf("%s %s %s %s <br>\n",$3,$4,$8,servlet);
}

/Error/{ 
  printf("<A NAME=\"Error%d\"/>",error);
  printf("<font color=red>%s %s %s Error at %s </font><br>\n"\
	 ,$3,$4,$8,$17);
  error++;
}

END{
  printf("<HR>\n");
  printf("<H1>Errors</H1><BR>\n");
  for(i=1;i<error;i++) {
    printf("<A HREF=\"#Error%d\">Error%d</A><BR>\n",i,i);
  }
  printf("<BR><HR>\n");

  print "</body></html>";
}
