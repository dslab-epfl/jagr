// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ThreadPool.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.util;

import java.io.InterruptedIOException;
import java.io.ObjectInputValidation;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;

/* ------------------------------------------------------------ */
/** A pool of threads.
 * <p>
 * Avoids the expense of thread creation by pooling threads after
 * their run methods exit for reuse.
 * <p>
 * If the maximum pool size is reached, jobs wait for a free thread.
 * By default there is no maximum pool size.  Idle threads timeout
 * and terminate until the minimum number of threads are running.
 * <p>
 * This implementation uses the run(Object) method to place a
 * job on a queue, which is read by the getJob(timeout) method.
 * Derived implementations may specialize getJob(timeout) to
 * obtain jobs from other sources without queing overheads.
 *
 * @version $Id: ThreadPool.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Juancarlo Añez <juancarlo@modelistica.com>
 * @author Greg Wilkins <gregw@mortbay.com>
 */
public class ThreadPool
    implements LifeCycle, Serializable
{
    /* ------------------------------------------------------------ */
    /** The number of times a null lock check should synchronize.
     */
    public static int __nullLockChecks =
        Integer.getInteger("THREADPOOL_NULL_LOCK_CHECKS",2).intValue();

    /* ------------------------------------------------------------ */
    static int __maxThreads = 
        Integer.getInteger("THREADPOOL_MAX_THREADS",256).intValue();
    static int __minThreads =
        Integer.getInteger("THREADPOOL_MIN_THREADS",2).intValue();
    static String __threadClass =
        System.getProperty("THREADPOOL_THREAD_CLASS");
    
    /* ------------------------------------------------------------------- */
    private int _maxThreads = __maxThreads;
    private int _minThreads = __minThreads;
    private int _maxIdleTimeMs=10000;
    private int _maxStopTimeMs=-1;
    private String _name;
    private String _threadClassName;
    
    private transient Class _threadClass;           
    private transient Constructor _constructThread; 

    private transient HashSet _threadSet;
    private transient BlockingQueue _queue;
    private transient int _queueChecks;
    private transient int _threadId=0;
    private transient HashSet _idleSet=new HashSet();
    private transient boolean _running=false;
    

    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public ThreadPool() 
    {
        try
        {
            if (__threadClass!=null)
                _threadClass = Loader.loadClass(this.getClass(), __threadClass );
            else
                _threadClass = PoolThread.class;
            Code.debug("Using thread class '", _threadClass.getName(),"'");
        }
        catch( Exception e )
        {
            Code.warning( "Invalid thread class (ignored) ",e );
            _threadClass = PoolThread.class;
        }
        setThreadClass(_threadClass);
    }
    
    /* ------------------------------------------------------------------- */
    /* Construct
     * @param name Pool name
     */
    public ThreadPool(String name) 
    {
        this();
        _name=name;
    }

    /* ------------------------------------------------------------ */
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _idleSet=new HashSet();
        if (_threadClass==null || !_threadClass.getName().equals(_threadClassName))
        {
            try
            {
                setThreadClass(Loader.loadClass(ThreadPool.class,_threadClassName));
            }
            catch (Exception e)
            {
                Code.warning(e);
                throw new java.io.InvalidObjectException(e.toString());
            }
        }
    }
    

    /* ------------------------------------------------------------ */
    /** 
     * @return The name of the ThreadPool.
     */
    public String getName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param name Name of the ThreadPool to use when naming Threads.
     */
    public void setName(String name)
    {
        _name=name;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the Thread class.
     * Sets the class used for threads in the thread pool. The class
     * must have a constractor taking a Runnable.
     * @param threadClas The class
     * @exception IllegalStateException If the pool has already
     *            been started.
     */
    public synchronized void setThreadClass(Class threadClass)
        throws IllegalStateException
    {
        Code.debug("setThreadClass("+threadClass+")");
        
        if (_running)
            throw new IllegalStateException("Thread Pool Running");
        
        _threadClass=threadClass;
        _threadClassName=_threadClass.getName();
                
        if(_threadClass == null ||
            !Thread.class.isAssignableFrom( _threadClass ) )
        {
            Code.warning( "Invalid thread class (ignored) "+
                          _threadClass.getName() );
            _threadClass = PoolThread.class;
        }

        try
        {
            Class[] args ={java.lang.Runnable.class};
            _constructThread = _threadClass.getConstructor(args);
        }
        catch(Exception e)
        {
            Code.warning("Invalid thread class (ignored)",e);
            setThreadClass(PoolThread.class);
        }

        if (_name==null)
        {
            _name=getClass().getName();
            _name=_name.substring(_name.lastIndexOf('.')+1);
        }
    }

    /* ------------------------------------------------------------ */
    public Class getThreadClass()
    {
        return _threadClass;
    }
    
    /* ------------------------------------------------------------ */
    /** Handle a job.
     * Unless the job is an instance of Runnable, then
     * this method must be specialized by a derived class.
     * @param job The Job to handle.  If it implements Runnable,
     * this implementation calls run().
     */
    protected void handle(Object job)
        throws InterruptedException
    {
        if (job!=null && job instanceof Runnable)
            ((Runnable)job).run();
        else
            Code.warning("Invalid job: "+job);
    }

    /* ------------------------------------------------------------ */
    /** Is the pool running jobs.
     * @return True if start() has been called.
     */
    public boolean isStarted()
    {
        return _running && _threadSet!=null;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the number of threads in the pool.
     * @return Number of threads
     */
    public int getThreads()
    {
        if (_threadSet==null)
            return 0;
        return _threadSet.size();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the number of threads in the pool.
     * @return Number of threads
     */
    public int getIdleThreads()
    {
        if (_idleSet==null)
            return 0;
        return _idleSet.size();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the minimum number of threads.
     * @return minimum number of threads.
     */
    public int getMinThreads()
    {
        return _minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the minimum number of threads.
     * @param minThreads minimum number of threads
     */
    public void setMinThreads(int minThreads)
    {
        _minThreads=minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @return maximum number of threads.
     */
    public int getMaxThreads()
    {
        return _maxThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @param maxThreads maximum number of threads.
     */
    public void setMaxThreads(int maxThreads)
    {
        _maxThreads=maxThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the maximum thread idle time.
     * @return Max idle time in ms.
     */
    public int getMaxIdleTimeMs()
    {
        return _maxIdleTimeMs;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum thread idle time.
     * Threads that are idle for longer than this period may be
     * stopped.
     * @param maxIdleTimeMs Max idle time in ms.
     */
    public void setMaxIdleTimeMs(int maxIdleTimeMs)
    {
        _maxIdleTimeMs=maxIdleTimeMs;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the maximum thread stop time.
     * Threads that do not stop within this time are interrupted and
     * then discarded.  If <0 the max idle time is used instead.
     * @return Max stop time in ms.
     */
    public int getMaxStopTimeMs()
    {
        return _maxStopTimeMs;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum thread stop time.
     * Threads that do not stop within this time are interrupted and
     * then discarded.  If <=0 the max idle time is used instead.
     * @param maxIdleTimeMs Max stop time in ms.
     */
    public void setMaxStopTimeMs(int maxStopTimeMs)
    {
        _maxStopTimeMs=maxStopTimeMs;
    }
    
    /* ------------------------------------------------------------ */
    /* Start the ThreadPool.
     * Construct the minimum number of threads.
     */
    synchronized public void start()
        throws Exception
    {   
        if (_running)
            return;
        Code.debug("Start Pool ",_name);

        // Start the threads
        _running=true;
        _threadSet=new HashSet(_maxThreads+_maxThreads/2+13);
        for (int i=0;i<_minThreads;i++)
            newThread();
    }

    /* ------------------------------------------------------------ */
    /** Stop the ThreadPool.
     * New jobs are no longer accepted,idle threads are interrupted
     * and stopJob is called on active threads.
     * The method then waits 
     * min(getMaxStopTimeMs(),getMaxIdleTimeMs()), for all jobs to
     * stop, at which time killJob is called.
     */
    public void stop()
        throws InterruptedException
    {
        Code.debug("Stop ThreadPool ",_name);
        _running=false;
        
        // setup timing for stop
        long now = System.currentTimeMillis();
        long stopped_at = now;
        int wait_time = getMaxStopTimeMs();
        if (wait_time<0)
            wait_time = getMaxIdleTimeMs();
        int sleep_time=wait_time/16;
        if (sleep_time<=0)
            sleep_time=100;


        // If we have threads, interrupt or stop them.
        if (_threadSet!=null && !_threadSet.isEmpty())
        {
            synchronized(this)
            {
                // for all threads
                Iterator iter = _threadSet.iterator();
                while(iter.hasNext())
                {
                    Thread thread=(Thread)iter.next();

                    if (_idleSet.contains(thread))
                    {
                        // interrupt idle thread
                        thread.interrupt();
                    }
                    else 
                    {
                        // request the job is stopped;
                        if (thread instanceof PoolThread)
                            stopJob(thread,((PoolThread)thread).getJob());
                        else
                            stopJob(thread,null);
                    }
                    Thread.yield();
                }
            }
        }
        
        // While we still have some threads and have not exceeded our
        // wait time.
        while (_threadSet!=null && !_threadSet.isEmpty() && now-stopped_at<=wait_time)
        {
            // wait for jobs to end, with backing off timer
            if (sleep_time>2000)
                Log.event("Stop waiting "+(sleep_time+999)/1000+"s ");
            Thread.sleep(sleep_time);
            now=System.currentTimeMillis();
            sleep_time*=2;
            if (now-stopped_at<sleep_time)
                sleep_time=(int)(now-stopped_at);
        }

            
        // If we STILL have threads, interrupt or kill them.
        if (_threadSet!=null && !_threadSet.isEmpty())
        {
            synchronized(this)
            {
                // for all threads
                Iterator iter = _threadSet.iterator();
                while(iter.hasNext())
                {
                    Thread thread=(Thread)iter.next();

                    if (_idleSet.contains(thread))
                    {
                        // interrupt idle thread
                        thread.interrupt();
                    }
                    else
                    {
                        // request the job is killed;
                        if (thread instanceof PoolThread)
                            killJob(thread,((PoolThread)thread).getJob());
                        else
                            killJob(thread,null);
                    }
                }
            }
            Thread.yield();
        }
        
        Thread.yield();
        
        if (_threadSet!=null && !_threadSet.isEmpty())
        {
            _threadSet.clear();
            _threadSet=null;
            Code.warning("All threads could not be stopped or killed");
        }
    }

    /* ------------------------------------------------------------ */
    /** Stop a job.
     * Called by stop() to encourage a active job to stop.
     * Implementations of this method are under no obligation to
     * interrupt active work and the default implementation waits for
     * the job to complete.
     * The default implementation interrupts inactive PoolThreads.
     * @param thread The Thread running the job
     * @param job The job, or null if it cannot be determined
     */
    protected void stopJob(Thread thread, Object job)
    {
        if (thread instanceof PoolThread)
        {
            PoolThread poolThread = (PoolThread)thread;
            if (!poolThread.isActive())
            {
                Log.event("Interrupt inactive "+thread);
                thread.interrupt();
                return;
            }
        }
        Log.event("Wait for "+thread);
    }
    
    /* ------------------------------------------------------------ */
    /** Kill a job.
     * Called by stop() to finally discard a job that has not stopped.
     * Implementations of this method should make all reasonable
     * attempts to interrupt the job and free any resources held.
     * The default implementation interrupts all threads.
     * @param thread The Thread running the job
     * @param job The job, or null if it cannot be determined
     */
    protected void killJob(Thread thread,Object job)
    {
        Log.event("Interrupt "+thread);
        thread.interrupt();
    }
    
    /* ------------------------------------------------------------ */
    /* Start a new Thread.
     */
    private synchronized void newThread()
        throws InvocationTargetException,IllegalAccessException,InstantiationException
    {
        Runnable runner = new JobRunner();
        Object[] args = {runner};
        Thread thread=
            (Thread)_constructThread.newInstance(args);
        thread.setName(_name+"-"+(_threadId++));
        _threadSet.add(thread);
        thread.start();
    }
    
  
    /* ------------------------------------------------------------ */
    /** Join the ThreadPool.
     * Wait for all threads to complete.
     * @exception java.lang.InterruptedException 
     */
    final public void join() 
        throws java.lang.InterruptedException
    {
        while(_threadSet!=null && _threadSet.size()>0)
        {
            Thread thread=null;
            synchronized(this)
            {
                Iterator iter=_threadSet.iterator();
                if(iter.hasNext())
                    thread=(Thread)iter.next();
            }
            if (thread!=null)
                thread.join();
        }
    }
  
    /* ------------------------------------------------------------ */
    /** Get a job.
     * This method is called by the ThreadPool to get jobs.
     * The call blocks until a job is available.
     * The default implementation removes jobs from the BlockingQueue
     * used by the run(Object) method. Derived implementations of
     * ThreadPool may specialize this method to obtain jobs from other
     * sources.
     * @param idleTimeoutMs The timout to wait for a job.
     * @return Job or null if no job available after timeout.
     * @exception InterruptedException 
     * @exception InterruptedIOException 
     */
    protected Object getJob(int idleTimeoutMs)
        throws InterruptedException, InterruptedIOException
    {
        if (_queue==null || _queueChecks<__nullLockChecks)
        {
            synchronized(this)
            {
                if (_queue==null)
                    _queue=new BlockingQueue(_maxThreads);
                _queueChecks++;
            }
        }
        
        return _queue.get(idleTimeoutMs);
    }
    

    /* ------------------------------------------------------------ */
    /** Run job.
     * Give a job to the pool. The job is passed via a BlockingQueue
     * with the same capacity as the ThreadPool.
     * @param job.  If the job is derived from Runnable, the run method
     * is called, otherwise it is passed as the argument to the handle
     * method.
     */
    public void run(Object job)
        throws InterruptedException
    {
        if (!_running)
            throw new IllegalStateException("Not started");
        
        if (job==null)
        {
            Code.warning("Null Job");
            return;
        }
        
        if (_queue==null || _queueChecks<2)
        {
            synchronized(this)
            {
                if (_queue==null)
                    _queue=new BlockingQueue(_maxThreads);
                _queueChecks++;
            }
        }
        _queue.put(job);
    }

    /* ------------------------------------------------------------ */
    /** Pool Thread run class.
     * This class or derivations of it are recommended for use with
     * the ThreadPool.  The PoolThread allows the threads job to be
     * retrieved and active status to be indicated.
     */
    public static class PoolThread extends Thread
    {
        JobRunner _jobRunner;
        boolean _active=true;
        
        /* ------------------------------------------------------------ */
        public PoolThread(Runnable r)
        {
            super(r);
            _jobRunner=(JobRunner)r;
        }
        
        /* ------------------------------------------------------------ */
        public String toString()
        {
            return _jobRunner.toString();
        }

        /* ------------------------------------------------------------ */
        public Object getJob()
        {
            return _jobRunner.getJob();
        }

        /* ------------------------------------------------------------ */
        /** Set active state.
         * @param active 
         */
        public void setActive(boolean active)
        {
            _active=active;
        }

        /* ------------------------------------------------------------ */
        /** Is the PoolThread active.
         * A PoolThread handling a job, may set it's own active state.
         * Implementation of of the ThreadPool.stopJob method should
         * attempt to wait for active threads to complete.
         * @return True if thread is active.
         */
        public boolean isActive()
        {
            return _active;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Pool Thread run class.
     */
    private class JobRunner
        implements Runnable
    {
        Object _job;
        int _runs;
        Thread _thread;
        String _threadName;

        /* ------------------------------------------------------------ */
        Object getJob()
        {
            return _job;
        }
        
        /* -------------------------------------------------------- */
        /** ThreadPool run.
         * Loop getting jobs and handling them until idle or stopped.
         */
        public void run() 
        {
            _thread=Thread.currentThread();
            _threadName=_thread.getName();
            _runs=0;
            
            if (Code.verbose(9))
                Code.debug( "Start thread in ", _name );
            try{
                jobloop:
                while(_running) 
                {
                    // clear interrupts
                    Thread.interrupted();
                    
                    _job=null;
                    try 
                    {
                        // increment accepting count
                        synchronized(ThreadPool.this){_idleSet.add(_thread);}
                    
                        // wait for a job
                        _job=ThreadPool.this.getJob(_maxIdleTimeMs);

                    }
                    catch(InterruptedException e)
                    {
                        Code.ignore(e);
                    }
                    catch(InterruptedIOException e)
                    {
                        Code.ignore(e);
                    }
                    catch(Exception e)
                    {
                        Code.warning(e);
                    }
                    finally
                    {
                        synchronized(ThreadPool.this)
                        {
                            _idleSet.remove(_thread);

                            // If we are still running
                            if (_running)
                            {
                                // If we have a job
                                if (_job!=null)
                                {
                                     // If not more threads accepting - start one
                                     if (_idleSet.size()==0 &&
                                         _threadSet.size()<_maxThreads)   
                                     {
                                         try{newThread();}
                                         catch(Exception e){Code.warning(e);}
                                     }
                                }
                                
                                else
                                {
                                    // No Job, are we still needed?
                                    if (_threadSet.size()>_minThreads &&
                                        _idleSet.size()>0)
                                    {
                                        if (Code.verbose(99))
                                            Code.debug("Idle death: "+_thread);
                                        break jobloop; // Break from the running loop
                                    }
                                }
                            }
                        }
                    }

                    // handle the job
                    if (_running && _job!=null)
                    {
                        try
                        {
                            // Tag thread if debugging
                            if (Code.debug())
                            {
                                _thread.setName(_threadName+"/"+_runs++);
                                if (Code.verbose(99))
                                    Code.debug("Handling ",_job);
                            }
                            
                            // handle the job
                            handle(_job);
                        }
                        catch (Exception e)
                        {
                            Code.warning(e);
                        }
                        finally
                        {
                            _job=null;
                        }
                    }
                }
            }
            finally
            {
                synchronized(ThreadPool.this)
                {
                    if (_threadSet!=null)
                        _threadSet.remove(_thread);
                }
                if (Code.verbose(9))
                    Code.debug("Stopped thread in ", _name);
            }
        }

        public String toString()
        {
            Object j=_job;
            return
                _threadName+"|"+_runs+"|"+((j==null)?"NoJob":j.toString());
        }
        
    }

    
    /* ------------------------------------------------------------ */
    /** Get the number of threads in the pool.
     * @return Number of threads
     * @deprecated use getThreads
     */
    public int getSize()
    {
        if (_threadSet==null)
            return 0;
        return _threadSet.size();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the minimum number of threads.
     * @return minimum number of threads.
     * @deprecated use getMinThreads
     */
    public int getMinSize()
    {
        return _minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the minimum number of threads.
     * @param minThreads minimum number of threads
     * @deprecated use setMinThreads
     */
    public void setMinSize(int minThreads)
    {
        _minThreads=minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @return maximum number of threads.
     * @deprecated use getMaxThreads
     */
    public int getMaxSize()
    {
        return _maxThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @param maxThreads maximum number of threads.
     * @deprecated use setMaxThreads
     */
    public void setMaxSize(int maxThreads)
    {
        _maxThreads=maxThreads;
    }    
}
