/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.authentication.mapper.api;

/**
 * Created by bryan on 3/18/16.
 */
public class MappingException extends Exception {
  public MappingException() {
  }

  public MappingException( String message ) {
    super( message );
  }

  public MappingException( String message, Throwable cause ) {
    super( message, cause );
  }

  public MappingException( Throwable cause ) {
    super( cause );
  }

  @FunctionalInterface
  public interface Function<T, R> {
    R apply( T t ) throws MappingException;
  }

  @FunctionalInterface
  public interface Supplier<R> {
    R get() throws MappingException;
  }
}
