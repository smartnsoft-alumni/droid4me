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

package com.smartnsoft.droid4me.framework;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.smartnsoft.droid4me.log.Logger;
import com.smartnsoft.droid4me.log.LoggerFactory;

/**
 * Gathers in one place some classes used by the {@link com.smartnsoft.droid4me.app.SmartActivity} class, which handles
 * {@link ListView list views}.
 *
 * @author Édouard Mercier
 * @since 2011.09.27
 */
public abstract class SmartAdapters
{

  /**
   * To capture how the simplified {@link BasisBusinessViewWrapper} work.
   *
   * @param <BusinessObjectClass> the business object class in relation with the {@link BasisBusinessViewWrapper} first type parameter
   * @param <ActivityClass>       the {@link Activity} class in relation with the {@link BasisBusinessViewWrapper} second type parameter
   * @param <LayoutInflaterClass> the {@link LayoutInflater} class in relation with the {@link BasisBusinessViewWrapper} third type parameter
   * @see BasisBusinessViewWrapper
   * @since 2014.05.23
   */
  public interface BusinessViewWrapperSimplified<BusinessObjectClass, ActivityClass extends Activity, LayoutInflaterClass extends LayoutInflater>
  {

    int getType(int position, BusinessObjectClass businessObjectClass);

    View createNewView(ActivityClass activity, LayoutInflaterClass layoutInflater, ViewGroup parent,
        BusinessObjectClass businessObject);

  }

  /**
   * Indicates the type of action on the underlying business object.
   */
  public enum ObjectEvent
  {
    Clicked, Selected, WipedLeftToRight, WipedRightToLeft
  }

  /**
   * Wraps a business object and its underlying Android {@link View} in a list or a grid, or whatever kind of {@link Adapter}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   * @param <ActivityClass>       the {@link Activity} class which will host the current wrapper graphical representation
   * @param <LayoutInflaterClass> the {@link LayoutInflater} class which will inflate the current wrapper layout
   * @since 2014.05.23
   */
  protected static abstract class BasicBusinessViewWrapper<BusinessObjectClass, ActivityClass extends Activity, LayoutInflaterClass extends LayoutInflater, ViewAttributesType>
  {

    /**
     * The default value that will be returned by {@link android.widget.ListAdapter#getItemId(int)} method.
     */
    public static final long DEFAULT_ITEM_ID = 0;

    private final BusinessObjectClass businessObject;

    public BasicBusinessViewWrapper(BusinessObjectClass businessObject)
    {
      this.businessObject = businessObject;
    }

    /**
     * @return the underlying business object
     */
    public final BusinessObjectClass getBusinessObject()
    {
      return businessObject;
    }

    // final void updateBusinessObject(BusinessObjectClass businessObject)
    // {
    // this.businessObject = businessObject;
    // }

    public final int getType(int position)
    {
      return getType(position, getBusinessObject());
    }

    public int getType(int position, BusinessObjectClass businessObjectClass)
    {
      return 0;
    }

    public final long getId()
    {
      return getId(getBusinessObject());
    }

    public final boolean isEnabled()
    {
      return isEnabled(getBusinessObject());
    }

    public boolean isEnabled(BusinessObjectClass businessObject)
    {
      return true;
    }

    public final boolean containsText(String lowerText)
    {
      return containsText(getBusinessObject(), lowerText);
    }

    public boolean containsText(BusinessObjectClass businessObject, String lowerText)
    {
      return true;
    }

    public final Intent computeIntent(ActivityClass activity, View view, ObjectEvent objectEvent,
        int position)
    {
      return computeIntent(activity, (view == null ? null : view.getTag()), view, getBusinessObject(), objectEvent,
          position);
    }

    public Intent computeIntent(ActivityClass activity, Object viewAttributes, View view,
        BusinessObjectClass businessObject, ObjectEvent objectEvent, int position)
    {
      return null;
    }

    public final boolean onObjectEvent(ActivityClass activity, View view, ObjectEvent objectEvent,
        int position)
    {
      return onObjectEvent(activity, (view == null ? null : view.getTag()), view, getBusinessObject(), objectEvent,
          position);
    }

    public boolean onObjectEvent(ActivityClass activity, Object viewAttributes, View view,
        BusinessObjectClass businessObject, ObjectEvent objectEvent, int position)
    {
      return false;
    }

    public final View getNewView(ViewGroup parent, ActivityClass activity, LayoutInflaterClass layoutInflater)
    {
      final View view = createNewView(activity, layoutInflater, parent, getBusinessObject());
      return setNewView(activity, view);
    }

    /**
     * Should only be invoked once the {@link #setNewView(Activity, View)} method has already been invoked.
     *
     * @param view the {@code View} which holds the business object representation
     * @return the view attributes that have been attached to the provided view
     */
    @SuppressWarnings("unchecked")
    public final ViewAttributesType getViewAttributes(View view)
    {
      return (ViewAttributesType) view.getTag();
    }

    public final void updateView(ActivityClass activity, LayoutInflaterClass layoutInflater, View view, int position)
    {
      final ViewAttributesType viewAttributes = getViewAttributes(view);
      updateView(activity, layoutInflater, viewAttributes, view, getBusinessObject(), position);
    }

    /**
     * Is responsible for creating a new {@link View}, which is able to represent the provided business object.
     *
     * @param activity            the Activity which hosts the view
     * @param parent              the parent view, as provided by the {@link Adapter#getView(int, View, ViewGroup)} method
     * @param businessObjectClass the business object the returned view will represent graphically
     * @return a new view, which will be used by the underlying {@link Adapter}
     */
    protected abstract View createNewView(ActivityClass activity, LayoutInflaterClass layoutInflater, ViewGroup parent,
        BusinessObjectClass businessObjectClass);

    protected abstract ViewAttributesType extractNewViewAttributes(ActivityClass activity, View view,
        BusinessObjectClass businessObjectClass);

    protected abstract void updateView(ActivityClass activity, LayoutInflaterClass layoutInflater,
        ViewAttributesType viewAttributes, View view,
        BusinessObjectClass businessObjectClass, int position);

    /**
     * @return the {@link Object#hashCode()} value, by default
     */
    protected long getId(BusinessObjectClass businessObject)
    {
      return businessObject == null ? BusinessViewWrapper.DEFAULT_ITEM_ID : businessObject.hashCode();
    }

    /**
     * Attaches a view to the underlying business object.
     *
     * @param activity the activity the view belongs to
     * @param view     the view to attach
     * @return the provided view
     */
    final View setNewView(ActivityClass activity, View view)
    {
      final Object viewAttributes = extractNewViewAttributes(activity, view, getBusinessObject());
      view.setTag(viewAttributes);
      return view;
    }

  }

  /**
   * The same exact purpose of its parent class, except that the {@code ViewAttributesType} type parameter is bound to the {@link Object class}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   * @param <ActivityClass>       the {@link Activity} class which will host the current wrapper graphical representation
   * @param <LayoutInflaterClass> the {@link LayoutInflater} class which will inflate the current wrapper layout
   * @since 2014.05.23
   */
  public static abstract class BasisBusinessViewWrapper<BusinessObjectClass, ActivityClass extends Activity, LayoutInflaterClass extends LayoutInflater>
      extends BasicBusinessViewWrapper<BusinessObjectClass, ActivityClass, LayoutInflaterClass, Object>
  {

    public BasisBusinessViewWrapper(BusinessObjectClass businessObject)
    {
      super(businessObject);
    }

  }

  /**
   * The same exact purpose of its parent class, except that the {@code ActivityClass} type parameter is bound to the actual Android built-in
   * {@link Activity class}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   * @since 2009.04.29
   */
  public static abstract class BusinessViewWrapper<BusinessObjectClass>
      extends BasisBusinessViewWrapper<BusinessObjectClass, Activity, LayoutInflater>
  {

    public BusinessViewWrapper(BusinessObjectClass businessObject)
    {
      super(businessObject);
    }

  }

  /**
   * An implementation which has been introduced so as to prevent from code duplication.
   *
   * @param <BusinessObjectClass> the business object class in relation with the {@link BasisBusinessViewWrapper} first type parameter
   * @param <ActivityClass>       the {@link Activity} class in relation with the {@link BasisBusinessViewWrapper} second type parameter
   * @param <LayoutInflaterClass> the {@link LayoutInflater} class in relation with the {@link BasisBusinessViewWrapper} third parameter
   * @see BasisBusinessViewWrapper
   * @since 2014.05.23
   */
  public static class BusinessViewWrapperSimplifier<BusinessObjectClass, ActivityClass extends Activity, LayoutInflaterClass extends LayoutInflater>
      implements BusinessViewWrapperSimplified<BusinessObjectClass, ActivityClass, LayoutInflaterClass>
  {

    public final int type;

    public final int layoutResourceId;

    public BusinessViewWrapperSimplifier(int type, int layoutResourceId)
    {
      this.type = type;
      this.layoutResourceId = layoutResourceId;
    }

    @Override
    public int getType(int position, BusinessObjectClass businessObjectClass)
    {
      return type;
    }

    @Override
    public View createNewView(ActivityClass activity, LayoutInflaterClass layoutInflater, ViewGroup parent,
        BusinessObjectClass businessObject)
    {
      // It is important that the activity itself be used as a basis context, otherwise, the inflated View context is limited!
      return layoutInflater.inflate(layoutResourceId, parent, false);
    }

  }

  /**
   * A basic wrapper implementation, which lets specify its {@link #getType(int, Object)} and representation layout identifier used to
   * {@link LayoutInflater#inflate(int, ViewGroup) inflate it}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   * @param <ActivityClass>       the {@link Activity} class which will host the current wrapper graphical representation
   * @param <LayoutInflaterClass> the {@link LayoutInflater} class which will inflate the current wrapper layout
   * @since 2014.05.23
   */
  public static abstract class BasisSimpleBusinessViewWrapper<BusinessObjectClass, ActivityClass extends Activity, LayoutInflaterClass extends LayoutInflater>
      extends BasisBusinessViewWrapper<BusinessObjectClass, ActivityClass, LayoutInflaterClass>
      implements BusinessViewWrapperSimplified<BusinessObjectClass, ActivityClass, LayoutInflaterClass>
  {

    protected final BusinessViewWrapperSimplifier<BusinessObjectClass, ActivityClass, LayoutInflaterClass> simplifier;

    public BasisSimpleBusinessViewWrapper(BusinessObjectClass businessObject, int type, int layoutResourceId)
    {
      super(businessObject);
      this.simplifier = new BusinessViewWrapperSimplifier<>(type,
          layoutResourceId);
    }

    @Override
    public int getType(int position, BusinessObjectClass businessObjectClass)
    {
      return simplifier.getType(position, businessObjectClass);
    }

    @Override
    public View createNewView(ActivityClass activity, LayoutInflaterClass layoutInflater, ViewGroup parent,
        BusinessObjectClass businessObject)
    {
      return simplifier.createNewView(activity, layoutInflater, parent, businessObject);
    }

  }

  /**
   * The same exact purpose of its parent class, except that the {@code ActivityClass} type parameter is bound to the actual Android built-in
   * {@link Activity class}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   */
  public static abstract class SimpleBusinessViewWrapper<BusinessObjectClass>
      extends BusinessViewWrapper<BusinessObjectClass>
      implements BusinessViewWrapperSimplified<BusinessObjectClass, Activity, LayoutInflater>
  {

    protected final BusinessViewWrapperSimplifier<BusinessObjectClass, Activity, LayoutInflater> simplifier;

    public SimpleBusinessViewWrapper(BusinessObjectClass businessObject, int type, int layoutResourceId)
    {
      super(businessObject);
      this.simplifier = new BusinessViewWrapperSimplifier<>(type,
          layoutResourceId);
    }

    @Override
    public int getType(int position, BusinessObjectClass businessObjectClass)
    {
      return simplifier.getType(position, businessObjectClass);
    }

    @Override
    public View createNewView(Activity activity, LayoutInflater layoutInflater, ViewGroup parent,
        BusinessObjectClass businessObject)
    {
      return simplifier.createNewView(activity, layoutInflater, parent, businessObject);
    }

  }

  /**
   * This class wraps the {@link BusinessViewWrapper} when not used inside a {@link ListView list}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   * @param <ActivityClass>       the {@link Activity} class which will host the current wrapper graphical representation
   * @param <LayoutInflaterClass> the {@link LayoutInflater} class which will inflate the current wrapper layout
   * @since 2014.05.23
   */
  public static class BasisBusinessViewHolder<BusinessObjectClass, ActivityClass extends Activity, LayoutInflaterClass extends LayoutInflater>
  {

    private final BasisBusinessViewWrapper<BusinessObjectClass, ActivityClass, LayoutInflaterClass> businessViewWrapper;

    private View view;

    public BasisBusinessViewHolder(
        BasisBusinessViewWrapper<BusinessObjectClass, ActivityClass, LayoutInflaterClass> businessViewWrapper)
    {
      this.businessViewWrapper = businessViewWrapper;
    }

    /**
     * Is allowed to be invoked once the {@link #getView(ViewGroup, Activity, LayoutInflater)} or {@link #setView(Activity, View)} method has been called.
     *
     * @return the view which represents the underlying business object
     */
    public final View getView()
    {
      return view;
    }

    /**
     * @return the wrapper passed in the constructor
     */
    public final BasisBusinessViewWrapper<BusinessObjectClass, ActivityClass, LayoutInflaterClass> getBusinessViewWrapper()
    {
      return businessViewWrapper;
    }

    // public final void updateBusinessObject(BusinessObjectClass businessObject)
    // {
    // businessViewWrapper.updateBusinessObject(businessObject);
    // }

    /**
     * This method should be called only once during the object life cycle.
     * <p>
     * <p>
     * This will invoke the {@link BusinessViewWrapper#getNewView(ViewGroup, Activity, LayoutInflater)} method.
     * </p>
     *
     * @param activity the activity on which the business object is being rendered
     * @return the initialized view that represent the underlying business object
     */
    public final View getView(ViewGroup parent, ActivityClass activity, LayoutInflaterClass layoutInflater)
    {
      view = businessViewWrapper.getNewView(parent, activity, layoutInflater);
      return view;
    }

    /**
     * Sets the view of the of the underlying business view wrapper, so that it is not necessary to invoke the {@link #getView(ViewGroup, Activity, LayoutInflater)} method.
     *
     * @param activity the activity on which the business object is being rendered
     * @param view     the view that will be attached to the business view wrapper
     * @return
     */
    public final View setView(ActivityClass activity, View view)
    {
      this.view = view;
      return businessViewWrapper.setNewView(activity, view);
    }

    /**
     * Synchronizes the rendering of the inner {@link View} with the state of the business object.
     * <p>
     * <p>
     * This will invoke the {@link BusinessViewWrapper#updateView(Activity, LayoutInflater, View, int)} method with a <code>position</code> set to
     * 0.
     * </p>
     *
     * @param activity the activity on which the business object is being rendered
     */
    public final void updateView(ActivityClass activity, LayoutInflaterClass layoutInflaterClass)
    {
      businessViewWrapper.updateView(activity, layoutInflaterClass, view, 0);
    }

  }

  /**
   * The same exact purpose of its parent class, except that the {@code ActivityClass} type parameter is bound to the actual Android built-in
   * {@link Activity class}.
   *
   * @param <BusinessObjectClass> the business object class which is represented by the current wrapper
   * @since 2010.06.23
   */
  public static class BusinessViewHolder<BusinessObjectClass>
      extends BasisBusinessViewHolder<BusinessObjectClass, Activity, LayoutInflater>
  {

    public BusinessViewHolder(
        BasisBusinessViewWrapper<BusinessObjectClass, Activity, LayoutInflater> businessViewWrapper)
    {
      super(businessViewWrapper);
    }

  }

  /**
   * A {@link ListView} adapter, which works closely with the {@link BusinessViewWrapper}.
   *
   * @param <ViewClass> the class which represents each graphical row of the list
   * @since 2012.11.23
   */
  public static class SmartListAdapter<ViewClass extends View>
      extends BaseAdapter
      implements AdapterView.OnItemClickListener
  {

    protected final Activity activity;

    protected List<? extends BusinessViewWrapper<?>> wrappers = new ArrayList<>();

    private final LayoutInflater layoutInflater;

    private int viewTypeCount = 1;

    private List<? extends BusinessViewWrapper<?>> forthcomingWrappers = null;

    /**
     * @param activity
     * @param viewTypeCount since the {@link #getViewTypeCount()} method is invoked only once, we need to state the number of different rows from the start
     */
    // Regarding the 'getViewTypeCount()' method invocation, read
    // http://stackoverflow.com/questions/15099041/listview-baseadapter-getviewtypecount-how-to-force-adapter-to-check-again
    public SmartListAdapter(Activity activity, LayoutInflater layoutInflater, int viewTypeCount)
    {
      this.activity = activity;
      this.viewTypeCount = viewTypeCount;
      this.layoutInflater = layoutInflater;
    }

    public final int getCount()
    {
      return wrappers.size();
    }

    public final Object getItem(int position)
    {
      return wrappers.get(position);
    }

    public final long getItemId(int position)
    {
      return wrappers.get(position).hashCode();
    }

    @Override
    public final boolean areAllItemsEnabled()
    {
      return false;
    }

    @Override
    public final boolean isEnabled(int position)
    {
      return wrappers.get(position).isEnabled();
    }

    @Override
    public final int getItemViewType(int position)
    {
      return wrappers.get(position).getType(position);
    }

    @Override
    public final boolean hasStableIds()
    {
      // i.e. "false"
      return super.hasStableIds();
    }

    @Override
    public int getViewTypeCount()
    {
      return viewTypeCount;
    }

    @SuppressWarnings("unchecked")
    public final View getView(int position, View convertView, ViewGroup parent)
    {
      try
      {
        final BusinessViewWrapper<?> businessObject = wrappers.get(position);
        final ViewClass innerView;
        final boolean isRecycled = (convertView != null);
        if (isRecycled == false)
        {
          innerView = (ViewClass) businessObject.getNewView(parent, activity, layoutInflater);
        }
        else
        {
          innerView = (ViewClass) convertView;
        }
        businessObject.updateView(activity, layoutInflater, innerView, position);

        // We let the opportunity to catch this update event
        onInterceptGetView(innerView, position, isRecycled);

        return innerView;
      }
      catch (Throwable throwable)
      {
        // TODO: find a more elegant way, and report that problem to the main Exception handler
        if (log.isErrorEnabled())
        {
          log.error("Could not get or update the list view at position '" + position + "'", throwable);
        }
        return new View(parent.getContext());
      }
    }

    @Override
    public void notifyDataSetChanged()
    {
      if (forthcomingWrappers != null)
      {
        wrappers = forthcomingWrappers;
        forthcomingWrappers = null;
      }
      super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated()
    {
      if (forthcomingWrappers != null)
      {
        wrappers = forthcomingWrappers;
        forthcomingWrappers = null;
      }
      super.notifyDataSetInvalidated();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
      if (adapterView.isEnabled() == false)
      {
        return;
      }
      final int actualPosition = position - (adapterView instanceof ListView ? ((ListView) adapterView).getHeaderViewsCount() : 0);
      if (actualPosition < 0 || actualPosition >= wrappers.size())
      {
        return;
      }

      if (adapterView.isEnabled() == false)
      {
        return;
      }
      if (actualPosition >= wrappers.size())
      {
        if (log.isErrorEnabled())
        {
          log.error(
              "The selected row " + actualPosition + " exceeds the size of the filtered business objetcs list which is " + wrappers.size());
        }
        return;
      }
      onInternalEvent(adapterView, view, wrappers.get(actualPosition), ObjectEvent.Clicked, actualPosition);
    }

    public void setWrappers(List<? extends BusinessViewWrapper<?>> wrappers)
    {
      this.forthcomingWrappers = wrappers;
    }

    public void setAdapter(ListView listView)
    {
      listView.setAdapter(this);
      listView.setOnItemClickListener(this);
    }

    public void setAdapter(GridView gridView)
    {
      gridView.setAdapter(this);
      gridView.setOnItemClickListener(this);
    }

    /**
     * Is invoked every time a {@link View} of the adapter is being updated. This is the right place for customizing the adapter.
     *
     * @param view       the view which holds the graphical representation of the business object
     * @param position   the position in the adapter
     * @param isRecycled {@code true} if and only if the provided view has just been created and is hence not recycled
     */
    protected void onInterceptGetView(ViewClass view, int position, boolean isRecycled)
    {
    }

    private boolean onInternalEvent(AdapterView<?> adapterView, View view,
        BusinessViewWrapper<?> businessObject, ObjectEvent objectEvent, int position)
    {
      if (adapterView.isEnabled() == true)
      {
        // We set a protection against a bad usage from the end-user
        final Intent intent;
        try
        {
          intent = businessObject.computeIntent(activity, view, objectEvent, position);
        }
        catch (Throwable throwable)
        {
          if (log.isErrorEnabled())
          {
            log.error(
                "The computing of the intent related to the business object with id '" + businessObject.getId() + "' and for the UI event '" + objectEvent + "' seems buggy; not taken into account!",
                throwable);
          }
          return false;
        }
        if (intent != null)
        {
          activity.startActivity(intent);
          return true;
        }
        else
        {
          // We set a protection against a bad usage from the end-user
          try
          {
            return businessObject.onObjectEvent(activity, view, objectEvent, position);
          }
          catch (Throwable throwable)
          {
            if (log.isErrorEnabled())
            {
              log.error(
                  "The computation of the action related to the business object with id '" + businessObject.getId() + "' and for the UI event '" + objectEvent + "' seems faulty; not taken into account!",
                  throwable);
            }
            return false;
          }
        }
      }
      return false;
    }

  }

  protected static final Logger log = LoggerFactory.getInstance("SmartAdapters");

}