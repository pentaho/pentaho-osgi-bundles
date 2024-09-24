/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.platform.pdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;

public class AgileBIUserSettingService implements IUserSettingService {

  Map<String, String> globalUserSettings = new HashMap<String, String>();
  Map<String, String> userSettings = new HashMap<String, String>();

  @Override
  public void init( IPentahoSession arg0 ) {
  }

  @Override
  public void deleteUserSettings() {
    userSettings.clear();
  }

  @Override
  public IUserSetting getGlobalUserSetting( String settingName, String defaultValue ) {
    return getSetting( globalUserSettings, settingName, defaultValue );
  }

  @Override
  public List<IUserSetting> getGlobalUserSettings() {
    return getSettingsList( globalUserSettings );
  }

  @Override
  public IUserSetting getUserSetting( String settingName, String defaultValue ) {
    return getSetting( userSettings, settingName, defaultValue );
  }

  @Override
  public List<IUserSetting> getUserSettings() {
    return getSettingsList( userSettings );
  }

  @Override
  public void setGlobalUserSetting( String settingName, String settingValue ) {
    setSettingValue( globalUserSettings, settingName, settingValue );
  }

  @Override
  public void setUserSetting( String settingName, String settingValue ) {
    setSettingValue( userSettings, settingName, settingValue );
  }

  private IUserSetting getSetting( Map<String, String> settingsMap, String settingName, String defaultValue ) {
    String value = settingsMap.get( settingName );
    UserSetting setting = new UserSetting();
    setting.setSettingName( settingName );
    setting.setSettingValue( value != null ? value : defaultValue );
    return setting;
  }

  private List<IUserSetting> getSettingsList( Map<String, String> settingsMap ) {
    List<IUserSetting> settingsList = new ArrayList<IUserSetting>();
    if ( settingsMap != null ) {
      for ( Map.Entry<String, String> me : settingsMap.entrySet() ) {
        IUserSetting userSetting = new UserSetting();
        userSetting.setSettingName( me.getKey() );
        userSetting.setSettingValue( me.getValue() );
        settingsList.add( userSetting );
      }
    }
    return settingsList;
  }

  private void setSettingValue( Map<String, String> settingsMap, String settingName, String settingValue ) {
    settingsMap.put( settingName, settingValue );
  }
}
