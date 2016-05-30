package org.pentaho.i18n.example;

import org.pentaho.osgi.i18n.LocalizationService;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by nbaker on 5/4/16.
 */
@Produces( { MediaType.TEXT_PLAIN } )
@WebService
public class ExampleService {

  private LocalizationService localizationService;

  public ExampleService() {

  }

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  @GET
  @Path( "/" )
  public String getTranslation( @QueryParam( "key" ) String key, @QueryParam( "locale" ) String locale,
                                @Context HttpServletRequest request ) {
    ResourceBundle resourceBundle = localizationService.getResourceBundle( key, new Locale( locale ) );

    if ( resourceBundle == null ) {
      return "Bundle not found";
    }
    if ( !resourceBundle.containsKey( key ) ) {
      return "Key not found";
    }
    return resourceBundle.getString( key );
  }
}
