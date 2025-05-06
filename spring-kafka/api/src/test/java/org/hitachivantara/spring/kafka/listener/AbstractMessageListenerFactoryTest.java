/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.hitachivantara.spring.kafka.listener;

import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class AbstractMessageListenerFactoryTest {

  private AbstractMessageListenerFactory abstractMessageListenerFactory;
  private AbstractMessageListener abstractMessageListener;

  @Before
  public void setup() {
    abstractMessageListener = new Listener();

    abstractMessageListenerFactory = new AbstractMessageListenerFactory() {
      @Override
      protected AbstractMessageListener getMessageListenerInstance() {
        return abstractMessageListener;
      }
    };
  }

  @Test
  public void testGetInstanceConsumerFactoryIsSet() {
    ConsumerFactory consumerFactoryMock = mock( ConsumerFactory.class );
    abstractMessageListenerFactory.setConsumerFactory( consumerFactoryMock );

    AbstractMessageListener messageListener = abstractMessageListenerFactory.getInstance( "topic", "groupId" );

    assertEquals( abstractMessageListener, messageListener );
    assertEquals( consumerFactoryMock, messageListener.getConsumerFactory() );
  }

  @Test
  public void testGetInstanceTopicAndGroupAreSet() {
    ConsumerFactory consumerFactoryMock = mock( ConsumerFactory.class );
    abstractMessageListenerFactory.setConsumerFactory( consumerFactoryMock );

    AbstractMessageListener messageListener = abstractMessageListenerFactory.getInstance( "topic", "groupId" );

    assertEquals( abstractMessageListener, messageListener );
    assertEquals( "topic", messageListener.getTopic() );
    assertEquals( "groupId", messageListener.getGroupId() );
  }

  private class Listener extends AbstractMessageListener {

    @Override public void onMessage( Object o, Acknowledgment acknowledgment ) {
      // noop
    }
  }
}
