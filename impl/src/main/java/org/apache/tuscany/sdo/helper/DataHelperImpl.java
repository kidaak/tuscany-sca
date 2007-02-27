/**
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tuscany.sdo.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.DataHelper;

/**
 * Data helper methods.
 */
public class DataHelperImpl implements DataHelper
{
  /**
   * @param dateString - Must comply to the pattern of yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'
   * @return null if dataString couldn't be parsed
   */
  public synchronized Date toDate(String dateString)
  {
    SimpleDateFormat format;
    Date result = null;
    boolean negative = false;
    String formatString;
    dateString = dateString.trim();
    
    if (dateString == null)
    {
      return null;
    }
    
    // Determine if it is a negative Date, DateTime, or Duration
    
    if (dateString.length() > 2 && dateString.charAt(0) == '-' && dateString.charAt(1) != '-')
    {
      negative = true;
      dateString = dateString.substring(1);
    }
    
    //  SDO Date Format ends with a Z
    
    if (dateString.endsWith("Z"))
    {
      if (dateString.indexOf('.') != -1)
        formatString = new String("yyyy-MM-dd'T'HH:mm:ss'.'S'Z'");
      else
        formatString = new String ("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
      format = new SimpleDateFormat(formatString);
      format.setTimeZone(TimeZone.getTimeZone("UTC"));
      
      result = checkFormat(dateString, format);
      
      // If no match, continue to try further possibilities
      
      if (result != null)
      {
        if (negative)
          return handleBCE(result);
        else
          return result;
      }
      
    }
    
    // Duration format begins with a P
    
    if (dateString.startsWith("P"))
    {
      // Remove any spaces in the dateString
        
      String durationString = dateString.replaceAll(" ", "");

      // Build the formatString based on the contents of dateString
        
      formatString = obtainDurationFormats(durationString);
      format = new SimpleDateFormat(formatString);
      result = checkFormat(durationString, format);
      if (result != null)
      {     
        if (negative)
          return handleNegative(result); 
        else
          return result;
      }
    }
    
    formatString = obtainSpecificFormat(dateString);

    if (formatString != null)
    {
      format = new SimpleDateFormat(formatString);
      result = checkFormat(dateString, format); 

      if (result != null)
      {
        if (negative)
          return handleBCE(result);   
        else
          return result;
      }
    }
    
    return null;
  }
  
  private synchronized Date checkFormat(String dateString, SimpleDateFormat format)
  {
    String formatPattern = format.toPattern();
    StringBuffer addedFields = new StringBuffer();
    String fieldsString, parseString;
    SimpleDateFormat compositeFormat;
    Date dateValue;

    // For certain permissable input strings (e.g. those resulting from toYear
    // toDay, toTime), there are fields missing which when converted to Date have
    // default values.  (e.g. Year -> 1970).  Because of this, there can be great 
    // variation in how daylight savings time is accounted for.  (e.g. In 1970 Britain
    // was on DST year round, and during the summer of 1944 was on double daylight time.)
    // Because these possible variations exist, it is assumed that the user would prefer
    // the current handling of daylight savings time.  As such, the year, month and day 
    // will default to their current values when absent.  (The user should not be checking
    // for Year=1970 (etc.) as evidence of taking the default, as explicitly setting a 
    // year to 1970 is valid and would then not be an instance of a default taken.)
    
    if (!(formatPattern.startsWith("P")))
    {
      if (formatPattern.indexOf('y') == -1)
        addedFields.append("yyyy ");
    
      if (formatPattern.indexOf('M') == -1)
        addedFields.append("MM ");
    
      if (formatPattern.indexOf('d') == -1)
        addedFields.append("dd ");
    }
    
    fieldsString = addedFields.toString();
    
    if (fieldsString.length() == 0)
    {
      parseString = dateString;
      compositeFormat = format;
    }
    
    else
    {
      compositeFormat = new SimpleDateFormat(fieldsString);
      dateValue = new Date(System.currentTimeMillis());
      parseString = compositeFormat.format(dateValue) + dateString;    
      compositeFormat.applyPattern(fieldsString + formatPattern);
    }

    try
    {
      return compositeFormat.parse(parseString);      
    }
    
    catch (ParseException parseException)
    {
    }    
      
    return null;
  }
  
  public synchronized String obtainSpecificFormat(String dateString)
  {
    StringBuffer formatBuffer = new StringBuffer();
    int colonIndex = dateString.indexOf(':');
    int hyphenIndex = dateString.indexOf('-');
      
    if (dateString.startsWith("--"))
    {
      if (dateString.charAt(2) == '-') // starts with ---
        formatBuffer.append("'---'dd"); 
      else if (dateString.substring(2).indexOf('-') == -1)
        formatBuffer.append("'--'MM");
      else
        formatBuffer.append("'--'MM'-'dd");     
    }
    
    else if (colonIndex == 1 || colonIndex == 2)
    {
      if (dateString.indexOf('.') != -1)
        formatBuffer.append("HH:mm:ss'.'S");
      else if (dateString.substring(colonIndex + 1).indexOf(':') != -1)
        formatBuffer.append("HH:mm:ss");
      else
        formatBuffer.append("HH:mm");  
    }
    
    else if (hyphenIndex != -1)
    {
      if (dateString.substring(hyphenIndex + 1).indexOf('-') == -1)    
        formatBuffer.append("yyyy-MM");
      else if (colonIndex != -1)
      {
        if (dateString.indexOf('.') != -1)
          formatBuffer.append("yyyy-MM-dd'T'HH:mm:ss'.'S");
        else if (dateString.substring(colonIndex + 1).indexOf(':') != -1)
          formatBuffer.append("yyyy-MM-dd'T'HH:mm:ss");
        else
          formatBuffer.append("yyyy-MM-dd'T'HH:mm"); 
      }
      else
        formatBuffer.append ("yyyy-MM-dd");
    }
    else if (colonIndex == -1)  // indexOf('-') == -1
    {
      formatBuffer.append("yyyy");
    }
    else
      return null;
    
    // Determine if a Time Zone is included and needs to be parsed.
    // ---------------------------------
    // The only letter allowed in the above formats is 'T'.
    // All times zones include at least one letter other than 'T'.

    int i = 0;
    boolean letterFound = false;
    char currentChar;
    while (i < dateString.length() && !letterFound)
    {
      currentChar = dateString.charAt(i);
      if (Character.isLetter(currentChar) && currentChar != 'T')
        letterFound = true;;  
      i++;
    }
    
    if (letterFound)
      formatBuffer.append(" z");
        
    return formatBuffer.toString();
  }
  
  public synchronized String obtainDurationFormats(String dateString)
  {
    String firstPart, secondPart;
    StringBuffer formatBuffer = new StringBuffer("'P'");

    // Must divide it into two parts to distinguish between Months and Minutes
      
    int time_index = dateString.indexOf('T');
    if (time_index != -1)
    {
      firstPart = dateString.substring(0, time_index + 1);    
      secondPart = dateString.substring(time_index);
    }
    else
    {
      firstPart = dateString;
      secondPart = null;
    }
      
    if (firstPart.indexOf('Y') != -1)
      formatBuffer.append("yyyy'Y'");
    if (firstPart.indexOf('M') != -1)
      formatBuffer.append("MM'M'");
    if (firstPart.indexOf('D') != -1)
      formatBuffer.append("dd'D'");
    if (time_index != -1)
    {
      formatBuffer.append("'T'");
       
      if (secondPart.indexOf('H') != -1)
        formatBuffer.append("HH'H'");
      if (secondPart.indexOf('M') != -1)
        formatBuffer.append("mm'M'");
      if (secondPart.indexOf("S.") != -1)
        formatBuffer.append("ss'S'.S");
      else if (secondPart.indexOf('S') != -1)
        formatBuffer.append("ss'S'");
    }

    return formatBuffer.toString().replaceAll("''", "");
  }

  // Return a negative Duration if a negative sign existed in dateString
  public synchronized Date handleNegative(Date output)
  {
    return new Date(0 - output.getTime());
  }
  
  // Return the date in BCE if a negative sign existed in dateString
  
  public synchronized Date handleBCE(Date output)
  {
    GregorianCalendar temp = new GregorianCalendar();
    temp.setTime(output);
    temp.set(GregorianCalendar.ERA, GregorianCalendar.BC);
        
    return temp.getTime();
  }
  
  public synchronized Calendar toCalendar(String dateString)
  {
    if (dateString == null)
    {
      return null;
    }
    
    Date date = toDate(dateString);
    if (date == null)
    {
      return null;
    }
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    
    return calendar;
  }
  
  public synchronized Calendar toCalendar(String dateString, Locale locale)
  {
    if (dateString == null || locale == null)
    {
      return null;
    }
    
    Date date = toDate(dateString);
    if (date == null)
    {
      return null;
    }
    
    Calendar calendar = new GregorianCalendar(locale);
    calendar.setTime(date);
    return calendar;
  }

  public synchronized String toDateTime(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
    f.setTimeZone(TimeZone.getTimeZone("UTC"));
    
    return f.format(date);
  }
  
  public synchronized String toDuration(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("'P'yyyy'Y' MM'M' dd'D' 'T' HH'H' mm'M' ss'S.'SSS");
    
    return f.format(date);
  }

  public synchronized String toTime(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss'.'SSS zz");
    
    return f.format(date);
  }
  
  public synchronized String toDay(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("---dd zz");
    
    return f.format(date);
  }
  
  public synchronized String toMonth(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("--MM zz");
    
    return f.format(date);
  }

  public synchronized String toMonthDay(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("--MM-dd zz");
    
    return f.format(date);
  }

  public synchronized String toYear(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("yyyy zz");
    
    return f.format(date); 
  }

  public synchronized String toYearMonth(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM zz");
    
    return f.format(date);
  }

  public synchronized String toYearMonthDay(Date date)
  {
    if (date == null)
    {
      return null;
    }
    
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd zz");
    
    return f.format(date);
  }

  public synchronized String toDateTime(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toDateTime(calendar.getTime());
  }

  public synchronized String toDuration(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toDuration(calendar.getTime());
  }

  public synchronized String toTime(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toTime(calendar.getTime());
  }

  public synchronized String toDay(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toDay(calendar.getTime());
  }

  public synchronized String toMonth(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toMonth(calendar.getTime());
  }

  public synchronized String toMonthDay(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toMonthDay(calendar.getTime());
  }

  public synchronized String toYear(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toYear(calendar.getTime());
  }

  public synchronized String toYearMonth(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toYearMonth(calendar.getTime());
  }

  public synchronized String toYearMonthDay(Calendar calendar)
  {
    if (calendar == null)
    {
      return null;
    }
    
    return toYearMonthDay(calendar.getTime());
  }
  
  public Object convert(Type type, Object value)
  {
    throw new UnsupportedOperationException();
  }
  
  public Object convert(Property property, Object value)
  {
    throw new UnsupportedOperationException();
  }

}
