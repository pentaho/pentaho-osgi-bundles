package org.pentaho.caching.api;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nhudak
 */
public class PentahoCacheSystemConfiguration {
  private static final Pattern GLOBAL_PATTERN = Pattern.compile( "global[.]([\\w_-]+)" );
  private static final int GLOBAL_PROP_GROUP = 1;
  private static final Pattern TEMPLATE_PATTERN = Pattern.compile( "template[.]([\\w_-]+)([.]([\\w_-]+))?" );
  private static final int TEMPLATE_NAME_GROUP = 1;
  private static final int TEMPLATE_PROP_GROUP = 3;

  private volatile ImmutableMap<String, String> configuration;
  private volatile ImmutableMap<String, String> global;
  private volatile ImmutableMap<String, Template> templateMap;

  public PentahoCacheSystemConfiguration() {
    configuration = ImmutableMap.of();
    global = ImmutableMap.of();
    templateMap = ImmutableMap.of();
  }

  public void setData( Map<String, String> config ) {
    Map<String, String> global = Maps.newHashMap();
    Map<String, Template> templateMap = Maps.newHashMap();

    for ( Map.Entry<String, String> entry : config.entrySet() ) {
      String key = entry.getKey(), value = entry.getValue();
      Matcher matcher;

      matcher = GLOBAL_PATTERN.matcher( key );
      if ( matcher.matches() ) {
        global.put( matcher.group( GLOBAL_PROP_GROUP ), value );
        continue;
      }

      matcher = TEMPLATE_PATTERN.matcher( key );
      if ( matcher.matches() ) {
        String templateName = matcher.group( TEMPLATE_NAME_GROUP );
        String propName = matcher.group( TEMPLATE_PROP_GROUP );
        if ( !templateMap.containsKey( templateName ) ) {
          templateMap.put( templateName, new Template() );
        }
        Template template = templateMap.get( templateName );

        if ( Strings.isNullOrEmpty( propName ) ) {
          template.description = value;
        } else {
          template.properties.put( propName, value );
        }
      }
    }

    this.configuration = ImmutableMap.copyOf( config );
    this.global = ImmutableMap.copyOf( global );
    this.templateMap = ImmutableMap.copyOf( templateMap );
  }

  public Map<String, PentahoCacheTemplateConfiguration> createTemplates( PentahoCacheManager cacheManager ) {
    ImmutableMap.Builder<String, PentahoCacheTemplateConfiguration> builder = ImmutableMap.builder();
    for ( Map.Entry<String, Template> entry : templateMap.entrySet() ) {
      Template template = entry.getValue();

      if ( !Strings.isNullOrEmpty( template.description ) ) {
        Map<String, String> properties = Maps.newHashMap( global );
        properties.putAll( template.properties );

        PentahoCacheTemplateConfiguration templateConfiguration =
          new PentahoCacheTemplateConfiguration( template.description, properties, cacheManager );
        builder.put( entry.getKey(), templateConfiguration );
      }
    }
    return builder.build();
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public Map<String, String> getGlobalProperties() {
    return global;
  }

  /**
   * @author nhudak
   */
  private class Template {
    private final Map<String, String> properties;
    private String description;

    public Template() {
      this.properties = Maps.newHashMap();
    }
  }
}
