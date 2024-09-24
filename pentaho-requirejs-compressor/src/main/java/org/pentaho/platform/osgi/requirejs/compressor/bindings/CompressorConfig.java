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
 * Copyright 2014 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.osgi.requirejs.compressor.bindings;

import java.util.HashMap;

/**
 * Created by nbaker on 10/2/14.
 */
public class CompressorConfig extends CompressorModule{
  String appDir;
  String optimize;
  String baseUrl;
  String dir;
  String optimizeCss;
  boolean skipDirOptimize;
  HashMap<String, String> paths;
  String mainConfigFile;
  CompressorModule[] modules;
  boolean generateSourceMaps;
  Closure closure;

  UglifyConfig uglify;
  Uglify2Config ugligy2;
  String cssImportIgnore;
  String cssPrefix;
  boolean inlineText;
  boolean useStrict;    // We may want to ensure this is always false
  HashMap<String, Boolean> pragmas;
  HashMap<String, Boolean> pragmasOnSave;
  HashMap<String, Boolean> has;
  HashMap<String, Boolean> hasOnSave;
  String namespace;
  boolean skipPragmas;
  boolean skipModuleInsertion;
  String[] stubModules;
  boolean optimizeAllPluginResources;
  boolean findNestedDependencies; // This maybe should be always true for us.
  boolean removeCombined;
  String[] deps;
  String fileExclusionRegExp;
  boolean preserveLicenseComments;
  int logLevel;
  ThrowWhen throwWhen;
  HashMap<String, String> rawText;
  boolean cjsTranslate;
  boolean useSourceUrl;
  int waitSeconds;
  boolean skipSemiColonInsertion;
  boolean keepAmdefine;
  boolean allowSourceOverwrites;

  Wrap wrap;


  // The following option turns on single-module mode
  String out;


  public String getAppDir() {
    return appDir;
  }

  public void setAppDir( String appDir ) {
    this.appDir = appDir;
  }

  public String getOptimize() {
    return optimize;
  }

  public void setOptimize( String optimize ) {
    this.optimize = optimize;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl( String baseUrl ) {
    this.baseUrl = baseUrl;
  }

  public String getDir() {
    return dir;
  }

  public void setDir( String dir ) {
    this.dir = dir;
  }

  public String getOptimizeCss() {
    return optimizeCss;
  }

  public void setOptimizeCss( String optimizeCss ) {
    this.optimizeCss = optimizeCss;
  }

  public boolean isSkipDirOptimize() {
    return skipDirOptimize;
  }

  public void setSkipDirOptimize( boolean skipDirOptimize ) {
    this.skipDirOptimize = skipDirOptimize;
  }

  public HashMap<String, String> getPaths() {
    return paths;
  }

  public void setPaths( HashMap<String, String> paths ) {
    this.paths = paths;
  }

  public String getMainConfigFile() {
    return mainConfigFile;
  }

  public void setMainConfigFile( String mainConfigFile ) {
    this.mainConfigFile = mainConfigFile;
  }

  public CompressorModule[] getModules() {
    return modules;
  }

  public void setModules( CompressorModule[] modules ) {
    this.modules = modules;
  }

  public String getOut() {
    return out;
  }

  public void setOut( String out ) {
    this.out = out;
  }

  public boolean isGenerateSourceMaps() {
    return generateSourceMaps;
  }

  public void setGenerateSourceMaps( boolean generateSourceMaps ) {
    this.generateSourceMaps = generateSourceMaps;
  }

  public UglifyConfig getUglify() {
    return uglify;
  }

  public void setUglify( UglifyConfig uglify ) {
    this.uglify = uglify;
  }

  public Closure getClosure() {
    return closure;
  }

  public void setClosure( Closure closure ) {
    this.closure = closure;
  }

  public Uglify2Config getUgligy2() {
    return ugligy2;
  }

  public void setUgligy2( Uglify2Config ugligy2 ) {
    this.ugligy2 = ugligy2;
  }

  public String getCssImportIgnore() {
    return cssImportIgnore;
  }

  public void setCssImportIgnore( String cssImportIgnore ) {
    this.cssImportIgnore = cssImportIgnore;
  }

  public String getCssPrefix() {
    return cssPrefix;
  }

  public void setCssPrefix( String cssPrefix ) {
    this.cssPrefix = cssPrefix;
  }

  public boolean isInlineText() {
    return inlineText;
  }

  public void setInlineText( boolean inlineText ) {
    this.inlineText = inlineText;
  }

  public boolean isUseStrict() {
    return useStrict;
  }

  public void setUseStrict( boolean useStrict ) {
    this.useStrict = useStrict;
  }

  public HashMap<String, Boolean> getPragmas() {
    return pragmas;
  }

  public void setPragmas( HashMap<String, Boolean> pragmas ) {
    this.pragmas = pragmas;
  }

  public HashMap<String, Boolean> getPragmasOnSave() {
    return pragmasOnSave;
  }

  public void setPragmasOnSave( HashMap<String, Boolean> pragmasOnSave ) {
    this.pragmasOnSave = pragmasOnSave;
  }

  public HashMap<String, Boolean> getHas() {
    return has;
  }

  public void setHas( HashMap<String, Boolean> has ) {
    this.has = has;
  }

  public HashMap<String, Boolean> getHasOnSave() {
    return hasOnSave;
  }

  public void setHasOnSave( HashMap<String, Boolean> hasOnSave ) {
    this.hasOnSave = hasOnSave;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  public boolean isSkipPragmas() {
    return skipPragmas;
  }

  public void setSkipPragmas( boolean skipPragmas ) {
    this.skipPragmas = skipPragmas;
  }

  public boolean isSkipModuleInsertion() {
    return skipModuleInsertion;
  }

  public void setSkipModuleInsertion( boolean skipModuleInsertion ) {
    this.skipModuleInsertion = skipModuleInsertion;
  }

  public String[] getStubModules() {
    return stubModules;
  }

  public void setStubModules( String[] stubModules ) {
    this.stubModules = stubModules;
  }

  public boolean isOptimizeAllPluginResources() {
    return optimizeAllPluginResources;
  }

  public void setOptimizeAllPluginResources( boolean optimizeAllPluginResources ) {
    this.optimizeAllPluginResources = optimizeAllPluginResources;
  }

  public boolean isFindNestedDependencies() {
    return findNestedDependencies;
  }

  public void setFindNestedDependencies( boolean findNestedDependencies ) {
    this.findNestedDependencies = findNestedDependencies;
  }

  public boolean isRemoveCombined() {
    return removeCombined;
  }

  public void setRemoveCombined( boolean removeCombined ) {
    this.removeCombined = removeCombined;
  }

  public String[] getDeps() {
    return deps;
  }

  public void setDeps( String[] deps ) {
    this.deps = deps;
  }

  public String getFileExclusionRegExp() {
    return fileExclusionRegExp;
  }

  public void setFileExclusionRegExp( String fileExclusionRegExp ) {
    this.fileExclusionRegExp = fileExclusionRegExp;
  }

  public boolean isPreserveLicenseComments() {
    return preserveLicenseComments;
  }

  public void setPreserveLicenseComments( boolean preserveLicenseComments ) {
    this.preserveLicenseComments = preserveLicenseComments;
  }

  public int getLogLevel() {
    return logLevel;
  }

  public void setLogLevel( int logLevel ) {
    this.logLevel = logLevel;
  }

  public ThrowWhen getThrowWhen() {
    return throwWhen;
  }

  public void setThrowWhen( ThrowWhen throwWhen ) {
    this.throwWhen = throwWhen;
  }

  public HashMap<String, String> getRawText() {
    return rawText;
  }

  public void setRawText( HashMap<String, String> rawText ) {
    this.rawText = rawText;
  }

  public boolean isCjsTranslate() {
    return cjsTranslate;
  }

  public void setCjsTranslate( boolean cjsTranslate ) {
    this.cjsTranslate = cjsTranslate;
  }

  public boolean isUseSourceUrl() {
    return useSourceUrl;
  }

  public void setUseSourceUrl( boolean useSourceUrl ) {
    this.useSourceUrl = useSourceUrl;
  }

  public int getWaitSeconds() {
    return waitSeconds;
  }

  public void setWaitSeconds( int waitSeconds ) {
    this.waitSeconds = waitSeconds;
  }

  public boolean isSkipSemiColonInsertion() {
    return skipSemiColonInsertion;
  }

  public void setSkipSemiColonInsertion( boolean skipSemiColonInsertion ) {
    this.skipSemiColonInsertion = skipSemiColonInsertion;
  }

  public boolean isKeepAmdefine() {
    return keepAmdefine;
  }

  public void setKeepAmdefine( boolean keepAmdefine ) {
    this.keepAmdefine = keepAmdefine;
  }

  public boolean isAllowSourceOverwrites() {
    return allowSourceOverwrites;
  }

  public void setAllowSourceOverwrites( boolean allowSourceOverwrites ) {
    this.allowSourceOverwrites = allowSourceOverwrites;
  }

  public Wrap getWrap() {
    return wrap;
  }

  public void setWrap( Wrap wrap ) {
    this.wrap = wrap;
  }
}
