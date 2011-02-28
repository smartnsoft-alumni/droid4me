/*
 * (C) Copyright 2009-2011 Smart&Soft SAS (http://www.smartnsoft.com/) and contributors.
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

package com.smartnsoft.droid4me;

import android.app.Activity;

/**
 * Identifies a typical work-flow for an {@link Activity activity of the framework}.
 * 
 * <p>
 * The methods are sorted in chronological order of call by the framework.
 * </p>
 * 
 * @author Édouard Mercier
 * @since 2008.05.18
 */
// TODO: add extra contracts so that the inherited Activity
// "onCreate/onResume/onStart/onPause/onStop/onDestroy/onSaveInstanceState/onRestoreInstanceState" methods can be marked
// "final" at last, and thus propose a solid framework!
public interface LifeCycle
{

  /**
   * This exception should be triggered on the framework methods which allow to throw it, when a business object is not accessible.
   */
  public final class BusinessObjectUnavailableException
      extends Exception
  {

    private static final long serialVersionUID = -6764122467050013650L;

    public BusinessObjectUnavailableException()
    {
    }

    public BusinessObjectUnavailableException(String message, Throwable cause)
    {
      super(message, cause);
    }

    public BusinessObjectUnavailableException(String message)
    {
      super(message);
    }

    public BusinessObjectUnavailableException(Throwable cause)
    {
      super(cause);
    }

  }

  /**
   * Indicates the way (synchronously or asynchronously) the business objects should be retrieved.
   * 
   * @since 2009.02.19
   */
  public interface BusinessObjectsRetrievalAsynchronousPolicy
  {

  }

  /**
   * This is the place where the derived class can get all graphical objects and put them into attributes. The method is invoked during the
   * {@link Activity#onCreate} execution, after the parent {@link Activity#onCreate} method has been invoked.
   * 
   * <p>
   * It is ensured that this method will be invoked from the UI thread!
   * </p>
   */
  void onRetrieveDisplayObjects();

  /**
   * This is the place where to load the business objects. It is strongly adviced not to modify the GUI during this method execution.
   * 
   * <p>
   * It is NOT ensured that this method will be invoked from the UI thread!
   * </p>
   */
  void onRetrieveBusinessObjects()
      throws BusinessObjectUnavailableException;

  /**
   * This is the typical callback that will be invoked when the business objects have been actually retrieved. It is strongly adviced not to modify
   * the GUI during this method execution.
   * 
   * <p>
   * It is NOT ensured that this method will be invoked from the UI thread! If the {@link Activity} implements the
   * {@link LifeCycle.BusinessObjectsRetrievalAsynchronousPolicy} interface, this method will be invoked from a high-priority worker thread.
   * </p>
   */
  void onBusinessObjectsRetrieved();

  /**
   * This is the place where the derived class can initialize the previously retrieved graphical objects. The method is invoked during the
   * {@link Activity#onResume} execution, and only the very first time.
   * 
   * <p>
   * It is ensured that this method will be invoked from the UI thread!
   * </p>
   */
  void onFulfillDisplayObjects();

  /**
   * Will be invoked during the {@link Activity#onResume} execution, every time. It is supposed to fulfill the display objects that may need some
   * refreshing due to the underlying business objects that have changed.
   * 
   * <p>
   * It is ensured that this method will be invoked from the UI thread!
   * </p>
   */
  void onSynchronizeDisplayObjects();

}