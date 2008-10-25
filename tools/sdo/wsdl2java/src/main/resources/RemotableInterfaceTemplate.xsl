<!--
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
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- import the databinding template-->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
         Note  -  these names would be handled by a special
         URI resolver during the xslt transformations
     -->
    <xsl:include href="externalTemplate"/>


    <xsl:template match="/interface">

    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>

    /**
     * <xsl:value-of select="@name"/>.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: #axisVersion# #today#
     */
    package <xsl:value-of select="$package"/>;

    import org.osoa.sca.annotations.Remotable;
    import org.osoa.sca.annotations.Service;
    
    /*
     *  <xsl:value-of select="@name"/> java interface
     */

    @Remotable
    @Service
    public interface <xsl:value-of select="@name"></xsl:value-of> {
          <xsl:for-each select="method">

            <!-- Code for in-out mep -->
         <xsl:if test="@mep='12'">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>

        <!-- start of the sync block -->                                          
         <xsl:if test="$isSync='1'">
        /**
         * Auto generated method signatures
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>&#10;         </xsl:text></xsl:for-each>
         <xsl:for-each select="fault/param[@name!='']">* @throws <xsl:value-of select="@name"></xsl:value-of><xsl:text>&#10;         *</xsl:text></xsl:for-each>

         */
         public <xsl:choose><xsl:when test="$outputtype=''">void</xsl:when><xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise></xsl:choose>
        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:for-each select="input/param[@type!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
          </xsl:for-each>) throws <xsl:for-each select="fault/param[@name!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/><xsl:if test="position()=last()">,</xsl:if>

          </xsl:for-each> java.rmi.RemoteException;
        <!-- end of the sync block -->
        </xsl:if>

       <!-- start of the async block -->
        <xsl:if test="$isAsync='1'">
         /**
          * Auto generated method signature
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>&#10;         </xsl:text></xsl:for-each>
         <xsl:for-each select="fault/param[@name!='']">* @throws <xsl:value-of select="@name"></xsl:value-of><xsl:text>&#10;         *</xsl:text></xsl:for-each>

          */

        public void start<xsl:value-of select="@name"/>(
         <xsl:variable name="paramCount"><xsl:value-of select="count(input/param[@type!=''])"></xsl:value-of></xsl:variable>
               <xsl:for-each select="input/param">
            <xsl:if test="@type!=''"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of></xsl:if></xsl:for-each>
           <xsl:if test="$paramCount>0">,</xsl:if>final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback) throws <xsl:for-each select="fault/param[@name!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/><xsl:if test="position()=last()">,</xsl:if>
          </xsl:for-each> java.rmi.RemoteException;
        </xsl:if>
       <!-- end of async block-->

     </xsl:if>
        <!-- Code for in-only mep -->
       <xsl:if test="@mep='10'">

       <!-- For in-only meps there would not be any asynchronous methods since there is no output -->
         /**
         * Auto generated method signature
         <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>&#10;         </xsl:text></xsl:for-each>
         <xsl:for-each select="fault/param[@name!='']">* @throws <xsl:value-of select="@name"></xsl:value-of><xsl:text>&#10;         *</xsl:text></xsl:for-each>
         */
         public  void
        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:for-each select="input/param">

            <xsl:if test="@type!=''"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
            </xsl:if>
         </xsl:for-each>) throws <xsl:for-each select="fault/param[@name!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/><xsl:if test="position()=last()">,</xsl:if>
          </xsl:for-each> java.rmi.RemoteException;

        </xsl:if>

       </xsl:for-each>

       <!-- Apply other templates --> 
       //<xsl:apply-templates/>
       }


    </xsl:template>
   </xsl:stylesheet>