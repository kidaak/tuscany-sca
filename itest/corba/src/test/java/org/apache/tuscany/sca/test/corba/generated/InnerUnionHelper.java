/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.tuscany.sca.test.corba.generated;


/**
* org/apache/tuscany/sca/test/corba/generated/InnerUnionHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from itest_scenario.idl
* niedziela, 17 sierpie� 2008 19:07:14 CEST
*/

abstract public class InnerUnionHelper
{
  private static String  _id = "IDL:org/apache/tuscany/sca/test/corba/generated/InnerUnion/InnerUnion:1.0";

  public static void insert (org.omg.CORBA.Any a, org.apache.tuscany.sca.test.corba.generated.InnerUnion that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.apache.tuscany.sca.test.corba.generated.InnerUnion extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      org.omg.CORBA.TypeCode _disTypeCode0;
      _disTypeCode0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      org.omg.CORBA.UnionMember[] _members0 = new org.omg.CORBA.UnionMember [2];
      org.omg.CORBA.TypeCode _tcOf_members0;
      org.omg.CORBA.Any _anyOf_members0;

      // Branch for x (case label 1)
      _anyOf_members0 = org.omg.CORBA.ORB.init ().create_any ();
      _anyOf_members0.insert_long ((int)1);
      _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
      _members0[0] = new org.omg.CORBA.UnionMember (
        "x",
        _anyOf_members0,
        _tcOf_members0,
        null);

      // Branch for y (case label 2)
      _anyOf_members0 = org.omg.CORBA.ORB.init ().create_any ();
      _anyOf_members0.insert_long ((int)2);
      _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_float);
      _members0[1] = new org.omg.CORBA.UnionMember (
        "y",
        _anyOf_members0,
        _tcOf_members0,
        null);
      __typeCode = org.omg.CORBA.ORB.init ().create_union_tc (org.apache.tuscany.sca.test.corba.generated.InnerUnionHelper.id (), "InnerUnion", _disTypeCode0, _members0);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.apache.tuscany.sca.test.corba.generated.InnerUnion read (org.omg.CORBA.portable.InputStream istream)
  {
    org.apache.tuscany.sca.test.corba.generated.InnerUnion value = new org.apache.tuscany.sca.test.corba.generated.InnerUnion ();
    int _dis0 = (int)0;
    _dis0 = istream.read_long ();
    switch (_dis0)
    {
      case 1:
        int _x = (int)0;
        _x = istream.read_long ();
        value.x (_x);
        break;
      case 2:
        float _y = (float)0;
        _y = istream.read_float ();
        value.y (_y);
        break;
      default:
        value._default( _dis0 ) ;
        break;
    }
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.apache.tuscany.sca.test.corba.generated.InnerUnion value)
  {
    ostream.write_long (value.discriminator ());
    switch (value.discriminator ())
    {
      case 1:
        ostream.write_long (value.x ());
        break;
      case 2:
        ostream.write_float (value.y ());
        break;
    }
  }

}
