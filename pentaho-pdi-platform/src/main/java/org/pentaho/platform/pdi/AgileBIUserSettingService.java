/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

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
  
  Map<String,String> globalUserSettings = new HashMap<String,String>();
  Map<String,String> userSettings = new HashMap<String,String>();

  @Override
  public void init(IPentahoSession arg0) {
  }

  @Override
  public void deleteUserSettings() {
    userSettings.clear();
  }

  @Override
  public IUserSetting getGlobalUserSetting(String settingName, String defaultValue) {
    return getSetting(globalUserSettings, settingName, defaultValue);
  }

  @Override
  public List<IUserSetting> getGlobalUserSettings() {
    return getSettingsList(globalUserSettings);
  }

  @Override
  public IUserSetting getUserSetting(String settingName, String defaultValue) {
    return getSetting(userSettings, settingName, defaultValue);
  }

  @Override
  public List<IUserSetting> getUserSettings() {
    return getSettingsList(userSettings);
  }

  @Override
  public void setGlobalUserSetting(String settingName, String settingValue) {
    setSettingValue(globalUserSettings, settingName, settingValue);
  }

  @Override
  public void setUserSetting(String settingName, String settingValue) {
    setSettingValue(userSettings, settingName, settingValue);
  }
  
  private IUserSetting getSetting(Map<String,String> settingsMap, String settingName, String defaultValue) {
    String value = settingsMap.get(settingName);
    UserSetting setting = new UserSetting();
    setting.setSettingName(settingName);
    setting.setSettingValue(value != null ? value : defaultValue);
    return setting;
  }
  
  private List<IUserSetting> getSettingsList(Map<String,String> settingsMap) {
    List<IUserSetting> settingsList = new ArrayList<IUserSetting>();
    if(settingsMap != null) {
      for(Map.Entry<String,String> me : settingsMap.entrySet()) {
        IUserSetting userSetting = new UserSetting();
        userSetting.setSettingName(me.getKey());
        userSetting.setSettingValue(me.getValue());
        settingsList.add(userSetting);
      }
    }
    return settingsList;
  }
  
  private void setSettingValue(Map<String,String> settingsMap, String settingName, String settingValue) {
    settingsMap.put(settingName, settingValue);    
  }
}
