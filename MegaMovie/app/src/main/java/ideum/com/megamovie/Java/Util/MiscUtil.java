// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ideum.com.megamovie.Java.Util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ideum.com.megamovie.Java.ApplicationConstants;
import ideum.com.megamovie.Java.units.Matrix33;
import ideum.com.megamovie.Java.units.Vector3;

/**
 * A collection of miscellaneous utility functions.
 * 
 * @author Brent Bryan
 */
public class MiscUtil {
  private MiscUtil() {}
  
  /** Returns the Tag for a class to be used in Android logging statements */
  public static String getTag(Object o) {
    if (o instanceof Class<?>) {
      return ApplicationConstants.APP_NAME + "." + ((Class<?>)o).getSimpleName();
    }
    return ApplicationConstants.APP_NAME + "." + o.getClass().getSimpleName();
  }

  public static void storeMatrix33InPreferences(Context context, String key, Matrix33 m) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putFloat(key + "xx",m.xx);
    editor.putFloat(key + "xy",m.xy);
    editor.putFloat(key + "xz",m.xz);
    editor.putFloat(key + "yx",m.yx);
    editor.putFloat(key + "yy",m.yy);
    editor.putFloat(key + "yz",m.zz);
    editor.putFloat(key + "zx",m.zx);
    editor.putFloat(key + "zy",m.zy);
    editor.putFloat(key + "zz",m.zz);
    editor.commit();
  }

  // Returns matrix with default entries d
  public static Matrix33 getMatrix33FromPreferences(Context context, String key, Matrix33 d) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    Matrix33 m = Matrix33.getIdMatrix();
    m.xx = preferences.getFloat(key + "xx",d.xx);
    m.xy = preferences.getFloat(key + "xy",d.xy);
    m.xz = preferences.getFloat(key + "xz",d.xz);
    m.yx = preferences.getFloat(key + "yx",d.yx);
    m.yy = preferences.getFloat(key + "yy",d.yy);
    m.yz = preferences.getFloat(key + "yz",d.yz);
    m.zx = preferences.getFloat(key + "zx",d.zx);
    m.zy = preferences.getFloat(key + "zy",d.zy);
    m.zz = preferences.getFloat(key + "zz",d.zz);

    return m;
  }

  public static void storeVector3InPreferences(Context context, String key, Vector3 v) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putFloat(key + "x",v.x);
    editor.putFloat(key + "y",v.y);
    editor.putFloat(key + "z",v.z);
    editor.commit();
  }

  public static Vector3 getVector3FromPreferences(Context context, String key, Vector3 d) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    Vector3 v = new Vector3(0,0,0);
    v.x = preferences.getFloat(key + "x",d.x);
    v.y = preferences.getFloat(key + "y",d.y);
    v.z = preferences.getFloat(key + "z",d.z);

    return v;
  }


}