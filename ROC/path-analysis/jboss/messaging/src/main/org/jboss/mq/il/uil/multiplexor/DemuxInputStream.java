/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.il.uil.multiplexor;
import java.io.IOException;
import java.io.InputStream;

import java.io.InterruptedIOException;

/**
 *  Objects of this class provide and an InputStream from a StreamDemux. Objects
 *  of this class are created by a StreamDemux object.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 1.1.1.1 $
 */
class DemuxInputStream extends InputStream {

   StreamDemux      streamDemux;
   short            streamId;
   boolean          atEOF = false;

   Object           bufferMutex = new Object();
   byte             buffer[];
   short            bufferEndPos;
   short            bufferStartPos;

   DemuxInputStream( StreamDemux demux, short id ) {
      streamDemux = demux;
      streamId = id;
      buffer = new byte[1000];
      bufferStartPos = 0;
      bufferEndPos = 0;
   }

   public int available()
      throws IOException {
      return getBufferFillSize();
   }

   public void close()
      throws IOException {
      streamDemux.closeStream( streamId );
   }

   public void loadBuffer( byte data[], short dataLength )
      throws IOException {
      int freeSize = 0;
      int dataPos = 0;
      while ( dataPos < dataLength ) {
         synchronized ( bufferMutex ) {
            while ( ( freeSize = getBufferFreeSize() ) == 0 ) {
               try {
                  // Wait till the consumer notifies us he has
                  // removed some data from the buffer.
                  bufferMutex.wait();
               } catch ( InterruptedException e ) {
                  throw new InterruptedIOException( e.getMessage() );
               }
            }
            // the buffer should have free space now.
            freeSize = Math.min( freeSize, dataLength - dataPos );
            for ( int i = 0; i < freeSize; i++ ) {
               buffer[bufferEndPos++] = data[dataPos + i];
               bufferEndPos = bufferEndPos == buffer.length ? 0 : bufferEndPos;
            }
         }
         dataPos += freeSize;
         // the consumer might be waiting for bytes to come in
         synchronized ( bufferMutex ) {
            bufferMutex.notify();
         }
      }
   }

   public int read()
      throws IOException {
      if ( bufferStartPos == bufferEndPos && atEOF ) {
         return -1;
      }
      synchronized ( bufferMutex ) {
         // Wait till the buffer has data
         while ( !atEOF && bufferStartPos == bufferEndPos && !streamDemux.pumpData( this ) ) {
            try {
               // Wait till the producer notifies us he has
               // put some data in the buffer.
               bufferMutex.wait();
            } catch ( InterruptedException e ) {
               throw new InterruptedIOException( e.getMessage() );
            }
         }
      }
      // We might break out due to EOF
      if ( bufferStartPos == bufferEndPos ) {
         return -1;
      }
      // the buffer should have data now.
      byte result = buffer[bufferStartPos++];
      bufferStartPos = bufferStartPos == buffer.length ? 0 : bufferStartPos;
      // the producer might be waiting for free space in the
      // buffer, we have to notify him.
      synchronized ( bufferMutex ) {
         bufferMutex.notify();
      }
      return result & 0xff;
   }

   public int read( byte b[], int off, int len )
      throws IOException {
      if ( b == null ) {
         throw new NullPointerException();
      } else if ( ( off < 0 ) || ( off > b.length ) || ( len < 0 ) || ( ( off + len ) > b.length ) || ( ( off + len ) < 0 ) ) {
         throw new IndexOutOfBoundsException();
      } else if ( len == 0 ) {
         return 0;
      }

      int c = read();
      if ( c == -1 ) {
         return -1;
      }
      b[off] = ( byte )c;
      len = Math.min( available(), len );
      int i = 1;
      try {
         for ( ; i < len; i++ ) {
            c = read();
            b[off + i] = ( byte )c;
         }
      } catch ( IOException ee ) {
      }
      return i;
   }

   private int getBufferFillSize() {
      return bufferStartPos <= bufferEndPos ? bufferEndPos - bufferStartPos : buffer.length - ( bufferStartPos - bufferEndPos );
   }

   private int getBufferFreeSize() {
      return ( buffer.length - 1 ) - getBufferFillSize();
   }
}
