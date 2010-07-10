/*
 * (C) Copyright 2009-20010 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     E2M - initial API and implementation
 *     Smart&Soft - initial API and implementation
 */

package com.smartnsoft.droid4me.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.smartnsoft.droid4me.framework.ActivityResultHandler;
import com.smartnsoft.droid4me.framework.Events;
import com.smartnsoft.droid4me.framework.LifeCycle;
import com.smartnsoft.droid4me.framework.ActivityResultHandler.CompositeHandler;
import com.smartnsoft.droid4me.framework.Events.OnCompletion;
import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;
import com.smartnsoft.droid4me.menu.MenuCommand;
import com.smartnsoft.droid4me.menu.MenuHandler;
import com.smartnsoft.droid4me.menu.StaticMenuCommand;

/**
 * A basis class for an activity that holds a map.
 * 
 * @author Édouard Mercier
 * @since 2009.02.16
 */
public abstract class SmartMapActivity
    extends MapActivity
    implements AppPublics.CommonActivity, LifeCycle.ForActivity, AppPublics.LifeCyclePublic, AppInternals.LifeCycleInternals
{

  protected final static Logger log = LoggerFactory.getInstance(SmartMapActivity.class);

  private final static float MILLION_AS_FLOAT = 1E6f;

  private MapView mapView;

  private LinearLayout headerView;

  private RelativeLayout mapWrapperLayout;

  private LinearLayout footerView;

  private LinearLayout innerHeaderView;

  private LinearLayout innerFooterView;

  private RelativeLayout wrapperLayout;

  protected abstract String getGoogleMapsApiKey();

  protected final MapView getMapView()
  {
    return mapView;
  }

  protected void onMapViewKnown(int width, int height)
  {
  }

  public void onRetrieveDisplayObjects()
  {
    wrapperLayout = new RelativeLayout(this);
    {
      headerView = new LinearLayout(this);
      headerView.setId(1230);
      final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
      wrapperLayout.addView(headerView, layoutParams);
    }
    {
      footerView = new LinearLayout(this);
      footerView.setId(headerView.getId() + 1);
      footerView.setGravity(Gravity.BOTTOM);
      final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
      wrapperLayout.addView(footerView, layoutParams);
    }
    {
      mapWrapperLayout = new RelativeLayout(this);
      mapView = createMapView();
      getMapWrapperLayout().addView(mapView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
      // We need to set an identifier to the map view, because we want to locate other views relatively to it
      mapView.setId(footerView.getId() + 1);
      {
        innerHeaderView = new LinearLayout(this);
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, mapView.getId());
        getMapWrapperLayout().addView(innerHeaderView, layoutParams);
      }
      {
        innerFooterView = new LinearLayout(this);
        innerFooterView.setId(mapView.getId() + 1);
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mapView.getId());
        getMapWrapperLayout().addView(innerFooterView, layoutParams);
      }
      {
        final LinearLayout zoomLayout = new LinearLayout(this);
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ABOVE, innerFooterView.getId());
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        getMapWrapperLayout().addView(zoomLayout, layoutParams);
        {
          final ZoomControls zoomView = (ZoomControls) mapView.getZoomControls();
          zoomView.setGravity(Gravity.CENTER_HORIZONTAL);
          zoomView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
          mapView.displayZoomControls(true);
          zoomLayout.addView(zoomView);
        }
      }
      final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
      layoutParams.addRule(RelativeLayout.BELOW, headerView.getId());
      layoutParams.addRule(RelativeLayout.ABOVE, footerView.getId());
      wrapperLayout.addView(getMapWrapperLayout(), layoutParams);
    }
    setContentView(wrapperLayout);
  }

  public void onBusinessObjectsRetrieved()
  {
  }

  @Override
  protected boolean isRouteDisplayed()
  {
    return false;
  }

  /**
   * @return the top view of the activity, which contains the {@link #getHeaderView() header view}, the {@link #getMapWrapperLayout() map view wrapper
   *         layout} and the {@link #getFooterView() footer view}
   */
  protected final RelativeLayout getWrapperLayout()
  {
    return wrapperLayout;
  }

  /**
   * @return the layout which contains the map activity header view
   */
  protected final LinearLayout getHeaderView()
  {
    return headerView;
  }

  /**
   * @return the layout which contains the {@link #getInnerHeaderView() overlay header view}, the {@link MapView} and the
   *         {@link #getInnerFooterView() overlay footer view}
   */
  protected final RelativeLayout getMapWrapperLayout()
  {
    return mapWrapperLayout;
  }

  /**
   * @return the layout which contains the map activity footer view
   */
  protected final LinearLayout getFooterView()
  {
    return footerView;
  }

  /**
   * @return the layout which is displayed in overlay mode at the top of the map activity
   */
  protected final LinearLayout getInnerHeaderView()
  {
    return innerHeaderView;
  }

  /**
   * @return the layout which is displayed in overlay mode at the bottom of the map activity
   */
  protected final LinearLayout getInnerFooterView()
  {
    return innerFooterView;
  }

  // Many explanations are taken from http://androidguys.com/?p=1413 and at
  // http://groups.google.com/group/android-developers/browse_thread/thread/db3eee0abe7aafe4/16a44c09741d9883?lnk=raot
  private MapView createMapView()
  {
    final MapView theMapView = new MapView(this, getGoogleMapsApiKey())
    {
      protected void onSizeChanged(int width, int height, int oldw, int oldh)
      {
        super.onSizeChanged(width, height, oldw, oldh);
        onMapViewKnown(width, height);
      }
    };
    // Without these two settings, it is not possible to click, zoom in/out, and navigate on the map
    theMapView.setEnabled(true);
    theMapView.setClickable(true);
    // mapView.setReticleDrawMode(ReticleDrawMode.DRAW_RETICLE_OVER);
    return theMapView;
  }

  private final static double EQUATOR_IN_PIXELS_WHEN_ZOOM_SET_TO_1 = 256.0d;

  // The information distance was taken from http://answers.yahoo.com/question/index?qid=20060626170225AA0RThe
  private final static double EQUATOR_DISTANCE_IN_METERS = 40075016.686d;

  private static double adjustWithLatitude(int latitudeE6, double pixelsCount)
  {
    return pixelsCount * Math.cos(((((float) latitudeE6) / MILLION_AS_FLOAT) / 90f) * (Math.PI / 2d));
  }

  public static final int convertMetersIntoPixels(int zoomLevel, int latitudeE6, float radiusInMeters)
  {
    // BUG: in the Android documentation, the equator zoom is 0: see
    // http://groups.google.com/group/android-developers/browse_thread/thread/24fc4fd4d6aa67cd/6a2567fb26f207a4?#6a2567fb26f207a4
    final double equatorPixels = EQUATOR_IN_PIXELS_WHEN_ZOOM_SET_TO_1 * Math.pow(2d, zoomLevel);
    final double pixelPerMeter = equatorPixels / EQUATOR_DISTANCE_IN_METERS;
    final double pixelsCount = pixelPerMeter * radiusInMeters;
    final double pixelsCountByTakingAccountLatitude = SmartMapActivity.adjustWithLatitude(latitudeE6, pixelsCount);
    // log.debug("The conversion of " + radiusInMeters + " m. in pixels with a zoom level set to " + zoomLevel + " gives a result of " +
    // pixelsCountByTakingAccountLatitude);
    return (int) (pixelsCountByTakingAccountLatitude);
  }

  public static final int convertLatitudeDistanceInMetersIntoZoom(int latitudeE6, float radiusInMeters)
  {
    int zoomLevel = 1;
    double screenWidthInMeters = EQUATOR_DISTANCE_IN_METERS;
    final double radiusInMetersTakingAccountLatitude = SmartMapActivity.adjustWithLatitude(latitudeE6, radiusInMeters);
    // We apply a dichotomy
    while ((screenWidthInMeters > radiusInMetersTakingAccountLatitude) && zoomLevel <= (21 + 1))
    {
      screenWidthInMeters = screenWidthInMeters / 2d;
      zoomLevel++;
    }
    return zoomLevel - 1 - 2;
  }

  public static final double convertZoomIntoMeters(int zoomLevel, int pixelSpan)
  {
    return (((double) pixelSpan / 256d) * EQUATOR_DISTANCE_IN_METERS) / (Math.pow(2, zoomLevel - 1));
  }

  // public static final int convertLatitudeDistanceInMetersIntoSpan(int latitudeE6, float latitudeDistanceInMeters)
  // {
  // int zoomLevel = 1;
  // double screenWidthInMeters = EQUATOR_DISTANCE_IN_METERS;
  // final double radiusInMetersTakingAccountLatitude = SmartMapActivity.adjustWithLatitude(latitudeE6, latitudeDistanceInMeters);
  // // We apply a dichotomy
  // while ((screenWidthInMeters > radiusInMetersTakingAccountLatitude) && zoomLevel <= (21 + 1))
  // {
  // screenWidthInMeters = screenWidthInMeters / 2d;
  // zoomLevel++;
  // }
  // return zoomLevel - 1 - 2;
  // }

  /**
   * ------------------- Beginning of "Copied from the SmartActivity class" -------------------
   */

  private AppInternals.StateContainer stateContainer = new AppInternals.StateContainer();

  public void onActuallyCreated()
  {
  }

  public void onActuallyDestroyed()
  {
  }

  public final boolean isFirstLifeCycle()
  {
    return stateContainer.isFirstLifeCycle();
  }

  public final boolean isBeingRedirected()
  {
    return stateContainer.beingRedirected;
  }

  public final Handler getHandler()
  {
    return stateContainer.handler;
  }

  public Object getAggregate()
  {
    return stateContainer.aggregate;
  }

  public void setAggregate(Object aggregate)
  {
    stateContainer.aggregate = aggregate;
  }

  public List<StaticMenuCommand> getMenuCommands()
  {
    return null;
  }

  public final void onException(final Throwable throwable, boolean fromGuiThread)
  {
    // Eventually, we do not ensure that the handling of the exception will be performed in a specific thread
    // if (fromGuiThread == true)
    {
      ActivityController.getInstance().handleException(this, throwable);
    }
    // else
    // {
    // runOnUiThread(new Runnable()
    // {
    // public void run()
    // {
    // ActivityController.getInstance().handleException(SmartActivity.this, throwable);
    // }
    // });
    // }
  }

  private final void onInternalCreate(Bundle savedInstanceState)
  {
    stateContainer.create(getApplicationContext());
  }

  private final void onInternalDestroy()
  {
  }

  protected void onBeforeRetrievingDisplayObjects()
  {
  }

  /**
   * It is ensured that this method will be invoked from the GUI thread.
   */
  protected void onBeforeRefreshBusinessObjectsAndDisplay()
  {
    stateContainer.onStartLoading(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onCreate");
    }
    super.onCreate(savedInstanceState);
    if (ActivityController.getInstance().needsRedirection(this) == true)
    {
      // We stop here if a redirection is needed
      stateContainer.beingRedirected = true;
      return;
    }
    else
    {
      ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onCreate);
    }

    if (savedInstanceState != null && savedInstanceState.containsKey(SmartActivity.ALREADY_STARTED) == true)
    {
      stateContainer.firstLifeCycle = false;
    }
    else
    {
      stateContainer.firstLifeCycle = true;
      onActuallyCreated();
      ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onActuallyCreatedDone);
    }
    stateContainer.registerBroadcastListener(this);

    onInternalCreate(savedInstanceState);
    onBeforeRetrievingDisplayObjects();
    onRetrieveDisplayObjects();
    // We add the static menu commands
    getCompositeActionHandler().add(new MenuHandler.Static()
    {
      @Override
      protected List<MenuCommand<Void>> retrieveCommands()
      {
        final List<StaticMenuCommand> staticMenuCommands = getMenuCommands();
        if (staticMenuCommands == null)
        {
          return null;
        }
        final ArrayList<MenuCommand<Void>> menuCommands = new ArrayList<MenuCommand<Void>>(staticMenuCommands.size());
        for (StaticMenuCommand staticMenuCommand : staticMenuCommands)
        {
          menuCommands.add(staticMenuCommand);
        }
        return menuCommands;
      }
    });
  }

  @Override
  protected void onResume()
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onResume");
    }
    super.onResume();
    stateContainer.doNotCallOnActivityDestroyed = false;
    if (isBeingRedirected() == true)
    {
      return;
    }
    try
    {
      businessObjectRetrievalAndResultHandlers();
    }
    catch (Throwable throwable)
    {
      onInternalHandleOtherException(throwable);
      return;
    }
  }

  private final void onInternalBusinessObjectAvailableException(Throwable throwable)
  {
    if (log.isErrorEnabled())
    {
      log.error("Cannot retrieve the business objects", throwable);
    }
    stateContainer.onStopLoading(this);
    // We need to invoke that method on the GUI thread, because that method may have been triggered from another thread
    onException(throwable, false);
  }

  private void onInternalHandleOtherException(Throwable throwable)
  {
    if (log.isErrorEnabled())
    {
      log.error("Unhandled problem", throwable);
    }
    onException(throwable, true);
  }

  private void businessObjectRetrievalAndResultHandlers()
  {
    if (stateContainer.businessObjectsRetrieved == false)
    {
      refreshBusinessObjectsAndDisplay(!stateContainer.businessObjectsRetrieved);
    }
    if (stateContainer.actionResultsRetrieved == false)
    {
      onRegisterResultHandlers(stateContainer.compositeActivityResultHandler);
      stateContainer.actionResultsRetrieved = true;
    }
  }

  protected final void onBeforeRetrievingBusinessObjectsOver(boolean retrieveBusinessObjects)
  {
    refreshBusinessObjectsAndDisplay(retrieveBusinessObjects);
  }

  /**
   * Must be invoked only from the GUI thread!
   * 
   * @see #refreshBusinessObjectsAndDisplay(boolean, OnCompletion)
   */
  protected final void refreshBusinessObjectsAndDisplay()
  {
    refreshBusinessObjectsAndDisplay(true, null);
  }

  /**
   * Must be invoked only from the GUI thread!
   * 
   * @see #refreshBusinessObjectsAndDisplay(boolean, OnCompletion)
   */
  protected final void refreshBusinessObjectsAndDisplay(boolean retrieveBusinessObjects)
  {
    refreshBusinessObjectsAndDisplay(retrieveBusinessObjects, null);
  }

  /**
   * Must be invoked only from the GUI thread!
   */
  protected final void refreshBusinessObjectsAndDisplay(final boolean retrieveBusinessObjects, final Events.OnCompletion onRefreshBusinessObjectsAndDisplay)
  {
    // We can safely retrieve the business objects
    if (!(this instanceof LifeCycle.BusinessObjectsRetrievalAsynchronousPolicy))
    {
      onBeforeRefreshBusinessObjectsAndDisplay();
      if (retrieveBusinessObjects == true)
      {
        try
        {
          onRetrieveBusinessObjects();
        }
        catch (Throwable throwable)
        {
          // We need to invoke that method on the GUI thread, because that method may have been triggered from another thread
          onInternalBusinessObjectAvailableException(throwable);
          return;
        }
      }
      stateContainer.businessObjectsRetrieved = true;
      if (stateContainer.resumedForTheFirstTime == true)
      {
        onFulfillDisplayObjects();
        ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onFulfillDisplayObjectsDone);
      }
      onSynchronizeDisplayObjects();
      stateContainer.onStopLoading(this);
      ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onSynchronizeDisplayObjectsDone);
      stateContainer.resumedForTheFirstTime = false;
      if (onRefreshBusinessObjectsAndDisplay != null)
      {
        onRefreshBusinessObjectsAndDisplay.done();
      }
    }
    else
    {
      // We invoke the method in the GUI thread
      runOnUiThread(new Runnable()
      {
        public void run()
        {
          onBeforeRefreshBusinessObjectsAndDisplay();

          // We call that method asynchronously in a specific thread
          AppPublics.THREAD_POOL.execute(SmartMapActivity.this, new Runnable()
          {
            public void run()
            {
              if (retrieveBusinessObjects == true)
              {
                try
                {
                  onRetrieveBusinessObjects();
                }
                catch (Throwable throwable)
                {
                  onInternalBusinessObjectAvailableException(throwable);
                  return;
                }
              }
              stateContainer.businessObjectsRetrieved = true;
              // We are handling the UI, and we need to make sure that this is done through the GUI thread
              runOnUiThread(new Runnable()
              {
                public void run()
                {
                  if (stateContainer.resumedForTheFirstTime == true)
                  {
                    onFulfillDisplayObjects();
                    ActivityController.getInstance().onLifeCycleEvent(SmartMapActivity.this,
                        ActivityController.Interceptor.InterceptorEvent.onFulfillDisplayObjectsDone);
                  }
                  onSynchronizeDisplayObjects();
                  stateContainer.onStopLoading(SmartMapActivity.this);
                  ActivityController.getInstance().onLifeCycleEvent(SmartMapActivity.this,
                      ActivityController.Interceptor.InterceptorEvent.onSynchronizeDisplayObjectsDone);
                  stateContainer.resumedForTheFirstTime = false;
                  if (onRefreshBusinessObjectsAndDisplay != null)
                  {
                    onRefreshBusinessObjectsAndDisplay.done();
                  }
                }
              });
            }
          });
        }
      });
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onSaveInstanceState");
    }
    super.onSaveInstanceState(outState);
    stateContainer.doNotCallOnActivityDestroyed = true;
    outState.putBoolean(SmartActivity.ALREADY_STARTED, true);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onRestoreInstanceState");
    }
    super.onRestoreInstanceState(savedInstanceState);
    try
    {
      businessObjectRetrievalAndResultHandlers();
    }
    catch (Throwable throwable)
    {
      onInternalHandleOtherException(throwable);
      return;
    }
  }

  @Override
  protected void onStart()
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onStart");
    }
    super.onStart();
    ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onStart);
    stateContainer.onStart(this);
  }

  @Override
  protected void onPause()
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onPause");
    }
    try
    {
      if (isBeingRedirected() == true)
      {
        // We stop here if a redirection is needed
        return;
      }
      else
      {
        ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onPause);
      }
    }
    finally
    {
      super.onPause();
    }
  }

  @Override
  protected void onStop()
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onStop");
    }
    try
    {
      ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onStop);
      stateContainer.onStop(this);
    }
    finally
    {
      super.onStop();
    }
  }

  @Override
  protected void onDestroy()
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onDestroy");
    }
    try
    {
      if (isBeingRedirected() == true)
      {
        // We stop here if a redirection is needed
        return;
      }
      onInternalDestroy();
      if (stateContainer.doNotCallOnActivityDestroyed == false)
      {
        onActuallyDestroyed();
        ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onActuallyDestroyedDone);
      }
      else
      {
        ActivityController.getInstance().onLifeCycleEvent(this, ActivityController.Interceptor.InterceptorEvent.onDestroy);
      }
      stateContainer.unregisterBroadcastListener(this);
    }
    finally
    {
      super.onDestroy();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onCreateOptionsMenu");
    }
    boolean result = super.onCreateOptionsMenu(menu);

    stateContainer.compositeActionHandler.onCreateOptionsMenu(this, menu);
    return result;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onPrepareOptionsMenu");
    }
    boolean result = super.onPrepareOptionsMenu(menu);

    stateContainer.compositeActionHandler.onPrepareOptionsMenu(menu);
    return result;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onOptionsItemSelected");
    }
    boolean result = super.onOptionsItemSelected(item);

    if (stateContainer.compositeActionHandler.onOptionsItemSelected(item) == true)
    {
      return true;
    }
    return result;
  }

  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onContextItemSelected");
    }
    boolean result = super.onContextItemSelected(item);

    if (stateContainer.compositeActionHandler.onContextItemSelected(item) == true)
    {
      return true;
    }
    return result;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (log.isDebugEnabled())
    {
      log.debug("SmartMapActivity::onActivityResult");
    }
    super.onActivityResult(requestCode, resultCode, data);

    // // BUG: this seems to be a bug in Android, because this method is invoked before the "onResume()"
    // try
    // {
    // businessObjectRetrievalAndResultHandlers();
    // }
    // catch (Throwable throwable)
    // {
    // handleUnhandledException(throwable);
    // return;
    // }

    getCompositeActivityResultHandler().handle(requestCode, resultCode, data);
  }

  protected void onRegisterResultHandlers(ActivityResultHandler.Handler resultHandler)
  {
  }

  /**
   * Does nothing, but we can overload it in derived classes.
   */
  public void onSynchronizeDisplayObjects()
  {
  }

  public MenuHandler.Composite getCompositeActionHandler()
  {
    return stateContainer.compositeActionHandler;
  }

  public CompositeHandler getCompositeActivityResultHandler()
  {
    return stateContainer.compositeActivityResultHandler;
  }

  protected SharedPreferences getPreferences()
  {
    return stateContainer.getPreferences(getApplicationContext());
  }

  /**
   * ------------------- End of "Copied from the SmartActivity class" -------------------
   */

}
