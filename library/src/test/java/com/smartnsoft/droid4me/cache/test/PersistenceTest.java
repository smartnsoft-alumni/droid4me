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

package com.smartnsoft.droid4me.cache.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.util.Xml.Encoding;

import com.smartnsoft.droid4me.bo.Business.InputAtom;
import com.smartnsoft.droid4me.cache.FilePersistence;
import com.smartnsoft.droid4me.cache.Persistence;
import com.smartnsoft.droid4me.test.BasisTests;
import com.smartnsoft.droid4me.ws.WebServiceCaller;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Édouard Mercier
 * @since 2011.09.06
 */
public final class PersistenceTest
    extends BasisTests
{

  private FilePersistence persistence;

  @Before
  public void setup()
  {
    super.setup();
    persistence = new FilePersistence(getTemporaryDirectory().getPath(), 0);
    persistence.initialize();
  }

  @After
  public void tearDown()
  {
    persistence.clear();
    persistence.close();
  }

  @Test
  public void lazyInitialization()
      throws IOException
  {
    final File directory = new File(getTemporaryDirectory(), "other");
    final Persistence aNewPersistence = new FilePersistence(directory.getPath(), 0);
    try
    {
      Assert.assertEquals("The persistence instance should not be initialized", false, aNewPersistence.isInitialized());
      aNewPersistence.getUris();
      Assert.assertEquals("The persistence instance should now be initialized", true, aNewPersistence.isInitialized());
    }
    finally
    {
      aNewPersistence.clear();
      aNewPersistence.close();
    }
  }

  @Test
  public void closeAndReinitialize()
      throws IOException
  {
    Assert.assertEquals("The persistence instance should now be initialized", true, persistence.isInitialized());
    persistence.close();
    Assert.assertEquals("The persistence instance should not be initialized", false, persistence.isInitialized());
    persistence.getUris();
    Assert.assertEquals("The persistence instance should now be initialized", true, persistence.isInitialized());
  }

  @Test
  public void filePersistenceRemove()
      throws IOException
  {
    final Date timestamp = new Date();
    final String uri2 = "myUri2";
    final String uri3 = "myUri3";
    final String persistedValue = new String("My persisted value");
    {
      final String uri1 = "myUri1";
      persistence.writeInputStream(uri1, new InputAtom(timestamp, new ByteArrayInputStream(persistedValue.getBytes())), false);
      persistence.writeInputStream(uri2, new InputAtom(timestamp, new ByteArrayInputStream(persistedValue.getBytes())), false);
      persistence.writeInputStream(uri3, new InputAtom(timestamp, new ByteArrayInputStream(persistedValue.getBytes())), false);
    }

    {
      persistence.remove(uri2);
      final InputAtom atom = persistence.readInputStream(uri3);
      Assert.assertNotNull("The atom should not be null", atom);
      Assert.assertNotNull("The input stream should not be null", atom.inputStream);
      final String retrievedPersistedValue = WebServiceCaller.getString(atom.inputStream, Encoding.UTF_8.toString());
      Assert.assertEquals("The input stream is not the expected one", persistedValue, retrievedPersistedValue);
    }

    {
      final String persistedValue4 = new String("My persisted value 4");
      final ByteArrayInputStream inputStream3 = new ByteArrayInputStream(persistedValue4.getBytes());
      final String uri4 = "myUri4";
      persistence.writeInputStream(uri4, new InputAtom(timestamp, inputStream3), false);
      final InputAtom atom = persistence.readInputStream(uri4);
      Assert.assertNotNull("The atom should not be null", atom);
      Assert.assertNotNull("The input stream should not be null", atom.inputStream);
      final String retrievedPersistedValue = WebServiceCaller.getString(atom.inputStream, Encoding.UTF_8.toString());
      Assert.assertEquals("The input stream is not the expected one", persistedValue4, retrievedPersistedValue);
    }
  }

}
