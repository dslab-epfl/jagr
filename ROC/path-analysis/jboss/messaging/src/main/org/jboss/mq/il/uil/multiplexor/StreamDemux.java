/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.il.uil.multiplexor;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

/**
 *  This class is used to demultiplex from a single stream into multiple
 *  streams.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 1.1.1.1 $
 */
public class StreamDemux {

   short            frameSize = 512;
   HashMap          openStreams = new HashMap();
   InputStream      in;
   DataInputStream  objectIn;

   boolean          pumpingData = false;
   byte             inputBuffer[] = new byte[frameSize];

   /**
    *  StreamMux constructor comment.
    *
    * @param  in               Description of Parameter
    * @exception  IOException  Description of Exception
    */
   public StreamDemux( InputStream in )
      throws IOException {
      this.in = in;
      this.objectIn = new DataInputStream( in );
   }

   /**
    *  Creation date: (11/15/00 5:30:55 PM)
    *
    * @param  newFrameSize     short
    * @exception  IOException  Description of Exception
    */
   public void setFrameSize( short newFrameSize )
      throws IOException {
      synchronized ( openStreams ) {
         if ( openStreams.size() > 0 ) {
            throw new IOException( "Cannot change the frame size while there are open streams." );
         }
         frameSize = newFrameSize;
         inputBuffer = new byte[frameSize];
      }
   }

   /**
    *  Creation date: (11/15/00 5:30:55 PM)
    *
    * @return    short
    */
   public short getFrameSize() {
      synchronized ( openStreams ) {
         return frameSize;
      }
   }

   public InputStream getStream( short id )
      throws IOException {
      if ( id == 0 ) {
         throw new IOException( "Stream id 0 is reserved for internal use." );
      }

      InputStream s;
      synchronized ( openStreams ) {
         s = ( InputStream )openStreams.get( new Short( id ) );
         ;

         if ( s != null ) {
            return s;
         }

         s = new DemuxInputStream( this, id );
         openStreams.put( new Short( id ), s );
      }
      return s;
   }

   public int available( DemuxInputStream s )
      throws IOException {
      return objectIn.available();
   }

   /**
    *  Pumps data to all input streams until data for the dest Stream arrives.
    *  Only on thread is allowed to pump data at a time and this method returns
    *  true if it pumped data into its input buffer. It returns false if another
    *  thread is allready pumping data.
    *
    * @param  dest             Description of Parameter
    * @return                  Description of the Returned Value
    * @exception  IOException  Description of Exception
    */
   public boolean pumpData( DemuxInputStream dest )
      throws IOException {

      synchronized ( this ) {
         if ( pumpingData ) {
            return false;
         } else {
            pumpingData = true;
         }
      }

      // Start pumping the data
      short nextFrameSize = frameSize;
      while ( true ) {
         short streamId = objectIn.readShort();
         // Was it a command on the admin stream?
         if ( streamId == 0 ) {
            // Next byte is the command.
            switch ( objectIn.readByte() ) {
               case StreamMux.OPEN_STREAM_COMMAND:
                  getStream( objectIn.readShort() );
                  break;
               case StreamMux.CLOSE_STREAM_COMMAND:
                  DemuxInputStream s;
                  synchronized ( openStreams ) {
                     s = ( DemuxInputStream )openStreams.get( new Short( objectIn.readShort() ) );
                  }
                  if ( s != null ) {
                     closeStream( s.streamId );
                     s.atEOF = true;
                     if ( s == dest ) {
                        break;
                     } else {
                        // Wake up the thread that was waiting for input (it got a EOF)
                        synchronized ( s.bufferMutex ) {
                           s.bufferMutex.notify();
                        }
                     }
                  }
                  break;
               case StreamMux.NEXT_FRAME_SHORT_COMMAND:
                  nextFrameSize = objectIn.readShort();
                  break;
            }

         } else {
            objectIn.readFully( inputBuffer, 0, nextFrameSize );
            DemuxInputStream s;
            synchronized ( openStreams ) {
               s = ( DemuxInputStream )openStreams.get( new Short( streamId ) );
            }
            if ( s == null ) {
               continue;
            }

            s.loadBuffer( inputBuffer, nextFrameSize );
            if ( s == dest ) {
               break;
            }

            nextFrameSize = frameSize;
         }
      }

      synchronized ( this ) {
         pumpingData = false;
      }

      // we are done pumping but another thread may be
      // intrested in pumping.
      synchronized ( openStreams ) {
         Iterator iter = openStreams.values().iterator();
         while ( iter.hasNext() && pumpingData == false ) {
            DemuxInputStream s = ( DemuxInputStream )iter.next();
            synchronized ( s.bufferMutex ) {
               s.bufferMutex.notify();
            }
         }
      }
      return true;
   }

   void closeStream( short id )
      throws IOException {
      if ( id == 0 ) {
         throw new IOException( "Stream id 0 is reserved for internal use." );
      }

      synchronized ( openStreams ) {
         openStreams.remove( new Short( id ) );
      }

   }
}
