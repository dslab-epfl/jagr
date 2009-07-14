// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: NullHandler.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.OutputStream;
import org.mortbay.http.ChunkableOutputStream;
import org.mortbay.util.Code;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.Log;
import org.mortbay.util.StringUtil;
import org.mortbay.util.ByteArrayISO8859Writer;

/* ------------------------------------------------------------ */
/** Abstract HTTP Handler.
 * @deprecated Use AbstractHttpHandler
 * @version $Id: NullHandler.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
abstract public class NullHandler extends AbstractHttpHandler
{}




