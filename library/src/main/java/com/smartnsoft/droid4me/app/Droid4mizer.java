// The MIT License (MIT)
//
// Copyright (c) 2017 Smart&Soft
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.smartnsoft.droid4me.app;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.smartnsoft.droid4me.app.AppInternals.StateContainer;
import com.smartnsoft.droid4me.app.AppPublics.BroadcastListener;

/**
 * The class that should be used when extending a legacy class to support the whole droid4me framework features.
 *
 * @param <AggregateClass> the aggregate class accessible though the {@link #setAggregate(Object)} and {@link #getAggregate()} methods
 * @param <ComponentClass> the instance that will be used to determine whether {@linkplain #onRetrieveBusinessObjects() the business object should be retrieved
 *                         asynchronously}, and to {@linkplain #registerBroadcastListeners(BroadcastListener[]) register broadcast listeners}
 * @author Édouard Mercier
 * @since 2011.06.14
 */
public final class Droid4mizer<AggregateClass, ComponentClass>
    implements Smartable<AggregateClass>
{

  /**
   * A flag which indicates whether the hereby {@code Droid4mizer} internal logs should be enabled. Logs will report all life-cycle events, for
   * instance. The default value is {@code false}.
   */
  public static boolean ARE_DEBUG_LOG_ENABLED = false;

  private static int allocatedCount = 0;

  private static int aliveCount = 0;

  /**
   * Provide statistics about the allocated and alive {@link Droid4mizer} instances. This may be useful when it comes to hunt for memory leaks.
   *
   * @param allocatedCount will eventually hold the total number of allocated {@link Droid4mizer} instances from the hosting process start
   * @param aliveCount     will eventually hold the total number of currently alive {@link Droid4mizer} instances within the hosting process, i.e. the number of
   *                       non yet garbage collected instances
   */
  public static void getStatistics(AtomicInteger allocatedCount, AtomicInteger aliveCount)
  {
    allocatedCount.set(Droid4mizer.allocatedCount);
    aliveCount.set(Droid4mizer.aliveCount);
  }

  private final Activity activity;

  private final ComponentClass component;

  private final ComponentClass interceptorComponent;

  private final Smartable<AggregateClass> smartable;

  private final AppInternals.StateContainer<AggregateClass, ComponentClass> stateContainer;

  /**
   * The only way to create an instance.
   *
   * @param activity             the activity this instance relies on
   * @param smartable            the component to be droid4mized
   * @param component            the declared component used to determine whether {@linkplain #onRetrieveBusinessObjects() the business object should be retrieved
   *                             asynchronously}, and to {@linkplain #registerBroadcastListeners(BroadcastListener[]) register broadcast listeners}
   * @param interceptorComponent the declared component used to send life-cycle events to the {@link ActivityController.Interceptor}
   */
  public Droid4mizer(Activity activity, Smartable<AggregateClass> smartable, ComponentClass component,
      ComponentClass interceptorComponent)
  {
    Droid4mizer.allocatedCount++;
    Droid4mizer.aliveCount++;
    this.activity = activity;
    this.smartable = smartable;
    this.component = component;
    this.interceptorComponent = interceptorComponent;
    stateContainer = new AppInternals.StateContainer<>(activity, component);
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED)
    {
      log.debug("Creating the droid4mizer for Activity belonging to class '" + activity.getClass().getName() + "'" + (interceptorComponent == null ? "" : " and with Fragment belonging to class '" + interceptorComponent.getClass().getName() + "'"));
    }
  }

  /*
   * The {@link Smarted} methods.
   */

  public void onRetrieveDisplayObjects()
  {
    smartable.onRetrieveDisplayObjects();
  }

  public void onRetrieveBusinessObjects()
      throws BusinessObjectUnavailableException
  {
    smartable.onRetrieveBusinessObjects();
  }

  public void onFulfillDisplayObjects()
  {
    smartable.onFulfillDisplayObjects();
  }

  public void onSynchronizeDisplayObjects()
  {
    smartable.onSynchronizeDisplayObjects();
  }

  public AggregateClass getAggregate()
  {
    return stateContainer.getAggregate();
  }

  public void setAggregate(AggregateClass aggregate)
  {
    stateContainer.setAggregate(aggregate);
  }

  public Handler getHandler()
  {
    return stateContainer.getHandler();
  }

  public void onException(Throwable throwable, boolean fromGuiThread)
  {
    ActivityController.getInstance().handleException(true, activity, interceptorComponent, throwable);
  }

  public void registerBroadcastListeners(BroadcastListener[] broadcastListeners)
  {
    stateContainer.registerBroadcastListeners(broadcastListeners);
  }

  public void onBusinessObjectsRetrieved()
  {
    smartable.onBusinessObjectsRetrieved();
  }

  /*
   * The {@link AppPublics.LifeCyclePublic} methods.
   */

  @SuppressWarnings("deprecation")
  public void refreshBusinessObjectsAndDisplay(final boolean retrieveBusinessObjects, final Runnable onOver,
      boolean immediately)
  {
    if (stateContainer.isAliveAsWellAsHostingActivity() == false)
    {
      // In that case, we skip the processing
      return;
    }
    if (stateContainer.shouldDelayRefreshBusinessObjectsAndDisplay(retrieveBusinessObjects, onOver, immediately) == true)
    {
      return;
    }
    if (stateContainer.isAliveAsWellAsHostingActivity() == false)
    {
      // In that case, we skip the processing
      return;
    }
    stateContainer.onRefreshingBusinessObjectsAndDisplayStart();
    // We can safely retrieve the business objects
    if (component.getClass().getAnnotation(BusinessObjectsRetrievalAsynchronousPolicy.class) != null)
    {
      // We call that routine asynchronously in a background thread
      stateContainer.execute(activity, component, new Runnable()
      {
        public void run()
        {
          if (onRetrieveBusinessObjectsInternal(retrieveBusinessObjects) == false)
          {
            return;
          }
          // We are handling the UI, and we need to make sure that this is done through the GUI thread
          activity.runOnUiThread(new Runnable()
          {
            public void run()
            {
              // If the hosting activity has been finished in the meantime, or the entity is not alive anymore, we should not update the UI
              if (stateContainer.isAliveAsWellAsHostingActivity() == false)
              {
                // And in that case, since we are currently in the UI thread, we are ensured that neither the entity nor the hosting Activity will be
                // destroyed in the meantime!
                return;
              }
              onFulfillAndSynchronizeDisplayObjectsInternal(onOver);
            }
          });
        }
      });
    }
    else
    {
      // We directly run in the UI thread
      if (onRetrieveBusinessObjectsInternal(retrieveBusinessObjects) == false)
      {
        return;
      }
      onFulfillAndSynchronizeDisplayObjectsInternal(onOver);
    }
  }

  public boolean isRefreshingBusinessObjectsAndDisplay()
  {
    return stateContainer.isRefreshingBusinessObjectsAndDisplay();
  }

  /*
   * Enables to know how many times the display synchronization cycle has been run so far for the current entity.
   *
   * @return the number of times the {@link #onSynchronizeDisplayObjects()} method has been invoked. The base
   * method {@link #onSynchronizeDisplayObjects()} increments a counter on each call, even if an exception is thrown.
   * This method will retrieve the current count the previous method has been invoked, starting from {@code 0}.
   * @see #onSynchronizeDisplayObjects()
   */
  public final int getOnSynchronizeDisplayObjectsCount()
  {
    return stateContainer.getOnSynchronizeDisplayObjectsCount();
  }

  /*
   * Indicates whether the entity has been restarted due to a configuruation change, or being restored from a saved state.
   *
   * @return {@code false} if and only if the life cycle is starting back from a saved instance state, i.e. whether
   * the {@link onSaveInstanceState(Bundle)} method has already been invoked. For instance, when the device
   * configuration changes, the application will start back from a saved instance state, and this results in
   * this method returning {@code false}.
   * @see #onSaveInstanceState(Bundle)
   */
  public final boolean isFirstLifeCycle()
  {
    return stateContainer.isFirstLifeCycle();
  }

  public final boolean isInteracting()
  {
    return stateContainer.isInteracting();
  }

  /*
   * The {@link AppInternals.LifeCycleInternals} methods.
   */

  public final boolean isAlive()
  {
    return stateContainer.isAlive();
  }

  /*
   * The {@link Activity}/{@link android.app.Fragment} methods.
   */

  public boolean shouldKeepOn()
  {
    return stateContainer.shouldKeepOn();
  }

  public SharedPreferences getPreferences()
  {
    return stateContainer.getPreferences(activity);
  }

  @Override
  protected void finalize()
      throws Throwable
  {
    Droid4mizer.aliveCount--;
    super.finalize();
  }

  /**
   * Same as invoking {@code refreshBusinessObjectsAndDisplay(true, null, false)}.
   *
   * @see #refreshBusinessObjectsAndDisplay(boolean, Runnable, boolean)
   */
  public void refreshBusinessObjectsAndDisplay()
  {
    refreshBusinessObjectsAndDisplay(true, null, false);
  }

  /**
   * Same as invoking {@code refreshBusinessObjectsAndDisplay(boolean, null, false)}.
   *
   * @see #refreshBusinessObjectsAndDisplay(boolean, Runnable, boolean)
   */
  public final void refreshBusinessObjectsAndDisplay(boolean retrieveBusinessObjects)
  {
    refreshBusinessObjectsAndDisplay(retrieveBusinessObjects, null, false);
  }

  public void onAttached(Activity activity)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onAttached");
    }
  }

  public void onCreate(Runnable superMethod, Bundle savedInstanceState)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onCreate");
    }

    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onSuperCreateBefore);
    superMethod.run();
    if (isFragment() == false && ActivityController.getInstance().needsRedirection(activity) == true)
    {
      // We stop here if a redirection is needed
      stateContainer.beingRedirected();
      return;
    }
    else
    {
      ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onCreate);
    }
    if (StateContainer.isFirstCycle(savedInstanceState) == true)
    {
      stateContainer.setFirstLifeCycle(false);
    }
    else
    {
      stateContainer.setFirstLifeCycle(true);
    }
    stateContainer.registerBroadcastListeners();

    stateContainer.initialize();
    try
    {
      onRetrieveDisplayObjects();
    }
    catch (Throwable throwable)
    {
      stateContainer.stopHandling();
      smartable.onException(throwable, true);
      return;
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onCreateDone);
  }

  public void onPostCreate(Bundle savedInstanceState)
  {
    if (shouldKeepOn() == false)
    {
      return;
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onPostCreate);
  }

  public void onNewIntent(Intent intent)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onNewIntent");
    }

    if (isFragment() == false && ActivityController.getInstance().needsRedirection(activity) == true)
    {
      // We stop here if a redirection is needed
      stateContainer.beingRedirected();
    }
  }

  public void onContentChanged()
  {
    if (shouldKeepOn() == false)
    {
      return;
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onContentChanged);
  }

  public void onResume()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onResume");
    }
    if (shouldKeepOn() == false)
    {
      return;
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onResume);
    stateContainer.onResume();
    refreshBusinessObjectsAndDisplayInternal();
  }

  public void onPostResume()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onPostResume");
    }
    if (shouldKeepOn() == false)
    {
      return;
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onPostResume);
    stateContainer.onPostResume();
  }

  public void onConfigurationChanged(Configuration newConfig)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onConfigurationChanged");
    }
  }

  public void onSaveInstanceState(Bundle outState)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onSaveInstanceState");
    }
    stateContainer.onSaveInstanceState(outState);
  }

  public void onRestoreInstanceState(Bundle savedInstanceState)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onRestoreInstanceState");
    }
    refreshBusinessObjectsAndDisplayInternal();
  }

  public void onStart()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onStart");
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onStart);
    stateContainer.onStart();
  }

  public void onRestart()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onRestart");
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onRestart);
    stateContainer.onRestart();
  }

  public void onPause()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onPause");
    }
    if (shouldKeepOn() == false)
    {
      // We stop here if a redirection is needed or is something went wrong
      return;
    }
    else
    {
      ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onPause);
      stateContainer.onPause();
    }
  }

  public void onStop()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onStop");
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onStop);
    stateContainer.onStop();
  }

  public void onDestroy()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onDestroy");
    }
    stateContainer.onDestroy();
    if (shouldKeepOn() == false)
    {
      // We stop here if a redirection is needed or is something went wrong
      return;
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onDestroy);
  }

  public void onDetached()
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onDetached");
    }
  }

  /*
   * The LifeCycle interface implementation.
   */

  public boolean onCreateOptionsMenu(boolean superResult, Menu menu)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onCreateOptionsMenu");
    }
    return superResult;
  }

  public boolean onPrepareOptionsMenu(boolean superResult, Menu menu)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onPrepareOptionsMenu");
    }
    return superResult;
  }

  public boolean onOptionsItemSelected(boolean superResult, MenuItem item)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onOptionsItemSelected");
    }
    return superResult;
  }

  public boolean onContextItemSelected(boolean superResult, MenuItem item)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onContextItemSelected");
    }
    return superResult;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (Droid4mizer.ARE_DEBUG_LOG_ENABLED == true && log.isDebugEnabled())
    {
      log.debug("Droid4mizer::onActivityResult");
    }
  }

  /*
   * The specific methods.
   */

  /**
   * Has the same responsibility as the {@link Context#getSystemService(String)} method. It only applies to the {@link Activity} entity, and not to the
   * {@link Fragment} entity!
   *
   * @param name           the name of the desired service
   * @param defaultService the default service provided by the underlying {@link #activity Activity}
   * @return the service or {@code null} if the name does not exist
   * @see Context#getSystemService(String)
   */
  @Deprecated
  public Object getSystemService(String name, Object defaultService)
  {
    return ActivityController.getInstance().getSystemService(activity, name, defaultService);
  }

  /**
   * This method should not trigger any exception, otherwise we have a huge bug!
   *
   * @return {@code true} if and only if the processing should resume
   */
  private boolean onRetrieveBusinessObjectsInternal(boolean retrieveBusinessObjects)
  {
    try
    {
      onBeforeRefreshBusinessObjectsAndDisplay();
      if (retrieveBusinessObjects == true)
      {
        if (stateContainer.isAliveAsWellAsHostingActivity() == false)
        {
          // If the entity is no more alive, we give up the process
          return false;
        }

        onRetrieveBusinessObjects();
        // We notify the entity that the business objects have actually been loaded
        if (stateContainer.isAliveAsWellAsHostingActivity() == false)
        {
          // If the entity is no more alive, we give up the process
          return false;
        }
        onBusinessObjectsRetrieved();

      }
      stateContainer.setBusinessObjectsRetrieved();
      return true;
    }
    catch (Throwable throwable)
    {
      stateContainer.onRefreshingBusinessObjectsAndDisplayStop(this);
      // We check whether the issue does not come from a non-alive entity
      if (stateContainer.isAliveAsWellAsHostingActivity() == false)
      {
        // In that case, we just ignore the exception: it is very likely that the entity or the hosting Activity have turned as non-alive
        // during the "onRetrieveBusinessObjects()" method!
        return false;
      }
      // Otherwise, we report the exception
      onInternalBusinessObjectAvailableException(throwable);
      return false;
    }
  }

  private void onBeforeRefreshBusinessObjectsAndDisplay()
  {
    stateContainer.onStartLoading();
  }

  private void onFulfillAndSynchronizeDisplayObjectsInternal(Runnable onOver)
  {
    if (stateContainer.isResumedForTheFirstTime() == true)
    {
      try
      {
        onFulfillDisplayObjects();
      }
      catch (Throwable throwable)
      {
        stateContainer.onRefreshingBusinessObjectsAndDisplayStop(this);
        smartable.onException(throwable, true);
        stateContainer.onStopLoading();
        return;
      }
      ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onFulfillDisplayObjectsDone);
    }
    try
    {
      stateContainer.onSynchronizeDisplayObjects();
      onSynchronizeDisplayObjects();
    }
    catch (Throwable throwable)
    {
      stateContainer.onRefreshingBusinessObjectsAndDisplayStop(this);
      smartable.onException(throwable, true);
      return;
    }
    finally
    {
      stateContainer.onStopLoading();
    }
    ActivityController.getInstance().onLifeCycleEvent(activity, interceptorComponent, ActivityController.Interceptor.InterceptorEvent.onSynchronizeDisplayObjectsDone);
    stateContainer.markNotResumedForTheFirstTime();
    if (onOver != null)
    {
      try
      {
        onOver.run();
      }
      catch (Throwable throwable)
      {
        if (log.isErrorEnabled())
        {
          log.error("An exception occurred while executing the 'refreshBusinessObjectsAndDisplay()' runnable!", throwable);
        }
      }
    }
    stateContainer.onRefreshingBusinessObjectsAndDisplayStop(this);
  }

  private void refreshBusinessObjectsAndDisplayInternal()
  {
    smartable.refreshBusinessObjectsAndDisplay(stateContainer.isRetrieveBusinessObjects(), stateContainer.getRetrieveBusinessObjectsOver(), true);
  }

  private final void onInternalBusinessObjectAvailableException(Throwable throwable)
  {
    if (log.isErrorEnabled())
    {
      log.error("Cannot retrieve the business objects", throwable);
    }
    stateContainer.onStopLoading();
    if (stateContainer.onInternalBusinessObjectAvailableExceptionWorkAround(throwable) == true)
    {
      return;
    }
    // We need to indicate to the method that it may have been triggered from another thread than the GUI's
    smartable.onException(throwable, false);
  }

  private boolean isFragment()
  {
    return activity != smartable;
  }

}
