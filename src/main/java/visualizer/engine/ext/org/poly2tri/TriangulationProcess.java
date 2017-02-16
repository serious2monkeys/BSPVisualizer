/**
 * TriangulationProcess.java
 * <p>
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */

package visualizer.engine.ext.org.poly2tri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Thomas ???, thahlen@gmail.com
 */
class TriangulationProcess implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(TriangulationProcess.class);

    private final TriangulationAlgorithm _algorithm;

    private TriangulationContext<?> _tcx;
    private Thread _thread;
    private boolean _isTerminated = false;
    private int _pointCount = 0;
    private long _timestamp = 0;
    private double _triangulationTime = 0;

    private boolean _awaitingTermination;
    private boolean _restart = false;

    private ArrayList<Triangulatable> _triangulations = new ArrayList<>();

    private ArrayList<TriangulationProcessListener> _listeners = new ArrayList<>();

    public void addListener(TriangulationProcessListener listener) {
        _listeners.add(listener);
    }

    public void removeListener(TriangulationProcessListener listener) {
        _listeners.remove(listener);
    }

    public void clearListeners() {
        _listeners.clear();
    }

    /**
     * Notify all listeners of this new event
     *
     * @param event
     */
    private void sendEvent(TriangulationProcessEvent event) {
        for (TriangulationProcessListener l : _listeners) {
            l.triangulationEvent(event, _tcx.getTriangulatable());
        }
    }

    public int getStepCount() {
        return _tcx.getStepCount();
    }

    public long getTimestamp() {
        return _timestamp;
    }

    public double getTriangulationTime() {
        return _triangulationTime;
    }

    /**
     * Uses SweepLine algorithm by default
     */
    public TriangulationProcess() {
        this(TriangulationAlgorithm.DTSweep);
    }

    public TriangulationProcess(TriangulationAlgorithm algorithm) {
        _algorithm = algorithm;
        _tcx = Poly2Tri.createContext(algorithm);
    }

    /**
     * This retriangulates same set as previous triangulation
     * useful if you want to do consecutive triangulations with 
     * same data. Like when you when you want to do performance 
     * tests.
     */
//    public void triangulate()
//    {
//        start();
//    }

    /**
     * Triangulate a PointSet with eventual constraints
     */
    public void triangulate(PointSet ps) {
        _triangulations.clear();
        _triangulations.add(ps);
        start();
    }

    /**
     * Triangulate a PointSet with eventual constraints
     *
     * @param cps
     */
    public void triangulate(ConstrainedPointSet cps) {
        _triangulations.clear();
        _triangulations.add(cps);
        start();
    }

    /**
     * Triangulate a PolygonSet
     *
     * @param ps
     */
    public void triangulate(PolygonSet ps) {
        _triangulations.clear();
        _triangulations.addAll(ps.getPolygons());
        start();
    }

    /**
     * Triangulate a Polygon
     */
    public void triangulate(Polygon polygon) {
        _triangulations.clear();
        _triangulations.add(polygon);
        start();
    }

    /**
     * Triangulate a List of Triangulatables
     */
    public void triangulate(List<Triangulatable> list) {
        _triangulations.clear();
        _triangulations.addAll(list);
        start();
    }

    private void start() {
        if (_thread == null || _thread.getState() == State.TERMINATED) {
            _isTerminated = false;
            _thread = new Thread(this, _algorithm.name() + "." + _tcx.getTriangulationMode());
            _thread.start();
            sendEvent(TriangulationProcessEvent.Started);
        } else {
            // Triangulation already running. Terminate it so we can start a new
            shutdown();
            _restart = true;
        }
    }

    public boolean isWaiting() {
        if (_thread != null && _thread.getState() == State.WAITING) {
            return true;
        }
        return false;
    }

    public void run() {
        _pointCount = 0;
        try {
            long time = System.nanoTime();
            for (Triangulatable t : _triangulations) {
                _tcx.clear();
                _tcx.prepareTriangulation(t);
                _pointCount += _tcx._points.size();
                Poly2Tri.triangulate(_tcx);
            }
            _triangulationTime = (System.nanoTime() - time) / 1e6;
            logger.info("Triangulation of {} points [{}ms]", _pointCount, _triangulationTime);
            sendEvent(TriangulationProcessEvent.Done);
        } catch (RuntimeException e) {
            if (_awaitingTermination) {
                _awaitingTermination = false;
                logger.info("Thread[{}] : {}", _thread.getName(), e.getMessage());
                sendEvent(TriangulationProcessEvent.Aborted);
            } else {
                e.printStackTrace();
                sendEvent(TriangulationProcessEvent.Failed);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Triangulation exception {}", e.getMessage());
            sendEvent(TriangulationProcessEvent.Failed);
        } finally {
            _timestamp = System.currentTimeMillis();
            _isTerminated = true;
            _thread = null;
        }

        // Autostart a new triangulation?
        if (_restart) {
            _restart = false;
            start();
        }
    }

    public void resume() {
        if (_thread != null) {
            // Only force a resume when process is waiting for a notification
            if (_thread.getState() == State.WAITING) {
                synchronized (_tcx) {
                    _tcx.notify();
                }
            } else if (_thread.getState() == State.TIMED_WAITING) {
                _tcx.waitUntilNotified(false);
            }
        }
    }

    public void shutdown() {
        _awaitingTermination = true;
        _tcx.terminateTriangulation();
        resume();
    }

    public TriangulationContext<?> getContext() {
        return _tcx;
    }

    public boolean isDone() {
        return _isTerminated;
    }

    public void requestRead() {
        _tcx.waitUntilNotified(true);
    }

    public boolean isReadable() {
        if (_thread == null) {
            return true;
        } else {
            synchronized (_thread) {
                if (_thread.getState() == State.WAITING) {
                    return true;
                } else if (_thread.getState() == State.TIMED_WAITING) {
                    // Make sure that it stays readable
                    _tcx.waitUntilNotified(true);
                    return true;
                }
                return false;
            }
        }
    }

    public int getPointCount() {
        return _pointCount;
    }
}