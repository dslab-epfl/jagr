/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util;

import java.io.Serializable;

/**
 * Simulates a stop watch with a <i>lap</i> counter.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class StopWatch
   implements Serializable, Cloneable
{
   /** Total time */
   protected long total = 0;

   /** Start time */
   protected long start = -1;

   /** Stop time */
   protected long stop = -1;

   /** The <i>lap</i> count */
   protected int count = 0;

   /** Is the watch started */
   protected boolean running = false;

   /**
    * Default constructor.
    */
   public StopWatch() {}

   /**
    * Construct a StopWatch.
    *
    * @param running    Start the watch
    */
   public StopWatch(final boolean running) {
      if (running) start();
   }

   /**
    * Start the watch.
    *
    * @param reset   True to reset the watch prior to starting.
    */
   public void start(final boolean reset) {
      if (!running) {
         if (reset) reset();
         start = System.currentTimeMillis();
         running = true;
      }
   }

   /**
    * Start the watch.
    */
   public void start() {
      start(false);
   }

   /**
    * Stop the watch.
    *
    * @return  Elapsed time or 0 if the watch was never started.
    */
   public long stop() {
      long lap = 0;

      if (running) {
         count++;
         stop = System.currentTimeMillis();
         lap = stop - start;
         total += lap;
         running = false;
      }

      return lap;
   }

   /**
    * Reset the watch.
    */
   public void reset() {
      start = -1;
      stop = -1;
      total = 0;
      count = 0;
      running = false;
   }

   /**
    * Get the <i>lap</i> count.
    *
    * @return  The <i>lap</i> count.
    */
   public int getLapCount() {
      return count;
   }

   /**
    * Get the elapsed <i>lap</i> time since the watch was started.
    *
    * @return  Elapsed <i>lap</i> time or 0 if the watch was never started
    */
   public long getLapTime() {
      if (start == -1) {
         return 0;
      }
      else if (running) {
         return System.currentTimeMillis() - start;
      }
      else {
         return stop - start;
      }
   }

   /**
    * Get the average <i>lap</i> time since the watch was started.
    *
    * @return  Average <i>lap</i> time since the watch was started.
    */
   public long getAverageLapTime() {
      return (count == 0) ? 0 : getLapTime() / getLapCount();
   }

   /**
    * Get the elapsed time since the watch was created or last reset.
    *
    * @return  Elapsed time or 0 if the watch was never started.
    */
   public long getTime() {
      if (start == -1) {
         return 0;
      }
      else if (running) {
         return total + System.currentTimeMillis() - start;
      }
      else {
         return total;
      }
   }

   /**
    * Check if the watch is running.
    *
    * @return  True if the watch is running.
    */
   public boolean isRunning() {
      return running;
   }

   /**
    * Return a string representation.
    *
    * @return  If the lap count is zero then the string value of
    *          elapsed time since the watch was created or last reset
    *          {@link #getTime} is returned.  If the lap count is greater
    *          than one, then a string in format of
    *          <code>total/average[count]</code> is returned.
    */
   public String toString() {
      if (count <= 1 || running) {
         return String.valueOf(getTime());
      }

      return String.valueOf(getTime()) + "/" + 
         getAverageLapTime() + "[" + getLapCount() + "]";
   }

   /**
    * Return a cloned copy of this object.
    *
    * @return  A cloned copy of this object.
    */
   public Object clone() {
      try {
         return super.clone();
      }
      catch (CloneNotSupportedException e) {
         throw new InternalError();
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                                Wrappers                             //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Base wrapper class for other wrappers.
    */
   private static class Wrapper
      extends StopWatch
   {
      protected StopWatch watch;

      public Wrapper(final StopWatch watch) {
         this.watch = watch;
      }

      public void start(final boolean reset) {
         watch.start(reset);
      }

      public void start() {
         watch.start();
      }

      public long stop() {
         return watch.stop();
      }

      public void reset() {
         watch.reset();
      }

      public long getLapTime() {
         return watch.getLapTime();
      }

      public long getAverageLapTime() {
         return watch.getAverageLapTime();
      }

      public int getLapCount() {
         return watch.getLapCount();
      }

      public long getTime() {
         return watch.getTime();
      }

      public boolean isRunning() {
         return watch.isRunning();
      }

      public String toString() {
         return watch.toString();
      }
   }

   /**
    * Return a synchronized stop watch.
    *
    * @param watch    StopWatch to synchronize.
    * @return         Synchronized stop watch.
    */
   public static StopWatch makeSynchronized(final StopWatch watch) {
      return new Wrapper(watch) {
            public synchronized void start(final boolean reset) {
               this.watch.start(reset);
            }

            public synchronized void start() {
               this.watch.start();
            }

            public synchronized long stop() {
               return this.watch.stop();
            }

            public synchronized void reset() {
               this.watch.reset();
            }

            public synchronized long getLapTime() {
               return this.watch.getLapTime();
            }

            public synchronized long getAverageLapTime() {
               return this.watch.getAverageLapTime();
            }

            public synchronized int getLapCount() {
               return this.watch.getLapCount();
            }

            public synchronized long getTime() {
               return this.watch.getTime();
            }

            public synchronized boolean isRunning() {
               return this.watch.isRunning();
            }

            public synchronized String toString() {
               return this.watch.toString();
            }
         };
   }
}
