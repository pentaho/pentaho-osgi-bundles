/*
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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class WebjarsURLConnectionTest {
  private static JSONParser parser;

  private static JSONObject npmJsonOutput;

  private static JSONObject pomJsonOutput;

  private static JSONObject scriptJsonOutput;

  private static JSONObject bowerJsonOutput;

  private static JSONObject fallbackJsonOutput;

  static {
    try {
      parser = new JSONParser();

      pomJsonOutput = (JSONObject) parser.parse( "{\"requirejs-osgi-meta\":{\"modules\":{\"smart-table\":{\"2.0"
        + ".3-1\":{\"dependencies\":{\"pentaho-webjar-deployer:org.webjars\\/angularjs\":\"1.2.27\"}}},"
        + "\"smart-table-min\":{\"2.0.3-1\":{\"dependencies\":{\"pentaho-webjar-deployer:org"
        + ".webjars\\/angularjs\":\"1.2.27\"}}}},\"artifacts\":{\"org.webjars\\/smart-table\":{\"2.0.3"
        + ".1\":{\"type\":\"pom.xml\",\"modules\":{\"smart-table\":\"2.0.3-1\",\"smart-table-min\":\"2.0.3-1\"}}}}},"
        + "\"paths\":{\"smart-table-min\\/2.0.3-1\":\"smart-table\\/2.0.3-1\\/smart-table.min\",\"smart-table\\/2.0"
        + ".3-1\":\"smart-table\\/2.0.3-1\\/smart-table\"},\"shim\":{\"smart-table\\/2.0.3-1\":[\"angular\"]},"
        + "\"map\":{}}" );

      scriptJsonOutput = (JSONObject) parser
        .parse( "{\"requirejs-osgi-meta\":{\"modules\":{\"angular-locale_en-gu\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sl\":{\"1.3.0-rc.0\":{}},\"angular-locale_bn-bd\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sk\":{\"1.3.0-rc.0\":{}},\"angular-locale_ko\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sw\":{\"1.3.0-rc.0\":{}},\"angular-locale_kn\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sv\":{\"1.3.0-rc.0\":{}},\"angular-locale_sl-si\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sr\":{\"1.3.0-rc.0\":{}},\"angular-locale_lt-lt\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_fil-ph\":{\"1.3.0-rc.0\":{}},\"angular-locale_sq\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ca\":{\"1.3.0-rc.0\":{}},\"angular-locale_ml-in\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_bn\":{\"1.3.0-rc.0\":{}},\"angular-locale_sr-cyrl-rs\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-route\":{\"1.3.0-rc.0\":{}},\"angular-locale_hu-hu\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_tl-ph\":{\"1.3.0-rc.0\":{}},\"angular-locale_th-th\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-gb\":{\"1.3.0-rc.0\":{}},\"angular-locale_et-ee\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_gsw-ch\":{\"1.3.0-rc.0\":{}},\"angular-locale_ro\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_is-is\":{\"1.3.0-rc.0\":{}},\"angular-locale_ja\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_bg\":{\"1.3.0-rc.0\":{}},\"angular-locale_ru\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ta-in\":{\"1.3.0-rc.0\":{}},\"angular\":{\"1.3.0-rc.0\":{}},\"angular-locale_am\":{\"1.3"
          + ".0-rc.0\":{}},\"angular-resource\":{\"1.3.0-rc.0\":{}},\"angular-locale_it\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sr-latn-rs\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-re\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-touch\":{\"1.3.0-rc.0\":{}},\"angular-locale_or-in\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_is\":{\"1.3.0-rc.0\":{}},\"angular-locale_sw-tz\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ca-es\":{\"1.3.0-rc.0\":{}},\"angular-locale_hr-hr\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-mocks\":{\"1.3.0-rc.0\":{}},\"angular-locale_ja-jp\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ar\":{\"1.3.0-rc.0\":{}},\"angular-locale_zh\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_iw\":{\"1.3.0-rc.0\":{}},\"angular-locale_it-it\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ur\":{\"1.3.0-rc.0\":{}},\"angular-locale_pl-pl\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-vi\":{\"1.3.0-rc.0\":{}},\"angular-locale_pt-pt\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_he-il\":{\"1.3.0-rc.0\":{}},\"angular-locale_uk\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_mr\":{\"1.3.0-rc.0\":{}},\"angular-locale_ur-pk\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_mo\":{\"1.3.0-rc.0\":{}},\"angular-locale_lv-lv\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ml\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-us\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_uk-ua\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-dsrt\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_lv\":{\"1.3.0-rc.0\":{}},\"angular-locale_lt\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-mp\":{\"1.3.0-rc.0\":{}},\"angular-locale_de-de\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-animate\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-bl\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-um\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-mh\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_de-lu\":{\"1.3.0-rc.0\":{}},\"angular-locale_tr-tr\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_id-id\":{\"1.3.0-rc.0\":{}},\"angular-locale_da\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_tl\":{\"1.3.0-rc.0\":{}},\"angular-locale_es-es\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sr-rs\":{\"1.3.0-rc.0\":{}},\"angular-locale_ln\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_de\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-ca\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_tr\":{\"1.3.0-rc.0\":{}},\"angular-locale_ta\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_de-ch\":{\"1.3.0-rc.0\":{}},\"angular-locale_zh-hans-cn\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_zh-hk\":{\"1.3.0-rc.0\":{}},\"angular-locale_fi-fi\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_kn-in\":{\"1.3.0-rc.0\":{}},\"angular-locale_th\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_te\":{\"1.3.0-rc.0\":{}},\"angular-locale_cs\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_de-at\":{\"1.3.0-rc.0\":{}},\"angular-locale_cs-cz\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_gl\":{\"1.3.0-rc.0\":{}},\"angular-locale_or\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_pt-br\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_de-be\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-mc\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_el-polyton\":{\"1.3.0-rc.0\":{}},\"angular-locale_mt-mt\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ko-kr\":{\"1.3.0-rc.0\":{}},\"angular-locale_ro-ro\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_vi-vn\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-dsrt-us\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_te-in\":{\"1.3.0-rc.0\":{}},\"angular-locale_fa\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_sv-se\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-mq\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-sg\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-mf\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ln-cd\":{\"1.3.0-rc.0\":{}},\"angular-locale_eu-es\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_fi\":{\"1.3.0-rc.0\":{}},\"angular-locale_no\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_fil\":{\"1.3.0-rc.0\":{}},\"angular-locale_nl\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_gsw\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-zz\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-iso\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_mt\":{\"1.3.0-rc.0\":{}},\"angular-locale_el\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ms\":{\"1.3.0-rc.0\":{}},\"angular-locale_vi\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_eu\":{\"1.3.0-rc.0\":{}},\"angular-locale_et\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_es\":{\"1.3.0-rc.0\":{}},\"angular-locale_el-gr\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ru-ru\":{\"1.3.0-rc.0\":{}},\"angular-locale_id\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-as\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-au\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_in\":{\"1.3.0-rc.0\":{}},\"angular-locale_en-za\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-loader\":{\"1.3.0-rc.0\":{}},\"angular-locale_zh-cn\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_hu\":{\"1.3.0-rc.0\":{}},\"angular-cookies\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_zh-hans\":{\"1.3.0-rc.0\":{}},\"angular-locale_hr\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-in\":{\"1.3.0-rc.0\":{}},\"angular-locale_mr-in\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_da-dk\":{\"1.3.0-rc.0\":{}},\"angular-locale_fr-fr\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_gu-in\":{\"1.3.0-rc.0\":{}},\"angular-scenario\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_en-ie\":{\"1.3.0-rc.0\":{}},\"angular-locale_sq-al\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_he\":{\"1.3.0-rc.0\":{}},\"angular-locale_pl\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_fa-ir\":{\"1.3.0-rc.0\":{}},\"angular-sanitize\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_ar-eg\":{\"1.3.0-rc.0\":{}},\"angular-locale_nl-nl\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_pt\":{\"1.3.0-rc.0\":{}},\"angular-locale_am-et\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_hi\":{\"1.3.0-rc.0\":{}},\"angular-locale_sk-sk\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_gl-es\":{\"1.3.0-rc.0\":{}},\"angular-locale_ms-my\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_zh-tw\":{\"1.3.0-rc.0\":{}},\"angular-locale_hi-in\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_fr-gp\":{\"1.3.0-rc.0\":{}},\"angular-locale_bg-bg\":{\"1.3.0-rc.0\":{}},"
          + "\"angular-locale_gu\":{\"1.3.0-rc.0\":{}}},\"artifacts\":{\"org.webjars\\/angularjs\":{\"1.3.0"
          + ".rc_0\":{\"type\":\"webjars-requirejs.js\",\"modules\":{\"angular-locale_en-gu\":\"1.3.0-rc.0\","
          + "\"angular-locale_sl\":\"1.3.0-rc.0\",\"angular-locale_bn-bd\":\"1.3.0-rc.0\",\"angular-locale_sk\":\"1.3"
          + ".0-rc.0\",\"angular-locale_ko\":\"1.3.0-rc.0\",\"angular-locale_sw\":\"1.3.0-rc.0\","
          + "\"angular-locale_kn\":\"1.3.0-rc.0\",\"angular-locale_sv\":\"1.3.0-rc.0\",\"angular-locale_sl-si\":\"1.3"
          + ".0-rc.0\",\"angular-locale_sr\":\"1.3.0-rc.0\",\"angular-locale_lt-lt\":\"1.3.0-rc.0\","
          + "\"angular-locale_fil-ph\":\"1.3.0-rc.0\",\"angular-locale_sq\":\"1.3.0-rc.0\",\"angular-locale_ca\":\"1.3"
          + ".0-rc.0\",\"angular-locale_ml-in\":\"1.3.0-rc.0\",\"angular-locale_bn\":\"1.3.0-rc.0\","
          + "\"angular-locale_sr-cyrl-rs\":\"1.3.0-rc.0\",\"angular-route\":\"1.3.0-rc.0\",\"angular-locale_hu-hu\":\"1"
          + ".3.0-rc.0\",\"angular-locale_tl-ph\":\"1.3.0-rc.0\",\"angular-locale_th-th\":\"1.3.0-rc.0\","
          + "\"angular-locale_en-gb\":\"1.3.0-rc.0\",\"angular-locale_et-ee\":\"1.3.0-rc.0\","
          + "\"angular-locale_gsw-ch\":\"1.3.0-rc.0\",\"angular-locale_ro\":\"1.3.0-rc.0\",\"angular-locale_is-is\":\"1"
          + ".3.0-rc.0\",\"angular-locale_ja\":\"1.3.0-rc.0\",\"angular-locale_bg\":\"1.3.0-rc.0\","
          + "\"angular-locale_ru\":\"1.3.0-rc.0\",\"angular-locale_ta-in\":\"1.3.0-rc.0\",\"angular\":\"1.3.0-rc.0\","
          + "\"angular-locale_am\":\"1.3.0-rc.0\",\"angular-resource\":\"1.3.0-rc.0\",\"angular-locale_it\":\"1.3.0-rc"
          + ".0\",\"angular-locale_sr-latn-rs\":\"1.3.0-rc.0\",\"angular-locale_fr-re\":\"1.3.0-rc.0\","
          + "\"angular-touch\":\"1.3.0-rc.0\",\"angular-locale_or-in\":\"1.3.0-rc.0\",\"angular-locale_is\":\"1.3.0-rc"
          + ".0\",\"angular-locale_sw-tz\":\"1.3.0-rc.0\",\"angular-locale_ca-es\":\"1.3.0-rc.0\","
          + "\"angular-locale_hr-hr\":\"1.3.0-rc.0\",\"angular-mocks\":\"1.3.0-rc.0\",\"angular-locale_ja-jp\":\"1.3"
          + ".0-rc.0\",\"angular-locale_ar\":\"1.3.0-rc.0\",\"angular-locale_zh\":\"1.3.0-rc.0\","
          + "\"angular-locale_iw\":\"1.3.0-rc.0\",\"angular-locale_it-it\":\"1.3.0-rc.0\",\"angular-locale_ur\":\"1.3"
          + ".0-rc.0\",\"angular-locale_pl-pl\":\"1.3.0-rc.0\",\"angular-locale_en-vi\":\"1.3.0-rc.0\","
          + "\"angular-locale_pt-pt\":\"1.3.0-rc.0\",\"angular-locale_he-il\":\"1.3.0-rc.0\",\"angular-locale_uk\":\"1"
          + ".3.0-rc.0\",\"angular-locale_mr\":\"1.3.0-rc.0\",\"angular-locale_ur-pk\":\"1.3.0-rc.0\","
          + "\"angular-locale_mo\":\"1.3.0-rc.0\",\"angular-locale_lv-lv\":\"1.3.0-rc.0\",\"angular-locale_ml\":\"1.3"
          + ".0-rc.0\",\"angular-locale_en-us\":\"1.3.0-rc.0\",\"angular-locale_uk-ua\":\"1.3.0-rc.0\","
          + "\"angular-locale_en-dsrt\":\"1.3.0-rc.0\",\"angular-locale_lv\":\"1.3.0-rc.0\",\"angular-locale_lt\":\"1.3"
          + ".0-rc.0\",\"angular-locale_en-mp\":\"1.3.0-rc.0\",\"angular-locale_de-de\":\"1.3.0-rc.0\","
          + "\"angular-animate\":\"1.3.0-rc.0\",\"angular-locale_fr-bl\":\"1.3.0-rc.0\",\"angular-locale_en-um\":\"1.3"
          + ".0-rc.0\",\"angular-locale_en-mh\":\"1.3.0-rc.0\",\"angular-locale_de-lu\":\"1.3.0-rc.0\","
          + "\"angular-locale_tr-tr\":\"1.3.0-rc.0\",\"angular-locale_id-id\":\"1.3.0-rc.0\",\"angular-locale_da\":\"1"
          + ".3.0-rc.0\",\"angular-locale_tl\":\"1.3.0-rc.0\",\"angular-locale_es-es\":\"1.3.0-rc.0\","
          + "\"angular-locale_sr-rs\":\"1.3.0-rc.0\",\"angular-locale_ln\":\"1.3.0-rc.0\",\"angular-locale_de\":\"1.3"
          + ".0-rc.0\",\"angular-locale_fr-ca\":\"1.3.0-rc.0\",\"angular-locale_tr\":\"1.3.0-rc.0\","
          + "\"angular-locale_ta\":\"1.3.0-rc.0\",\"angular-locale_de-ch\":\"1.3.0-rc.0\","
          + "\"angular-locale_zh-hans-cn\":\"1.3.0-rc.0\",\"angular-locale_zh-hk\":\"1.3.0-rc.0\","
          + "\"angular-locale_fi-fi\":\"1.3.0-rc.0\",\"angular-locale_kn-in\":\"1.3.0-rc.0\",\"angular-locale_th\":\"1"
          + ".3.0-rc.0\",\"angular-locale_te\":\"1.3.0-rc.0\",\"angular-locale_cs\":\"1.3.0-rc.0\","
          + "\"angular-locale_de-at\":\"1.3.0-rc.0\",\"angular-locale_cs-cz\":\"1.3.0-rc.0\",\"angular-locale_gl\":\"1"
          + ".3.0-rc.0\",\"angular-locale_or\":\"1.3.0-rc.0\",\"angular-locale_pt-br\":\"1.3.0-rc.0\","
          + "\"angular-locale_fr\":\"1.3.0-rc.0\",\"angular-locale_de-be\":\"1.3.0-rc.0\",\"angular-locale_fr-mc\":\"1"
          + ".3.0-rc.0\",\"angular-locale_el-polyton\":\"1.3.0-rc.0\",\"angular-locale_mt-mt\":\"1.3.0-rc.0\","
          + "\"angular-locale_ko-kr\":\"1.3.0-rc.0\",\"angular-locale_ro-ro\":\"1.3.0-rc.0\","
          + "\"angular-locale_vi-vn\":\"1.3.0-rc.0\",\"angular-locale_en-dsrt-us\":\"1.3.0-rc.0\","
          + "\"angular-locale_te-in\":\"1.3.0-rc.0\",\"angular-locale_fa\":\"1.3.0-rc.0\",\"angular-locale_sv-se\":\"1"
          + ".3.0-rc.0\",\"angular-locale_fr-mq\":\"1.3.0-rc.0\",\"angular-locale_en-sg\":\"1.3.0-rc.0\","
          + "\"angular-locale_fr-mf\":\"1.3.0-rc.0\",\"angular-locale_ln-cd\":\"1.3.0-rc.0\","
          + "\"angular-locale_eu-es\":\"1.3.0-rc.0\",\"angular-locale_fi\":\"1.3.0-rc.0\",\"angular-locale_no\":\"1.3"
          + ".0-rc.0\",\"angular-locale_fil\":\"1.3.0-rc.0\",\"angular-locale_nl\":\"1.3.0-rc.0\","
          + "\"angular-locale_gsw\":\"1.3.0-rc.0\",\"angular-locale_en-zz\":\"1.3.0-rc.0\",\"angular-locale_en\":\"1.3"
          + ".0-rc.0\",\"angular-locale_en-iso\":\"1.3.0-rc.0\",\"angular-locale_mt\":\"1.3.0-rc.0\","
          + "\"angular-locale_el\":\"1.3.0-rc.0\",\"angular-locale_ms\":\"1.3.0-rc.0\",\"angular-locale_vi\":\"1.3.0-rc"
          + ".0\",\"angular-locale_eu\":\"1.3.0-rc.0\",\"angular-locale_et\":\"1.3.0-rc.0\",\"angular-locale_es\":\"1.3"
          + ".0-rc.0\",\"angular-locale_el-gr\":\"1.3.0-rc.0\",\"angular-locale_ru-ru\":\"1.3.0-rc.0\","
          + "\"angular-locale_id\":\"1.3.0-rc.0\",\"angular-locale_en-as\":\"1.3.0-rc.0\",\"angular-locale_en-au\":\"1"
          + ".3.0-rc.0\",\"angular-locale_in\":\"1.3.0-rc.0\",\"angular-locale_en-za\":\"1.3.0-rc.0\","
          + "\"angular-loader\":\"1.3.0-rc.0\",\"angular-locale_zh-cn\":\"1.3.0-rc.0\",\"angular-locale_hu\":\"1.3.0-rc"
          + ".0\",\"angular-cookies\":\"1.3.0-rc.0\",\"angular-locale_zh-hans\":\"1.3.0-rc.0\","
          + "\"angular-locale_hr\":\"1.3.0-rc.0\",\"angular-locale_en-in\":\"1.3.0-rc.0\",\"angular-locale_mr-in\":\"1"
          + ".3.0-rc.0\",\"angular-locale_da-dk\":\"1.3.0-rc.0\",\"angular-locale_fr-fr\":\"1.3.0-rc.0\","
          + "\"angular-locale_gu-in\":\"1.3.0-rc.0\",\"angular-scenario\":\"1.3.0-rc.0\",\"angular-locale_en-ie\":\"1.3"
          + ".0-rc.0\",\"angular-locale_sq-al\":\"1.3.0-rc.0\",\"angular-locale_he\":\"1.3.0-rc.0\","
          + "\"angular-locale_pl\":\"1.3.0-rc.0\",\"angular-locale_fa-ir\":\"1.3.0-rc.0\",\"angular-sanitize\":\"1.3"
          + ".0-rc.0\",\"angular-locale_ar-eg\":\"1.3.0-rc.0\",\"angular-locale_nl-nl\":\"1.3.0-rc.0\","
          + "\"angular-locale_pt\":\"1.3.0-rc.0\",\"angular-locale_am-et\":\"1.3.0-rc.0\",\"angular-locale_hi\":\"1.3"
          + ".0-rc.0\",\"angular-locale_sk-sk\":\"1.3.0-rc.0\",\"angular-locale_gl-es\":\"1.3.0-rc.0\","
          + "\"angular-locale_ms-my\":\"1.3.0-rc.0\",\"angular-locale_zh-tw\":\"1.3.0-rc.0\","
          + "\"angular-locale_hi-in\":\"1.3.0-rc.0\",\"angular-locale_fr-gp\":\"1.3.0-rc.0\","
          + "\"angular-locale_bg-bg\":\"1.3.0-rc.0\",\"angular-locale_gu\":\"1.3.0-rc.0\"}}}}},"
          + "\"paths\":{\"angular-locale_nl-nl\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_nl-nl\","
          + "\"angular-locale_tl\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_tl\","
          + "\"angular-locale_lt-lt\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_lt-lt\","
          + "\"angular-locale_en-vi\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-vi\","
          + "\"angular-locale_cs-cz\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_cs-cz\","
          + "\"angular-locale_am-et\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_am-et\","
          + "\"angular\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/angular\",\"angular-locale_da-dk\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_da-dk\",\"angular-locale_nl\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_nl\",\"angular-locale_ml\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ml\",\"angular-locale_tr\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_tr\",\"angular-locale_hi\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_hi\",\"angular-locale_sr\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_sr\",\"angular-locale_id-id\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_id-id\",\"angular-locale_ml-in\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ml-in\",\"angular-locale_hu-hu\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_hu-hu\",\"angular-locale_fi\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fi\",\"angular-locale_no\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_no\",\"angular-locale_vi\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_vi\",\"angular-locale_mo\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_mo\",\"angular-touch\\/1.3.0-rc.0\":\"angularjs\\/1"
          + ".3.0-rc.0\\/angular-touch\",\"angular-route\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/angular-route\","
          + "\"angular-locale_es\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_es\","
          + "\"angular-locale_gl\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_gl\","
          + "\"angular-locale_fr-mc\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fr-mc\","
          + "\"angular-locale_el\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_el\","
          + "\"angular-locale_cs\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_cs\","
          + "\"angular-locale_or\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_or\","
          + "\"angular-locale_am\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_am\","
          + "\"angular-locale_is\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_is\","
          + "\"angular-locale_ru-ru\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ru-ru\","
          + "\"angular-locale_tl-ph\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_tl-ph\","
          + "\"angular-locale_gsw\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_gsw\","
          + "\"angular-locale_mr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_mr\","
          + "\"angular-locale_fr-fr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fr-fr\","
          + "\"angular-locale_ro\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ro\","
          + "\"angular-locale_fr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fr\","
          + "\"angular-locale_pt-br\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_pt-br\","
          + "\"angular-locale_is-is\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_is-is\","
          + "\"angular-locale_fr-bl\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fr-bl\","
          + "\"angular-locale_en-mp\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-mp\","
          + "\"angular-locale_hr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_hr\","
          + "\"angular-locale_en-as\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-as\","
          + "\"angular-locale_ta-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ta-in\","
          + "\"angular-locale_gsw-ch\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_gsw-ch\","
          + "\"angular-scenario\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/angular-scenario\",\"angular-locale_ar-eg\\/1"
          + ".3.0-rc.0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ar-eg\",\"angular-locale_gu-in\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_gu-in\",\"angular-locale_zh\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_zh\",\"angular-locale_en-us\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-us\",\"angular-locale_fil\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fil\",\"angular-locale_en-au\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-au\",\"angular-locale_da\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_da\",\"angular-locale_tr-tr\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_tr-tr\",\"angular-locale_fa-ir\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fa-ir\",\"angular-locale_fa\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fa\",\"angular-locale_lv-lv\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_lv-lv\",\"angular-locale_ar\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ar\",\"angular-locale_en-ie\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-ie\",\"angular-locale_ko\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ko\",\"angular-locale_zh-hk\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_zh-hk\",\"angular-locale_sk\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_sk\",\"angular-locale_ln-cd\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ln-cd\",\"angular-locale_bg-bg\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_bg-bg\",\"angular-locale_zh-tw\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_zh-tw\",\"angular-locale_kn\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_kn\",\"angular-locale_ca\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ca\",\"angular-locale_uk\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_uk\",\"angular-locale_pl\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_pl\",\"angular-locale_fr-re\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fr-re\",\"angular-locale_ln\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ln\",\"angular-locale_zh-hans-cn\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_zh-hans-cn\",\"angular-locale_sl\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_sl\",\"angular-locale_fr-mf\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_fr-mf\",\"angular-locale_en-za\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-za\",\"angular-locale_en-gu\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_en-gu\",\"angular-locale_in\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_in\",\"angular-locale_ur-pk\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/i18n\\/angular-locale_ur-pk\",\"angular-sanitize\\/1.3.0-rc"
          + ".0\":\"angularjs\\/1.3.0-rc.0\\/angular-sanitize\",\"angular-locale_lt\\/1.3.0-rc.0\":\"angularjs\\/1.3"
          + ".0-rc.0\\/i18n\\/angular-locale_lt\",\"angular-locale_vi-vn\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_vi-vn\",\"angular-locale_en\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en\",\"angular-locale_en-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-in\",\"angular-locale_zh-cn\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_zh-cn\",\"angular-locale_th-th\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_th-th\",\"angular-locale_en-iso\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-iso\",\"angular-locale_iw\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_iw\",\"angular-locale_mt\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_mt\",\"angular-locale_et\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_et\",\"angular-locale_mr-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_mr-in\",\"angular-locale_en-dsrt\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-dsrt\",\"angular-locale_sr-cyrl-rs\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sr-cyrl-rs\",\"angular-locale_fi-fi\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_fi-fi\",\"angular-locale_en-dsrt-us\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-dsrt-us\",\"angular-locale_el-gr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_el-gr\",\"angular-locale_ro-ro\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ro-ro\",\"angular-locale_en-zz\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-zz\",\"angular-locale_hi-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_hi-in\",\"angular-mocks\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/angular-mocks\",\"angular-locale_sl-si\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sl-si\",\"angular-locale_bn\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_bn\",\"angular-locale_it\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_it\",\"angular-locale_sr-latn-rs\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sr-latn-rs\",\"angular-locale_en-mh\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-mh\",\"angular-locale_ko-kr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ko-kr\",\"angular-locale_fr-mq\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_fr-mq\",\"angular-locale_fil-ph\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_fil-ph\",\"angular-locale_en-gb\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-gb\",\"angular-locale_ja\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ja\",\"angular-locale_sr-rs\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sr-rs\",\"angular-locale_en-sg\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-sg\",\"angular-locale_sq-al\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sq-al\",\"angular-locale_sq\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sq\",\"angular-locale_de\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_de\",\"angular-locale_pl-pl\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_pl-pl\",\"angular-locale_de-ch\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_de-ch\",\"angular-locale_el-polyton\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_el-polyton\",\"angular-locale_ta\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ta\",\"angular-locale_th\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_th\",\"angular-locale_he-il\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_he-il\",\"angular-locale_en-um\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_en-um\",\"angular-locale_te-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_te-in\",\"angular-locale_zh-hans\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_zh-hans\",\"angular-locale_sw-tz\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sw-tz\",\"angular-locale_sv-se\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sv-se\",\"angular-loader\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/angular-loader\",\"angular-locale_kn-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_kn-in\",\"angular-locale_sw\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sw\",\"angular-locale_mt-mt\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_mt-mt\",\"angular-cookies\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/angular-cookies\",\"angular-locale_de-at\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_de-at\",\"angular-resource\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/angular-resource\",\"angular-locale_te\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_te\",\"angular-locale_bg\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_bg\",\"angular-locale_ms-my\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ms-my\",\"angular-locale_hu\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_hu\",\"angular-locale_fr-gp\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_fr-gp\",\"angular-locale_hr-hr\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_hr-hr\",\"angular-locale_id\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_id\",\"angular-locale_pt-pt\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_pt-pt\",\"angular-locale_sv\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sv\",\"angular-locale_de-lu\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_de-lu\",\"angular-locale_gu\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_gu\",\"angular-locale_uk-ua\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_uk-ua\",\"angular-animate\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/angular-animate\",\"angular-locale_et-ee\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_et-ee\",\"angular-locale_eu-es\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_eu-es\",\"angular-locale_eu\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_eu\",\"angular-locale_ms\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ms\",\"angular-locale_ru\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ru\",\"angular-locale_it-it\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_it-it\",\"angular-locale_es-es\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_es-es\",\"angular-locale_he\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_he\",\"angular-locale_fr-ca\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_fr-ca\",\"angular-locale_sk-sk\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_sk-sk\",\"angular-locale_ca-es\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ca-es\",\"angular-locale_lv\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_lv\",\"angular-locale_or-in\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_or-in\",\"angular-locale_ur\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ur\",\"angular-locale_de-be\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_de-be\",\"angular-locale_pt\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_pt\",\"angular-locale_de-de\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_de-de\",\"angular-locale_gl-es\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_gl-es\",\"angular-locale_bn-bd\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_bn-bd\",\"angular-locale_ja-jp\\/1.3.0-rc.0\":\"angularjs\\/1.3.0-rc"
          + ".0\\/i18n\\/angular-locale_ja-jp\"},\"shim\":{\"angular-locale_nl-nl\\/1.3.0-rc.0\":[\"angular\","
          + "\"angular\"],\"angular-locale_tl\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_lt-lt\\/1.3"
          + ".0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-vi\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_cs-cz\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_am-et\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular\\/1.3.0-rc.0\":{\"exports\":\"angular\"},"
          + "\"angular-locale_da-dk\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_nl\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ml\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_tr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_hi\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_sr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_id-id\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_ml-in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_hu-hu\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fi\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_no\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_vi\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_mo\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-touch\\/1.3.0-rc.0\":[\"angular\","
          + "\"angular\"],\"angular-route\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_es\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_gl\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fr-mc\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_el\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_cs\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_or\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_am\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_is\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_ru-ru\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_tl-ph\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_gsw\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_mr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_fr-fr\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ro\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_pt-br\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_is-is\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fr-bl\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-mp\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_hr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-as\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_ta-in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_gsw-ch\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-scenario\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_ar-eg\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_gu-in\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_zh\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-us\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_fil\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-au\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_da\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_tr-tr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fa-ir\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_fa\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_lv-lv\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_ar\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-ie\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ko\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_zh-hk\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sk\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ln-cd\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_bg-bg\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_zh-tw\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_kn\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_ca\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_uk\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_pl\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fr-re\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_ln\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_zh-hans-cn\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_sl\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_fr-mf\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_en-za\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-gu\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ur-pk\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-sanitize\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_lt\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_vi-vn\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_zh-cn\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_th-th\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-iso\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_iw\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_mt\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_et\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_mr-in\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-dsrt\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sr-cyrl-rs\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_fi-fi\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-dsrt-us\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_el-gr\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ro-ro\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-zz\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_hi-in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-mocks\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_sl-si\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_bn\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_it\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_sr-latn-rs\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_en-mh\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ko-kr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fr-mq\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_fil-ph\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_en-gb\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_ja\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sr-rs\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_en-sg\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_sq-al\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sq\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_de\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_pl-pl\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_de-ch\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_el-polyton\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_ta\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_th\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_he-il\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_en-um\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_te-in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_zh-hans\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_sw-tz\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sv-se\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-loader\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_kn-in\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sw\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_mt-mt\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-cookies\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_de-at\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-resource\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_te\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_bg\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ms-my\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_hu\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_fr-gp\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_hr-hr\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_id\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_pt-pt\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_sv\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_de-lu\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_gu\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_uk-ua\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-animate\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_et-ee\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_eu-es\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_eu\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_ms\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ru\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_it-it\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_es-es\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_he\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_fr-ca\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_sk-sk\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ca-es\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_lv\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_or-in\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ur\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_de-be\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_pt\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_de-de\\/1.3.0-rc.0\":[\"angular\",\"angular\"],"
          + "\"angular-locale_gl-es\\/1.3.0-rc.0\":[\"angular\",\"angular\"],\"angular-locale_bn-bd\\/1.3.0-rc"
          + ".0\":[\"angular\",\"angular\"],\"angular-locale_ja-jp\\/1.3.0-rc.0\":[\"angular\",\"angular\"]},"
          + "\"map\":{}}" );

      npmJsonOutput = (JSONObject) parser.parse( "{\"requirejs-osgi-meta\":{\"modules\":{\"asap\":{\"2.0.3\":{}}},"
        + "\"artifacts\":{\"org.webjars.npm\\/asap\":{\"2.0.3\":{\"type\":\"package.json\",\"modules\":{\"asap\":\"2.0"
        + ".3\"}}}}},\"paths\":{\"asap\\/2.0.3\\/asap\":\"asap\\/2.0.3\\/browser-asap\",\"asap\\/2.0"
        + ".3\\/raw\":\"asap\\/2.0.3\\/browser-raw\",\"asap\\/2.0.3\\/test\\/domain\":\"asap\\/2.0"
        + ".3\\/test\\/browser-domain\",\"asap\\/2.0.3\":\"asap\\/2.0.3\"},\"packages\":[{\"name\":\"asap\\/2.0.3\","
        + "\"main\":\".\\/asap\"}],\"shim\":{},\"map\":{}}" );

      bowerJsonOutput = (JSONObject) parser.parse( "{\"requirejs-osgi-meta\":{\"modules\":{\"angular-ui-router"
        + ".stateHelper\":{\"1.3.1\":{\"dependencies\":{\"angular\":\">=1.2.0\",\"angular-ui-router\":\"~0.2"
        + ".11\"}}}},\"artifacts\":{\"org.webjars.bower\\/angular-ui-router.stateHelper\":{\"1.3"
        + ".1\":{\"type\":\"bower.json\",\"modules\":{\"angular-ui-router.stateHelper\":\"1.3.1\"}}}}},"
        + "\"paths\":{\"angular-ui-router.stateHelper\\/1.3.1\":\"angular-ui-router.stateHelper\\/1.3.1\"},"
        + "\"packages\":[{\"name\":\"angular-ui-router.stateHelper\\/1.3.1\",\"main\":\"statehelper\"}],"
        + "\"shim\":{\"angular-ui-router.stateHelper\\/1.3.1\":{\"deps\":[\"angular\",\"angular-ui-router\"]},"
        + "\"angular-ui-router.stateHelper\\/1.3.1\\/statehelper\":{\"deps\":[\"angular\",\"angular-ui-router\"]}},"
        + "\"map\":{}}" );

      fallbackJsonOutput = (JSONObject) parser.parse( "{\"requirejs-osgi-meta\":{\"modules\":{\"angular-dateparser"
        + "\":{\"1.0.9\":{}}},\"artifacts\":{\"org.webjars\\/angular-dateparser\":{\"1.0.9\":{\"type\":\"*\","
        + "\"modules\":{\"angular-dateparser\":\"1.0.9\"}}}}},\"paths\":{\"angular-dateparser\\/1.0"
        + ".9\":\"angular-dateparser\\/1.0.9\"},\"shim\":{},\"map\":{}}" );
    } catch ( ParseException e ) {
      e.printStackTrace();
    }
  }

  ;

  @Before
  public void before() throws MalformedURLException {
    File input = new File( "src/test/resources/mockRepo" );

    System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
    System.setProperty( "org.ops4j.pax.url.mvn.repositories",
      input.toURI().toURL().toString() + "@snapshots@id=mock-repo" );
    System.setProperty( "org.ops4j.pax.url.mvn.localRepository", input.toURI().toURL().toString() );
    System.setProperty( "org.ops4j.pax.url.mvn.proxySupport", "false" );
  }

  @Test
  public void testClassicWebjarPomConfig() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "smart-table/2.0.3-1" );
    verifyRequireJson( zipInputStream, WebjarsURLConnectionTest.pomJsonOutput );
  }

  @Test
  public void testClassicWebjarScriptedConfig() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/angularjs/1.3.0-rc.0" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angularjs/1.3.0-rc.0" );
    verifyRequireJson( zipInputStream, WebjarsURLConnectionTest.scriptJsonOutput );
  }

  @Test
  public void testNpmWebjar() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.npm/asap/2.0.3" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "asap/2.0.3" );
    verifyRequireJson( zipInputStream, WebjarsURLConnectionTest.npmJsonOutput );
  }

  @Test
  public void testBowerWebjar() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.bower/angular-ui-router.stateHelper/1.3.1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-ui-router.stateHelper/1.3.1" );
    verifyRequireJson( zipInputStream, WebjarsURLConnectionTest.bowerJsonOutput );
  }

  @Test
  public void testMalformedWebjarFallback() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-dateparser/1.0.9" );
    verifyRequireJson( zipInputStream, WebjarsURLConnectionTest.fallbackJsonOutput );
  }

  private void verifyManifest( ZipFile zipInputStream ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull( entry );
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( "Bundle-SymbolicName is not pentaho-webjars-",
      manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ) );
  }

  private void verifyBlueprint( ZipFile zipInputStream, String expectedPath ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull( entry );

    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( "blueprint.xml does not include path for " + expectedPath,
      bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/" + expectedPath + "\" />" ) );
  }

  private void verifyRequireJson( ZipFile zipInputStream, JSONObject jsonOutput ) throws IOException, ParseException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull( entry );

    String jsonFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    JSONObject json = (JSONObject) parser.parse( jsonFile );

    assertEquals( jsonOutput, json );
  }

  private ZipFile getDeployedJar( URL webjar_url ) throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( webjar_url );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjar_test", ".zip" ); //new File("src/test/resources/testOutput.jar");
    byte[] buff = new byte[ 2048 ];
    int ret;

    FileOutputStream fileOutputStream = new FileOutputStream( tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    return new ZipFile( tempFile );
  }
}
