/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.kettle;

import org.apache.log4j.FileAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.XmlTestConstants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringBufferInputStream;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KettleSystemListenerTest {
  private IApplicationContext mockApplicationContext;
  private FileAppender fileAppender = new FileAppender();

  @Before
  public void setup() {
    mockApplicationContext = mock( IApplicationContext.class );
    org.apache.log4j.Logger.getRootLogger().addAppender( fileAppender );
    PentahoSystem.setApplicationContext( mockApplicationContext );
  }

  @After
  public void teardown() {
    org.apache.log4j.Logger.getRootLogger().removeAppender( fileAppender );
  }

  @Test
  public void testStartup() {
    KettleSystemListener ksl = new KettleSystemListener();
    assertTrue( ksl.startup( null ) );
  }

  @Test
  public void testDefaultDIHome() throws Exception {
    System.setProperty( "DI_HOME", "" );
    when( mockApplicationContext.getSolutionPath( nullable( String.class ) ) ).thenReturn( "/kettle" );

    KettleSystemListener ksl = new KettleSystemListener();
    ksl.startup( null );
    assertThat( "Empty DI_HOME should be defaulted", System.getProperty( "DI_HOME" ), equalTo( "/kettle" ) );


    System.setProperty( "DI_HOME", "custom" );
    ksl = new KettleSystemListener();
    ksl.startup( null );

    assertThat( "Validly set DI_HOME not preserved", System.getProperty( "DI_HOME" ), equalTo( "custom" ) );
  }

  @Test( timeout = 2000, expected = SAXException.class )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven() throws IOException, ParserConfigurationException, SAXException {
    KettleSystemListener ksl = new KettleSystemListener();

    ksl.getSlaveServerConfigNode( new StringBufferInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";
    KettleSystemListener ksl = new KettleSystemListener();

    assertNotNull( ksl.getSlaveServerConfigNode( new StringBufferInputStream( xml ) ) );
  }
}
